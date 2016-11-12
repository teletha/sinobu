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
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import kiss.model.Model;

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
 * @version 2016/11/12 13:37:44
 */
class Module {

    /** The root of this module. */
    final File path;

    /** The class pattern which this module loads. */
    final String pattern;

    /** The module classloader. */
    final ClassLoader loader;

    /** The list of classes (by class and interface). [java.lang.String or Class, int[]] */
    private final List<Object[]> infos = new CopyOnWriteArrayList();

    /**
     * <p>
     * Module constructor should be package private.
     * </p>
     *
     * @param file A module path as classpath, A <code>null</code> is not accepted.
     */
    Module(File file, String pattern) throws MalformedURLException {
        // we don't need to check null because this is internal class
        // if (moduleFile == null) {
        // }

        // Store original module path for unloading.
        this.path = file.getAbsoluteFile();
        this.pattern = pattern;
        this.loader = new URLClassLoader(new URL[] {file.toURI().toURL()}, I.$loader);

        // start scanning class files
        try {
            int prefix = this.path.getPath().length() + 1;

            // At first, we must scan the specified directory or archive. If the module file is
            // archive, Sinobu automatically try to switch to other file system (e.g.
            // ZipFileSystem).
            Events<String> names = file.isFile() ? Events.from(new ZipFile(file).entries()).map(ZipEntry::getName)
                    : Events.from(scan(file, new ArrayList())).map(File::getAbsolutePath).map(name -> name.substring(prefix));

            names.take(name -> name.endsWith(".class") && name.startsWith(pattern)).to(name -> {
                infos.add(new Object[] {name.substring(0, name.length() - 6).replace(File.separatorChar, '.'), null});
            });
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }

    /**
     * <p>
     * Dig class files in directory.
     * </p>
     * 
     * @param file
     * @param files
     * @return
     */
    private List<File> scan(File file, List<File> files) {
        if (file.isDirectory()) {
            for (File sub : file.listFiles()) {
                scan(sub, files);
            }
        } else {
            files.add(file);
        }
        return files;
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
        for (Object[] info : infos) {
            if (test(hash, info)) {
                list.add(info[0]);

                if (single) {
                    return list;
                }
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
    private boolean test(int hash, Object[] info) {
        int[] hashs = (int[]) info[1];

        if (hashs != null) {
            return -1 < Arrays.binarySearch(hashs, hash);
        }

        try {
            // lazy evaluation
            Class clazz = loader.loadClass((String) info[0]);

            if (Modifier.isAbstract(clazz.getModifiers()) || clazz.isEnum()) {
                info[1] = new int[0];
                return false;
            }

            Set<Class> types = Model.collectTypes(clazz);
            Annotation[] marks = clazz.getAnnotations();

            // compute hash
            int i = 0;
            hashs = new int[types.size() + marks.length];

            if (hashs.length == 2) {
                info[1] = new int[0];
                return false;
            }

            for (Class c : types) {
                hashs[i++] = c.getName().hashCode();
            }

            for (Annotation a : marks) {
                hashs[i++] = a.annotationType().getName().hashCode();
            }

            // sort for search
            Arrays.sort(hashs);

            // register information of the service provider class
            info[0] = clazz;
            info[1] = hashs;

            // API definition
            return test(hash, info);
        } catch (ClassNotFoundException e) {
            info[1] = new int[0];
            return false;
        }
    }
}
