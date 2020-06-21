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

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.jupiter.api.Test;

import net.bytebuddy.jar.asm.ClassWriter;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.jar.asm.Opcodes;
import net.bytebuddy.jar.asm.Type;

public class WiseListTest {

    WiseList<Integer> list1 = list(BrrayList.class);

    WiseList<Integer> list2 = list(ArrayList.class);

    WiseList<Integer> list = I.list(ArrayList.class);

    public static class BrrayList extends ArrayList {

    }

    public static class CrrayList extends ArrayList {

    }

    @Test
    void testName() {
        list.add(1);

        assert list.peekFirst().orElse(2) == 1;
    }

    private static final Map<Class, Class> wised = new ConcurrentHashMap();

    /**
     * Create {@link WiseList} by the specified list type.
     * 
     * @param <E>
     * @param list
     * @return
     */
    public static <E> WiseList<E> list(Class<? extends List> list) {
        return I.make((Class<WiseList>) wised.computeIfAbsent(list, key -> {
            if (Modifier.isAbstract(list.getModifiers()) || list.isEnum()) {
                throw new IllegalArgumentException("Class must be concrete public normal class.");
            }

            try {
                ClassWriter cw = new ClassWriter(0);
                cw.visit(Opcodes.V14, Opcodes.ACC_PUBLIC, "kiss/" + Type.getInternalName(list).replace('/', '_'), null, Type
                        .getInternalName(list), new String[] {Type.getInternalName(WiseList.class)});
                MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
                mv.visitMaxs(2, 1);
                mv.visitVarInsn(Opcodes.ALOAD, 0); // push `this` to the operand stack
                mv.visitMethodInsn(Opcodes.INVOKESPECIAL, Type.getInternalName(list), "<init>", "()V", false); // call
                mv.visitInsn(Opcodes.RETURN);

                System.out.println(Arrays.toString(cw.toByteArray()));

                return MethodHandles.lookup().defineClass(cw.toByteArray());
            } catch (IllegalAccessException e) {
                throw I.quiet(e);
            }
        }));
    }
}
