/*
 * Copyright (C) 2024 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Provide the partial application functionality.
 */
public interface Narrow<FirstBound, First, LastBound, Last> extends Wise {

    /**
     * Apply first parameter partially.
     * 
     * @param param A fixed parameter. (accept null)
     * @return A partial applied function.
     * @see #bindLazily(Supplier)
     * @see #bindLast(Object)
     * @see #bindLastLazily(Supplier)
     */
    default FirstBound bind(First param) {
        return bindLazily(Variable.of(param));
    }

    /**
     * Apply first parameter partially. Unlike {@link #bind(Object)}, null parameter will throw
     * {@link NullPointerException}.
     * 
     * @param param A fixed parameter. (reject null)
     * @return A partial applied function.
     * @throws NullPointerException The supplier is null.
     * @see #bind(Object)
     * @see #bindLast(Object)
     * @see #bindLastLazily(Supplier)
     */
    default FirstBound bindLazily(Supplier<First> param) {
        Objects.requireNonNull(param);

        return I.make(this, Narrow.class, args -> invoke(I.array(new Object[] {param.get()}, args)));
    }

    /**
     * Apply last parameter partially.
     * 
     * @param param A fixed parameter. (accept null)
     * @return A partial applied function.
     * @see #bind(Object)
     * @see #bindLazily(Supplier)
     * @see #bindLastLazily(Supplier)
     */
    default LastBound bindLast(Last param) {
        return bindLastLazily(Variable.of(param));
    }

    /**
     * Apply last parameter partially. Unlike {@link #bindLast(Object)}, null parameter will throw
     * {@link NullPointerException}.
     * 
     * @param param A fixed parameter. (reject null)
     * @return A partial applied function.
     * @throws NullPointerException The supplier is null.
     * @see #bind(Object)
     * @see #bindLazily(Supplier)
     * @see #bindLast(Object)
     */
    default LastBound bindLastLazily(Supplier<Last> param) {
        Objects.requireNonNull(param);

        return I.make(this, Narrow.class, args -> invoke(I.array(args, param.get())));
    }
}