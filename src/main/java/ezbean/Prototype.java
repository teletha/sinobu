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
package ezbean;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;

import javax.annotation.Resource;

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
 * @version 2010/01/05 22:45:49
 */
public class Prototype<M> implements Lifestyle<M> {

    /** The cache for instantiator. */
    protected final Constructor<M> instantiator;

    /** The cache for instantiator's parameters. */
    protected final Class[] params;

    /** The cache for dependency fields. */
    protected final Field[] fields;

    /**
     * Create Prototype instance.
     * 
     * @param modelClass
     */
    public Prototype(Class<M> modelClass) {
        try {
            // find default constructor as instantiator
            instantiator = ClassUtil.getMiniConstructor(modelClass);
            params = instantiator.getParameterTypes();

            // We can safely call the method 'newInstance()' because the generated class has
            // only one public constructor without arguments. But we shoud make this
            // instantiator accessible because it makes the creation speed faster.
            instantiator.setAccessible(true);

            // find all dependency fields
            ArrayList fields = new ArrayList();

            for (Field field : modelClass.getDeclaredFields()) {
                if (field.isAnnotationPresent(Resource.class)) {
                    fields.add(field);

                    // We can safely access to the field by the following process. And we should
                    // make this field accessible because it makes the access speed faster.
                    field.setAccessible(true);
                }
            }

            // array iteration is very faster than list iteration
            this.fields = (Field[]) fields.toArray(new Field[fields.size()]);
        } catch (Exception e) {
            // If this exception will be thrown, it is bug of this program. So we must rethrow the
            // wrapped error in here.
            throw new Error(e);
        }
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
        try {
            // constructor injection
            Object[] params = null;

            if (this.params.length != 0) {
                params = new Object[this.params.length];

                for (int i = 0; i < params.length; i++) {
                    params[i] = I.make(this.params[i]);
                }
            }

            // create new instance
            M instance = instantiator.newInstance(params);

            // field injection
            for (Field field : fields) {
                field.set(instance, I.make(field.getType()));
            }

            // API definition
            return instance;
        } catch (Exception e) {
            // We must throw the checked exception quietly and pass the original exception instead
            // of wrapped exception.
            throw I.quiet(e);
        }
    }
}
