/*
 * Copyright (C) 2012 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package antibug.xml;

import java.util.BitSet;

/**
 * @version 2012/02/16 12:03:13
 */
public class Except {

    /** The flag. */
    private static final int Prefix = 0;

    /** The flag. */
    private static final int Comment = 1;

    /** The flag. */
    private static final int WhiteSpace = 2;

    /** The bit flag. */
    BitSet bits = new BitSet();

    /**
     * Hide constructor.
     */
    Except() {
    }

    /**
     * <p>
     * Ignore prefix name.
     * </p>
     * 
     * @return A current state.
     */
    public static Except Prefix() {
        return new Except().andPrefix();
    }

    /**
     * <p>
     * Ignore prefix name.
     * </p>
     * 
     * @return A current state.
     */
    public Except andPrefix() {
        bits.set(Prefix);
        return this;
    }

    /**
     * <p>
     * Can we ignore prefix name?
     * </p>
     * 
     * @return A result.
     */
    boolean ignorePrefix() {
        return bits.get(Prefix);
    }

    /**
     * <p>
     * Ignore comment node.
     * </p>
     * 
     * @return A current state.
     */
    public static Except Comment() {
        return new Except().andComment();
    }

    /**
     * <p>
     * Ignore comment node.
     * </p>
     * 
     * @return A current state.
     */
    public Except andComment() {
        bits.set(Comment);
        return this;
    }

    /**
     * <p>
     * Can we ignore comment node?
     * </p>
     * 
     * @return A result.
     */
    boolean ignoreComment() {
        return bits.get(Comment);
    }

    /**
     * <p>
     * Ignore whitespace node.
     * </p>
     * 
     * @return A current state.
     */
    public static Except WhiteSpace() {
        return new Except().andWhiteSpace();
    }

    /**
     * <p>
     * Ignore whitespace node.
     * </p>
     * 
     * @return A current state.
     */
    public Except andWhiteSpace() {
        bits.set(WhiteSpace);
        return this;
    }

    /**
     * <p>
     * Can we ignore whitespace node?
     * </p>
     * 
     * @return A result.
     */
    boolean ignoreWhiteSpace() {
        return bits.get(WhiteSpace);
    }
}
