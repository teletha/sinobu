/*
 * Copyright (C) 2010 Nameless Production Committee.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *         http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ezbean.module;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;

import ezbean.ClassLoadListener;
import ezbean.I;
import ezbean.Manageable;
import ezbean.Singleton;
import ezbean.io.FileSystem;
import ezbean.model.ClassUtil;
import ezbean.model.Model;

/**
 * @version 2009/08/01 16:56:11
 */
@Manageable(lifestyle = Singleton.class)
public final class Modules implements ClassLoadListener {

    /** The list of module aware maps. */
    private static final List<WeakReference<Map>> awares = new CopyOnWriteArrayList();

    /** The module list. */
    final List<Module> modules = new CopyOnWriteArrayList();

    /**
     * The two length class array for class load listener. (0 : ClassLoadListener class, 1 : Target
     * class to listen)
     */
    final List<Object[]> types = new CopyOnWriteArrayList();

    /**
     * Avoid construction
     */
    private Modules() {
        // built-in ClassLoadListener
        types.add(new Object[] {this, ClassLoadListener.class});
    }

    /**
     * <p>
     * Find a service provider class associated with the service provider interface.
     * </p>
     * 
     * @param <S> A type of service provider interface.
     * @param spi A service provider interface to find. An abstract class is only accepted.
     * @return A finded service provider class.
     * @throws TypeNotPresentException If the suitable service provider class is not found.
     */
    public <S> Class<S> find(Class<S> spi) {
        for (Module module : modules) {
            List<Class<S>> list = module.find(spi, true);

            if (list.size() != 0) {
                return list.get(0);
            }
        }
        throw new TypeNotPresentException(spi.getName(), null);
    }

    /**
     * @see ezbean.ClassLoadListener#load(java.lang.Class)
     */
    public void load(Class clazz) {
        if (clazz != Modules.class && clazz != ModuleLoader.class) {
            Object[] types = {I.make(clazz), Object.class};
            Class[] params = ClassUtil.getParameter(clazz, ClassLoadListener.class);

            if (params.length != 0) {
                types[1] = params[0];
            }

            // The new ClassLoadListener is introduced by some module. For all existing modules,
            // that is unknown. So we must notify this event to all modules.
            for (Module module : modules) {
                for (Class provider : module.find((Class<?>) types[1], false)) {
                    ((ClassLoadListener) types[0]).load(provider);
                }
            }

            // register
            this.types.add(types);
        }
    }

    /**
     * @see ezbean.ClassLoadListener#unload(java.lang.Class)
     */
    public void unload(Class clazz) {
        for (Object[] types : this.types) {
            if (Model.load(types[0].getClass()).type == clazz) {
                this.types.remove(types);
                return;
            }
        }
    }

    /**
     * <p>
     * Load the file as an additional classpath into JVM. If the file indicates the classpath which
     * is already loaded, that will be reloaded. The classpath can accept directory or archive (like
     * Jar). If it is <code>null</code> or a file, this method does nothing.
     * </p>
     * 
     * @param moduleFile A moduleFile to load. Directory or archive file (like Jar) can be accepted.
     * @param core A flag whether the module should be loaded as <dfn>system module</dfn> or Ezbean
     *            core module. The system module will be loaded by the class loader which isn't
     *            managed by Ezbean (e.g. {@link ClassLoader#getSystemClassLoader()}), and it never
     *            be unloaded or reloaded.
     */
    public void load(File moduleFile) {
        // check module file
        if (moduleFile != null && moduleFile.exists()) {
            // If the given module file has been already loaded, we must unload it on ahead.
            unload(moduleFile);

            // build module
            Module module = new Module(moduleFile);

            // Load module for the specified directory. The new module has high priority than
            // previous.
            modules.add(0, module);

            // fire event
            for (Object[] types : this.types) {
                for (Class provider : module.find((Class<?>) types[1], false)) {
                    ((ClassLoadListener) types[0]).load(provider);
                }
            }
        }
    }

    /**
     * <p>
     * Unload the file which is an additional classpath in JVM. If the file indicates the classpath
     * which is not loaded yet, that will be ignored. The classpath can accept directory or archive
     * (like Jar). If it is <code>null</code> or a file, this method does nothing.
     * </p>
     * 
     * @param moduleFile A moduleFile to unload. Directory or archive file (like Jar) can be
     *            accepted.
     */
    public void unload(File moduleFile) {
        // check module file
        if (moduleFile != null && moduleFile.exists()) {
            for (Module module : modules) {
                if (FileSystem.equals(moduleFile, module.moduleFile)) {
                    // fire event
                    for (Object[] types : this.types) {
                        for (Class provider : module.find((Class<?>) types[1], false)) {
                            ((ClassLoadListener) types[0]).unload(provider);
                        }
                    }

                    // unload class key from module aware map
                    for (WeakReference<Map> reference : awares) {
                        Map aware = reference.get();

                        if (aware == null) {
                            awares.remove(reference);
                        } else {
                            Iterator<Entry> iterator = aware.entrySet().iterator();

                            while (iterator.hasNext()) {
                                if (module.find((Class) iterator.next().getKey(), true).size() == 1) {
                                    iterator.remove();
                                }
                            }
                        }
                    }

                    // unload
                    modules.remove(module);
                    break;
                }
            }
        }
    }

    /**
     * <p>
     * Make the {@link Map} which has any key be recognized to the module unloading event and
     * disposes the key which is associated with the module automatically.
     * </p>
     * <p>
     * This method has same syntax of {@link Collections#synchronizedMap(Map)}.
     * </p>
     * 
     * @param map A target {@link Map} object to be aware of module unloading event.
     * @return The given {@link Map} object.
     */
    public static final Map aware(Map<Class, ?> map) {
        // We don't need to check whether the given map is already passed or not.
        // Because this method will be not called so frequently and duplicated item
        // will rise no problem except for amount of memory usage.
        awares.add(new WeakReference(map));

        // API definition
        return map;
    }
}
