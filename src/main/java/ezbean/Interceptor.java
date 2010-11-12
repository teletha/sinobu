/*
 * Copyright (C) 2010 Nameless Production Committee.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ezbean;

import java.lang.annotation.Annotation;

import ezbean.model.Model;

/**
 * @version 2010/11/12 9:28:46
 */
public class Interceptor<P extends Annotation> implements Extensible {

    /** The actual object. */
    protected Accessible that;

    /** The associated annotation. */
    protected P annotation;

    /** The property identifier. */
    private int id;

    /** The property name. */
    private String name;

    /** The parent interceptor to chain. */
    private Interceptor parent;

    protected void invoke(Object param) {
        if (parent != null) {
            parent.invoke(param);
        } else {
            // Retrieve old value.
            Object old = that.ezAccess(id, null);

            // Apply new value.
            that.ezAccess(id + 2, param);

            // Notify to all listeners.
            that.ezContext().notify(that, name, old, param);
        }
    }

    /**
     * <p>
     * NOTE : This is internal method. A user of Ezbean <em>does not have to use</em> this method.
     * </p>
     * 
     * @param that
     * @param id
     * @param name
     * @param param
     */
    public static final void invoke(Accessible that, int id, String name, Object param) {
        Interceptor current = new Interceptor();
        current.id = id;
        current.name = name;
        current.that = that;

        Annotation[] annotations = Model.load(that.getClass()).getProperty(name).getAccessor(true).getAnnotations();

        for (int i = annotations.length - 1; 0 <= i; --i) {
            Interceptor interceptor = I.find(Interceptor.class, annotations[i].annotationType());

            if (interceptor != null) {
                interceptor.annotation = annotations[i];
                interceptor.id = id;
                interceptor.name = name;
                interceptor.that = that;
                interceptor.parent = current;

                current = interceptor;
            }
        }

        // Invoke chain of interceptors.
        current.invoke(param);
    }
}
