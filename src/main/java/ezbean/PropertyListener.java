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

/**
 * @version 2008/06/05 4:54:16
 */
public interface PropertyListener<M> {

    /**
     * <p>
     * This method is invoked whenever a bean's property has changed.
     * </p>
     * 
     * @param bean An event source. (not be <code>null</code>)
     * @param name A name of chenged property. (not be <code>null</code>)
     * @param oldValue The old value. (may be <code>null</code>)
     * @param newValue The new value. (may be <code>null</code>)
     */
    void change(Object bean, String name, M oldValue, M newValue);
}
