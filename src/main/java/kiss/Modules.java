/*
 * Copyright (C) 2016 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss;

import java.io.File;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

import kiss.model.Model;

/**
 * @version 2014/01/31 10:54:06
 */
@Manageable(lifestyle = Singleton.class)
class Modules extends ClassVariable<Lifestyle> implements ClassListener, Decoder<Class>, Encoder<Class>, Lifestyle<Locale> {

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
    Modules() {
        // built-in ClassLoadListener
        types.add(new Object[] {this, ClassListener.class});
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void load(Class clazz) {
        if (clazz != Modules.class) {
            Object[] types = {I.make(clazz), Object.class};
            Type[] params = Model.collectParameters(clazz, ClassListener.class);

            if (params.length != 0) {
                types[1] = params[0];
            }

            // The new ClassLoadListener is introduced by some module. For all existing modules,
            // that is unknown. So we must notify this event to all modules.
            for (Module module : modules) {
                for (Class provider : module.find((Class<?>) types[1], false)) {
                    ((ClassListener) types[0]).load(provider);
                }
            }

            // register
            this.types.add(types);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unload(Class clazz) {
        for (Object[] types : this.types) {
            if (Model.of(types[0]).type == clazz) {
                this.types.remove(types);
                return;
            }
        }
    }

    /**
     * <p>
     * Load the path as an additional classpath into JVM. If the file indicates the classpath which
     * is already loaded, that will be reloaded. The classpath can accept directory or archive (like
     * Jar). If it is <code>null</code> or a file, this method does nothing.
     * </p>
     * 
     * @param path A module path to load. Directory or archive path (like Jar) can be accepted.
     */
    ClassLoader load(File path, String pattern) {
        // check module file
        if (path != null && path.exists()) {
            try {
                // check duplication
                for (Module module : modules) {
                    if (module.path.equals(path) && pattern.startsWith(module.pattern)) {
                        return module.loader;
                    }
                }

                // build module
                Module module = new Module(path, pattern);

                // Load module for the specified directory. The new module has high priority than
                // previous.
                modules.add(0, module);

                // fire event
                for (Object[] types : this.types) {
                    for (Class provider : module.find((Class<?>) types[1], false)) {
                        if (!provider.isAnonymousClass()) ((ClassListener) types[0]).load(provider);
                    }
                }
                return module.loader;
            } catch (Exception e) {
                throw I.quiet(e);
            }
        } else {
            return null;
        }
    }

    /**
     * <p>
     * Unload the path which is an additional classpath in JVM. If the file indicates the classpath
     * which is not loaded yet, that will be ignored. The classpath can accept directory or archive
     * (like Jar). If it is <code>null</code> or a file, this method does nothing.
     * </p>
     * 
     * @param path A module path to unload. Directory or archive path (like Jar) can be accepted.
     */
    void unload(File path) {
        // check module file
        if (path != null && path.exists()) {
            for (Module module : modules) {
                try {
                    if (module.path.equals(path)) {
                        // fire event
                        for (Object[] types : this.types) {
                            for (Class provider : module.find((Class<?>) types[1], false)) {
                                if (!provider.isAnonymousClass()) ((ClassListener) types[0]).unload(provider);
                            }
                        }

                        // unload
                        modules.remove(module);

                        // close classloader
                        I.quiet(module);
                        break;
                    }
                } catch (Exception e) {
                    throw I.quiet(e);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class decode(String value) {
        return I.type(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String encode(Class value) {
        return value.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Locale get() {
        return Locale.getDefault();
    }
}
