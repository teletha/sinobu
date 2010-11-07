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
package ezbean.module;

import java.lang.annotation.Annotation;

import ezbean.Extensible;
import ezbean.I;
import ezbean.module.InterceptorTest.SuperCallable;

/**
 * @version 2009/12/30 0:21:52
 */
public class Interceptor<P extends Annotation> implements Extensible {

    /** The method identifier of original. */
    int id;

    /** The parent interceptor to chain. */
    private Interceptor parent;

    /** The annotation. */
    private P annotation;

    /**
     * <p>
     * Invoke
     * </p>
     * 
     * @return
     */
    protected Object invoke(Object that, Object[] params, P annotation) {
        return parent != null ? parent.invoke(that, params, parent.annotation)
                : ((SuperCallable) that).ezCall(id, params);
    }

    /**
     * <p>
     * NOTE : This is internal method. A user of Ezbean <em>does not have to use</em> this method.
     * </p>
     * 
     * @param that
     * @param id
     * @param params
     */
    public static Object invoke(Object that, int id, Object[] params) {
        Interceptor current = new Interceptor();
        current.id = id;

        Annotation[] annotations = InterceptorTest.InterceptorEnhancer.cache.get(that.getClass().getSuperclass())[id];

        for (int i = annotations.length - 1; 0 <= i; i--) {
            Interceptor interceptor = I.find(Interceptor.class, annotations[i].annotationType());

            if (interceptor != null) {
                interceptor.parent = current;
                interceptor.annotation = annotations[i];

                current = interceptor;
            }
        }

        return current.invoke(that, params, current.annotation);
    }
}
