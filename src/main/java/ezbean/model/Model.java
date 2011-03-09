/*
 * Copyright (C) 2011 Nameless Production Committee.
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

import static java.lang.reflect.Modifier.*;

import java.beans.Introspector;
import java.beans.Transient;
import java.io.File;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import ezbean.Accessible;
import ezbean.I;
import ezbean.Modules;
import ezbean.io.FilePath;

/**
 * <p>
 * <code>Model</code> is the representation of {@link Class} in Ezbean. This class specializes in
 * property. All Models can be classified into three types.
 * </p>
 * <dl>
 * <dt>Attribute Model</dt>
 * <dd>This is a model which has no property at all. You can check whether a model is attribute or
 * not by using {@link #isAttribute()} method. Normaly, {@link Model} class represents this model
 * type.</dd>
 * <dt>Collection Model</dt>
 * <dd>This is a model whose all.properties indicate same type of Model (e.g. List, Map, Attribute).
 * You can check whether a model is collection or not by using {@link #isCollection()} method.</dd>
 * <dt>Basic Model</dt>
 * <dd>This is a model which has some propertis and indicates various type of Model. All models
 * which is neither Immutable nor Collection are Basic.</dd>
 * </dl>
 * 
 * @version 2011/03/07 22:16:00
 */
public class Model<M> {

    /** The model repository. */
    static final Map<Class, Model> models = Modules.aware(new ConcurrentHashMap());

    /** The repository of built-in codecs. */
    private static final ArrayList<Class> codecs = new ArrayList();

    // initialize
    static {
        // primitives and wrappers
        for (int i = 0; i < 8; i++) {
            codecs.add(ClassUtil.PRIMITIVES[i]);
            codecs.add(ClassUtil.WRAPPERS[i]);
        }

        // lang
        codecs.add(String.class);

        // util
        codecs.add(Locale.class);

        // net
        codecs.add(URL.class);
        codecs.add(URI.class);

        // arbitrary-precision numeric numbers
        codecs.add(BigInteger.class);
        codecs.add(BigDecimal.class);

        // io, nio
        codecs.add(File.class);
        codecs.add(FilePath.class);
    }

    /** The {@link Class} which is represented by this {@link Model}. */
    public final Class<M> type;

    /** The human readable identifier of this object model. */
    public final String name;

    /** The unmodifiable properties list of this object model. */
    public final List<Property> properties;

    /** The built-in codec. */
    private Codec codec = null;

    // public final List<Method> intercepts = new ArrayList();

    /**
     * Create Model instance.
     * 
     * @param type A target class to analyze as model.
     * @throws NullPointerException If the specified model class is <code>null</code>.
     */
    protected Model(Class<M> type) {
        // Skip null check because this method can throw NullPointerException.
        // if (model == null) throw new NullPointerException("Model class shouldn't be null.");

        this.type = type;
        this.name = type.getSimpleName();

        // To avoid StackOverFlowException caused by circular reference of Model, you must define
        // this model in here.
        models.put(type, this);

        // search from built-in codecs
        if (codecs.contains(type) || type.isEnum()) codec = new Codec(type);

        // examine all methods without private, final, static or native
        Map<String, Method[]> candidates = new HashMap();

        for (Class clazz : ClassUtil.getTypes(type)) {
            for (Method method : clazz.getDeclaredMethods()) {
                // exclude the method which modifier is final, static, private or native
                if (((STATIC | PRIVATE | NATIVE) & method.getModifiers()) != 0) {
                    continue;
                }

                // exclude the method which is created by compiler
                if (method.isBridge() || method.isSynthetic()) {
                    continue;
                }

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
                if (name.length() <= prefix.length() || !name.startsWith(prefix) || Character.isLowerCase(name.charAt(prefix.length()))) {
                    continue;
                }

                // exclude the method (by parameter signature)
                if (method.getGenericParameterTypes().length != length) {
                    continue;
                }

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

        // build valid properties
        ArrayList properties = new ArrayList(); // don't use type parameter to reduce footprint

        Iterator<Entry<String, Method[]>> iterator = candidates.entrySet().iterator();

        while (iterator.hasNext()) {
            Entry<String, Method[]> entry = iterator.next();
            Method[] methods = entry.getValue();

            if (methods[0] != null && methods[1] != null && ((methods[0].getModifiers() | methods[1].getModifiers()) & FINAL) == 0) {
                // create model for the property
                try {
                    Model model = load(methods[0].getGenericReturnType(), type);

                    if (model.type == load(methods[1].getGenericParameterTypes()[0], type).type) {
                        // this property is valid
                        Property property = new Property(model, entry.getKey());
                        property.accessors = methods;
                        property.type = methods[0].getAnnotation(Transient.class) != null || methods[1].getAnnotation(Transient.class) != null;

                        // register it
                        properties.add(property);
                    }
                } catch (SecurityException e) {
                    // for GAE environment
                }
            }
        }

        // trim and sort property list
        properties.trimToSize();
        Collections.sort(properties);

        // reorder property index
        for (int i = 0; i < properties.size(); i++) {
            ((Property) properties.get(i)).id = i * 3;
        }

        // exposed property list must be unmodifiable
        this.properties = Collections.unmodifiableList(properties);
    }

    /**
     * Find the property which has the specified name in this object model. If the suitable property
     * is not found, <code>null</code> is returned.
     * 
     * @param propertyIName A name of property.
     * @return A suitable property or <code>null</code>.
     */
    public Property getProperty(String propertyIName) {
        // check whether this model is attribute or not.
        if (getCodec() == null) {
            for (Property property : properties) {
                if (property.name.equals(propertyIName)) {
                    return property;
                }
            }
        }

        // API definition
        return null;
    }

    /**
     * <p>
     * Find the {@link Codec} for this model.
     * </p>
     * 
     * @return A suitable codec or <code>null</code>.
     */
    public Codec<M> getCodec() {
        return codec != null ? codec : I.find(Codec.class, type);
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
     * Returns the value of the given property in the given object.
     * 
     * @param object A object as source. This value must not be <code>null</code>,
     * @param property A property. This value must not be <code>null</code>,
     * @return A resolved property value. This value may be <code>null</code>.
     * @throws IllegalArgumentException If the given object can't resolve the given property.
     */
    public Object get(M object, Property property) {
        if (object instanceof Accessible) {
            return ((Accessible) object).access(property.id, null);
        }

        try {
            return property.accessors[0].invoke(object);
        } catch (Exception e) {
            throw I.quiet(e);
        }
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
    public void set(M object, Property property, Object propertyValue) {
        if (object instanceof Accessible) {
            ((Accessible) object).access(property.id + 1, propertyValue);
        } else {
            try {
                property.accessors[1].invoke(object, propertyValue);
            } catch (Exception e) {
                throw I.quiet(e);
            }
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
    public void walk(M object, PropertyWalker walker) {
        // check whether this model is attribute or not.
        if (walker != null && getCodec() == null) {
            for (Property property : properties) {
                Object value = get(object, property);

                if (value != null) walker.walk(this, property, value);
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
     * @param <M> A type of model class.
     * @param modelClass A model class.
     * @return The information about the given model class.
     * @throws NullPointerException If the given model class is null.
     * @throws IllegalArgumentException If the given model class is not found.
     */
    public static <M> Model<M> load(Class<M> modelClass) {
        // check whether the specified model class is enhanced or not
        if (Accessible.class.isAssignableFrom(modelClass)) {
            modelClass = (Class<M>) modelClass.getSuperclass();
        } else if (Path.class.isAssignableFrom(modelClass)) {
            modelClass = (Class<M>) Path.class;
        }

        // check cache
        Model<M> model = models.get(modelClass);

        if (model == null) {
            // create new model
            model = new Model(modelClass);

            // store it
            models.put(modelClass, model);
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
     */
    static Model load(Type type, Type base) {
        // class
        if (type instanceof Class) {
            return load((Class) type);
        }

        // parameterized type
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterized = (ParameterizedType) type;
            Class clazz = (Class) parameterized.getRawType();

            // ListModel
            if (List.class.isAssignableFrom(clazz)) {
                return new ListModel(parameterized, base);
            }

            // MapModel
            if (Map.class.isAssignableFrom(clazz)) {
                return new MapModel(parameterized, base);
            }

            // ClassModel
            return load(clazz);
        }

        // wildcard type
        if (type instanceof WildcardType) {
            WildcardType wildcard = (WildcardType) type;

            Type[] types = wildcard.getLowerBounds();

            if (types.length != 0) {
                return load(types[0], base);
            }

            types = wildcard.getUpperBounds();

            if (types.length != 0) {
                return load(types[0], base);
            }
        }

        // variable type
        if (type instanceof TypeVariable) {
            TypeVariable variable = (TypeVariable) type;
            TypeVariable[] variables = variable.getGenericDeclaration().getTypeParameters();

            for (int i = 0; i < variables.length; i++) {
                if (variable == variables[i]) {
                    if (base == variable.getGenericDeclaration()) {
                        return load(variable.getBounds()[0], base);
                    } else {
                        return load(ClassUtil.getParameter(base, (Class) variable.getGenericDeclaration())[i], base);
                    }
                }
            }
        }

        // generic array type
        if (type instanceof GenericArrayType) {
            return load(((GenericArrayType) type).getGenericComponentType(), base);
        }

        // If this error will be thrown, it is bug of this program. Please send a bug report to us.
        throw new Error();
    }
}
