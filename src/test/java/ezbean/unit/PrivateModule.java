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
package ezbean.unit;

import java.io.File;
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

    /**
     * <p>
     * Create private module with package name which is related to test class name.
     * </p>
     */
    public PrivateModule() {
        this(null);
    }

    /**
     * <p>
     * Create private module with the specified package name.
     * </p>
     * 
     * @param packageName
     */
    public PrivateModule(String packageName) {
        overriddenPackage = packageName != null ? packageName : testcase.getPackage().getName();
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
     * @see ezbean.unit.EzRule#beforeClass()
     */
    @Override
    protected void beforeClass() throws Exception {
        originalPackage = testcase.getPackage().getName().replace('.', '/');
        overriddenPackage = overriddenPackage.replace('.', '/');
        System.out.println(module);
        // create package directory
        File dest = I.locate(module, overriddenPackage);
        dest.mkdirs();

        // copy class files
        File source = I.locate(ClassUtil.getArchive(testcase), originalPackage);

        for (File file : source.listFiles()) {
            if (file.isFile() && file.getName().startsWith(testcase.getSimpleName() + "$")) {
                try {
                    // setup
                    ClassWriter writer = new ClassWriter(0);

                    // convert class file
                    new ClassReader(new FileInputStream(file)).accept(new ClassConverter(writer), 0);

                    // write new class file
                    FileOutputStream stream = new FileOutputStream(I.locate(dest, file.getName()));
                    stream.write(writer.toByteArray());
                    stream.close();
                } catch (IOException e) {
                    I.quiet(e);
                }
            }
        }
    }

    /**
     * @see ezbean.unit.EzRule#before(java.lang.reflect.Method)
     */
    @Override
    protected void before(Method method) throws Exception {
        load();
    }

    /**
     * @see ezbean.unit.EzRule#after(java.lang.reflect.Method)
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
        return name.replace(originalPackage + "/" + testcase.getSimpleName(), overriddenPackage + "/" + testcase.getSimpleName());
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
    }
}
