/*
 * Copyright (C) 2014 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.scratchpad;

import static jdk.internal.org.objectweb.asm.Opcodes.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.Label;
import jdk.internal.org.objectweb.asm.MethodVisitor;

/**
 * DOCUMENT.
 * 
 * @author <a href="mailto:Teletha.NPC@gmail.com">Teletha Testarossa</a>
 * @version $ Id: ConstructorInterception.java,v 1.0 2007/05/10 13:00:39 Teletha Exp $
 */
public class ConstructorInterception {

    public ConstructorInterception() {
        super();

        System.out.println("sample.modifier");
    }

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        cw.visit(V1_5, ACC_SUPER, "sample/modifier/Inner$Test", null, "sample/modifier/Inner$PrivateStatic", null);

        cw.visitSource("Inner.java", null);

        cw.visitInnerClass("sample/modifier/Inner$PrivateStatic", "sample/modifier/Inner", "PrivateStatic", ACC_PRIVATE + ACC_STATIC);

        cw.visitInnerClass("sample/modifier/Inner$Test", "sample/modifier/Inner", "Test", ACC_PRIVATE + ACC_STATIC);

        {
            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
            mv.visitCode();
            Label l0 = new Label();
            mv.visitLabel(l0);
            mv.visitLineNumber(56, l0);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL, "sample/modifier/Inner$PrivateStatic", "<init>", "()V", false);
            Label l1 = new Label();
            mv.visitLabel(l1);
            mv.visitLineNumber(57, l1);
            mv.visitInsn(RETURN);
            Label l2 = new Label();
            mv.visitLabel(l2);
            mv.visitLocalVariable("this", "Lsample/modifier/Inner$Test;", null, l0, l2, 0);
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        }
        cw.visitEnd();

        byte[] bytes = cw.toByteArray();

        Class sss = Class.forName("sample.modifier.Inner$PrivateStatic");
        Constructor aaa = sss.getDeclaredConstructor(new Class[0]);
        System.out.println(aaa + "     " + ((aaa.getModifiers() & Modifier.PRIVATE) == 0));
        aaa.setAccessible(true);

        Loader loader = new Loader();
        loader.define("sample.modifier.Inner$Test", bytes);

        Class clazz = loader.loadClass("sample.modifier.Inner$Test");
        System.out.println(clazz);

        Constructor constructor = clazz.getDeclaredConstructor(new Class[0]);
        constructor.setAccessible(true);
        System.out.println(constructor);

        System.out.println(constructor.newInstance(new Object[0]));
    }

    private static class Loader extends ClassLoader {

        public Loader() {
            super(Thread.currentThread().getContextClassLoader());
        }

        private void define(String name, byte[] bytes) {
            defineClass(name, bytes, 0, bytes.length);
        }
    }

}
