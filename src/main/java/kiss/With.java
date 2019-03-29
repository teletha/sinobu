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

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.function.Supplier;

import kiss.model.Model;

public interface With<Head, HeadParam> {

    /**
     * <p>
     * Apply parameter partialy.
     * </p>
     * 
     * @param function A target function to apply parameter.
     * @param param A fixed parameter.
     * @return A partial applied function.
     */
    default Head with(HeadParam param) {
        return with(Variable.of(param));
    }

    /**
     * <p>
     * Apply parameter partialy.
     * </p>
     * 
     * @param function A target function to apply parameter.
     * @param param A fixed parameter.
     * @return A partial applied function.
     */
    default Head with(Supplier<HeadParam> param) {
        try {
            Type[] types = Model.collectParameters(getClass().getInterfaces()[0], With.class);
            ParameterizedType type = (ParameterizedType) types[0];

            Lookup lookup = MethodHandles.lookup();

            MethodHandle proxy = lookup.findVirtual(WiseSupplier.class, "GET", MethodType.genericMethodType(0));

            Object[] handle = handle();

            return create(proxy, (MethodHandle) handle[0], handle[1], param.get());
        } catch (Throwable e) {
            throw I.quiet(e);
        }
    }

    public static <I> I create(MethodHandle proxy, MethodHandle base, Object... context) throws Throwable {
        MethodType t = proxy.type().dropParameterTypes(0, 1);

        CallSite callsite = LambdaMetafactory
                .metafactory(MethodHandles.lookup(), "GET", base.type().changeReturnType(proxy.type().lastParameterType()), t, base, t);
        MethodHandle target = callsite.getTarget();

        return (I) target.invokeWithArguments(context);
    }

    default Object[] handle() throws Exception {
        return null;
    }

    public static void main(String[] args) {

        WiseFunction<String, String> a = c -> c;
        WiseSupplier<String> with = a.with("AA");
        System.out.println(with.get());
    }
}
