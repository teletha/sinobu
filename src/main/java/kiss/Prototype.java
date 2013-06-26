/*
 * Copyright (C) 2012 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss;

import java.lang.reflect.Constructor;

import kiss.model.ClassUtil;

/**
 * <p>
 * This lifestyle creates a new instance every time demanded. This is default lifestyle in Sinobu.
 * If you want to create new lifestyle in your application, you can extend this class and override
 * the method {@link #resolve()}.
 * </p>
 * 
 * @param <M> A {@link Manageable} class.
 * @see Singleton
 * @see ThreadSpecific
 * @see Preference
 * @version 2011/11/04 0:11:32
 */
public class Prototype<M> implements Lifestyle<M> {

    /** The cache for instantiator. */
    protected final Constructor<M> instantiator;

    /** The cache for instantiator's parameters. */
    protected final Class[] params;

    /**
     * Create Prototype instance.
     * 
     * @param modelClass
     */
    protected Prototype(Class<M> modelClass) {
        // find default constructor as instantiator
        instantiator = ClassUtil.getMiniConstructor(modelClass);
        params = instantiator.getParameterTypes();

        // We can safely call the method 'newInstance()' because the generated class has
        // only one public constructor without arguments. But we shoud make this
        // instantiator accessible because it makes the creation speed faster.
        instantiator.setAccessible(true);
    }

    /**
     * <p>
     * The sub class of {@link Prototype} will override this method to resolve the instance
     * management. If you want to create a new instance, you can use this method with super call
     * like the following.
     * </p>
     * 
     * <pre>
     * Object newInstance = super.resolve();
     * </pre>
     * 
     * @see kiss.Lifestyle#resolve()
     */
    public M resolve() {
        // constructor injection
        Object[] params = null;

        // We should use lazy initialization of parameter array to avoid that the constructor
        // without parameters doesn't create futile array instance.
        if (this.params.length != 0) {
            params = new Object[this.params.length];

            for (int i = 0; i < params.length; i++) {
                if (this.params[i] == Lifestyle.class) {
                    params[i] = I.makeLifestyle(ClassUtil.getParameter(instantiator.getGenericParameterTypes()[i], Lifestyle.class)[0]);
                } else if (this.params[i] == Class.class) {
                    params[i] = I.dependencies.resolve().peekLast();
                } else {
                    params[i] = I.make(this.params[i]);
                }
            }
        }

        try {
            // create new instance
            return instantiator.newInstance(params);
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }
}
