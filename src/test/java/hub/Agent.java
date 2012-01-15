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
import java.util.HashSet;
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
     * @version 2012/01/14 13:08:33
     */
    public static abstract class Translator extends MethodVisitor {

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
            System.out.println("refine");
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
}
