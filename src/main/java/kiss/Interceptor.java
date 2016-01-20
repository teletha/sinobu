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

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;

/**
 * <p>
 * This is generic method interceptor.
 * </p>
 * 
 * @param <P> An annotation type to intercept.
 * @version 2011/12/19 2:20:56
 */
public class Interceptor<P extends Annotation> implements Extensible {

    /** The callee instance. */
    protected Object that;

    /** The associated annotation. */
    protected P annotation;

    /** The delegation method name. */
    protected String name;

    /** The delegation method. */
    protected MethodHandle method;

    /** The parent interceptor to chain. */
    private Interceptor parent;

    /**
     * Hide constructor.
     */
    protected Interceptor() {
    }

    /**
     * <p>
     * Intercept non-static method invocation.
     * </p>
     * <p>
     * You can invoke original method by using super call like the following.
     * </p>
     * 
     * <pre>
     * public class YourInterceptor extends Interceptor<YourAnnotation> {
     *     
     *     protected Object invoke(Object... params) {
     *         return super.invoke(params);
     *     }
     * }
     * </pre>
     * 
     * @param params Parameters.
     */
    protected Object invoke(Object... params) {
        if (parent != null) {
            return parent.invoke(params);
        } else {
            try {
                return method.bindTo(that).invokeWithArguments(params);
            } catch (Throwable e) {
                throw I.quiet(e);
            }
        }
    }

    /**
     * <p>
     * NOTE : This is internal method. A user of Sinobu <em>does not have to use</em> this method.
     * </p>
     * 
     * @param name A intercepted method name.
     * @param method A delegation method.
     * @param that A current processing object.
     * @param params A current method parameters.
     * @param annotations A interceptable annotation list.
     * @return A result of method invocation.
     */
    public static Object invoke(String name, MethodHandle method, Object that, Object[] params, Annotation[] annotations) {
        Interceptor current = new Interceptor();
        current.method = method;
        current.that = that;

        for (int i = annotations.length - 1; 0 <= i; --i) {
            Interceptor interceptor = I.find(Interceptor.class, annotations[i].annotationType());

            if (interceptor != null) {
                interceptor.that = that;
                interceptor.parent = current;
                interceptor.annotation = annotations[i];
                interceptor.name = name;
                interceptor.method = method;

                current = interceptor;
            }
        }

        // Invoke chain of interceptors.
        return current.invoke(params);
    }
}
