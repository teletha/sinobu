/*
 * Copyright (C) 2024 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss;

import static java.lang.reflect.Modifier.*;

import java.lang.annotation.Annotation;
import java.lang.annotation.Repeatable;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@link Model} is the advanced representation of {@link Class} in Sinobu.
 */
public class Model<M> {

    /** The model repository. */
    static final Map<Ⅱ<Class, Type[]>, Model> models = new ConcurrentHashMap();

    /** The {@link Class} which is represented by this {@link Model}. */
    public final Class<M> type;

    /** Whether this {@link Model} is an atomic type or object type. */
    public final boolean atomic;

    /** The associated decoder. */
    public final Decoder<M> decoder;

    /** The associated encoder. */
    public final Encoder<M> encoder;

    /** The unmodifiable properties list of this object model. */
    private Map<String, Property> properties;

    /**
     * Create Model instance.
     * 
     * @param type A target class to analyze as model.
     * @throws NullPointerException If the specified model class is <code>null</code>.
     */
    Model(Class type) {
        // Skip null check because this method can throw NullPointerException.
        // if (type == null) throw new NullPointerException("Model class shouldn't be null.");
        this.type = type;
        this.decoder = I.find(Decoder.class, type);
        this.encoder = I.find(Encoder.class, type);
        this.atomic = decoder != null || type.isArray();
    }

    /**
     * Initialize this {@link Model} only once.
     */
    private synchronized void init(Type... hints) {
        if (properties == null) {
            properties = Collections.EMPTY_MAP;
            try {
                // examine all methods without private, final, static or native
                Map<String, Method[]> candidates = new HashMap();

                for (Class clazz : Model.collectTypes(type)) {
                    if (!Proxy.isProxyClass(clazz) && clazz.getModule().isOpen(clazz.getPackageName())) {
                        for (Method method : clazz.getDeclaredMethods()) {
                            // exclude the method which modifier is final, static, private or native
                            if (((STATIC | NATIVE) & method.getModifiers()) == 0) {
                                // exclude the method which is created by compiler
                                if (!method.isBridge() && !method.isSynthetic()) {
                                    int length = 1;
                                    String prefix = "set";
                                    String name = method.getName();

                                    if (method.getGenericReturnType() != Void.TYPE) {
                                        length = 0;
                                        prefix = name.charAt(0) == 'i' ? "is" : "get";
                                    }

                                    // exclude the method (by name)
                                    if (prefix.length() < name.length() && name.startsWith(prefix) && !Character
                                            .isLowerCase(name.charAt(prefix.length()))) {
                                        // exclude the method (by parameter signature)
                                        if (method.getGenericParameterTypes().length == length) {
                                            // compute property name
                                            name = name.substring(prefix.length());
                                            name = Character.toLowerCase(name.charAt(0)) + name.substring(1);

                                            // store a candidate of property accessor
                                            Method[] methods = candidates.get(name);

                                            if (methods == null) {
                                                methods = new Method[2];
                                                candidates.put(name, methods);
                                            }

                                            if (methods[length] == null) {
                                                methods[length] = method;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // build valid properties
                // don't use type parameter to reduce footprint
                properties = new TreeMap();

                for (Entry<String, Method[]> entry : candidates.entrySet()) {
                    Method[] methods = entry.getValue();
                    if (methods[0] != null && methods[1] != null) {
                        // create model for the property
                        try {
                            Model model = of(methods[0].getGenericReturnType(), type);

                            if (of(methods[1].getGenericParameterTypes()[0], type).type.isAssignableFrom(model.type)) {
                                // this property is valid
                                WiseBiConsumer setter = createSetter(methods[1]);
                                Property property = new Property(model, entry.getKey(), null);
                                property.getter = createGetter(methods[0]);
                                property.setter = (m, v) -> {
                                    setter.ACCEPT(m, v);
                                    // methods[1].invoke(m, v);
                                    return m;
                                };

                                // register it
                                properties.put(property.name, property);
                            }
                        } catch (Throwable e) {
                            throw I.quiet(e);
                        }
                    }
                }

                // Search field properties.
                Class clazz = type;
                while (clazz != null) {
                    for (Field field : clazz.getDeclaredFields()) {
                        int modifier = field.getModifiers();
                        boolean notFinal = (FINAL & modifier) == 0;

                        // reject the field which modifier is static or native
                        if (((STATIC | NATIVE) & modifier) == 0) {
                            // accept fields which
                            // -- is public modifier (implicitly)
                            // -- is annotated by Managed (explicitly)
                            // -- is Record component (implicitly)
                            if ((PUBLIC & modifier) == PUBLIC //
                                    || field.isAnnotationPresent(Managed.class) //
                                    || (type.isRecord() && (PRIVATE & modifier) == PRIVATE)) {
                                field.setAccessible(true);
                                try {
                                    Model fieldModel = of(specialize(field.getGenericType(), field.getDeclaringClass()
                                            .getTypeParameters(), hints), type);
                                    if (Variable.class.isAssignableFrom(fieldModel.type)) {
                                        // variable
                                        Property property = new Property(of(collectParameters(field
                                                .getGenericType(), Variable.class, type)[0], type), field.getName(), field);
                                        property.getter = m -> ((Variable) field.get(m)).v;
                                        property.setter = (m, v) -> {
                                            ((Variable) field.get(m)).set(v);
                                            return m;
                                        };
                                        property.observer = m -> ((Variable) field.get(m)).observe();

                                        // register it
                                        properties.put(property.name, property);
                                    } else if ((fieldModel.atomic && notFinal) || !fieldModel.atomic || type.isRecord()) {
                                        Property property = new Property(fieldModel, field.getName(), field);

                                        property.getter = field::get;
                                        if (type.isRecord()) {
                                            property.setter = (m, v) -> {
                                                Constructor c = collectConstructors(type)[0];
                                                c.setAccessible(true);
                                                Parameter[] params = c.getParameters();
                                                Object[] values = new Object[params.length];
                                                for (int i = 0; i < params.length; i++) {
                                                    String name = params[i].getName();
                                                    values[i] = name.equals(property.name) ? v : get((M) m, property(name));
                                                }
                                                return c.newInstance(values);
                                            };
                                        } else {
                                            MethodHandle setter = MethodHandles.lookup().unreflectSetter(field);
                                            property.setter = (m, v) -> {
                                                setter.invoke(m, v);
                                                return m;
                                            };
                                        }

                                        // register it
                                        properties.put(property.name, property);
                                    }
                                } catch (Throwable e) {
                                    throw I.quiet(e);
                                }
                            }
                        }
                    }
                    clazz = clazz.getSuperclass();
                }
            } catch (Exception e) {
                throw I.quiet(e);
            }
        }
    }

    /**
     * List up all properties.
     * 
     * @return
     */
    public Collection<Property> properties() {
        return properties.values();
    }

    /**
     * Find the property which has the specified name in this object model. If the suitable property
     * is not found, <code>null</code> is returned.
     * 
     * @param name A name of property.
     * @return A suitable property or <code>null</code>.
     */
    public Property property(String name) {
        return properties.get(name);
    }

    /**
     * Returns the value of the given property in the given object.
     * 
     * @param object An object as source. This value must not be <code>null</code>.
     * @param property A property. This value must not be <code>null</code>.
     * @return A resolved property value. This value may be <code>null</code>.
     * @throws IllegalArgumentException If the given object can't resolve the given property.
     */
    public Object get(M object, Property property) {
        if (object == null || property == null) {
            return null;
        }
        return property.getter.apply(object);
    }

    /**
     * Change the given property in the given object to the given new property value.
     * 
     * @param object An object as source. This value must not be <code>null</code>.
     * @param property A property. This value must not be <code>null</code>.
     * @param value A new property value that you want to set. This value accepts <code>null</code>.
     * @throws IllegalArgumentException If the given object can't resolve the given property.
     */
    public M set(M object, Property property, Object value) {
        return (M) property.setter.apply(object, value);
    }

    /**
     * Observe the given property in the given object.
     * 
     * @param object An object as source. This value must not be <code>null</code>.
     * @param property A property. This value must not be <code>null</code>.
     * @return A property observer.
     * @throws IllegalArgumentException If the given object can't resolve the given property.
     */
    public Signal observe(M object, Property property) {
        if (object != null && property != null && property.observer != null) {
            return property.observer.apply(object);
        } else {
            return Signal.never();
        }
    }

    /**
     * Iterate over all properties in the given object and propagate the property, and it's value to
     * the given property walker.
     * 
     * @param object An object as source. This value must not be <code>null</code>,
     * @param walker A property iterator. This value accepts <code>null</code>.
     */
    public void walk(M object, WiseTriConsumer<Model<M>, Property, Object> walker) {
        // check whether this model is attribute or not.
        if (walker != null) {
            for (Property property : properties.values()) {
                walker.accept(this, property, get(object, property));
            }
        }
    }

    /**
     * Utility method to retrieve the cached model. If the model of the given class is not found,
     * {@link IllegalArgumentException} will be thrown.
     * <p>
     * If the given model has no cached information, it will be created automatically. This
     * operation is thread-safe.
     * <p>
     * Note : All classes do not necessary have each information. Some classes might share same
     * {@link Model} object. (e.g. AutoGenerated Class)
     * 
     * @param modelType A model class.
     * @return The information about the given model class.
     * @throws NullPointerException If the given model class is null.
     * @throws IllegalArgumentException If the given model class is not found.
     */
    public static <M> Model<M> of(M modelType) {
        return of((Class<M>) modelType.getClass());
    }

    /**
     * Utility method to retrieve the cached model. If the model of the given class is not found,
     * {@link IllegalArgumentException} will be thrown.
     * <p>
     * If the given model has no cached information, it will be created automatically. This
     * operation is thread-safe.
     * <p>
     * Note : All classes do not necessary have each information. Some classes might share same
     * {@link Model} object. (e.g. AutoGenerated Class)
     * 
     * @param modelClass A model class.
     * @return The information about the given model class.
     * @throws NullPointerException If the given model class is null.
     * @throws IllegalArgumentException If the given model class is not found.
     */
    public static synchronized <M> Model<M> of(Class<? super M> modelClass) {
        Ⅱ<Class, Type[]> key = I.pair(modelClass, null);

        // check cache
        Model model = models.get(key);
        if (model == null) {
            if (List.class.isAssignableFrom(modelClass)) {
                model = new ListModel(modelClass, Model.collectParameters(modelClass, List.class), List.class);
            } else if (Map.class.isAssignableFrom(modelClass)) {
                model = new MapModel(modelClass, Model.collectParameters(modelClass, Map.class), Map.class);
            } else {
                // To resolve cyclic reference, try to retrieve from cache.
                model = models.computeIfAbsent(key, x -> new Model(modelClass));
                model.init();
            }
            models.put(key, model);
        }
        return model;
    }

    /**
     * Utility method to retrieve the cached model. If the model of the given type is not found,
     * {@link IllegalArgumentException} will be thrown.
     * 
     * @param type A target type to analyze.
     * @param base A declaration class.
     * @return A cached model information.
     * @throws IllegalArgumentException If the given model type is null.
     * @see TypeVariable
     */
    static synchronized Model of(Type type, Type base) {
        // =======================================
        // Class
        // =======================================
        if (type instanceof Class) {
            return of((Class) type);
        }

        // =======================================
        // Parameterized Type
        // =======================================
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterized = (ParameterizedType) type;
            Class clazz = (Class) parameterized.getRawType();

            // ListModel
            if (List.class.isAssignableFrom(clazz)) {
                return new ListModel(clazz, parameterized.getActualTypeArguments(), base);
            }

            // MapModel
            if (Map.class.isAssignableFrom(clazz)) {
                return new MapModel(clazz, parameterized.getActualTypeArguments(), base);
            }

            // ClassModel
            // To resolve cyclic reference, try to retrieve from cache.
            Model model = models.computeIfAbsent(I.pair(clazz, parameterized.getActualTypeArguments()), x -> new Model(clazz));
            model.init(parameterized.getActualTypeArguments());

            return model;
        }

        // =======================================
        // Wildcard Type
        // =======================================
        if (type instanceof WildcardType) {
            WildcardType wildcard = (WildcardType) type;

            Type[] types = wildcard.getLowerBounds();

            if (types.length != 0) {
                return of(types[0], base);
            }

            types = wildcard.getUpperBounds();

            if (types.length != 0) {
                return of(types[0], base);
            }
        }

        // =======================================
        // Variable Type
        // =======================================
        if (type instanceof TypeVariable) {
            TypeVariable variable = (TypeVariable) type;
            TypeVariable[] variables = variable.getGenericDeclaration().getTypeParameters();

            for (int i = 0; i < variables.length; i++) {
                // use equals method instead of "==".
                //
                // +++ From TypeVariable Javadoc +++
                // Multiple objects may be instantiated at run-time to represent a given type
                // variable. Even though a type variable is created only once, this does not imply
                // any requirement to cache instances representing the type variable. However, all
                // instances representing a type variable must be equal() to each other. As a
                // consequence, users of type variables must not rely on the identity of instances
                // of classes implementing this interface.
                if (variable.equals(variables[i])) {
                    Type[] types = collectParameters(base, variable.getGenericDeclaration());
                    return of(types.length == 0 || types.length < i ? variable.getBounds()[0] : types[i], base);
                }
            }
        }

        // =======================================
        // Generic Array Type
        // =======================================
        if (type instanceof GenericArrayType) {
            return of(((GenericArrayType) type).getGenericComponentType(), base);
        }

        // If this error will be thrown, it is bug of this program. Please send a bug report to us.
        throw new Error();
    }

    /**
     * Collect all annotated methods and their annotations.
     * 
     * @param clazz A target class.
     * @return A table of method and annotations.
     */
    public static Map<Method, List<Annotation>> collectAnnotatedMethods(Class clazz) {
        Map<Method, List<Annotation>> table = new HashMap();

        for (Class type : collectTypes(clazz)) {
            if (type != Object.class) {
                for (Method method : type.getDeclaredMethods()) {
                    // exclude the method which is created by compiler
                    // exclude the private method which is not declared in the specified class
                    if (!method.isBridge() && !method
                            .isSynthetic() && (((method.getModifiers() & Modifier.PRIVATE) == 0) || method.getDeclaringClass() == clazz)) {
                        Annotation[] annotations = method.getAnnotations();

                        if (annotations.length != 0) {
                            List<Annotation> list = new ArrayList();

                            // disclose container annotation
                            for (Annotation annotation : annotations) {
                                try {
                                    Class annotationType = annotation.annotationType();
                                    Method value = annotationType.getMethod("value");
                                    Class returnType = value.getReturnType();

                                    if (returnType.isArray()) {
                                        Class<?> componentType = returnType.getComponentType();
                                        Repeatable repeatable = componentType.getAnnotation(Repeatable.class);

                                        if (repeatable != null && repeatable.value() == annotationType) {
                                            value.setAccessible(true);

                                            Collections.addAll(list, (Annotation[]) value.invoke(annotation));
                                            continue;
                                        }
                                    }
                                } catch (Exception e) {
                                    // do nothing
                                }
                                list.add(annotation);
                            }

                            // check method overriding
                            for (Method candidate : table.keySet()) {
                                if (candidate.getName().equals(method.getName()) && Arrays
                                        .deepEquals(candidate.getParameterTypes(), method.getParameterTypes())) {
                                    method = candidate; // detect overriding
                                    break;
                                }
                            }

                            add: for (Annotation annotation : list) {
                                Class annotationType = annotation.annotationType();
                                List<Annotation> items = table.computeIfAbsent(method, m -> new ArrayList());

                                if (!annotationType.isAnnotationPresent(Repeatable.class)) {
                                    for (Annotation item : items) {
                                        if (item.annotationType() == annotationType) {
                                            continue add;
                                        }
                                    }
                                }
                                items.add(annotation);
                            }
                        }
                    }
                }
            }
        }
        return table;
    }

    /**
     * Collect all constructors which are defined in the specified {@link Class}. If the given class
     * is interface, primitive types, array class or <code>void</code>, <code>empty array</code>
     * will be return.
     * 
     * @param <T> A class type.
     * @param clazz A target class.
     * @return A collected constructors.
     */
    public static <T> Constructor<T>[] collectConstructors(Class<T> clazz) {
        Constructor[] cc = clazz.getDeclaredConstructors();
        Arrays.sort(cc, Comparator.comparingInt(c -> {
            for (Annotation a : c.getAnnotations()) {
                if (a.annotationType() == Managed.class || a.annotationType().getSimpleName().equals("Inject")) {
                    return -1;
                }
            }

            // Constructor#getParameters is not supported in lower version than Android O.
            // So we must use Class#getParameterType#length instead.
            return c.getParameterTypes().length;
        }));
        return cc;
    }

    // public static Signal<Class> findTypes(Class... clazz) {
    // return Signal.from(clazz)
    // .skipNull()
    // .flatMap(c -> Signal.from(c).concat(() -> findTypes(c.getSuperclass())).concat(() ->
    // findTypes(c.getInterfaces())))
    // .distinct();
    // }

    /**
     * Collect all classes which are extended or implemented by the target class.
     * 
     * @param clazz A target class. <code>null</code> will be return the empty set.
     * @return A set of classes, with predictable bottom-up iteration order.
     */
    public static Set<Class> collectTypes(Class clazz) {
        // check null
        if (clazz == null) {
            return Collections.EMPTY_SET;
        }

        // container
        Set<Class> set = new LinkedHashSet(); // order is important

        // add current class
        set.add(clazz);

        // add super class
        set.addAll(collectTypes(clazz.getSuperclass()));

        // add interface classes
        for (Class c : clazz.getInterfaces()) {
            set.addAll(collectTypes(c));
        }

        // API definition
        return set;
    }

    /**
     * List up all target types which are implemented or extended by the specified class.
     * 
     * @param type A class type which implements(extends) the specified target interface(class).
     *            <code>null</code> will be return the zero-length array.
     * @param target A target type to list up types. <code>null</code> will be return the
     *            zero-length array.
     * @return A list of actual types.
     */
    public static Type[] collectParameters(Type type, GenericDeclaration target) {
        return collectParameters(type, target, type);
    }

    /**
     * List up all target types which are implemented or extended by the specified class.
     * 
     * @param clazz A class type which implements(extends) the specified target interface(class).
     *            <code>null</code> will be return the zero-length array.
     * @param target A target type to list up types. <code>null</code> will be return the
     *            zero-length array.
     * @param base A base class type.
     * @return A list of actual types.
     */
    private static Type[] collectParameters(Type clazz, GenericDeclaration target, Type base) {
        // check null
        if (clazz == null || clazz == target) {
            return new Class[0];
        }

        // compute actual class
        //
        // If Model.of(Type, Type) is used to detect the raw class, the target Model will be
        // defined first when the Decoder or Encoder definition class is loaded, which will
        // adversely affect the determination of whether the Model is atomic or not. Do not use
        // Model.of(Type,Type).
        Class raw = (Class) (clazz instanceof Class ? clazz
                : clazz instanceof ParameterizedType ? ((ParameterizedType) clazz).getRawType() : Object.class);

        // collect all types
        Set<Type> types = new LinkedHashSet();
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
                            if (args[i] == Object.class && clazz != base) {
                                args[i] = collectParameters(clazz, target)[i];
                            }
                        }
                    }
                    return args;
                }
            }
        }

        // search from superclass
        Type[] parameters = collectParameters(raw.getGenericSuperclass(), target, base);

        if (parameters.length != 0) {
            return parameters;
        }

        // search from interfaces
        for (Type type : raw.getInterfaces()) {
            parameters = collectParameters(type, target, base);

            if (parameters.length != 0) {
                return parameters;
            }
        }
        return parameters;
    }

    /**
     * Specialize the {@link TypeVariable}.
     * 
     * @param type A target type to be specialized.
     * @param vars A definition of type variables.
     * @param specials An actual types.
     * @return
     */
    private static Type specialize(Type type, TypeVariable[] vars, Type[] specials) {
        if (type instanceof ParameterizedType param) {
            Type[] params = param.getActualTypeArguments();
            for (int i = 0; i < params.length; i++) {
                params[i] = specialize(params[i], vars, specials);
            }

            I p = new I();
            p.par = I.pair(param.getRawType(), param.getOwnerType(), params);
            return p;
        } else if (type instanceof TypeVariable) {
            for (int i = 0; i < specials.length; i++) {
                if (vars[i] == type) {
                    return specials[i] instanceof TypeVariable ? Object.class : specials[i];
                }
            }
        }
        return type;
    }

    // static WiseSupplier createConstructor(Constructor constructor) throws Throwable {
    // Lookup lookup = MethodHandles.privateLookupIn(constructor.getDeclaringClass(),
    // MethodHandles.lookup());
    // MethodHandle mh = lookup.unreflectConstructor(constructor);
    //
    // return (WiseSupplier) LambdaMetafactory
    // .metafactory(lookup, "call", MethodType.methodType(WiseSupplier.class), mh.type().generic(),
    // mh, mh.type())
    // .dynamicInvoker()
    // .invokeExact();
    // }

    /**
     * In Java today, there are three main types of reflection methods. The first is using old
     * methods and fields, the second is using MethodHandle, and the third is using
     * LambdaMetaFactory. The fastest of the three methods is the one using MethodHandle, but it has
     * the restriction that each MethodHandle must be stored in a static final field. Therefore,
     * LambdaMetaFactory, the second fastest method, is used here. However, this method is still as
     * fast as a direct call.
     * 
     * See benchmarks for verification against speed.
     */
    static WiseFunction createGetter(Method method) {
        try {
            Lookup lookup = MethodHandles.privateLookupIn(method.getDeclaringClass(), MethodHandles.lookup());
            MethodHandle mh = lookup.unreflect(method);

            return (WiseFunction) LambdaMetafactory
                    .metafactory(lookup, "APPLY", MethodType.methodType(WiseFunction.class), mh.type().generic(), mh, mh.type())
                    .dynamicInvoker()
                    .invokeExact();
        } catch (Throwable e) {
            // This fallback process is mainly used when running in environments where the
            // LambdaMetaFactory is not available (e.g., Native-Image in GraalVM). A normal process
            // can access private members even if setAccessible is not executed, but since it falls
            // back to the old reflection method, it is necessary to execute it.
            method.setAccessible(true);
            return method::invoke;
        }
    }

    /**
     * In Java today, there are three main types of reflection methods. The first is using old
     * methods and fields, the second is using MethodHandle, and the third is using
     * LambdaMetaFactory. The fastest of the three methods is the one using MethodHandle, but it has
     * the restriction that each MethodHandle must be stored in a static final field. Therefore,
     * LambdaMetaFactory, the second fastest method, is used here. However, this method is still as
     * fast as a direct call.
     * 
     * See benchmarks for verification against speed.
     */
    static WiseBiConsumer createSetter(Method method) {
        try {
            Lookup lookup = MethodHandles.privateLookupIn(method.getDeclaringClass(), MethodHandles.lookup());
            MethodHandle mh = lookup.unreflect(method);

            return (WiseBiConsumer) LambdaMetafactory
                    .metafactory(lookup, "ACCEPT", MethodType.methodType(WiseBiConsumer.class), mh.type()
                            .generic()
                            .changeReturnType(void.class), mh, mh.type().wrap().changeReturnType(void.class))
                    .dynamicInvoker()
                    .invokeExact();
        } catch (Throwable e) {
            // This fallback process is mainly used when running in environments where the
            // LambdaMetaFactory is not available (e.g., Native-Image in GraalVM). A normal process
            // can access private members even if setAccessible is not executed, but since it falls
            // back to the old reflection method, it is necessary to execute it.
            method.setAccessible(true);
            return method::invoke;
        }
    }
}