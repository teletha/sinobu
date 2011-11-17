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

import java.util.Locale;

import ezbean.model.Codec;

/**
 * <p>
 * This is dual-purpose implementation class. One is codec for {@link Class}. The other is lifestyle
 * for {@link Locale}.
 * </p>
 * <p>
 * This class locates ezbean package to access {@link Modules}'s package-private field.
 * </p>
 * 
 * @version 2010/01/16 19:33:09
 */
class ClassCodec extends Codec<Class> implements Lifestyle<Locale> {

    /**
     * @see ezbean.model.Codec#decode(java.lang.String)
     */
    public Class decode(String value) {
        for (Module module : I.make(Modules.class).modules) {
            try {
                return module.loadClass(value);
            } catch (ClassNotFoundException e) {
                // continue
            }
        }
        throw new IllegalArgumentException(value);
    }

    /**
     * @see ezbean.model.Codec#encode(java.lang.Object)
     */
    public String encode(Class value) {
        return value.getName();
    }

    /**
     * @see ezbean.Lifestyle#resolve()
     */
    public Locale resolve() {
        return Locale.getDefault();
    }
}
