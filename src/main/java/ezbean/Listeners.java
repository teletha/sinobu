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

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * <p>
 * This class is a kind of MultiMap.
 * </p>
 * 
 * @version 2010/01/18 17:49:36
 */
@SuppressWarnings("serial")
public class Listeners<K, V> extends ConcurrentHashMap<K, List<V>> {

    /**
     */
    public Listeners() {
        super(4);
    }

    public V find(K key) {
        List<V> list = get(key);

        return list == null ? null : list.get(0);
    }

    /**
     * Register the given {@link PropertyListener} to this context.
     * 
     * @param key A property name that you want to listen. If <code>null</code> is specified, the
     *            listener will listen to property change event on all properties.
     * @param value A listener to register.
     */
    public void put(K key, V value) {
        // check null
        if (key != null && value != null) {
            List<V> list = get(key);

            if (list == null) {
                list = new CopyOnWriteArrayList();
                put(key, list);
            }

            // register if not duplicated
            if (!list.contains(value)) {
                list.add(0, value);
            }
        }
    }

    /**
     * Unregister the given {@link PropertyListener} from this context.
     * 
     * @param key A property name that you want to unlisten. If <code>null</code> is specified, the
     *            listener will listen to property change event on all properties.
     * @param value A listener to unregister.
     */
    public void remove(K key, V value) {
        // check null
        if (key != null && value != null) {
            List<V> list = get(key);

            // unregister
            if (list != null) {
                list.remove(value);

                // remove if the specified property's pool is empty
                if (list.size() == 0) {
                    remove(key);
                }
            }
        }
    }

    /**
     * @see ezbean.PropertyListener#change(java.lang.Object, java.lang.String, java.lang.Object,
     *      java.lang.Object)
     */
    public void notify(Object bean, String name, Object oldValue, Object newValue) {
        // check diff
        if (name != null && (oldValue != null || newValue != null) && (oldValue == null || !oldValue.equals(newValue))) {
            List<V> set = get(name);

            // fire event
            if (set != null) {
                for (V listener : set) {
                    if (listener instanceof PropertyListener) {
                        ((PropertyListener) listener).change(bean, name, oldValue, newValue);
                    }
                }
            }
        }
    }
}
