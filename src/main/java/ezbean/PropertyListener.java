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
 * DOCUMENT.
 * 
 * @version 2008/06/05 4:54:16
 */
public interface PropertyListener<M> {

    /**
     * This method is invoked whenever a bean's property has changed
     * 
     * @param bean An event source.
     * @param propertyName A name of chenged property.
     * @param oldValue The old value.
     * @param newValue The new value.
     */
    void change(Object bean, String propertyName, M oldValue, M newValue);
}
