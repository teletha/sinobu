/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @version 2016/03/26 21:14:51
 */
public class Ⅲ<Param1, Param2, Param3> {

    /** The first parameter. */
    public final Param1 ⅰ;

    /** The second parameter. */
    public final Param2 ⅱ;

    /** The third parameter. */
    public final Param3 ⅲ;

    /**
     * @param param1
     * @param param2
     * @param param3
     */
    Ⅲ(Param1 param1, Param2 param2, Param3 param3) {
        this.ⅰ = param1;
        this.ⅱ = param2;
        this.ⅲ = param3;
    }

    /**
     * <p>
     * Method access to {@link #ⅰ}.
     * </p>
     */
    public Param1 ⅰ() {
        return ⅰ;
    }

    /**
     * <p>
     * Method access to {@link #ⅱ}.
     * </p>
     */
    public Param2 ⅱ() {
        return ⅱ;
    }

    /**
     * <p>
     * Method access to {@link #ⅲ}.
     * </p>
     */
    public Param3 ⅲ() {
        return ⅲ;
    }

    /**
     * <p>
     * Create new scope with the human-readable named parameters and return a calculated value.
     * </p>
     * <p>
     * Parameter name (ⅰ, ⅱ and ⅲ) are confusing. This method names parameters to the context-aware
     * names.
     * </p>
     * <pre>
     *  // Original parameter names (ⅰ, ⅱ and ⅲ) are confusing.
     * context.with(name -> age -> gender -> {
     *      // Named parameter (name, age and gender) are comprehensible.
     *      return name + "(" + age + " : " + gender + ")";
     * });
     * </pre>
     * 
     * @param params A list of named parameters.
     * @return A calculated value.
     */
    public <Result> Result map(Function<Param1, Function<Param2, Function<Param3, Result>>> params) {
        return params.apply(ⅰ).apply(ⅱ).apply(ⅲ);
    }

    /**
     * <p>
     * Create new scope with the human-readable named parameters.
     * </p>
     * <p>
     * Parameter name (ⅰ, ⅱ and ⅲ) are confusing. This method names parameters to the context-aware
     * names.
     * </p>
     * <pre>
     *  // Original parameter names (ⅰ, ⅱ and ⅲ) are confusing.
     * context.with(name -> age  -> gender -> {
     *      // Named parameter (name, age and gender) are comprehensible.
     * });
     * </pre>
     * 
     * @param params A list of named parameters.
     */
    public void with(Function<Param1, Function<Param2, Consumer<Param3>>> params) {
        if (params != null) {
            Function<Param2, Consumer<Param3>> second = params.apply(ⅰ);

            if (second != null) {
                Consumer<Param3> consumer = second.apply(ⅱ);

                if (consumer != null) {
                    consumer.accept(ⅲ);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(ⅰ, ⅱ, ⅲ);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Ⅲ) {
            Ⅲ other = (Ⅲ) obj;

            return Objects.equals(ⅰ, other.ⅰ) && Objects.equals(ⅱ, other.ⅱ) && Objects.equals(ⅲ, other.ⅲ);
        }
        return false;
    }
}
