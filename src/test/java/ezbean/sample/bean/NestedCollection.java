/**
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
package ezbean.sample.bean;

import java.util.List;
import java.util.Map;

/**
 * DOCUMENT.
 * 
 * @version 2008/07/30 10:16:56
 */
public class NestedCollection {

    /** The complex property. */
    private Map<String, List<Person>> nest;

    /**
     * Get the nest property of this {@link NestedCollection}.
     * 
     * @return The nest property.
     */
    public Map<String, List<Person>> getNest() {
        return nest;
    }

    /**
     * Set the nest property of this {@link NestedCollection}.
     * 
     * @param nest The nest value to set.
     */
    public void setNest(Map<String, List<Person>> nest) {
        this.nest = nest;
    }
}
