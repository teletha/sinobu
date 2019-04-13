/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss;

import java.lang.reflect.Constructor;

import kiss.model.Model;

/**
 * This lifestyle creates a new instance every time demanded. This is default lifestyle in Sinobu.
 * If you want to create new lifestyle in your application, you can extend this class and override
 * the method {@link #get()}.
 * 
 * @param <M> A {@link Manageable} class.
 * @see Singleton
 * @see ThreadSpecific
 */
public class Prototype<M> implements Lifestyle<M> {

    /** The cache for instantiator. */
    protected final Constructor<M> constructor;

    /**
     * Create a prototype instance.
     * 
     * @param modelClass A target class.
     */
    protected Prototype(Class<M> modelClass) {
        // find default constructor as instantiator
        constructor = Model.collectConstructors(modelClass)[0];

        // We can safely call the method 'newInstance()' because the generated class has
        // only one public constructor without arguments. But we should make this
        // instantiator accessible because it makes the creation speed faster.
        constructor.setAccessible(true);
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
    public M GET() throws Throwable {
        Class[] types = constructor.getParameterTypes();

        // constructor injection
        Object[] params = null;

        // We should use lazy initialization of parameter array to avoid that the constructor
        // without parameters doesn't create futile array instance.
        if (types.length != 0) {
            params = new Object[types.length];

            for (int i = 0; i < params.length; i++) {
                if (types[i] == Lifestyle.class) {
                    params[i] = I
                            .makeLifestyle((Class) Model.collectParameters(constructor.getGenericParameterTypes()[i], Lifestyle.class)[0]);
                } else if (types[i] == Class.class) {
                    params[i] = I.dependencies.get().peekLast();
                } else {
                    params[i] = I.make(types[i]);
                }
            }
        }
        // create new instance
        return constructor.newInstance(params);
    }
}
