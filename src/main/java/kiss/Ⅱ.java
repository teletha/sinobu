/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss;

import java.util.Arrays;

public class Ⅱ<Param1, Param2> {

    /** The first parameter. */
    public final Param1 ⅰ;

    /** The second parameter. */
    public final Param2 ⅱ;

    /**
     * @param param1
     * @param param2
     */
    Ⅱ(Param1 param1, Param2 param2) {
        this.ⅰ = param1;
        this.ⅱ = param2;
    }

    /**
     * Method access to {@link #ⅰ}.
     */
    public Param1 ⅰ() {
        return ⅰ;
    }

    /**
     * Method access to {@link #ⅱ}.
     */
    public Param2 ⅱ() {
        return ⅱ;
    }

    /**
     * Create new tuple which set the third parameter.
     * 
     * @param param New third parameter.
     * @return A created new tuple.
     */
    public <New> Ⅲ<Param1, Param2, New> ⅲ(New param) {
        return I.pair(ⅰ, ⅱ, param);
    }

    /**
     * <p>
     * Create new scope with the human-readable named parameters and return a calculated value.
     * </p>
     * <p>
     * Parameter name (ⅰ and ⅱ) are confusing. This method names parameters to the context-aware
     * names.
     * </p>
     * <pre>
     *  // Original parameter names (ⅰ and ⅱ) are confusing.
     * context.with((name, age)  -> {
     *      // Named parameter (name and age) are comprehensible.
     *      return name + "(" + age + ")";
     * });
     * </pre>
     * 
     * @param params A list of named parameters.
     * @return A calculated value.
     */
    public <Result> Result map(WiseBiFunction<Param1, Param2, Result> params) {
        return params.apply(ⅰ, ⅱ);
    }

    /**
     * <p>
     * Create new scope with the human-readable named parameters.
     * </p>
     * <p>
     * Parameter name (ⅰ and ⅱ) are confusing. This method names parameters to the context-aware
     * names.
     * </p>
     * <pre>
     *  // Original parameter names (ⅰ and ⅱ) are confusing.
     * context.with((name, age)  -> {
     *      // Named parameter (name and age) are comprehensible.
     * });
     * </pre>
     * 
     * @param params A list of named parameters.
     */
    public void to(WiseBiConsumer<Param1, Param2> params) {
        params.accept(ⅰ, ⅱ);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int hashCode() {
        return Arrays.hashCode(values());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean equals(Object obj) {
        return obj instanceof Ⅱ ? Arrays.equals(values(), ((Ⅱ) obj).values()) : false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String toString() {
        return Arrays.toString(values());
    }

    /**
     * Collect all values.
     * 
     * @return
     */
    protected Object[] values() {
        return new Object[] {ⅰ, ⅱ};
    }
}
