/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss;

import java.util.function.Supplier;

/**
 * 
 */
public interface Narrow<ApplyTail, Tail, ApplyHead, Head, Self> extends Flexible<Self> {
    /**
     * <p>
     * Apply tail parameter partialy.
     * </p>
     * 
     * @param param A fixed parameter.
     * @return A partial applied function.
     */
    default ApplyTail dump(Tail param) {
        return dumpBy(Variable.of(param));
    }

    /**
     * <p>
     * Apply tail parameter partialy.
     * </p>
     * 
     * @param param A fixed parameter.
     * @return A partial applied function.
     */
    default ApplyTail dumpBy(Supplier<Tail> param) {
        return I.make(this, Narrow.class, args -> {
            return invoke(I.array(args, param == null ? null : param.get()));
        });
    }

    /**
     * <p>
     * Apply head parameter partialy.
     * </p>
     * 
     * @param param A fixed parameter.
     * @return A partial applied function.
     */
    default ApplyHead hide(Head param) {
        return hideBy(Variable.of(param));
    }

    /**
     * <p>
     * Apply head parameter partialy.
     * </p>
     * 
     * @param param A fixed parameter.
     * @return A partial applied function.
     */
    default ApplyHead hideBy(Supplier<Head> param) {
        return I.make(this, Narrow.class, args -> {
            return invoke(I.array(new Object[] {param == null ? null : param.get()}, args));
        });
    }
}
