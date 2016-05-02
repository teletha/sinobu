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

import static java.lang.reflect.Modifier.*;

import java.beans.Introspector;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

import javafx.beans.value.WritableValue;

import kiss.Decoder;
import kiss.Encoder;
import kiss.I;
import kiss.Variable;
import kiss.Ⅲ;

/**
 * <p>
 * <code>Model</code> is the representation of {@link Class} in Sinobu. This class specializes in
 * property. All Models can be classified into three types.
 * </p>
 * <dl>
 * <dt>Attribute Model</dt>
 * <dd>This is a model which has no property at all. You can check whether a model is attribute or
 * not by using that {@link #getCodec()} method returns <code>null</code> or not. Normaly,
 * {@link Model} class represents this model type.</dd>
 * <dt>Collection Model</dt>
 * <dd>This is a model whose all.properties indicate same type of Model (e.g. List, Map, Attribute).
 * You can check whether a model is collection or not by using {@link #isCollection()} method.</dd>
 * <dt>Basic Model</dt>
 * <dd>This is a model which has some propertis and indicates various type of Model. All models
 * which is neither Immutable nor Collection are Basic.</dd>
 * </dl>
 * 
 * @version 2014/03/11 13:52:21
 */
@SuppressWarnings("unchecked")
public class Model<M> {

    /** The model repository. */
    static final Variable<Model> models = new Variable();

    /** The {@link Class} which is represented by this {@link Model}. */
    public final Class<M> type;

    /** The human readable identifier of this object model. */
    public final String name;

    /** The unmodifiable properties list of this object model. */
    public List<Property> properties = Collections.EMPTY_LIST;

    /** The built-in codec. */
    private Decoder decoder;

    /** The built-in codec. */
    private Encoder encoder = String::valueOf;;

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
        this.name = type.getSimpleName();
    }

    void init() {
        // To avoid StackOverFlowException caused by circular reference of Model, you must define
        // this model in here.
        models.set(type, this);

        try {
            // search from built-in codecs
            if (type.isEnum()) {
                decoder = value -> Enum.valueOf((Class<Enum>) type, value);
                encoder = value -> ((Enum) value).name();
            } else {
                switch (type.getName().hashCode()) {
                case 64711720: // boolean
                case 344809556: // java.lang.Boolean
                case 104431: // int
                case -2056817302: // java.lang.Integer
                case 3327612: // long
                case 398795216: // java.lang.Long
                case 97526364: // float
                case -527879800: // java.lang.Float
                case -1325958191: // double
                case 761287205: // java.lang.Double
                case 3039496: // byte
                case 398507100: // java.lang.Byte
                case 109413500: // short
                case -515992664: // java.lang.Short
                case 1195259493: // java.lang.String
                case -1555282570: // java.lang.StringBuilder
                case 1196660485: // java.lang.StringBuffer
                case 2130072984: // java.io.File
                case 2050244018: // java.net.URL
                case 2050244015: // java.net.URI
                case -989675752: // java.math.BigInteger
                case -1405464277: // java.math.BigDecimal
                    // constructer pattern
                    Constructor<?> constructor = ClassUtil.wrap(type).getConstructor(String.class);

                    decoder = value -> {
                        try {
                            return constructor.newInstance(value);
                        } catch (Exception e) {
                            throw I.quiet(e);
                        }
                    };
                    break;

                case 3052374: // char
                case 155276373: // java.lang.Character
                    decoder = value -> value.charAt(0);
                    break;

                case -1246033885: // java.time.LocalTime
                case -1246518012: // java.time.LocalDate
                case -1179039247: // java.time.LocalDateTime
                case -682591005: // java.time.OffsetDateTime
                case -1917484011: // java.time.OffsetTime
                case 1505337278: // java.time.ZonedDateTime
                case 649475153: // java.time.MonthDay
                case -537503858: // java.time.YearMonth
                case -1062742510: // java.time.Year
                case -1023498007: // java.time.Duration
                case 649503318: // java.time.Period
                case 1296075756: // java.time.Instant
                    // parse method pattern
                    Method method = type.getMethod("parse", CharSequence.class);

                    decoder = value -> {
                        try {
                            return method.invoke(null, value);
                        } catch (Exception e) {
                            throw I.quiet(e);
                        }
                    };
                    break;

                case -1165211622: // java.util.Locale
                    decoder = Locale::forLanguageTag;
                    break;

                case 1464606545: // java.nio.file.Path
                    decoder = I::locate;
                    break;

                // case -89228377: // java.nio.file.attribute.FileTime
                // decoder = value -> FileTime.fromMillis(Long.valueOf(value));
                // encoder = (Encoder<FileTime>) value -> String.valueOf(value.toMillis());
                // break;

                default:
                    decoder = I.find(Decoder.class, type);
                    encoder = I.find(Encoder.class, type);
                    break;
                }
            }

            // examine all methods without private, final, static or native
            Map<String, Method[]> candidates = new HashMap();

            for (Class clazz : ClassUtil.getTypes(type)) {
                for (Method method : clazz.getDeclaredMethods()) {
                    // exclude the method which modifier is final, static, private or native
                    if (((STATIC | PRIVATE | NATIVE) & method.getModifiers()) == 0) {
                        // exclude the method which is created by compiler
                        if (!method.isBridge() && !method.isSynthetic()) {
                            // if (method.getAnnotations().length != 0) {
                            // intercepts.add(method);
                            // }

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
                                    name = Introspector.decapitalize(name.substring(prefix.length()));

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

            Lookup look = MethodHandles.lookup();

            // build valid properties
            ArrayList properties = new ArrayList(); // don't use type parameter to reduce footprint

            for (Entry<String, Method[]> entry : candidates.entrySet()) {
                Method[] methods = entry.getValue();

                if (methods[0] != null && methods[1] != null && ((methods[0].getModifiers() | methods[1].getModifiers()) & FINAL) == 0) {
                    // create model for the property
                    try {
                        Model model = of(methods[0].getGenericReturnType(), type);

                        if (of(methods[1].getGenericParameterTypes()[0], type).type.isAssignableFrom(model.type)) {
                            methods[0].setAccessible(true);
                            methods[1].setAccessible(true);

                            // this property is valid
                            Property property = new Property(model, entry.getKey(), methods);
                            property.accessors = new MethodHandle[] {look.unreflect(methods[0]), look.unreflect(methods[1])};

                            // register it
                            properties.add(property);
                        }
                    } catch (Exception e) {
                        throw I.quiet(e);
                    }
                }
            }

            // Search field properties.
            for (Field field : type.getFields()) {
                // exclude the field which modifier is static, private or native
                int modifier = field.getModifiers();

                if (((STATIC | PRIVATE | NATIVE) & modifier) == 0) {
                    Model fieldModel = of(field.getGenericType(), type);

                    if (WritableValue.class.isAssignableFrom(fieldModel.type)) {
                        // property
                        Property property = new Property(
                                of(fieldModel.type.getMethod("getValue").getGenericReturnType(), field.getGenericType()), field.getName());
                        property.accessors = new MethodHandle[] {look.unreflectGetter(field), null};
                        property.type = 2;

                        // register it
                        properties.add(property);
                    } else if ((FINAL & modifier) == 0) {
                        // field
                        field.setAccessible(true);

                        Property property = new Property(fieldModel, field.getName(), field);
                        property.accessors = new MethodHandle[] {look.unreflectGetter(field), look.unreflectSetter(field)};
                        property.type = 1;

                        // register it
                        properties.add(property);
                    }
                }
            }

            // trim and sort property list
            properties.trimToSize();
            Collections.sort(properties);

            // exposed property list must be unmodifiable
            this.properties = Collections.unmodifiableList(properties);
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }

    /**
     * Check whether this object model is Collection Model or not.
     * 
     * @return A result.
     */
    public boolean isCollection() {
        return false;
    }

    /**
     * Find the property which has the specified name in this object model. If the suitable property
     * is not found, <code>null</code> is returned.
     * 
     * @param propertyName A name of property.
     * @return A suitable property or <code>null</code>.
     */
    public Property property(String propertyName) {
        // check whether this model is attribute or not.
        if (decoder() == null) {
            for (Property property : properties) {
                if (property.name.equals(propertyName)) {
                    return property;
                }
            }
        }

        // API definition
        return null;
    }

    /**
     * <p>
     * Retrieve {@link Decoder} for this model.
     * </p>
     * 
     * @return An associated {@link Decoder}.
     */
    public Decoder<M> decoder() {
        return decoder != null ? decoder : I.find(Decoder.class, type);
    }

    /**
     * <p>
     * Retrieve {@link Encoder} for this model.
     * </p>
     * 
     * @return An associated {@link Encoder}.
     */
    public Encoder<M> encoder() {
        return encoder != null ? encoder : I.find(Encoder.class, type);
    }

    /**
     * Returns the value of the given property in the given object.
     * 
     * @param object A object as source. This value must not be <code>null</code>,
     * @param property A property. This value must not be <code>null</code>,
     * @return A resolved property value. This value may be <code>null</code>.
     * @throws IllegalArgumentException If the given object can't resolve the given property.
     */
    public <V> V get(M object, Property<M, V> property) {
        if (property == null) {
            return null;
        }
        return property.get(object);
    }

    /**
     * Change the given property in the given object to the given new property value.
     * 
     * @param object A object as source. This value must not be <code>null</code>,
     * @param property A property. This value must not be <code>null</code>,
     * @param propertyValue A new property value that you want to set. This value accepts
     *            <code>null</code>.
     * @throws IllegalArgumentException If the given object can't resolve the given property.
     */
    public <V> void set(M object, Property<M, V> property, V propertyValue) {
        if (property != null) {
            property.set(object, propertyValue);
        }
    }

    /**
     * Iterate over all properties in the given object and propagate the property and it's value to
     * the given {@link PropertyWalker}.
     * 
     * @param object A object as source. This value must not be <code>null</code>,
     * @param walker A property iterator. This value accepts <code>null</code>.
     * @see PropertyWalker#walk(Model, Property, Object)
     */
    public void walk(M object, Consumer<Ⅲ<Model<M>, Property, Object>> walker) {
        // check whether this model is attribute or not.
        if (walker != null && decoder() == null) {
            for (Property property : properties) {
                Object value = get(object, property);

                if (value != null) walker.accept(I.pair(this, property, value));
            }
        }
    }

    /**
     * <p>
     * Utility method to retrieve the cached model. If the model of the given class is not found,
     * {@link IllegalArgumentException} will be thrown.
     * </p>
     * <p>
     * If the given model has no cached information, it will be created automatically. This
     * operation is thread-safe.
     * </p>
     * <p>
     * Note : All classes do not necessary have each information. Some classes might share same
     * {@link Model} object. (e.g. AutoGenerated Class)
     * </p>
     * 
     * @param modelClass A model class.
     * @return The information about the given model class.
     * @throws NullPointerException If the given model class is null.
     * @throws IllegalArgumentException If the given model class is not found.
     */
    public static <M> Model<M> of(M modelType) {
        return of((Class<M>) modelType.getClass());
    }

    /**
     * <p>
     * Utility method to retrieve the cached model. If the model of the given class is not found,
     * {@link IllegalArgumentException} will be thrown.
     * </p>
     * <p>
     * If the given model has no cached information, it will be created automatically. This
     * operation is thread-safe.
     * </p>
     * <p>
     * Note : All classes do not necessary have each information. Some classes might share same
     * {@link Model} object. (e.g. AutoGenerated Class)
     * </p>
     * 
     * @param modelClass A model class.
     * @return The information about the given model class.
     * @throws NullPointerException If the given model class is null.
     * @throws IllegalArgumentException If the given model class is not found.
     */
    public static <M> Model<M> of(Class<? super M> modelClass) {
        // check whether the specified model class is enhanced or not
        if (modelClass.isSynthetic()) {
            modelClass = modelClass.getSuperclass();
        }

        // check cache
        Model model = models.get(modelClass);

        if (model == null) {
            // create new model
            if (List.class.isAssignableFrom(modelClass)) {
                model = new ListModel(modelClass, ClassUtil.getParameter(modelClass, List.class), List.class);
            } else if (Map.class.isAssignableFrom(modelClass)) {
                model = new MapModel(modelClass, ClassUtil.getParameter(modelClass, Map.class), Map.class);
            } else {
                model = new Model(modelClass);
                model.init();
            }
        }

        // API definition
        return model;
    }

    /**
     * <p>
     * Utility method to retrieve the cached model. If the model of the given type is not found,
     * {@link IllegalArgumentException} will be thrown.
     * </p>
     * 
     * @param type A target type to analyze.
     * @param base A declaration class.
     * @return A cached model information.
     * @throws IllegalArgumentException If the given model type is null.
     * @see TypeVariable
     */
    static Model of(Type type, Type base) {
        // class
        if (type instanceof Class) {
            return of((Class) type);
        }

        // parameterized type
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
            return of(clazz);
        }

        // wildcard type
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

        // variable type
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
                    if (base == variable.getGenericDeclaration()) {
                        return of(variable.getBounds()[0], base);
                    } else {
                        return of(ClassUtil.getParameter(base, variable.getGenericDeclaration())[i], base);
                    }
                }
            }
        }

        // generic array type
        if (type instanceof GenericArrayType) {
            return of(((GenericArrayType) type).getGenericComponentType(), base);
        }

        // If this error will be thrown, it is bug of this program. Please send a bug report to us.
        throw new Error();
    }
}
