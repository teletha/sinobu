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

/**
 * @version 2012/01/18 10:12:18
 */
public class LocalVariable extends Bytecode<LocalVariable> {

    /** The operation code. */
    public int opcode;

    /** The local variable index. */
    public int index;

    /**
     * @param opcode
     * @param index
     */
    public LocalVariable(int opcode, int index) {
        this.opcode = opcode;
        this.index = index;
    }

    /**
     * @see hub.bytecode.Bytecode#write(org.objectweb.asm.MethodVisitor)
     */
    @Override
    void write(MethodVisitor visitor) {
        visitor.visitVarInsn(opcode, index);
    }
}
