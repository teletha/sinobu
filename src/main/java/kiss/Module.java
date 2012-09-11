/*
 * Copyright (C) 2012 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss;

import java.lang.annotation.Annotation;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import kiss.model.ClassUtil;

/**
 * <h2>Module System</h2>
 * <p>
 * Module is a kind of classpath. It can be dynamically unloaded and knows much about information of
 * all classes which is managed by this.
 * </p>
 * <p>
 * The module doesn't collect detailed information on the managed class until needing it. This lazy
 * evaluation relieves us of the following annoying works.
 * </p>
 * <ul>
 * <li>Module Loading Order</li>
 * <li>Dependency Graph Management</li>
 * </ul>
 * <h2>Generated Class Naming Strategy</h2>
 * <p>
 * We adopted Suffix Naming Strategy of an automatically generated class at first (e.g. SomeClass+,
 * AnotherClass-). But this strategy has problem against to core package classes (e.g.
 * java.util.Date class, java.awt.Dimension). Therefore, we adopt Preffix Naming Strategy now.
 * </p>
 * 
 * @version 2011/11/19 19:06:03
 */
class Module extends URLClassLoader {

    /** The root of this module. */
    final Path path;

    final String pattern;

    /** The class scanner. */
    ModuleVisitor visitor;

    /**
     * <p>
     * Module constructor should be package private.
     * </p>
     * 
     * @param path A module path as classpath, A <code>null</code> is not accepted.
     */
    Module(Path path, String pattern) throws MalformedURLException {
        super(new URL[] {path.toUri().toURL()}, I.$loader);

        // we don't need to check null because this is internal class
        // if (moduleFile == null) {
        // }

        // Store original module path for unloading.
        this.path = path;
        this.pattern = pattern;
        this.visitor = new ModuleVisitor(this, pattern);
    }

    /**
     * Collect all service provider classes which is managed by this module.
     * 
     * @param <S> A type of service provider interface.
     * @param spi A service provider interface.
     * @param single A flag for finding mode. <code>true</code> is single mode, <code>false</code>
     *            is all mode.
     * @return A list of all service provider classes in this module. Never be <code>null</code>.
     */
    <S> List<Class<S>> find(Class<S> spi, boolean single) {
        // check null
        if (spi == null) {
            return Collections.EMPTY_LIST;
        }

        // compute hash of service provider interface
        int hash = spi.getName().hashCode();

        // set up container
        List list = new ArrayList(4);

        // try to find all service providers
        for (Object[] info : visitor.infos) {
            try {
                if (test(hash, info)) {
                    list.add(loadClass((String) info[0]));

                    if (single) {
                        return list;
                    }
                }
            } catch (ClassNotFoundException e) {
                throw I.quiet(e);
            }
        }

        // API definition
        return list;
    }

    /**
     * Check whether the class is a subclass of the class which is represented by the hash or not.
     * 
     * @param hash A hash to find.
     * @param info A class information.
     * @return A result.
     */
    private boolean test(int hash, Object[] info) throws ClassNotFoundException {
        int[] hashs = (int[]) info[1];

        if (hashs != null) {
            return -1 < Arrays.binarySearch(hashs, hash);
        }

        // lazy evaluation
        Class<?> clazz = loadClass((String) info[0]);
        Set<Class> set = ClassUtil.getTypes(clazz);
        Annotation[] annotations = clazz.getAnnotations();

        // compute hash
        int i = 0;
        hashs = new int[set.size() + annotations.length];

        for (Class c : set) {
            hashs[i++] = c.getName().hashCode();
        }

        for (Annotation a : annotations) {
            hashs[i++] = a.annotationType().getName().hashCode();
        }

        // sort for search
        Arrays.sort(hashs);

        // register information of the service provider class
        info[1] = hashs;

        // API definition
        return test(hash, info);
    }
}
