/*
 * Copyright (C) 2016 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.model;

import java.lang.reflect.Constructor;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * <p>
 * This utility provides methods to collect information form the specified class.
 * </p>
 * 
 * @version 2011/12/11 20:16:03
 */
@SuppressWarnings("unchecked")
public final class ClassUtil {

    /**
     * Avoid construction.
     */
    private ClassUtil() {
    }

    /**
     * <p>
     * Helper method to find the constructor which has minimum parameters. If the given class is
     * interface, primitive types, array class or <code>void</code>, <code>null</code> will be
     * return.
     * </p>
     * 
     * @param <T> A class type.
     * @param clazz A target class.
     * @return A minimum constructor or <code>null</code>.
     */
    public static <T> Constructor<T> getMiniConstructor(Class<T> clazz) {
        // the candidate of minimum constructor
        Constructor mini = null;

        for (Constructor constructor : clazz.getDeclaredConstructors()) {
            // test parameter size
            if (mini == null || constructor.getParameterTypes().length < mini.getParameterTypes().length) {
                mini = constructor;
            }
        }

        // API definition
        return mini;
    }

    /**
     * <p>
     * List up all target types which are implemented or extended by the specified class.
     * </p>
     * 
     * @param type A class type which implements(extends) the specified target interface(class).
     *            <code>null</code> will be return the zero-length array.
     * @param target A target type to list up types. <code>null</code> will be return the
     *            zero-length array.
     * @return A list of actual types.
     */
    public static Type[] getParameter(Type type, GenericDeclaration target) {
        return getParameter(type, target, type);
    }

    /**
     * <p>
     * List up all target types which are implemented or extended by the specified class.
     * </p>
     * 
     * @param clazz A class type which implements(extends) the specified target interface(class).
     *            <code>null</code> will be return the zero-length array.
     * @param target A target type to list up types. <code>null</code> will be return the
     *            zero-length array.
     * @param base A base class type.
     * @return A list of actual types.
     */
    private static Type[] getParameter(Type clazz, GenericDeclaration target, Type base) {
        // check null
        if (clazz == null || clazz == target) {
            return new Class[0];
        }

        // compute actual class
        Class raw = clazz instanceof Class ? (Class) clazz : Model.of(clazz, base).type;

        // collect all types
        Set<Type> types = new HashSet();
        types.add(clazz);
        types.add(raw.getGenericSuperclass());
        Collections.addAll(types, raw.getGenericInterfaces());

        // check them all
        for (Type type : types) {
            // check ParameterizedType
            if (type instanceof ParameterizedType) {
                ParameterizedType param = (ParameterizedType) type;

                // check raw type
                if (target == param.getRawType()) {
                    Type[] args = param.getActualTypeArguments();

                    for (int i = 0; i < args.length; i++) {
                        if (args[i] instanceof TypeVariable) {
                            args[i] = Model.of(args[i], base).type;
                        }
                    }
                    return args;
                }
            }
        }

        // search from superclass
        Type[] parameters = getParameter(raw.getGenericSuperclass(), target, base);

        if (parameters.length != 0) {
            return parameters;
        }

        // search from interfaces
        for (Type type : raw.getInterfaces()) {
            parameters = getParameter(type, target, base);

            if (parameters.length != 0) {
                return parameters;
            }
        }
        return parameters;
    }
}
