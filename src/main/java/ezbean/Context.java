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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * NOTE : This is internal class. A user of Ezbean <em>does not have to use</em> this class.
 * </p>
 * 
 * @version 2008/06/21 17:03:06
 */
public final class Context {

    /** The pool for property change listeners. */
    private final Map<String, Set<PropertyListener>> listeners = new HashMap(4);

    /**
     * Register the given {@link PropertyListener} to this context.
     * 
     * @param propertyName A property name that you want to listen. If <code>null</code> is
     *            specified, the listener will listen to property change event on all properties.
     * @param listener A listener to register.
     */
    public void addListener(String propertyName, PropertyListener listener) {
        // check null
        if (listener != null) {
            Set<PropertyListener> set = listeners.get(propertyName);

            if (set == null) {
                set = new HashSet(4);
                listeners.put(propertyName, set);
            }

            // register
            set.add(listener);
        }
    }

    /**
     * Unregister the given {@link PropertyListener} from this context.
     * 
     * @param propertyName A property name that you want to unlisten. If <code>null</code> is
     *            specified, the listener will listen to property change event on all properties.
     * @param listener A listener to unregister.
     */
    public void removeListener(String propertyName, PropertyListener listener) {
        Set<PropertyListener> set = listeners.get(propertyName);

        // unregister
        if (set != null) {
            set.remove(listener);

            // remove if the specified property's pool is empty
            if (set.size() == 0) {
                listeners.remove(propertyName);
            }
        }
    }

    /**
     * @see ezbean.PropertyListener#change(java.lang.Object, java.lang.String,
     *      java.lang.Object, java.lang.Object)
     */
    public void propertyChange(Object bean, String propertyName, Object oldValue, Object newValue) {
        // check diff
        if ((oldValue != null || newValue != null) && (oldValue == null || !oldValue.equals(newValue))) {
            // collect listeners
            Set<PropertyListener> set = new HashSet(4);

            if (listeners.containsKey(null)) {
                set.addAll(listeners.get(null));
            }

            if (listeners.containsKey(propertyName)) {
                set.addAll(listeners.get(propertyName));
            }

            // fire event
            for (PropertyListener listener : set) {
                listener.change(bean, propertyName, oldValue, newValue);
            }
        }
    }
}
