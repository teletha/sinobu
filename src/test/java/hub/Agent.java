/*
 * Copyright (C) 2012 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package hub;

import static org.objectweb.asm.Opcodes.*;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import kiss.I;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * <p>
 * Provide functionality to transform bytecode.
 * </p>
 * 
 * @version 2012/01/10 19:09:14
 */
public class Agent extends ReusableRule {

    /** The entry point for Attach API. */
    private static Method attach;

    /** The entry point for Attach API. */
    private static Method loadAgent;

    // load Attach API
    static {
        // search attach method
        try {
            Class clazz = UnsafeUtility.getTool("com.sun.tools.attach.VirtualMachine");

            attach = clazz.getMethod("attach", String.class);
            loadAgent = clazz.getMethod("loadAgent", String.class);
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }

    /** The redefined classes. */
    private static final Set<String> redefines = new HashSet();

    /** The Instrumentation tool. */
    private volatile static Instrumentation tool;

    /** The actual agent. */
    private final ClassFileTransformer agent;

    /**
     * <p>
     * Create dynamic Agent.
     * </p>
     * 
     * @param translator
     */
    public Agent(Class<? extends Translator> translator) {
        this(new TranslatorTransformer(translator));
    }

    /**
     * <p>
     * Create dynamic Agent.
     * </p>
     * 
     * @param agent
     */
    public Agent(ClassFileTransformer agent) {
        synchronized (Agent.class) {
            if (tool == null) {
                createTool();
            }
        }

        // store agent
        this.agent = agent;

        // register agent
        tool.addTransformer(agent, true);
    }

    /**
     * <p>
     * Force to transform the target class.
     * </p>
     * 
     * @param target
     */
    public void transform(Class target) {
        try {
            redefines.add(target.getName().replace('.', '/'));
            tool.retransformClasses(target);
        } catch (UnmodifiableClassException e) {
            throw I.quiet(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void afterClass() {
        tool.removeTransformer(agent);
    }

    /**
     * <p>
     * Create instrumentation tool.
     * </p>
     */
    private static void createTool() {
        // Build manifest.
        Manifest manifest = new Manifest();
        Attributes attributes = manifest.getMainAttributes();
        attributes.putValue("Manifest-Version", "1.0");
        attributes.putValue("Agent-Class", Agent.class.getName());
        attributes.putValue("Can-Redefine-Classes", "true");
        attributes.putValue("Can-Retransform-Classes", "true");
        attributes.putValue("Can-Set-Native-Method-Prefix", "true");

        try {
            // Build temporary agent jar.
            Path jar = I.locateTemporary();
            new JarOutputStream(Files.newOutputStream(jar), manifest).close();

            // Load agent dynamically.
            String name = ManagementFactory.getRuntimeMXBean().getName();
            loadAgent.invoke(attach.invoke(null, name.substring(0, name.indexOf('@'))), jar.toString());
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }

    /**
     * Agent entry point.
     */
    @SuppressWarnings("unused")
    private static void agentmain(String args, Instrumentation instrumentation) throws Exception {
        tool = instrumentation;
    }

    /**
     * @version 2012/01/14 13:09:23
     */
    private static final class TranslatorTransformer implements ClassFileTransformer {

        /** The delegator. */
        private final Class<? extends Translator> translator;

        /**
         * @param translator
         */
        private TranslatorTransformer(Class<? extends Translator> translator) {
            this.translator = translator;
        }

        /**
         * @see java.lang.instrument.ClassFileTransformer#transform(java.lang.ClassLoader,
         *      java.lang.String, java.lang.Class, java.security.ProtectionDomain, byte[])
         */
        @Override
        public byte[] transform(ClassLoader loader, String name, Class<?> clazz, ProtectionDomain domain, byte[] bytes) {
            if (!redefines.contains(name)) {
                return bytes;
            }

            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
            ClassTranslator visitor = new ClassTranslator(writer, name);
            ClassReader reader = new ClassReader(bytes);
            reader.accept(visitor, ClassReader.EXPAND_FRAMES);

            return writer.toByteArray();
        }

        /**
         * @version 2012/01/14 13:16:21
         */
        private class ClassTranslator extends ClassVisitor {

            /** The internal class name. */
            private final String className;

            /**
             * @param arg0
             */
            private ClassTranslator(ClassWriter writer, String className) {
                super(Opcodes.ASM4, writer);

                this.className = className;
            }

            /**
             * @see org.objectweb.asm.ClassVisitor#visitMethod(int, java.lang.String,
             *      java.lang.String, java.lang.String, java.lang.String[])
             */
            @Override
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                MethodVisitor visitor = super.visitMethod(access, name, desc, signature, exceptions);
                Translator translator = I.make(TranslatorTransformer.this.translator);
                translator.set(visitor, className, name, Type.getMethodType(desc));

                return translator;
            }
        }
    }

    /**
     * @version 2012/01/14 13:08:33
     */
    public static abstract class Translator<T extends Translator<T>> extends MethodVisitor {

        /** The internal class name. */
        protected String className;

        /** The internal method name. */
        protected String methodName;

        /** The internal method type. */
        protected Type methodType;

        /**
         * 
         */
        protected Translator() {
            super(Opcodes.ASM4, null);
        }

        /**
         * <p>
         * Lazy set up.
         * </p>
         */
        final void set(MethodVisitor visitor, String className, String methodName, Type methodDescriptor) {
            mv = visitor;
            this.className = className;
            this.methodName = methodName;
            this.methodType = methodDescriptor;
        }

        protected T insn(int opcode) {
            mv.visitInsn(opcode);

            return (T) this;
        }

        /**
         * <p>
         * Write code for invokevirtual.
         * </p>
         * 
         * @param sam A single abstract method class.
         */
        protected final void invokeVirtual(Class invoker, Class sam) {
            SAMInfo info = SAMInfo.get(sam);

            mv.visitMethodInsn(INVOKEVIRTUAL, Type.getType(invoker).getInternalName(), info.name, info.descriptor);
        }

        /**
         * <p>
         * Write code for the specified instruction.
         * </p>
         * 
         * @param instruction A code to write.
         */
        protected final void write(Instruction... instructions) {
            for (Instruction instruction : instructions) {
                instruction.toBytecode(mv);

                for (Instruction next : instruction.list) {
                    next.toBytecode(mv);
                }
            }
        }

        /**
         * <p>
         * Search wrapper type of the specified primitive type.
         * </p>
         * 
         * @param type
         * @return
         */
        private static Type getWrapperType(Type type) {
            switch (type.getSort()) {
            case Type.BOOLEAN:
                return Type.getType(Boolean.class);

            case Type.INT:
                return Type.getType(Integer.class);

            case Type.LONG:
                return Type.getType(Long.class);

            case Type.FLOAT:
                return Type.getType(Float.class);

            case Type.DOUBLE:
                return Type.getType(Double.class);

            case Type.CHAR:
                return Type.getType(Character.class);

            case Type.BYTE:
                return Type.getType(Byte.class);

            case Type.SHORT:
                return Type.getType(Short.class);

            default:
                return type;
            }
        }

        /**
         * @version 2012/01/15 11:56:30
         */
        public static abstract class Instruction {

            /** The instruction sequence. */
            private List<Instruction> list = new ArrayList(2);

            /**
             * <p>
             * Helper method to write bytecode which wrap the primitive value.
             * </p>
             * 
             * @param type
             * @return
             */
            public final Instruction wrap(Type type) {
                Type wrapper = getWrapperType(type);

                if (wrapper != type) {
                    list.add(new Method(INVOKESTATIC, wrapper.getInternalName(), "valueOf", Type.getMethodDescriptor(wrapper, type)));
                }
                return this;
            }

            /**
             * <p>
             * Produce bytecode for this instruction.
             * </p>
             * 
             * @param visitor
             */
            abstract void toBytecode(MethodVisitor visitor);

        }

        /**
         * @version 2012/01/15 11:58:06
         */
        public static class Insn extends Instruction {

            /** The operation code. */
            public int opecode;

            /**
             * @param opecode
             */
            public Insn(int opecode) {
                this.opecode = opecode;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            void toBytecode(MethodVisitor visitor) {
                visitor.visitInsn(opecode);
            }
        }

        /**
         * @version 2012/01/15 23:15:47
         */
        public static class Method extends Instruction {

            /** The operation code. */
            public int opcode;

            /** The method invoker internal name. */
            public String owner;

            /** The method name. */
            public String name;

            /** The method descriptor. */
            public String descriptor;

            /**
             * @param opcode
             * @param owner
             * @param name
             * @param descriptor
             */
            public Method(int opcode, String owner, String name, String descriptor) {
                this.opcode = opcode;
                this.owner = owner;
                this.name = name;
                this.descriptor = descriptor;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            void toBytecode(MethodVisitor visitor) {
                visitor.visitMethodInsn(opcode, owner, name, descriptor);
            }
        }
    }

    /**
     * @version 2012/01/14 2:02:54
     */
    private static class SAMInfo {

        /** The cache for recoder type. */
        private static final Map<Class, SAMInfo> types = new HashMap();

        /** The method name. */
        private final String name;

        /** The method descriptor. */
        private final String descriptor;

        /** The method owner. */
        private final String owner;

        /** The method type. */
        private final Type type;

        /**
         * 
         */
        private SAMInfo(Class sam) {
            Method method = sam.getMethods()[0];
            this.name = method.getName();
            this.type = Type.getType(method);
            this.descriptor = type.getDescriptor();
            this.owner = Type.getType(method.getDeclaringClass()).getInternalName();
        }

        /**
         * <p>
         * Search recoder method.
         * </p>
         * 
         * @param sam
         * @return
         */
        private static SAMInfo get(Class sam) {
            SAMInfo info = types.get(sam);

            if (info == null) {
                info = new SAMInfo(sam);

                types.put(sam, info);
            }
            return info;
        }
    }
}
