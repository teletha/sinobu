/*
 * Copyright (C) 2015 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.scratchpad;

import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

import kiss.I;

/**
 * @version 2015/08/25 16:26:20
 */
class MethodFinder {

    static Method method(Object target) {
        try {
            Method replaceMethod = target.getClass().getDeclaredMethod("writeReplace");
            replaceMethod.setAccessible(true);

            SerializedLambda lambda = (SerializedLambda) replaceMethod.invoke(target);
            Class clazz = Class.forName(lambda.getImplClass().replaceAll("/", "."));

            return Arrays.asList(clazz.getDeclaredMethods())
                    .stream()
                    .filter(method -> Objects.equals(method.getName(), lambda.getImplMethodName()))
                    .findFirst()
                    .orElseThrow(IllegalStateException::new);
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }
}