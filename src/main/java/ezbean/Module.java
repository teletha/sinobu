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
package ezbean;

import static org.objectweb.asm.Opcodes.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

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
 * @version 2010/11/07 0:20:52
 */
class Module extends URLClassLoader implements ClassVisitor {

    /**
     * The root class loader in EasyBena environment. This {@link Module} can access all classes
     * which are managed and enhanced by Ezbean.
     */
    static final Module root = new Module(null);

    /** The dirty process management. */
    private static final IllegalStateException STOP = new IllegalStateException();

    /** The root of this module. */
    final File moduleFile;

    /** The list of service provider classes (by class and interface). [java.lang.String, int[]] */
    private final List<Object[]> infos = new CopyOnWriteArrayList();

    /** The expected internal form fully qualified class name. */
    private String fqcn;

    /**
     * Module constructor should be package private.
     * 
     * @param moduleFile A module file as classpath, A <code>null</code> is not accepted.
     * @param core A flag whether the module should be loaded as <dfn>system module</dfn> or Ezbean
     *            core module. The system module will be loaded by the class loader which isn't
     *            managed by Ezbean (e.g. {@link ClassLoader#getSystemClassLoader()}), and it never
     *            be unloaded or reloaded.
     */
    Module(File moduleFile) {
        super(convert(moduleFile), I.loader);

        // we don't need to check null because this is internal class
        // if (moduleFile == null) {
        // }

        if (moduleFile == null) {
            this.moduleFile = null;
            return;
        }

        // cast to ezbean.io.File if the specified module file is java.io.File
        this.moduleFile = I.locate(moduleFile.getPath());

        // start scanning class files
        try {
            // At first, we must scan the specified directory or archive. If the module file is
            // archive, Ezbean automatically try to unpack it into the temporary region of the
            // file system. But, in some enviroment, SecurityManager doesn't allow a file to be
            // created. In that case, we switch to the alternative scanning method based on the
            // fault tolerant design.
            try {
                // don't write the following because we need ezbean.io.File
                //
                // dig(moduleFile, moduleFile.toString().length() + 1);
                dig(this.moduleFile, this.moduleFile.toString().length() + 1);
            } catch (SecurityException e) {
                // The current environment doesn't allow a file to be created for transparent
                // archive access. So we retry to scan a module file directly using ZipFIle.
                ZipFile zip = new ZipFile(moduleFile);
                Enumeration<? extends ZipEntry> entries = zip.entries();

                while (entries.hasMoreElements()) {
                    ZipEntry file = entries.nextElement();

                    if (!file.isDirectory()) {
                        scan(file.getName(), zip.getInputStream(file));
                    }
                }
            }
        } catch (IOException e) {
            // If this exception will be thrown, it is bug of this program. So we must rethrow
            // the wrapped error in here.
            throw new Error(e);
        }
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
            if (test(hash, info)) {
                try {
                    list.add(loadClass((String) info[0]));

                    if (single) {
                        return list;
                    }
                } catch (ClassNotFoundException e) {
                    // If this exception will be thrown, it is bug of this program. So we must
                    // rethrow the wrapped error in here.
                    throw new Error(e);
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

        // lazy evaluation
        try {
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
        } catch (ClassNotFoundException e) {
            // If this exception will be thrown, it is bug of this program. So we must rethrow the
            // wrapped error in here.
            throw new Error(e);
        }
    }

    /**
     * Helper method to dig the module structure.
     * 
     * @param directory A directory to search.
     * @parma start A start position of the fully qualified class name.
     * @throws IOException If the specified file can't read.
     * @throws SecurityException If a security manager exists and its
     *             {@link SecurityManager#checkWrite(String)} method does not allow a file to be
     *             created.
     */
    private void dig(File directory, int start) throws IOException {
        for (File file : directory.listFiles()) {
            // recursive function call only for directory, not archive
            if (file.isFile()) {
                scan(file.toString().substring(start), new FileInputStream(file));
            } else {
                dig(file, start);
            }
        }
    }

    /**
     * Helper method to scan class file.
     * 
     * @param name A file name.
     * @param input A input stream for the specified file.
     * @throws IOException If the class file can't open.
     */
    private void scan(String name, InputStream input) throws IOException {
        // exclude non-class file
        if (name.endsWith(".class")) {
            // Don't write the following because "fqnc" field requires actual class name. If a
            // module returns a non-class file (e.g. properties, xml, txt) at the end, there is
            // a possibility that the module can't distinguish between system and Ezbean
            // module correctly.
            //
            // this.fqcn = name;
            //
            // compute fully qualified class name
            this.fqcn = name.substring(0, name.length() - 6).replace('/', '.');

            // try to read class file and check it
            try {
                // static field reference will be inlined by compiler, so we should pass all
                // flags to ClassReader (it doesn't increase byte code size)
                new ClassReader(input).accept(this, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            } catch (FileNotFoundException e) {
                // this exception means that the given class name may indicate some external
                // extension class or interface, so we can ignore it
            } catch (IllegalStateException e) {
                // scanning was stoped normally
            } finally {
                input.close();
            }
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

    /**
     * <p>
     * Helper method to convert File to array of URL.
     * </p>
     * 
     * @param file
     * @return
     */
    private static final URL[] convert(File file) {
        try {
            return file == null ? new URL[0] : new URL[] {file.toURI().toURL()};
        } catch (MalformedURLException e) {
            // If this exception will be thrown, it is bug of this program. So we must rethrow the
            // wrapped error in here.
            throw new Error(e);
        }
    }
}
