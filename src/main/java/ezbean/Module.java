/*
 * Copyright (C) 2011 Nameless Production Committee.
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
package ezbean;

import static java.nio.file.FileVisitResult.*;
import static org.objectweb.asm.ClassReader.*;
import static org.objectweb.asm.Opcodes.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;

import ezbean.model.ClassUtil;
import ezbean.model.Model;

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
 * @version 2011/03/07 15:45:35
 */
class Module extends URLClassLoader implements ClassVisitor, FileVisitor<Path> {

    /** The dirty process management. */
    private static final IllegalStateException STOP = new IllegalStateException();

    /** The root of this module. */
    final Path path;

    /** The list of service provider classes (by class and interface). [java.lang.String, int[]] */
    private final List<Object[]> infos = new CopyOnWriteArrayList();

    /** The expected internal form fully qualified class name. */
    private String fqcn;

    /**
     * <p>
     * Module constructor should be package private.
     * </p>
     * 
     * @param path A module path as classpath, A <code>null</code> is not accepted.
     */
    Module(Path path) throws MalformedURLException {
        super(new URL[] {path.toUri().toURL()}, I.loader);

        // we don't need to check null because this is internal class
        // if (moduleFile == null) {
        // }

        // Store original module path for unloading.
        this.path = path;

        // start scanning class files
        try {
            // At first, we must scan the specified directory or archive. If the module file is
            // archive, Ezbean automatically try to switch to other file system (e.g.
            // ZipFileSystem).
            if (Files.isRegularFile(path)) {
                path = FileSystems.newFileSystem(path, this).getPath("/");
            }

            // Then, we can scan module transparently.
            Files.walkFileTree(path, this);
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }

    /**
     * @see java.nio.file.FileVisitor#preVisitDirectory(java.lang.Object,
     *      java.nio.file.attribute.BasicFileAttributes)
     */
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        return CONTINUE;
    }

    /**
     * @see java.nio.file.FileVisitor#postVisitDirectory(java.lang.Object, java.io.IOException)
     */
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        return CONTINUE;
    }

    /**
     * @see java.nio.file.FileVisitor#visitFile(java.lang.Object,
     *      java.nio.file.attribute.BasicFileAttributes)
     */
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        // exclude non-class file
        if (file.toString().endsWith(".class")) {
            InputStream input = Files.newInputStream(file);
            String name = path.relativize(file).toString();

            // Don't write the following because "fqnc" field requires actual class name. If a
            // module returns a non-class file (e.g. properties, xml, txt) at the end, there is
            // a possibility that the module can't distinguish between system and Ezbean
            // module correctly.
            //
            // this.fqcn = name;
            //
            // compute fully qualified class name
            this.fqcn = name.substring(0, name.length() - 6).replace(File.separatorChar, '.');

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
                input.close();
            }
        }
        return CONTINUE;
    }

    /**
     * @see java.nio.file.FileVisitor#visitFileFailed(java.lang.Object, java.io.IOException)
     */
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        return CONTINUE;
    }

    /**
     * Returns the automatic generated class which implements or extends the given model.
     * 
     * @param model A class information of the model.
     * @param trace If trace is <code>1</code>, the generated code will be traceable code for mock
     *            object, otherwise for normal bean object.
     * @return A generated {@link Class} object.
     */
    synchronized Class define(Model model, char trace) {
        // Compute fully qualified class name for the generated class.
        // The coder class name is prefix to distinguish enhancer type by a name and make core
        // package classes (e.g. swing components) enhance.
        // The statement "String name = coder.getName() + model.type.getName();" produces larger
        // byte code and more objects. To reduce them, we should use the method "concat".
        String name = model.type.getName().concat(String.valueOf(trace));

        if (name.startsWith("java.")) {
            name = "$".concat(name);
        }

        // find class from cache of class loader
        Class clazz = findLoadedClass(name);

        if (clazz == null) {
            // start writing byte code
            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);

            // build interceptor chain
            ClassVisitor current = writer;

            for (Enhancer enhancer : I.find(Enhancer.class)) {
                current = enhancer.chain(current, model, name);
            }

            // write code actually
            ((Enhancer) current).write(trace);

            // retrieve byte code
            byte[] bytes = writer.toByteArray();

            // define class
            clazz = defineClass(name, bytes, 0, bytes.length, model.type.getProtectionDomain());
        }

        // API definition
        return clazz;
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

        // stealth class must be hidden from module
        if (ClassUtil.getMiniConstructor(clazz) == null) {
            return !infos.remove(info); // test method requires false for stealth classs
        }

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
     * @see org.objectweb.asm.ClassAdapter#visitOuterClass(java.lang.String, java.lang.String,
     *      java.lang.String)
     */
    public void visitOuterClass(String owner, String name, String desc) {
        // skip for annotation
    }

    /**
     * @see org.objectweb.asm.ClassAdapter#visitEnd()
     */
    public void visitEnd() {
        // finish scanning
    }

    /**
     * @see org.objectweb.asm.ClassAdapter#visitSource(java.lang.String, java.lang.String)
     */
    public void visitSource(String source, String debug) {
        // skip for annotation
    }

    /**
     * @see org.objectweb.asm.ClassAdapter#visitAttribute(org.objectweb.asm.Attribute)
     */
    public void visitAttribute(Attribute attr) {
        // skip for annotation
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
