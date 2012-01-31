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

import static org.objectweb.asm.ClassReader.*;
import static org.objectweb.asm.Opcodes.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;

/**
 * @version 2011/11/19 9:11:22
 */
class ModuleVisitor extends ClassVisitor {

    /** The dirty process management. */
    private static final IllegalStateException STOP = new IllegalStateException();

    /** The list of service provider classes (by class and interface). [java.lang.String, int[]] */
    final List<Object[]> infos = new CopyOnWriteArrayList();

    /** The expected internal form fully qualified class name. */
    private String fqcn;

    /**
     * 
     */
    ModuleVisitor(Module module) {
        super(ASM4);

        Path base = module.path;

        // start scanning class files
        try {
            // At first, we must scan the specified directory or archive. If the module file is
            // archive, Sinobu automatically try to switch to other file system (e.g.
            // ZipFileSystem).
            if (Files.isRegularFile(module.path)) {
                base = FileSystems.newFileSystem(base, module).getPath("/");
            }

            // Then, we can scan module transparently.
            for (Path file : I.walk(base, "**.class")) {
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
                } catch (FileNotFoundException e) {
                    // this exception means that the given class name may indicate some external
                    // extension class or interface, so we can ignore it
                } catch (IllegalStateException e) {
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
     * @see org.objectweb.asm.ClassAdapter#visit(int, int, java.lang.String, java.lang.String,
     *      java.lang.String, java.lang.String[])
     */
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
     * @see org.objectweb.asm.ClassAdapter#visitAnnotation(java.lang.String, boolean)
     */
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
     * @see org.objectweb.asm.ClassAdapter#visitField(int, java.lang.String, java.lang.String,
     *      java.lang.String, java.lang.Object)
     */
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        // all needed information was scanned, stop parsing
        throw STOP;
    }

    /**
     * @see org.objectweb.asm.ClassAdapter#visitMethod(int, java.lang.String, java.lang.String,
     *      java.lang.String, java.lang.String[])
     */
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        // all needed information was scanned, stop parsing
        throw STOP;
    }

    /**
     * @see org.objectweb.asm.ClassAdapter#visitInnerClass(java.lang.String, java.lang.String,
     *      java.lang.String, int)
     */
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

        if (className.startsWith("org/w3c/") || className.startsWith("org/xml/")) {
            return false;
        }
        return true;
    }
}
