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

/**
 * <p>
 * This lifestyle guarantees that only one instance of a specific class exists per thread.
 * </p>
 * <p>
 * This class can be used instead of {@link ThreadLocal} class like the following:
 * </p>
 * 
 * <pre>
 * private static final ThreadSpecific&lt;SimpleDateFormat&gt; formatter = new ThreadSpecific(SimpleDateFormat.class);
 * </pre>
 * <p>
 * This is equivalent to the next code.
 * </p>
 * 
 * <pre>
 * private static final ThreadLocal&lt;SimpleDateFormat&gt; formatter = new ThreadLocal&lt;SimpleDateFormat&gt;() {
 *     &#064;Override
 *     protected SimpleDateFormat initialValue() {
 *         return new SimpleDateFormat();
 *     }
 * };
 * </pre>
 * 
 * @see Prototype
 * @see Singleton
 * @version 2007/06/13 14:15:57
 */
public class ThreadSpecific<M> extends Prototype<M> {

    /** The actual storage. */
    protected final ThreadLocal<M> local = new ThreadLocal();

    /**
     * Create ThreadSpecific instance.
     * 
     * @param modelClass
     */
    public ThreadSpecific(Class<M> modelClass) {
        super(modelClass);
    }

    /**
     * @see ezbean.Lifestyle#resolve()
     */
    @Override
    public M resolve() {
        M object = local.get();

        if (object == null) {
            object = super.resolve();
            local.set(object);
        }
        return object;
    }
}
