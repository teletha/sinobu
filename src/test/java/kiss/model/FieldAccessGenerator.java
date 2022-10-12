/*
 * Copyright (C) 2022 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.model;

import static net.bytebuddy.jar.asm.Opcodes.*;

import java.util.Arrays;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.Function;

import kiss.I;
import kiss.json.JSONMappingBenchmark.Person;
import net.bytebuddy.jar.asm.ClassWriter;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.jar.asm.Type;

public class FieldAccessGenerator {

    public static void main(String[] args) {

        Random random = new Random();
        Class modelClass = Person.class;
        String propertyName = "name";
        Class propertyClass = String.class;

        String superClassName = Type.getInternalName(Object.class);
        String uniquedClassName = "kiss/A" + random.nextInt();
        String modelClassName = Type.getInternalName(modelClass);
        String getterInterface = Type.getInternalName(Function.class);
        String setterInterface = Type.getInternalName(BiConsumer.class);
        String propertyClassName = Type.getInternalName(I.wrap(propertyClass));
        String propertyClassDescriptor = Type.getDescriptor(propertyClass);

        ClassWriter cw = new ClassWriter(0);
        cw.visit(V17, ACC_PUBLIC | ACC_SUPER, uniquedClassName, null, "java/lang/Object", new String[] {"java/util/function/Function",
                "java/util/function/BiConsumer"});

        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        mv.visitInsn(RETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();

        mv = cw.visitMethod(ACC_PUBLIC, "accept", "(Ljava/lang/Object;Ljava/lang/Object;)V", null, null);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitTypeInsn(CHECKCAST, modelClassName);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitTypeInsn(CHECKCAST, propertyClassName);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false);
        mv.visitFieldInsn(PUTFIELD, modelClassName, propertyName, propertyClassDescriptor);
        mv.visitInsn(RETURN);
        mv.visitMaxs(2, 3);
        mv.visitEnd();

        mv = cw.visitMethod(ACC_PUBLIC, "apply", "(Ljava/lang/Object;)Ljava/lang/Object;", null, null);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitTypeInsn(CHECKCAST, modelClassName);
        mv.visitFieldInsn(GETFIELD, modelClassName, propertyName, propertyClassDescriptor);
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
        mv.visitInsn(ARETURN);
        mv.visitMaxs(1, 2);
        mv.visitEnd();

        cw.visitEnd();

        String bytes = literalize(cw.toByteArray()).replaceAll(literalize(modelClassName), "M")
                .replaceAll(literalize(propertyClassName), "P")
                .replaceAll(literalize(propertyClassDescriptor), "D")
                .replaceAll(literalize(propertyName), "N")
                .replaceAll(",8,105,110,116,86,97,108,117,101,1,0,3,40,41,D,12,0,19,0,20,10,0,18,0,21,1", "U")
                .replaceAll(",7,118,97,108,117,101,79,102,1,0,22,40,D,41,76,P,59,12,0,29,0,30,10,0,18,0,31,1,0", "W");

        System.out.println('"' + bytes + '"');
    }

    private static String literalize(String value) {
        return literalize(value.getBytes());
    }

    private static String literalize(byte[] bytes) {
        String value = Arrays.toString(bytes).replaceAll(" ", "");
        return value.substring(1, value.length() - 1);
    }
}
