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
public interface Narrow<Assigned, Assigner, Preassigned, Preassigner, Self> extends Flexible<Self> {
    /**
     * <p>
     * Apply tail parameter partialy.
     * </p>
     * 
     * @param param A fixed parameter.
     * @return A partial applied function.
     */
    default Assigned hideEnd(Assigner param) {
        return hideEndLazy(Variable.of(param));
    }

    /**
     * <p>
     * Apply tail parameter partialy.
     * </p>
     * 
     * @param param A fixed parameter.
     * @return A partial applied function.
     */
    default Assigned hideEndLazy(Supplier<Assigner> param) {
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
    default Preassigned hide(Preassigner param) {
        return hideLazy(Variable.of(param));
    }

    /**
     * <p>
     * Apply head parameter partialy.
     * </p>
     * 
     * @param param A fixed parameter.
     * @return A partial applied function.
     */
    default Preassigned hideLazy(Supplier<Preassigner> param) {
        return I.make(this, Narrow.class, args -> {
            return invoke(I.array(new Object[] {param == null ? null : param.get()}, args));
        });
    }

    default Self fixEnd(Assigner value) {
        return fixEndLazy(Variable.of(value));
    }

    default Self fixEndLazy(Supplier<Assigner> value) {
        return I.make(this, Flexible.class, args -> {
            args[args.length - 1] = value.get();
            return invoke(args);
        });
    }
}
