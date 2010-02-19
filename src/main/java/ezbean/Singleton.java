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
 * This lifestyle guarantees that only one instance of the specific class exists in Ezbean.
 * </p>
 * 
 * @see Prototype
 * @see ThreadSpecific
 * @version 2010/02/19 17:49:22
 */
public class Singleton<M> extends Prototype<M> {

    /** The singleton instance. */
    protected final M instance;

    /**
     * Create Singleton instance.
     * 
     * @param modelClass
     */
    public Singleton(Class<M> modelClass) {
        super(modelClass);

        instance = super.resolve();
    }

    /**
     * @see ezbean.Lifestyle#resolve()
     */
    @Override
    public M resolve() {
        return instance;
    }
}
