/*
 * Copyright (C) 2012 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package hub.bytecode;

import static org.objectweb.asm.Opcodes.*;

import java.util.LinkedList;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

/**
 * @version 2012/01/18 8:42:38
 */
public abstract class Bytecode<T extends Bytecode<T>> {

    /** The sub sequence. */
    private LinkedList<Bytecode> sequence = new LinkedList();

    /**
     * <p>
     * Helper method to write wrap primitive type.
     * </p>
     * 
     * @param type
     * @return
     */
    public final T wrap(Type type) {
        Type wrapper = getWrapperType(type);

        if (wrapper != type) {
            sequence.add(new MethodCall(INVOKESTATIC, wrapper.getInternalName(), "valueOf", Type.getMethodDescriptor(wrapper, type)));
        }

        // API definition
        return (T) this;
    }

    /**
     * <p>
     * Write bytecode actually.
     * </p>
     * 
     * @param visitor
     */
    public final void toBytecode(MethodVisitor visitor) {
        write(visitor);

        for (Bytecode code : sequence) {
            code.toBytecode(visitor);
        }
    }

    /**
     * <p>
     * Write bytecode.
     * </p>
     * 
     * @param visitor
     */
    abstract void write(MethodVisitor visitor);

    /**
     * <p>
     * Search wrapper type of the specified primitive type.
     * </p>
     * 
     * @param type
     * @return
     */
    public static Type getWrapperType(Type type) {
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
}
