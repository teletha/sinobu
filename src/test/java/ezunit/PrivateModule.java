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
package ezunit;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Path;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;

import ezbean.I;
import ezbean.Modules;
import ezbean.io.FilePath;

/**
 * @version 2010/11/15 0:00:48
 */
public class PrivateModule extends ReusableRule {

    /** The actual private module. */
    public final Path path = FilePath.createTemporary().toPath();

    /** The original package name. */
    private final String originalPackage;

    /** The overridden package name. */
    private final String overriddenPackage;

    /** The private class filter. */
    private final PrivateClassStrategy strategy;

    private boolean createJar;

    /**
     * <p>
     * Create private module with package name which is related to test class name.
     * </p>
     */
    public PrivateModule() {
        this(false, false);
    }

    /**
     * <p>
     * Create private module with package name which is related to test class name.
     * </p>
     */
    public PrivateModule(boolean renamePackage, boolean createJar) {
        this.createJar = createJar;

        // compute packaging structure
        originalPackage = testcase.getPackage().getName().replace('.', '/');
        overriddenPackage = renamePackage ? testcase.getSimpleName().toLowerCase() : originalPackage;

        strategy = new PrivateClassStrategy() {

            /**
             * {@inheritDoc}
             */
            @Override
            protected boolean accept(String fqcn) {
                return fqcn.startsWith(testcase.getName().replace('.', '/').concat("$"));
            }
        };
    }

    /**
     * <p>
     * Create private module with package name which is related to test class name.
     * </p>
     */
    public PrivateModule(String relativePath) {
        this(relativePath, false, false);
    }

    /**
     * <p>
     * Create private module with package name which is related to test class name.
     * </p>
     */
    public PrivateModule(String relativePath, boolean renamePackage, boolean createJar) {
        this.createJar = createJar;

        // normalize
        relativePath = relativePath.replace(File.separatorChar, '/');

        // compute packaging structure
        int index = relativePath.lastIndexOf('/');
        originalPackage = testcase.getPackage().getName().replace('.', '/') + "/" + relativePath;
        overriddenPackage = renamePackage ? index == -1 ? relativePath : relativePath.substring(index + 1)
                : originalPackage;

        strategy = new PrivateClassStrategy() {

            /**
             * {@inheritDoc}
             */
            @Override
            protected boolean accept(String fqcn) {
                return fqcn.startsWith(originalPackage);
            }
        };
    }

    /**
     * <p>
     * load this private module.
     * </p>
     */
    public void load() {
        I.load(path.toFile());
    }

    /**
     * <p>
     * Unload this private module.
     * </p>
     */
    public void unload() {
        I.unload(path.toFile());
    }

    /**
     * <p>
     * Convert to the class associated with this private module.
     * </p>
     * 
     * @param clazz
     * @return
     */
    public Class convert(Class clazz) {
        return Modules.load(clazz.getName()
                .replace(originalPackage.replace('/', '.'), overriddenPackage.replace('/', '.')));
    }

    /**
     * @see ezunit.ReusableRule#beforeClass()
     */
    @Override
    protected void beforeClass() throws Exception {
        // copy class file with type conversion
        copy(new File(testcaseRoot, originalPackage), I.locate(path.toFile(), overriddenPackage));
    }

    /**
     * @see ezunit.ReusableRule#before(java.lang.reflect.Method)
     */
    @Override
    protected void before(Method method) throws Exception {
        load();
    }

    /**
     * @see ezunit.ReusableRule#after(java.lang.reflect.Method)
     */
    @Override
    protected void after(Method method) {
        unload();
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
            File dist = I.locate(target, file.getName());

            if (file.isDirectory()) {
                copy(file, dist);
            } else {
                try {
                    if (!file.getName().endsWith("class")) {
                        I.copy(source, dist);
                    } else {
                        // setup
                        ClassWriter writer = new ClassWriter(0);

                        // convert class file
                        new ClassReader(new FileInputStream(file)).accept(new ClassConverter(writer), 0);

                        // write new class file
                        FileOutputStream stream = new FileOutputStream(dist);
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
     * <p>
     * Helper method to conver package name.
     * </p>
     * 
     * @param name A class name.
     * @return A converted name.
     */
    private String convert(String name) {
        return strategy.accept(name) ? name.replace(originalPackage, overriddenPackage) : name;
    }

    /**
     * <p>
     * Helper method to conver package name.
     * </p>
     * 
     * @param name A class name.
     * @return A converted name.
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
     * @version 2010/10/14 1:16:59
     */
    private abstract class PrivateClassStrategy implements FileFilter {

        /** The pre-conputed path length. */
        private int prefix = testcaseRoot.getAbsolutePath().length() + 1;

        /**
         * {@inheritDoc}
         */
        public final boolean accept(File file) {
            return accept(file.getAbsolutePath().substring(prefix).replace(File.separatorChar, '/'));
        }

        /**
         * <p>
         * Determinate whether the given class name or path is acceptable or not.
         * </p>
         * 
         * @param fqcn Internal Form of Fully Qualified Class Name (JVMS 4.2).
         * @return A result.
         */
        protected abstract boolean accept(String fqcn);
    }
}
