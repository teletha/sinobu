/*
 * Copyright (C) 2022 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.experimental;

import static net.bytebuddy.jar.asm.Opcodes.*;

import java.util.ArrayList;
import java.util.Arrays;

import net.bytebuddy.jar.asm.ClassWriter;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.jar.asm.Type;

public class WisedBytecodeGenerator {

    public static void main(String... xxx) {
        Class clazz = ArrayList.class;
        String superClassName = Type.getInternalName(clazz);
        String uniquedClassName = "kiss/" + superClassName.replace('/', '_');
        String interfaceName = Type.getInternalName(WiseList.class);

        ClassWriter cw = new ClassWriter(0);
        cw.visit(V14, ACC_PUBLIC, uniquedClassName, null, superClassName, new String[] {interfaceName});
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitMaxs(2, 1);
        mv.visitVarInsn(ALOAD, 0); // push `this` to the operand stack
        mv.visitMethodInsn(INVOKESPECIAL, superClassName, "<init>", "()V", false); // call
        mv.visitInsn(RETURN);

        String bytes = literalize(cw.toByteArray()).replaceAll(literalize(uniquedClassName), "U")
                .replaceAll(literalize(interfaceName), "I")
                .replaceAll(literalize(superClassName), "S");

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