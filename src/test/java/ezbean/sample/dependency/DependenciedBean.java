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
package ezbean.sample.dependency;

import java.io.Serializable;

/**
 * @version 2009/07/01 18:05:59
 */
@SuppressWarnings("serial")
public class DependenciedBean implements Serializable {

    private final NoDependencySingleton singleton;

    private String name;

    /**
     * @param singleton
     */
    public DependenciedBean(NoDependencySingleton singleton) {
        this.singleton = singleton;
    }

    /**
     * Get the name property of this {@link DependenciedBean}.
     * 
     * @return The name property.
     */
    public String getName() {
        return name + singleton;
    }

    /**
     * Set the name property of this {@link DependenciedBean}.
     * 
     * @param name The name value to set.
     */
    public void setName(String name) {
        this.name = name;
    }

}
