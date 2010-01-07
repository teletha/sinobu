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
package ezbean.sample.bean;

import java.util.Map;

/**
 * DOCUMENT.
 * 
 * @version 2007/06/01 10:44:18
 */
public class NonStringKeyMapBean {

    private Map<Integer, Class> integerKey;

    /**
     * Get the integerKey property of this {@link NonStringKeyMapBean}.
     * 
     * @return The integerKey property.
     */
    public Map<Integer, Class> getIntegerKey() {
        return integerKey;
    }

    /**
     * Set the integerKey property of this {@link NonStringKeyMapBean}.
     * 
     * @param integerKey The integerKey value to set.
     */
    public void setIntegerKey(Map<Integer, Class> integerKey) {
        this.integerKey = integerKey;
    }
}
