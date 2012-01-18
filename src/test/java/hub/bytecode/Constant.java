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

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

/**
 * @version 2012/01/18 9:56:27
 */
public class Constant extends Bytecode<Constant> {

    /** The constant value. */
    private Object value;

    /**
     * @param value
     */
    public Constant(Object value) {
        this.value = value;
    }

    /**
     * @see hub.bytecode.Bytecode#write(org.objectweb.asm.MethodVisitor)
     */
    @Override
    void write(MethodVisitor visitor) {
        visitor.visitLdcInsn(value);

        if (value instanceof Integer) {
            wrap(Type.INT_TYPE);
        } else if (value instanceof Long) {
            wrap(Type.LONG_TYPE);
        } else if (value instanceof Float) {
            wrap(Type.FLOAT_TYPE);
        } else if (value instanceof Double) {
            wrap(Type.DOUBLE_TYPE);
        }
    }
}
