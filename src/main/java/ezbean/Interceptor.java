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
import java.lang.invoke.MethodHandle;
import java.util.List;

/**
 * <p>
 * This is generic method interceptor.
 * </p>
 * 
 * @version 2011/12/11 19:57:33
 */
public class Interceptor<P extends Annotation> implements Extensible {

    /** The actual object. */
    protected Object that;

    /** The associated annotation. */
    protected P annotation;

    /** The delegation method. */
    private MethodHandle handle;

    /** The parent interceptor to chain. */
    private Interceptor parent;

    /**
     * Hide constructor.
     */
    protected Interceptor() {
    }

    /**
     * <p>
     * Intercept property access.
     * </p>
     * 
     * @param param A new value.
     */
    protected Object invoke(Object... param) {
        if (parent != null) {
            return parent.invoke(param);
        } else {
            try {
                return handle.bindTo(that).invokeWithArguments(param);
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
     * @param handle A delegation method.
     * @param that A current processing object.
     * @param parames A current method parameters.
     * @param annotations A interceptable annotation list.
     */
    public static Object invoke(MethodHandle handle, Object that, Object[] params, List<Annotation> annotations) {
        Interceptor current = new Interceptor();
        current.handle = handle;
        current.that = that;

        for (int i = annotations.size() - 1; 0 <= i; --i) {
            Interceptor interceptor = I.find(Interceptor.class, annotations.get(i).annotationType());

            if (interceptor != null) {
                interceptor.that = that;
                interceptor.parent = current;
                interceptor.annotation = annotations.get(i);

                current = interceptor;
            }
        }

        // Invoke chain of interceptors.
        return current.invoke(params);
    }
}
