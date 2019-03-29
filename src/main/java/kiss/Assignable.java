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

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import kiss.model.Model;

/**
 * 
 */
public interface Assignable<Assigned, HeadParam, Preassigned, TailParam> {
    /**
     * <p>
     * Apply parameter partialy.
     * </p>
     * 
     * @param function A target function to apply parameter.
     * @param param1 A fixed parameter.
     * @return A partial applied function.
     */
    default Assigned assign(HeadParam param) {
        return assign(Variable.of(param));
    }

    /**
     * <p>
     * Apply parameter partialy.
     * </p>
     * 
     * @param function A target function to apply parameter.
     * @param param1 A fixed parameter.
     * @return A partial applied function.
     */
    default Assigned assign(Supplier<HeadParam> param) {
        return create(args -> args.add(param.get()));
    }

    /**
     * <p>
     * Apply parameter partialy.
     * </p>
     * 
     * @param function A target function to apply parameter.
     * @param param1 A fixed parameter.
     * @return A partial applied function.
     */
    default Preassigned preassign(TailParam param) {
        return preassign(Variable.of(param));
    }

    /**
     * <p>
     * Apply parameter partialy.
     * </p>
     * 
     * @param function A target function to apply parameter.
     * @param param1 A fixed parameter.
     * @return A partial applied function.
     */
    default Preassigned preassign(Supplier<TailParam> param) {
        return create(args -> args.add(0, param.get()));
    }

    private <F> F create(Consumer<List<Object>> converter) {
        Class baseType = getClass().getInterfaces()[0];
        Class<F> type;
        Type proxyType = Model.collectParameters(baseType, Assignable.class)[0];

        if (proxyType instanceof ParameterizedType) {
            type = (Class) ((ParameterizedType) proxyType).getRawType();
        } else {
            type = (Class) proxyType;
        }

        Method baseMethod = I.signal(baseType.getMethods()).skip(Method::isDefault).to().v;

        return I.make(type, (proxy, method, args) -> {
            if (method.isDefault()) {
                return MethodHandles.privateLookupIn(type, MethodHandles.lookup())
                        .unreflectSpecial(method, type)
                        .bindTo(proxy)
                        .invokeWithArguments(args);
            } else {
                List list = I.list(args);
                converter.accept(list);

                return baseMethod.invoke(this, list.toArray(new Object[list.size()]));
            }
        });
    }
}
