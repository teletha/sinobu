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

import java.util.function.Supplier;

/**
 * Provide the partial application functionality.
 */
public interface Narrow<FirstBinded, First, LastBinded, Last> extends Wise {

    /**
     * Apply first parameter partialy.
     * 
     * @param param A fixed parameter.
     * @return A partial applied function.
     */
    default FirstBinded bind(First param) {
        return bindLazily(Variable.of(param));
    }

    /**
     * Apply first parameter partialy. The null {@link Supplier} will be treated as null value.
     * 
     * @param param A fixed parameter.
     * @return A partial applied function.
     */
    default FirstBinded bindLazily(Supplier<First> param) {
        return I.make(this, Narrow.class, args -> {
            return invoke(I.array(new Object[] {param == null ? null : param.get()}, args));
        });
    }

    /**
     * Apply last parameter partialy.
     * 
     * @param param A fixed parameter.
     * @return A partial applied function.
     */
    default LastBinded bindLast(Last param) {
        return bindLastLazily(Variable.of(param));
    }

    /**
     * Apply last parameter partialy. The null {@link Supplier} will be treated as null value.
     * 
     * @param param A fixed parameter.
     * @return A partial applied function.
     */
    default LastBinded bindLastLazily(Supplier<Last> param) {
        return I.make(this, Narrow.class, args -> {
            return invoke(I.array(args, param == null ? null : param.get()));
        });
    }
}