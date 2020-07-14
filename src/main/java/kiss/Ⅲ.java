/*
 * Copyright (C) 2020 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss;

import java.util.List;

/**
 * {@link List#of(Object, Object)}
 */
public class Ⅲ<Param1, Param2, Param3> extends Ⅱ<Param1, Param2> {

    /** The third parameter. */
    public final Param3 ⅲ;

    /**
     * @param param1
     * @param param2
     * @param param3
     */
    Ⅲ(Param1 param1, Param2 param2, Param3 param3) {
        super(param1, param2);
        this.ⅲ = param3;
    }

    /**
     * Method access to {@link #ⅲ}.
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
     * context.with((name, age, gender) -> {
     *      // Named parameter (name, age and gender) are comprehensible.
     *      return name + "(" + age + " : " + gender + ")";
     * });
     * </pre>
     * 
     * @param params A list of named parameters.
     * @return A calculated value.
     */
    public <Result> Result map(WiseTriFunction<Param1, Param2, Param3, Result> params) {
        return params.apply(ⅰ, ⅱ, ⅲ);
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
     * context.with((name, age, gender) -> {
     *      // Named parameter (name, age and gender) are comprehensible.
     * });
     * </pre>
     * 
     * @param params A list of named parameters.
     */
    public void to(WiseTriConsumer<Param1, Param2, Param3> params) {
        params.accept(ⅰ, ⅱ, ⅲ);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object[] values() {
        return new Object[] {ⅰ, ⅱ, ⅲ};
    }
}