/*
 * Copyright (C) 2016 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @version 2016/03/27 12:13:49
 */
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
     * Create new tuple which add the third parameter.
     * 
     * @param param New third parameter.
     * @return A created new tuple.
     */
    public <AdditionalParam> Ⅲ<Param1, Param2, AdditionalParam> append(AdditionalParam param) {
        return I.pair(ⅰ, ⅱ, param);
    }

    /**
     * @param value
     * @return
     */
    public <AdditionalParam> Ⅲ<AdditionalParam, Param1, Param2> prepend(AdditionalParam value) {
        return I.pair(value, ⅰ, ⅱ);
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
     * context.with(name -> age  -> {
     *      // Named parameter (name and age) are comprehensible.
     *      return name + "(" + age + ")";
     * });
     * </pre>
     * 
     * @param params A list of named parameters.
     * @return A calculated value.
     */
    public <Result> Result map(Function<Param1, Function<Param2, Result>> params) {
        return params.apply(ⅰ).apply(ⅱ);
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
     * context.with(name -> age  -> {
     *      // Named parameter (name and age) are comprehensible.
     * });
     * </pre>
     * 
     * @param params A list of named parameters.
     */
    public void with(Function<Param1, Consumer<Param2>> params) {
        if (params != null) {
            Consumer<Param2> consumer = params.apply(ⅰ);

            if (consumer != null) {
                consumer.accept(ⅱ);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(ⅰ, ⅱ);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Ⅱ) {
            Ⅱ other = (Ⅱ) obj;

            return Objects.equals(ⅰ, other.ⅰ) && Objects.equals(ⅱ, other.ⅱ);
        }
        return false;
    }
}
