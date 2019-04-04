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

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Provide the partial application functionality.
 */
public interface Narrow<FirstBinded, First, LastBinded, Last, Spined, Self> extends Flexible {

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

    // /**
    // * Fix first parameter partialy. The actual argument from the caller will be ignored.
    // *
    // * @param param A fixed parameter.
    // * @return A partial fixed function.
    // */
    // default Self fix(First param) {
    // return fixLazily(Variable.of(param));
    // }
    //
    // /**
    // * Fix first parameter partialy. The actual argument from the caller will be ignored. The null
    // * {@link Supplier} will be treated as null value.
    // *
    // * @param param A fixed parameter.
    // * @return A partial fixed function.
    // */
    // default Self fixLazily(Supplier<First> param) {
    // return I.make(this, Flexible.class, args -> {
    // args[0] = param == null ? null : param.get();
    // return invoke(args);
    // });
    // }
    //
    // /**
    // * Fix last parameter partialy. The actual argument from the caller will be ignored.
    // *
    // * @param param A fixed parameter.
    // * @return A partial fixed function.
    // */
    // default Self fixLast(Last param) {
    // return fixLastLazily(Variable.of(param));
    // }
    //
    // /**
    // * Fix last parameter partialy. The actual argument from the caller will be ignored. The null
    // * {@link Supplier} will be treated as null value.
    // *
    // * @param param A fixed parameter.
    // * @return A partial fixed function.
    // */
    // default Self fixLastLazily(Supplier<Last> param) {
    // return I.make(this, Flexible.class, args -> {
    // args[args.length - 1] = param == null ? null : param.get();
    // return invoke(args);
    // });
    // }

    /**
     * Create memoized function.
     *
     * @return The created memoized function.
     */
    default Self memo() {
        Map cache = new ConcurrentHashMap();

        return I.make(this, null, args -> {
            synchronized (cache) {
                return cache.computeIfAbsent(Objects.hash(args), k -> invoke(args));
            }
        });
    }

    /**
     * <p>
     * Rotates the parameters of this interface. After calling this method, the parameter at index i
     * will be the parameter previously at index (i - 1). The last parameter moves to the beginning.
     * This method has no effect on the size of parameters.
     * </p>
     * <p>
     * For example, suppose interface <code>Some&lt;Param1, Param2, Pram3&gt;</code>. After invoking
     * {@link #shift()}, the interface will be <code>Some&lt;Param3, Param2, Param1%gt;</code>.
     * </p>
     * 
     * @return
     */
    default Spined shift() {
        // "rotate" is not used because it suffers from the first character of "Runnable#run".
        return I.make(this, null, args -> {
            for (int i = 0; i < args.length - 1; i++) {
                Object o = args[i];
                args[i] = args[i + 1];
                args[i + 1] = o;
            }
            return invoke(args);
        });
    }
}
