/*
 * Copyright (C) 2014 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss;

import static jdk.internal.org.objectweb.asm.ClassReader.*;
import static jdk.internal.org.objectweb.asm.Opcodes.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import jdk.internal.org.objectweb.asm.AnnotationVisitor;
import jdk.internal.org.objectweb.asm.ClassReader;
import jdk.internal.org.objectweb.asm.ClassVisitor;
import jdk.internal.org.objectweb.asm.FieldVisitor;
import jdk.internal.org.objectweb.asm.MethodVisitor;
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
 * @version 2014/07/17 21:57:29
 */
@SuppressWarnings("unchecked")
class Module extends ClassVisitor {

    /** The dirty process management. */
    private static final IllegalStateException STOP = new IllegalStateException();

    /** The root of this module. */
    final Path path;

    /** The class pattern which this module loads. */
    final String pattern;

    /** The module classloader. */
    final ClassLoader loader;

    /** The list of service provider classes (by class and interface). [java.lang.String, int[]] */
    private List<Object[]> infos = new CopyOnWriteArrayList();

    /** The expected internal form fully qualified class name. */
    private String fqcn;

    /**
     * <p>
     * Module constructor should be package private.
     * </p>
     *
     * @param path A module path as classpath, A <code>null</code> is not accepted.
     */
    Module(Path path, String pattern) throws MalformedURLException {
        super(ASM5);
        // we don't need to check null because this is internal class
        // if (moduleFile == null) {
        // }

        // Store original module path for unloading.
        this.path = path;
        this.pattern = pattern;
        this.loader = new URLClassLoader(new URL[] {path.toUri().toURL()}, I.$loader);

        Path base = path;

        // start scanning class files
        try {
            // At first, we must scan the specified directory or archive. If the module file is
            // archive, Sinobu automatically try to switch to other file system (e.g.
            // ZipFileSystem).
            if (Files.isRegularFile(path)) {
                base = FileSystems.newFileSystem(base, loader).getPath("/");
            }

            // Then, we can scan module transparently.
            for (Path file : I.walk(base, pattern.concat("**.class"))) {
                // exclude non-class file
                InputStream input = Files.newInputStream(file);
                String name = base.relativize(file).toString();

                // Don't write the following because "fqnc" field requires actual class name. If a
                // module returns a non-class file (e.g. properties, xml, txt) at the end, there is
                // a possibility that the module can't distinguish between system and Sinobu
                // module correctly.
                //
                // this.fqcn = name;
                //
                // compute fully qualified class name
                this.fqcn = name.substring(0, name.length() - 6).replace(File.separatorChar, '.').replace('/', '.');

                // try to read class file and check it
                try {
                    // static field reference will be inlined by compiler, so we should pass all
                    // flags to ClassReader (it doesn't increase byte code size)
                    new ClassReader(input).accept(this, SKIP_CODE | SKIP_DEBUG | SKIP_FRAMES);
                } catch (FileNotFoundException | IllegalStateException e) {
                    // FileNotFoundException
                    // this exception means that the given class name may indicate some external
                    // extension class or interface, so we can ignore it

                    // IllegalStateException
                    // scanning was stoped normally
                } finally {
                    I.quiet(input);
                }
            }
        } catch (IOException e) {
            throw I.quiet(e);
        }
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
            try {
                if (test(hash, info)) {
                    list.add(loader.loadClass((String) info[0]));

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
        Class<?> clazz = loader.loadClass((String) info[0]);
        Set<Class<?>> set = ClassUtil.getTypes(clazz);
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        // exclude a class file which is in invalid location
        if (!name.equals(fqcn.replace('.', '/'))) {
            throw STOP;
        }

        // must be public class (annotation is classified as interface)
        // must be non-abstract
        // must be non-enum
        // must be non-deprecated
        if ((ACC_SUPER & access) == 0 || ((ACC_ABSTRACT | ACC_ENUM | ACC_DEPRECATED) & access) != 0) {
            throw STOP;
        }

        // verify super class
        if (verify(superName)) {
            infos.add(new Object[] {fqcn, null});

            // this class may be extention point, we can stop scanning
            throw STOP;
        }

        // verify interfaces
        for (String interfaceClassName : interfaces) {
            if (verify(interfaceClassName)) {
                infos.add(new Object[] {fqcn, null});

                // this class may be extention point, we can stop scanning
                throw STOP;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        // exclude runtime invisible
        if (visible) {
            // verify annotation
            if (verify(desc.substring(1, desc.length() - 1))) {
                infos.add(new Object[] {fqcn, null});

                // this class may be extention point, we can stop scanning
                throw STOP;
            }
        }

        // continue scan process
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        // all needed information was scanned, stop parsing
        throw STOP;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        // all needed information was scanned, stop parsing
        throw STOP;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitInnerClass(String name, String outerName, String innerName, int access) {
        // all needed information was scanned, stop parsing
        throw STOP;
    }

    /**
     * Helper method to check whether the given class is non system class or not.
     *
     * @param className A class name.
     * @return A result.
     */
    private static final boolean verify(String className) {
        if (className.startsWith("java")) {
            char c = className.charAt(4);
            return c != '/' && (c != 'x' || className.charAt(5) != '/');
        }
        return !(className.startsWith("org/w3c/") || className.startsWith("org/xml/"));
    }
}
