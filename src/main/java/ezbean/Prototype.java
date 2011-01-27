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
package ezbean;

import java.lang.reflect.Constructor;

import ezbean.model.ClassUtil;


/**
 * <p>
 * This lifestyle creates a new instance every time demanded. This is default lifestyle in Ezbean.
 * If you want to create new lifestyle in your application, you can extend this class and override
 * the method {@link #resolve()}.
 * </p>
 * 
 * @see Singleton
 * @see ThreadSpecific
 * @version 2010/02/19 18:06:21
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
    public Prototype(Class<M> modelClass) {
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
     * @see ezbean.Lifestyle#resolve()
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
