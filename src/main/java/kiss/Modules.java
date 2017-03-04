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
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @version 2014/01/31 10:54:06
 */
@Manageable(lifestyle = Singleton.class)
class Modules extends ClassVariable<Lifestyle> implements Decoder<Class>, Encoder<Class>, Lifestyle<Locale> {

    /** The module list. */
    final List<Module> modules = new CopyOnWriteArrayList();

    /**
     * Avoid construction
     */
    Modules() {
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
    Disposable load(File path, String pattern) {
        // check module file
        if (path != null && path.exists()) {
            try {
                // check duplication
                for (Module module : modules) {
                    if (module.path.equals(path) && pattern.startsWith(module.pattern)) {
                        return module;
                    }
                }

                // build module
                Module module = new Module(path, pattern);

                // Load module for the specified directory. The new module has high priority than
                // previous.
                modules.add(0, module);

                // fire event
                for (Class provider : module.find(Extensible.class, false)) {
                    if (!provider.isAnonymousClass()) I.load(provider);
                }
                return module;
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
                        for (Class provider : module.find(Extensible.class, false)) {
                            if (!provider.isAnonymousClass()) I.unload(provider);
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
