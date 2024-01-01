/*
 * Copyright (C) 2024 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.experimental;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.function.Consumer;
import java.util.function.Function;

import kiss.I;

public interface Reflectable extends Serializable {

    /**
     * Static method pattern. Don't provide Reflectable interface, user must define your original
     * serializable interface.
     * 
     * @param lambda
     * @return
     */
    static Method reflect(Serializable lambda) {
        try {
            Method m = lambda.getClass().getDeclaredMethod("writeReplace");
            m.setAccessible(true);
            SerializedLambda s = (SerializedLambda) m.invoke(lambda);

            return I.signal(I.type(s.getImplClass().replaceAll("/", ".")).getDeclaredMethods())
                    .take(x -> x.getName().equals(s.getImplMethodName()))
                    .first()
                    .to()
                    .exact();
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }

    /**
     * Get the implementation of this lambda.
     * 
     * @return The implementation method of this lambda.
     */
    default Method method() {
        try {
            Method m = getClass().getDeclaredMethod("writeReplace");
            m.setAccessible(true);
            SerializedLambda s = (SerializedLambda) m.invoke(this);

            return I.signal(I.type(s.getImplClass().replaceAll("/", ".")).getDeclaredMethods())
                    .take(x -> x.getName().equals(s.getImplMethodName()))
                    .first()
                    .to()
                    .exact();
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }

    /**
     * 
     */
    public interface ReflectableConsumer<T> extends Consumer<T>, Reflectable {
    }

    /**
     * 
     */
    public interface ReflectableFunction<P, R> extends Function<P, R>, Reflectable {
    }

    /**
     * 
     */
    public interface ReflectableRunnable extends Runnable, Reflectable {
    }
}