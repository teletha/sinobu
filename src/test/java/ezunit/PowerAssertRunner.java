/*
 * Copyright (C) 2011 Nameless Production Committee.
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

import static org.objectweb.asm.Opcodes.*;

import org.junit.Rule;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;

import ezbean.I;

/**
 * Can't recognize {@link Rule}.
 * 
 * @version 2011/03/09 13:18:28
 */
public class PowerAssertRunner extends BlockJUnit4ClassRunner {

    /** The actual class loader for test class. */
    private static final Loader loader = new Loader();

    // initialization
    static {
        loader.setDefaultAssertionStatus(true);
    }

    /**
     * <p>
     * Provide Power Assert into Java.
     * </p>
     * 
     * @param clazz
     * @throws InitializationError
     */
    public PowerAssertRunner(Class<?> clazz) throws InitializationError {
        super(convert(clazz));
    }

    /**
     * <p>
     * Convert class.
     * </p>
     * 
     * @param clazz
     * @return
     */
    private static Class convert(Class clazz) {
        try {
            ClassReader reader = new ClassReader(clazz.getName());
            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);

            reader.accept(writer, 0);

            return loader.defineClass(clazz.getName(), writer.toByteArray());
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }

    /**
     * @version 2011/03/09 13:35:11
     */
    private static final class Converter extends ClassAdapter {

        /**
         * @param arg0
         */
        public Converter(ClassVisitor visitor) {
            super(visitor);
        }

        /**
         * @see org.objectweb.asm.ClassAdapter#visit(int, int, java.lang.String, java.lang.String,
         *      java.lang.String, java.lang.String[])
         */
        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            super.visit(version, access, name, signature, superName, interfaces);
        }

        /**
         * @see org.objectweb.asm.ClassAdapter#visitMethod(int, java.lang.String, java.lang.String,
         *      java.lang.String, java.lang.String[])
         */
        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            return new MethodConverter(super.visitMethod(access, name, desc, signature, exceptions));
        }

    }

    /**
     * @version 2011/03/09 13:35:11
     */
    private static final class MethodConverter extends MethodAdapter {

        /** The flag. */
        private boolean nextJumpIsAssertionStart = false;

        /** The flag. */
        private boolean whileAssertion = false;

        /** The assertion end label. */
        private Label assertionEndLabel;

        /**
         * @param arg0
         */
        public MethodConverter(MethodVisitor visitor) {
            super(visitor);
        }

        /**
         * @see org.objectweb.asm.MethodAdapter#visitFieldInsn(int, java.lang.String,
         *      java.lang.String, java.lang.String)
         */
        @Override
        public void visitFieldInsn(int opcode, String owner, String name, String desc) {
            super.visitFieldInsn(opcode, owner, name, desc);

            if (opcode == GETSTATIC && name.equals("$assertionsDisabled")) {
                // start assert code
                nextJumpIsAssertionStart = true;
            }
        }

        /**
         * @see org.objectweb.asm.MethodAdapter#visitJumpInsn(int, org.objectweb.asm.Label)
         */
        @Override
        public void visitJumpInsn(int opcode, Label label) {
            super.visitJumpInsn(opcode, label);

            if (nextJumpIsAssertionStart) {
                nextJumpIsAssertionStart = false;
                assertionEndLabel = label;
                whileAssertion = true;
            }
        }

        /**
         * @see org.objectweb.asm.MethodAdapter#visitVarInsn(int, int)
         */
        @Override
        public void visitVarInsn(int opcode, int var) {
            if (!whileAssertion) {
                super.visitVarInsn(opcode, var);
            } else {
                super.visitVarInsn(opcode, var);
            }

        }

    }

    /**
     * <p>
     * Special class loader for Power Assert.
     * </p>
     * 
     * @version 2011/03/09 18:47:00
     */
    private static final class Loader extends ClassLoader {

        /**
         * @param parent
         */
        private Loader() {
            super(Thread.currentThread().getContextClassLoader());
        }

        /**
         * <p>
         * Expose class definition method.
         * </p>
         * 
         * @param name
         * @param bytes
         * @return
         */
        private Class defineClass(String name, byte[] bytes) {
            return defineClass(name, bytes, 0, bytes.length);
        }
    }
}
