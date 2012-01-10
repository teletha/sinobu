/*
 * Copyright (C) 2012 Nameless Production Committee.
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
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.Iterator;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import com.sun.tools.attach.spi.AttachProvider;

import ezbean.I;
import ezbean.model.ClassUtil;

/**
 * @version 2012/01/10 9:52:42
 */
public class PowerAssert extends ReusableRule {

    /** The agent archive. */
    private static Path file = I.locateTemporary().resolveSibling("aa.zip");

    /** The current processing virtual machine. */
    private static VirtualMachine vm;

    static {
        String name = ManagementFactory.getRuntimeMXBean().getName();
        String id = name.substring(0, name.indexOf('@'));

        root: for (AttachProvider provider : AttachProvider.providers()) {
            for (VirtualMachineDescriptor descriptor : provider.listVirtualMachines()) {
                if (descriptor.id().equals(id)) {
                    try {
                        vm = provider.attachVirtualMachine(descriptor);
                    } catch (Exception e) {
                        throw I.quiet(e);
                    }
                    break root;
                }
            }
        }

        // create agent archive
        try {
            Path archive = ClassUtil.getArchive(Agent.class);
            Path agent = archive.resolve(Agent.class.getName().replace('.', File.separatorChar).concat(".class"));

            Manifest manifest = new Manifest();
            Attributes attributes = manifest.getMainAttributes();
            attributes.putValue("Manifest-Version", "1.0");
            attributes.putValue("Agent-Class", Agent.class.getName());
            attributes.putValue("Can-Retransform-Classes", "true");
            JarOutputStream stream = new JarOutputStream(Files.newOutputStream(file), manifest);
            JarEntry entry = new JarEntry("ezunit/PowerAssert$Agent.class");
            stream.putNextEntry(entry);
            stream.write(Files.readAllBytes(agent));
            stream.closeEntry();
            stream.finish();
            stream.close();
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }

    public PowerAssert() {
        System.out.println(vm);

        try {
            vm.loadAgent(file.toString());
        } catch (AgentLoadException e) {
            throw I.quiet(e);
        } catch (AgentInitializationException e) {
            throw I.quiet(e);
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }

    /**
     * @see ezunit.ReusableRule#customError(java.lang.AssertionError)
     */
    @Override
    protected AssertionError customError(AssertionError error) {

        return new AssertionError("cusom");
    }

    /**
     * @version 2012/01/10 11:51:03
     */
    public static class Agent implements ClassFileTransformer {

        /**
         * @see java.lang.instrument.ClassFileTransformer#transform(java.lang.ClassLoader,
         *      java.lang.String, java.lang.Class, java.security.ProtectionDomain, byte[])
         */
        @Override
        public byte[] transform(ClassLoader loader, String name, Class<?> clazz, ProtectionDomain domain, byte[] bytes)
                throws IllegalClassFormatException {
            if (name.equals("ezunit/PowerAssertTest")) {
                System.out.println("trans");
                ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
                ClassNode visitor = new ClassNode();
                ClassReader reader = new ClassReader(bytes);
                reader.accept(visitor, ClassReader.EXPAND_FRAMES | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);

                Iterator<MethodNode> iterator = visitor.methods.iterator();

                while (iterator.hasNext()) {
                    MethodNode node = iterator.next();
                    System.out.println(node.name);
                    System.out.println(node.localVariables + "  " + node.instructions.size());
                    System.out.println(Arrays.toString(node.instructions.toArray()));

                }

                visitor.accept(writer);
                return writer.toByteArray();
            }
            return bytes;
        }

        /**
         * <p>
         * Start up agent.
         * </p>
         * 
         * @param args
         * @param instrumentation
         */
        public static void agentmain(String args, Instrumentation instrumentation) {
            System.out.println(instrumentation);
            instrumentation.addTransformer(new Agent(), true);

            try {
                instrumentation.retransformClasses(PowerAssertTest.class);
            } catch (UnmodifiableClassException e) {
                throw I.quiet(e);
            }
        }

        /**
         * @version 2012/01/10 14:38:00
         */
        private static class Visitor extends ClassVisitor {

            private Visitor(ClassVisitor visitor) {
                super(Opcodes.ASM4, visitor);
            }

            /**
             * @see org.objectweb.asm.ClassVisitor#visitMethod(int, java.lang.String,
             *      java.lang.String, java.lang.String, java.lang.String[])
             */
            @Override
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                System.out.println(name);
                MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);

                if (name.equals("testname")) {

                    return new Transformer(mv);
                } else {
                    return mv;
                }

            }
        }

        /**
         * @version 2012/01/10 14:40:38
         */
        private static class Transformer extends MethodNode {

            private Transformer(MethodVisitor visitor) {
                super(Opcodes.ASM4);
                mv = visitor;
            }

            /**
             * @see org.objectweb.asm.MethodVisitor#visitFieldInsn(int, java.lang.String,
             *      java.lang.String, java.lang.String)
             */
            @Override
            public void visitFieldInsn(int opcode, String owner, String name, String desc) {
                if (name.equals("$assertionsDisabled")) {
                    System.out.println("assert");
                }
                super.visitFieldInsn(opcode, owner, name, desc);
            }
        }
    }

    public static final void validate() {

    }
}
