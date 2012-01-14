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

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.ProtectionDomain;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import kiss.I;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

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
    public Agent(Translator translator) {
        this(new TranslatorTransformer(translator));
    }

    /**
     * <p>
     * Create dynamic Agent.
     * </p>
     * 
     * @param translator
     */
    public Agent(MethodTranslator translator) {
        this(new MethodTranslatorTransformer(translator));
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
     * @version 2012/01/14 13:08:33
     */
    public static abstract class MethodTranslator extends MethodVisitor {

        /**
         * 
         */
        protected MethodTranslator() {
            super(Opcodes.ASM4, null);
        }

        void set(MethodVisitor visitor) {
            mv = visitor;
        }

        /**
         * <p>
         * Chech whether the specified class should translate or not.
         * </p>
         * 
         * @param internalName A internal class name.
         * @return A result.
         */
        public abstract boolean canTranslate(String internalName);
    }

    /**
     * @version 2012/01/10 19:59:03
     */
    public static interface Translator {

        /**
         * <p>
         * Chech whether the specified class should translate or not.
         * </p>
         * 
         * @param internalName A internal class name.
         * @return A result.
         */
        boolean canTranslate(String internalName);

        /**
         * <p>
         * Translate class file.
         * </p>
         * 
         * @param node An abstract syntax tree.
         */
        void translate(ClassNode ast);
    }

    /**
     * @version 2012/01/14 13:09:23
     */
    private static final class MethodTranslatorTransformer implements ClassFileTransformer {

        /** The delegator. */
        private final MethodTranslator translator;

        /**
         * @param translator
         */
        private MethodTranslatorTransformer(MethodTranslator translator) {
            this.translator = translator;
        }

        /**
         * @see java.lang.instrument.ClassFileTransformer#transform(java.lang.ClassLoader,
         *      java.lang.String, java.lang.Class, java.security.ProtectionDomain, byte[])
         */
        @Override
        public byte[] transform(ClassLoader loader, String name, Class<?> clazz, ProtectionDomain domain, byte[] bytes) {
            if (!translator.canTranslate(name)) {
                return bytes;
            }

            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
            ClassTranslator visitor = new ClassTranslator(writer);
            ClassReader reader = new ClassReader(bytes);
            reader.accept(visitor, ClassReader.EXPAND_FRAMES);

            return writer.toByteArray();
        }

        /**
         * @version 2012/01/14 13:16:21
         */
        private class ClassTranslator extends ClassVisitor {

            /**
             * @param arg0
             */
            private ClassTranslator(ClassWriter writer) {
                super(Opcodes.ASM4, writer);
            }

            /**
             * @see org.objectweb.asm.ClassVisitor#visitMethod(int, java.lang.String,
             *      java.lang.String, java.lang.String, java.lang.String[])
             */
            @Override
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                translator.set(super.visitMethod(access, name, desc, signature, exceptions));

                return translator;
            }
        }
    }

    /**
     * @version 2012/01/10 11:51:03
     */
    private static final class TranslatorTransformer implements ClassFileTransformer {

        /** The delegator. */
        private final Translator translator;

        /**
         * @param translator
         */
        private TranslatorTransformer(Translator translator) {
            this.translator = translator;
        }

        /**
         * @see java.lang.instrument.ClassFileTransformer#transform(java.lang.ClassLoader,
         *      java.lang.String, java.lang.Class, java.security.ProtectionDomain, byte[])
         */
        @Override
        public byte[] transform(ClassLoader loader, String name, Class<?> clazz, ProtectionDomain domain, byte[] bytes) {
            if (!translator.canTranslate(name)) {
                return bytes;
            }

            ClassNode ast = new ClassNode();

            // build abstract syntax tree
            new ClassReader(bytes).accept(ast, ClassReader.EXPAND_FRAMES);

            // translate
            translator.translate(ast);

            // write translated byte code
            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
            try {
                ast.accept(writer);
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return writer.toByteArray();
        }
    }
}
