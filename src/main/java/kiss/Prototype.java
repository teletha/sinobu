/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss;

import java.lang.reflect.Constructor;
import java.util.function.Supplier;

import kiss.model.Model;

/**
 * <p>
 * This lifestyle creates a new instance every time demanded. This is default lifestyle in Sinobu.
 * If you want to create new lifestyle in your application, you can extend this class and override
 * the method {@link #get()}.
 * </p>
 * 
 * @param <M> A {@link Manageable} class.
 * @see Singleton
 * @see ThreadSpecific
 * @version 2017/04/21 21:03:39
 */
public class Prototype<M> implements Lifestyle<M> {

    /** The cache for instantiator. */
    protected final Supplier<M> instantiator;

    /**
     * Create Prototype instance.
     * 
     * @param modelClass A target class.
     */
    protected Prototype(Class<M> modelClass) {
        // find default constructor as instantiator
        Constructor<M>[] constructors = Model.collectConstructors(modelClass);

        if (constructors.length == 0) {
            throw new Error(modelClass + " is invalid model.");
        }
        Constructor<M> constructor = constructors[0];

        Class[] types = constructor.getParameterTypes();

        // We can safely call the method 'newInstance()' because the generated class has
        // only one public constructor without arguments. But we should make this
        // instantiator accessible because it makes the creation speed faster.
        constructor.setAccessible(true);

        this.instantiator = () -> {
            // constructor injection
            Object[] params = null;

            // We should use lazy initialization of parameter array to avoid that the constructor
            // without parameters doesn't create futile array instance.
            if (types.length != 0) {
                params = new Object[types.length];

                for (int i = 0; i < params.length; i++) {
                    if (types[i] == Lifestyle.class) {
                        params[i] = I.makeLifestyle((Class) Model
                                .collectParameters(constructor.getGenericParameterTypes()[i], Lifestyle.class)[0]);
                    } else if (types[i] == Class.class) {
                        params[i] = I.dependencies.get().peekLast();
                    } else {
                        params[i] = I.make(types[i]);
                    }
                }
            }

            try {
                // create new instance
                return constructor.newInstance(params);
            } catch (Exception e) {
                throw I.quiet(e);
            }
        };
    }

    /**
     * Create Prototype instance.
     * 
     * @param inistantiator A instantiator.
     */
    protected Prototype(Supplier<M> inistantiator) {
        this.instantiator = inistantiator;
    }

    /**
     * <p>
     * The sub class of {@link Prototype} will override this method to resolve the instance
     * management. If you want to create a new instance, you can use this method with super call
     * like the following.
     * </p>
     * <pre>
     * Object newInstance = super.get();
     * </pre>
     * 
     * @see kiss.Lifestyle#get()
     */
    @Override
    public M get() {
        return instantiator.get();
    }
}
