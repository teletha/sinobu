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

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

import org.junit.Test;

/**
 * @version 2010/01/17 9:33:47
 */
public class PropertyContextTest {

    @Test(expected = NullPointerException.class)
    public void putNull() {
        Listeners<String, String> listeners = new Listeners();
        listeners.put(null, (String) null);
    }

    @Test(expected = NullPointerException.class)
    public void putNullKey() {
        Listeners<String, String> listeners = new Listeners();
        listeners.put(null, "test");
    }

    @Test
    public void putNullValue() {
        Listeners<String, String> listeners = new Listeners();
        listeners.put("test", (String) null);
    }

    @Test(expected = NullPointerException.class)
    public void removeNull() {
        Listeners<String, String> listeners = new Listeners();
        listeners.remove(null, null);
    }

    @Test(expected = NullPointerException.class)
    public void removeNullKey() {
        Listeners<String, String> listeners = new Listeners();
        listeners.remove(null, "test");
    }

    @Test
    public void removeNullValue() {
        Listeners<String, String> listeners = new Listeners();
        listeners.remove("test", null);
    }

    @Test
    public void notifySingle() {
        Listener listener = new Listener();
        Listeners<String, PropertyListener> listeners = new Listeners();
        listeners.put("name", listener);

        // emulate property change event
        listeners.notify("bean", "name", "old", "new");

        assertEquals("bean", listener.source);
        assertEquals("name", listener.propertyName);
        assertEquals("old", listener.oldObject);
        assertEquals("new", listener.newValue);

        // emulate property change event (this invocation makes no change)
        listeners.notify("bean", "any", "foo", "bar");

        assertEquals("bean", listener.source);
        assertEquals("name", listener.propertyName);
        assertEquals("old", listener.oldObject);
        assertEquals("new", listener.newValue);
    }

    /**
     * Test multiple listeners registration.
     */
    @Test
    public void notifyMultiple() {
        Listener listener1 = new Listener();
        Listener listener2 = new Listener();

        Listeners<String, PropertyListener> listeners = new Listeners();
        listeners.put("one", listener1);
        listeners.put("two", listener2);

        // emulate property change event
        listeners.notify("bean", "one", "foo", "bar");

        assertEquals("bean", listener1.source);
        assertEquals("one", listener1.propertyName);
        assertEquals("foo", listener1.oldObject);
        assertEquals("bar", listener1.newValue);

        assertEquals(null, listener2.source);
        assertEquals(null, listener2.propertyName);
        assertEquals(null, listener2.oldObject);
        assertEquals(null, listener2.newValue);

        // emulate property change event
        listeners.notify("bean", "two", "bar", "baz");

        assertEquals("bean", listener1.source);
        assertEquals("one", listener1.propertyName);
        assertEquals("foo", listener1.oldObject);
        assertEquals("bar", listener1.newValue);

        assertEquals("bean", listener2.source);
        assertEquals("two", listener2.propertyName);
        assertEquals("bar", listener2.oldObject);
        assertEquals("baz", listener2.newValue);
    }

    /**
     * Test listener removing with specified property name.
     */
    @Test
    public void notifySingleWithRemove() {
        Listener listener = new Listener();
        Listeners<String, PropertyListener> listeners = new Listeners();
        listeners.put("name", listener);

        // emulate property change event
        listeners.notify("bean", "name", "old", "new");

        assertEquals("bean", listener.source);
        assertEquals("name", listener.propertyName);
        assertEquals("old", listener.oldObject);
        assertEquals("new", listener.newValue);

        // remove listener
        listeners.remove("name", listener);

        // emulate property change event
        listeners.notify("bean", "name", "foo", "bar");

        assertEquals("bean", listener.source);
        assertEquals("name", listener.propertyName);
        assertEquals("old", listener.oldObject);
        assertEquals("new", listener.newValue);
    }

    /**
     * Test multiple listeners unregistration.
     */
    @Test
    public void notifyMultipleWithRemove() {
        Listener listener1 = new Listener();
        Listener listener2 = new Listener();

        Listeners<String, PropertyListener> listeners = new Listeners();
        listeners.put("one", listener1);
        listeners.put("one", listener2);

        // emulate property change event
        listeners.notify("bean", "one", "foo", "bar");

        assertEquals("bean", listener1.source);
        assertEquals("one", listener1.propertyName);
        assertEquals("foo", listener1.oldObject);
        assertEquals("bar", listener1.newValue);

        assertEquals("bean", listener2.source);
        assertEquals("one", listener2.propertyName);
        assertEquals("foo", listener2.oldObject);
        assertEquals("bar", listener2.newValue);

        // remove listener
        listeners.remove("one", listener1);

        // emulate property change event
        listeners.notify("bean", "one", "bar", "baz");

        assertEquals("bean", listener1.source);
        assertEquals("one", listener1.propertyName);
        assertEquals("foo", listener1.oldObject);
        assertEquals("bar", listener1.newValue);

        assertEquals("bean", listener2.source);
        assertEquals("one", listener2.propertyName);
        assertEquals("bar", listener2.oldObject);
        assertEquals("baz", listener2.newValue);
    }

    @Test
    public void dontCallTwiceSameListener() {
        final List<String> counter = new ArrayList();

        PropertyListener listener = new PropertyListener() {

            /**
             * @see ezbean.PropertyListener#change(java.lang.Object, java.lang.String,
             *      java.lang.Object, java.lang.Object)
             */
            public void change(Object bean, String propertyName, Object oldValue, Object newValue) {
                counter.add(propertyName);
            }
        };

        Listeners<String, PropertyListener> listeners = new Listeners();
        listeners.put("name", listener);
        listeners.put("name", listener);

        // emulate property change event
        listeners.notify("bean", "name", "old", "new");
        assertEquals(1, counter.size());
    }

    /**
     * Use snapshot style pool for listeners. (Don't throw {@link ConcurrentModificationException} )
     */
    @Test
    public void listenerIsSnapshotStyle() {
        final List<PropertyListener> counter = new ArrayList();
        final Listeners<String, PropertyListener> listeners = new Listeners();
        final PropertyListener listener1 = new PropertyListener() {

            /**
             * @see ezbean.PropertyListener#change(java.lang.Object, java.lang.String,
             *      java.lang.Object, java.lang.Object)
             */
            public void change(Object bean, String propertyName, Object oldValue, Object newValue) {
                if (counter.contains(this)) {
                    listeners.remove("name", this);
                }
                counter.add(this);
            }
        };

        listeners.put("name", listener1);

        // emulate property change event
        listeners.notify("bean", "name", "old", "new");
        assertEquals(1, counter.size());

        // emulate property change event (listener will remove itself)
        listeners.notify("bean", "name", "foo", "bar");
        assertEquals(2, counter.size());

        // emulate property change event (context has no listener)
        listeners.notify("bean", "name", "bar", "baz");
        assertEquals(2, counter.size());
    }

    public void notifyNullSource() {
        Listeners<String, PropertyListener> listeners = new Listeners();

        // emulate property change event
        listeners.notify(null, "test", "old", "new");
    }

    @Test(expected = NullPointerException.class)
    public void notifyNullName() {
        Listeners<String, PropertyListener> listeners = new Listeners();

        // emulate property change event
        listeners.notify("bean", null, "old", "new");
    }

    /**
     * Test the propagation condition of changeProperty.
     */
    @Test
    public void notifyNullValues() {
        // initialize listener
        Listener listener = new Listener();
        listener.newValue = 0;

        // register listener
        Listeners<String, PropertyListener> listeners = new Listeners();
        listeners.put("test", listener);

        // emulate property change event
        listeners.notify("some", "test", null, null);

        // assert
        assertEquals(0, listener.newValue); // change was not propagated
    }

    /**
     * Test the propagation condition of changeProperty.
     */
    @Test
    public void notifyNullOldValue() {
        // initialize listener
        Listener listener = new Listener();
        listener.newValue = 0;

        // register listener
        Listeners<String, PropertyListener> listeners = new Listeners();
        listeners.put("test", listener);

        // emulate property change event
        listeners.notify("some", "test", null, 1);

        // assert
        assertEquals(1, listener.newValue); // change was propagated
    }

    /**
     * Test the propagation condition of changeProperty.
     */
    @Test
    public void notifyNullNewValue() {
        // initialize listener
        Listener listener = new Listener();
        listener.newValue = 0;

        // register listener
        Listeners<String, PropertyListener> listeners = new Listeners();
        listeners.put("test", listener);

        // emulate property change event
        listeners.notify("some", "test", 1, null);

        // assert
        assertEquals(null, listener.newValue); // change was propagated
    }

    /**
     * Test the propagation condition of changeProperty.
     */
    @Test
    public void notifyNotNullValues() {
        // initialize listener
        Listener listener = new Listener();
        listener.newValue = 0;

        // register listener
        Listeners<String, PropertyListener> listeners = new Listeners();
        listeners.put("test", listener);

        // emulate property change event
        listeners.notify("some", "test", 1, 1);

        // assert
        assertEquals(0, listener.newValue); // change was not propagated
    }

    /**
     * @version 2010/01/17 9:49:27
     */
    private static final class Listener implements PropertyListener {

        /** Store. */
        private Object source = null;

        private String propertyName = null;

        private Object oldObject = null;

        private Object newValue = null;

        /**
         * @see ezbean.PropertyListener#change(java.lang.Object, java.lang.String, java.lang.Object,
         *      java.lang.Object)
         */
        public void change(Object bean, String propertyName, Object oldValue, Object newValue) {
            this.source = bean;
            this.propertyName = propertyName;
            this.oldObject = oldValue;
            this.newValue = newValue;
        }
    }
}
