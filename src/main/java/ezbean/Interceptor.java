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

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Field;

import ezbean.model.Model;
import ezbean.model.Property;

/**
 * <p>
 * This is property access interceptor.
 * </p>
 * 
 * @version 2011/11/19 19:03:23
 */
public class Interceptor<P extends Annotation> implements Extensible {

    /** The trusted loojup. */
    private static Lookup lookup;

    static {
        try {
            Field field = Lookup.class.getDeclaredField("IMPL_LOOKUP");
            field.setAccessible(true);

            lookup = (Lookup) field.get(null);
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }

    /** The actual object. */
    protected Object that;

    /** The associated annotation. */
    protected P annotation;

    /** The property. */
    private Property property;

    /** The parent interceptor to chain. */
    private Interceptor parent;

    /**
     * <p>
     * Intercept property access.
     * </p>
     * 
     * @param param A new value.
     */
    protected void invoke(Object param) {
        if (parent != null) {
            parent.invoke(param);
        } else {
            try {
                // Retrieve old value.
                Object old = lookup.unreflect(property.getAccessor(false)).invoke(that);

                // Apply new value.
                lookup.unreflectSpecial(property.getAccessor(true), that.getClass()).invoke(that, param);

                // Notify to all listeners.
                Enhancer.context(that).notify(that, property.name, old, param);
            } catch (Throwable e) {
                throw I.quiet(e);
            }
        }
    }

    /**
     * <p>
     * NOTE : This is internal method. A user of Ezbean <em>does not have to use</em> this method.
     * </p>
     * 
     * @param that A current processing object.
     * @param name A property name.
     * @param param A new value.
     */
    public static final void invoke(Object that, String name, Object param) {
        Property property = Model.load(that.getClass()).getProperty(name);
        Interceptor current = new Interceptor();
        current.property = property;
        current.that = that;

        Annotation[] annotations = property.getAccessor(true).getAnnotations();

        for (int i = annotations.length - 1; 0 <= i; --i) {
            Interceptor interceptor = I.find(Interceptor.class, annotations[i].annotationType());

            if (interceptor != null) {
                interceptor.that = that;
                interceptor.parent = current;
                interceptor.annotation = annotations[i];

                current = interceptor;
            }
        }

        // Invoke chain of interceptors.
        current.invoke(param);
    }
}
