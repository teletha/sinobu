/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss;

import static net.bytebuddy.jar.asm.Opcodes.*;

import java.util.ArrayList;
import java.util.Arrays;

import net.bytebuddy.jar.asm.ClassWriter;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.jar.asm.Type;

public class WisedBytecodeGenerator {

    public static void main(String[] args) {
        Class clazz = ArrayList.class;
        String superClassName = Type.getInternalName(clazz);
        String uniquedClassName = superClassName.replace('/', '_');
        String interfaceName = Type.getInternalName(WiseList.class);

        ClassWriter cw = new ClassWriter(0);
        cw.visit(V14, ACC_PUBLIC, "kiss/" + uniquedClassName, null, superClassName, new String[] {interfaceName});
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitMaxs(2, 1);
        mv.visitVarInsn(ALOAD, 0); // push `this` to the operand stack
        mv.visitMethodInsn(INVOKESPECIAL, superClassName, "<init>", "()V", false); // call
        mv.visitInsn(RETURN);

        String bytes = literalize(cw.toByteArray()).replaceAll(literalize(uniquedClassName), "UniquedClassName")
                .replaceAll(literalize(interfaceName), "InterfaceName")
                .replaceAll(literalize(superClassName), "SuperClassName");

        System.out.println("[" + bytes + "]");
        System.out.println(String.valueOf((char) 0));
        System.out.println((byte) "".charAt(0));
    }

    private static String literalize(String value) {
        return literalize(value.getBytes());
    }

    private static String literalize(byte[] bytes) {
        String value = Arrays.toString(bytes).replaceAll(" ", "");
        return value.substring(1, value.length() - 1);
    }
}
