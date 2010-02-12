/*
 * Copyright (C) 2010 Nameless Production Committee.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ezunit;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;

import ezbean.I;
import ezbean.io.FileSystem;
import ezbean.model.ClassUtil;
import ezbean.module.ModuleLoader;

/**
 * @version 2010/02/03 23:12:40
 */
public class PrivateModule extends EzRule {

    /** The actual private module. */
    private final File module = FileSystem.createTemporary();

    /** The original package name. */
    private String originalPackage;

    /** The overridden package name. */
    private String overriddenPackage;

    /** The private class filter. */
    private final PrivateClassStrategy strategy;

    /**
     * <p>
     * Create private module with package name which is related to test class name.
     * </p>
     */
    public PrivateModule() {
        this((String) null);
    }

    /**
     * <p>
     * Create private module with the specified package name.
     * </p>
     * 
     * @param packageName
     */
    public PrivateModule(String packageName) {
        originalPackage = testcase.getPackage().getName();
        overriddenPackage = packageName != null ? packageName : testcase.getPackage().getName();
        strategy = new PrivateClassStrategy() {

            /**
             * @see java.io.FileFilter#accept(java.io.File)
             */
            public boolean accept(File file) {
                return file.isFile() && file.getName().startsWith(testcase.getSimpleName() + "$");
            }

            /**
             * @see ezunit.PrivateModule.PrivateClassStrategy#compute(java.lang.String)
             */
            public String compute(String name) {
                return overriddenPackage + "." + testcase.getSimpleName() + "$" + name;
            }
        };
    }

    /**
     * <p>
     * Make the specified package as private module.
     * </p>
     * 
     * @param packageClass
     */
    public PrivateModule(Class packageClass) {
        originalPackage = packageClass.getPackage().getName();
        overriddenPackage = originalPackage.substring(originalPackage.lastIndexOf('.') + 1);
        strategy = new PrivateClassStrategy() {

            /**
             * @see java.io.FileFilter#accept(java.io.File)
             */
            public boolean accept(File file) {
                return true;
            }

            /**
             * @see ezunit.PrivateModule.PrivateClassStrategy#compute(java.lang.String)
             */
            public String compute(String name) {
                return overriddenPackage + "." + name;
            }
        };
    }

    /**
     * <p>
     * load this private module.
     * </p>
     */
    public void load() {
        I.load(module);
    }

    /**
     * <p>
     * Unload this private module.
     * </p>
     */
    public void unload() {
        I.unload(module);
    }

    /**
     * <p>
     * Helper method to load class in this module by simple name.
     * </p>
     * 
     * @param name A simple class name.
     * @return A class in this module.
     * @throws ClassNotFoundException If the class name is invalid.
     */
    public Class forName(String name) {
        try {
            return ModuleLoader.getModuleLoader(null).loadClass(compute(name));
        } catch (ClassNotFoundException e) {
            throw I.quiet(e);
        }
    }

    /**
     * <p>
     * Helper method to load class in this module by simple name.
     * </p>
     * 
     * @param name A simple class name.
     * @return A class in this module.
     * @throws IllegalArgumentException If the class is <code>null</code>.
     */
    public String forName(Class clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("The given class is null.");
        }
        return compute(clazz.getSimpleName());
    }

    /**
     * <p>
     * Helper method to compute the class name in this private module.
     * </p>
     * 
     * @param name A simple name.
     * @return A fully qualified class name.
     */
    private String compute(String name) {
        if (name == null || name.length() == 0) {
            throw new IllegalArgumentException("The given name is null.");
        }
        return strategy.compute(name);
    }

    /**
     * @see ezunit.EzRule#beforeClass()
     */
    @Override
    protected void beforeClass() throws Exception {
        originalPackage = originalPackage.replace('.', '/');
        overriddenPackage = overriddenPackage.replace('.', '/');

        // copy class file with type conversion
        File source = I.locate(ClassUtil.getArchive(testcase), originalPackage);
        File target = I.locate(module, overriddenPackage);

        copy(source, target);
    }

    /**
     * <p>
     * Copy class file with type conversion.
     * </p>
     * 
     * @param source A source directory.
     * @param target A target directory.
     */
    private void copy(File source, File target) {
        target.mkdirs();

        for (File file : source.listFiles(strategy)) {
            if (file.isDirectory()) {
                copy(file, new File(target, file.getName()));
            } else {
                try {
                    if (!file.getName().endsWith("class")) {
                        FileSystem.copy(source, I.locate(target, file.getName()));
                    } else {
                        // setup
                        ClassWriter writer = new ClassWriter(0);

                        // convert class file
                        new ClassReader(new FileInputStream(file)).accept(new ClassConverter(writer), 0);

                        // write new class file
                        FileOutputStream stream = new FileOutputStream(I.locate(target, file.getName()));
                        stream.write(writer.toByteArray());
                        stream.close();
                    }
                } catch (IOException e) {
                    I.quiet(e);
                }
            }
        }
    }

    /**
     * @see ezunit.EzRule#before(java.lang.reflect.Method)
     */
    @Override
    protected void before(Method method) throws Exception {
        load();
    }

    /**
     * @see ezunit.EzRule#after(java.lang.reflect.Method)
     */
    @Override
    protected void after(Method method) {
        unload();
    }

    /**
     * Helper method to conver package name.
     * 
     * @param name
     * @return
     */
    private String convert(String name) {
        return name.replace(originalPackage.concat("/"), overriddenPackage.concat("/"));
    }

    /**
     * Helper method to conver package name.
     * 
     * @param names
     * @return
     */
    private String[] convert(String[] names) {
        if (names != null) {
            for (int i = 0; i < names.length; i++) {
                names[i] = convert(names[i]);
            }
        }

        return names;
    }

    /**
     * @version 2010/02/04 0:28:39
     */
    private final class ClassConverter extends ClassAdapter {

        /**
         * @param visiter
         */
        public ClassConverter(ClassVisitor visiter) {
            super(visiter);
        }

        /**
         * @see org.objectweb.asm.ClassAdapter#visit(int, int, java.lang.String, java.lang.String,
         *      java.lang.String, java.lang.String[])
         */
        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            super.visit(version, access, convert(name), signature, convert(superName), convert(interfaces));
        }

        /**
         * @see org.objectweb.asm.ClassAdapter#visitMethod(int, java.lang.String, java.lang.String,
         *      java.lang.String, java.lang.String[])
         */
        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            return new MethodConverter(super.visitMethod(access, convert(name), convert(desc), signature, convert(exceptions)));
        }
    }

    /**
     * @version 2010/02/04 0:28:33
     */
    private final class MethodConverter extends MethodAdapter {

        /**
         * @param paramMethodVisitor
         */
        public MethodConverter(MethodVisitor paramMethodVisitor) {
            super(paramMethodVisitor);
        }

        /**
         * @see org.objectweb.asm.MethodAdapter#visitMethodInsn(int, java.lang.String,
         *      java.lang.String, java.lang.String)
         */
        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc) {
            super.visitMethodInsn(opcode, convert(owner), convert(name), convert(desc));
        }

        /**
         * @see org.objectweb.asm.MethodAdapter#visitFieldInsn(int, java.lang.String,
         *      java.lang.String, java.lang.String)
         */
        @Override
        public void visitFieldInsn(int opcode, String owner, String name, String desc) {
            super.visitFieldInsn(opcode, convert(owner), convert(name), convert(desc));
        }
    }

    /**
     * @version 2010/02/04 12:44:12
     */
    private static interface PrivateClassStrategy extends FileFilter {

        /**
         * Compute fully qualified class name.
         * 
         * @param name A simple class name.
         * @return A fully qualified class name.
         */
        String compute(String name);
    }
}
