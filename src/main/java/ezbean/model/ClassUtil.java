/*
 * Copyright (C) 2010 Nameless Production Committee.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *         http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ezbean.model;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.security.CodeSource;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import ezbean.I;

/**
 * <p>
 * This utility provides methods to collect information form the specified class.
 * </p>
 * 
 * @version 2009/12/30 0:23:53
 */
public final class ClassUtil {

    /** The list of primitive classes. (except for void type) */
    public static final Class[] PRIMITIVES = {boolean.class, int.class, long.class, float.class, double.class,
            byte.class, short.class, char.class};

    /** The list of wrapper classes. (except for void type) */
    public static final Class[] WRAPPERS = {Boolean.class, Integer.class, Long.class, Float.class, Double.class,
            Byte.class, Short.class, Character.class};

    /**
     * Avoid construction.
     */
    private ClassUtil() {
    }

    /**
     * <p>
     * Helper method to find the class archive (e.g. jar file, classes directory) by the specified
     * sample class. If the sample class belongs to system classloader (e.g. {@link String}),
     * <code>null</code> will be returned.
     * </p>
     * 
     * @param clazz A sample class.
     * @return A class archive (e.g. jar file, classes directory) or <code>null</code>.
     */
    public static File getArchive(Class clazz) {
        // retrieve code source of this sample class
        CodeSource source = clazz.getProtectionDomain().getCodeSource();

        // API definition
        return (source == null) ? null : I.locate(source.getLocation());
    }

    /**
     * Helper method to collect all classes which are extended or implemented by the target class.
     * 
     * @param clazz A target class. <code>null</code> will be return the empty set.
     * @return A set of classes, with predictable bottom-up iteration order.
     */
    public static Set<Class> getTypes(Class clazz) {
        // check null
        if (clazz == null) {
            return Collections.EMPTY_SET;
        }

        // container
        Set<Class> set = new LinkedHashSet(); // order is important

        // add current class
        set.add(clazz);

        // add super class
        set.addAll(getTypes(clazz.getSuperclass()));

        // add interface classes
        for (Class c : clazz.getInterfaces()) {
            set.addAll(getTypes(c));
        }

        // API definition
        return set;
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
     * @param clazz A class which implements(extends) the specified target interface(class).
     *            <code>null</code> will be return the zero-length array.
     * @param target A target type to list up types.
     * @return A list of actual types.
     * @throws NullPointerException If the speficied target class is <code>null</code>.
     */
    public static Class[] getParameter(Class clazz, Class target) {
        return getParameter(clazz, target, clazz);
    }

    /**
     * <p>
     * Helper method to collect information for parameter types.
     * </p>
     * 
     * @param clazz
     * @param target
     * @param base
     * @return
     */
    private static Class[] getParameter(Class clazz, Class target, Class base) {
        // check null
        if (clazz == null) {
            return new Class[0];
        }

        // collect all types
        Set<Type> types = new HashSet();
        types.add(clazz.getGenericSuperclass());

        for (Type type : clazz.getGenericInterfaces()) {
            types.add(type);
        }

        // check them all
        for (Type type : types) {
            // check ParameterizedType
            if (type instanceof ParameterizedType) {
                ParameterizedType param = (ParameterizedType) type;

                // check raw type
                if (target == param.getRawType()) {
                    Type[] args = param.getActualTypeArguments();
                    Class[] classes = new Class[args.length];

                    for (int i = 0; i < args.length; i++) {
                        // resolve various type (TypeVariable, ParameterizedType and WildcardType)
                        classes[i] = Model.load(args[i], base).type;
                    }
                    return classes;
                }
            }
        }

        // search from superclass
        return getParameter(clazz.getSuperclass(), target, base);
    }

    /**
     * <p>
     * Return a non-primitive {@link java.lang.Class} of the specified class object.
     * <code>null</code> will be return <code>null</code>.
     * </p>
     * 
     * @param clazz A class object to convert to non-primitive class.
     * @return A non-primitive class object.
     */
    public static Class wrap(Class clazz) {
        // check primitive classes
        for (int i = 0; i < PRIMITIVES.length; i++) {
            if (PRIMITIVES[i] == clazz) {
                return WRAPPERS[i];
            }
        }

        // the specified class is not primitive
        return clazz;
    }
}
