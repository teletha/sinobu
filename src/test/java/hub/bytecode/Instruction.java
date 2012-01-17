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

/**
 * @version 2012/01/18 8:37:28
 */
public class Instruction {

    /** The operation code. */
    public int opcode;

    /**
     * @param opcode
     */
    public Instruction(int opcode) {
        this.opcode = opcode;
    }
}
