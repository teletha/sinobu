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


import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;


import junit.framework.TestCase;


import org.junit.Test;

import ezbean.Context;
import ezbean.PropertyListener;

/**
 * DOCUMENT.
 * 
 * @version 2008/06/13 22:42:20
 */
public class PropertyContextTest extends TestCase {

    /**
     * Test <code>null</code> arguments.
     */
    @Test
    public void testAddListener01() {
        Context context = new Context();
        context.addListener(null, null);
        context.addListener("some", null);
        // no error (e.g. NullPointerException)
    }

    /**
     * Test <code>null</code> arguments.
     */
    @Test
    public void testRemoveListener01() {
        Context context = new Context();
        context.removeListener(null, null);
        context.removeListener("some", null);
        // no error (e.g. NullPointerException)
    }

    /**
     * Test remove listener at first.
     */
    @Test
    public void testRemoveListener02() {
        Listener listener = new Listener();
        Context context = new Context();
        context.removeListener(null, listener);
        // no error (e.g. NullPointerException)
    }

    /**
     * Test <code>null</code> property name.
     */
    @Test
    public void testListener01() {
        Listener listener = new Listener();
        Context context = new Context();
        context.addListener(null, listener);

        // emulate property change event
        context.propertyChange("bean", "name", "old", "new");

        assertEquals("bean", listener.source);
        assertEquals("name", listener.propertyName);
        assertEquals("old", listener.oldObject);
        assertEquals("new", listener.newValue);

        // emulate property change event
        context.propertyChange("bean", "any", "foo", "bar");

        assertEquals("bean", listener.source);
        assertEquals("any", listener.propertyName);
        assertEquals("foo", listener.oldObject);
        assertEquals("bar", listener.newValue);
    }

    /**
     * Test specified property name.
     */
    @Test
    public void testListener02() {
        Listener listener = new Listener();
        Context context = new Context();
        context.addListener("name", listener);

        // emulate property change event
        context.propertyChange("bean", "name", "old", "new");

        assertEquals("bean", listener.source);
        assertEquals("name", listener.propertyName);
        assertEquals("old", listener.oldObject);
        assertEquals("new", listener.newValue);

        // emulate property change event (this invocation makes no change)
        context.propertyChange("bean", "any", "foo", "bar");

        assertEquals("bean", listener.source);
        assertEquals("name", listener.propertyName);
        assertEquals("old", listener.oldObject);
        assertEquals("new", listener.newValue);
    }

    /**
     * Test multiple listeners registration.
     */
    @Test
    public void testListener03() {
        Listener listener1 = new Listener();
        Listener listener2 = new Listener();
        Listener listener3 = new Listener();

        Context context = new Context();
        context.addListener("one", listener1);
        context.addListener("two", listener2);
        context.addListener(null, listener3);

        // emulate property change event
        context.propertyChange("bean", "one", "foo", "bar");

        assertEquals("bean", listener1.source);
        assertEquals("one", listener1.propertyName);
        assertEquals("foo", listener1.oldObject);
        assertEquals("bar", listener1.newValue);

        assertEquals(null, listener2.source);
        assertEquals(null, listener2.propertyName);
        assertEquals(null, listener2.oldObject);
        assertEquals(null, listener2.newValue);

        assertEquals("bean", listener3.source);
        assertEquals("one", listener3.propertyName);
        assertEquals("foo", listener3.oldObject);
        assertEquals("bar", listener3.newValue);

        // emulate property change event
        context.propertyChange("bean", "two", "bar", "baz");

        assertEquals("bean", listener1.source);
        assertEquals("one", listener1.propertyName);
        assertEquals("foo", listener1.oldObject);
        assertEquals("bar", listener1.newValue);

        assertEquals("bean", listener2.source);
        assertEquals("two", listener2.propertyName);
        assertEquals("bar", listener2.oldObject);
        assertEquals("baz", listener2.newValue);

        assertEquals("bean", listener3.source);
        assertEquals("two", listener3.propertyName);
        assertEquals("bar", listener3.oldObject);
        assertEquals("baz", listener3.newValue);
    }

    /**
     * Test listener removing with <code>null</code> property name.
     */
    @Test
    public void testListener04() {
        Listener listener = new Listener();
        Context context = new Context();
        context.addListener(null, listener);

        // emulate property change event
        context.propertyChange("bean", "name", "old", "new");

        assertEquals("bean", listener.source);
        assertEquals("name", listener.propertyName);
        assertEquals("old", listener.oldObject);
        assertEquals("new", listener.newValue);

        // remove listener
        context.removeListener(null, listener);

        // emulate property change event
        context.propertyChange("bean", "any", "foo", "bar");

        assertEquals("bean", listener.source);
        assertEquals("name", listener.propertyName);
        assertEquals("old", listener.oldObject);
        assertEquals("new", listener.newValue);
    }

    /**
     * Test listener removing with specified property name.
     */
    @Test
    public void testListener05() {
        Listener listener = new Listener();
        Context context = new Context();
        context.addListener("name", listener);

        // emulate property change event
        context.propertyChange("bean", "name", "old", "new");

        assertEquals("bean", listener.source);
        assertEquals("name", listener.propertyName);
        assertEquals("old", listener.oldObject);
        assertEquals("new", listener.newValue);

        // remove listener
        context.removeListener("name", listener);

        // emulate property change event
        context.propertyChange("bean", "any", "foo", "bar");

        assertEquals("bean", listener.source);
        assertEquals("name", listener.propertyName);
        assertEquals("old", listener.oldObject);
        assertEquals("new", listener.newValue);
    }

    /**
     * Test multiple listeners unregistration.
     */
    @Test
    public void testListener06() {
        Listener listener1 = new Listener();
        Listener listener2 = new Listener();
        Listener listener3 = new Listener();

        Context context = new Context();
        context.addListener("one", listener1);
        context.addListener("one", listener2);
        context.addListener(null, listener3);

        // emulate property change event
        context.propertyChange("bean", "one", "foo", "bar");

        assertEquals("bean", listener1.source);
        assertEquals("one", listener1.propertyName);
        assertEquals("foo", listener1.oldObject);
        assertEquals("bar", listener1.newValue);

        assertEquals("bean", listener2.source);
        assertEquals("one", listener2.propertyName);
        assertEquals("foo", listener2.oldObject);
        assertEquals("bar", listener2.newValue);

        assertEquals("bean", listener3.source);
        assertEquals("one", listener3.propertyName);
        assertEquals("foo", listener3.oldObject);
        assertEquals("bar", listener3.newValue);

        // remove listener
        context.removeListener("one", listener1);
        context.removeListener(null, listener3);

        // emulate property change event
        context.propertyChange("bean", "one", "bar", "baz");

        assertEquals("bean", listener1.source);
        assertEquals("one", listener1.propertyName);
        assertEquals("foo", listener1.oldObject);
        assertEquals("bar", listener1.newValue);

        assertEquals("bean", listener2.source);
        assertEquals("one", listener2.propertyName);
        assertEquals("bar", listener2.oldObject);
        assertEquals("baz", listener2.newValue);

        assertEquals("bean", listener3.source);
        assertEquals("one", listener3.propertyName);
        assertEquals("foo", listener3.oldObject);
        assertEquals("bar", listener3.newValue);
    }

    /**
     * Don't call twice same listener.
     */
    @Test
    public void testListener07() {
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

        Context context = new Context();
        context.addListener(null, listener);
        context.addListener("name", listener);

        // emulate property change event
        context.propertyChange("bean", "name", "old", "new");
        assertEquals(1, counter.size());
    }

    /**
     * Use snapshot style pool for listeners. (Don't throw {@link ConcurrentModificationException} )
     */
    @Test
    public void testListener08() {
        final List<PropertyListener> counter = new ArrayList();
        final Context context = new Context();
        final PropertyListener listener1 = new PropertyListener() {

            /**
             * @see ezbean.PropertyListener#change(java.lang.Object, java.lang.String,
             *      java.lang.Object, java.lang.Object)
             */
            public void change(Object bean, String propertyName, Object oldValue, Object newValue) {
                if (counter.contains(this)) {
                    context.removeListener(null, this);
                }
                counter.add(this);
            }
        };

        context.addListener(null, listener1);

        // emulate property change event
        context.propertyChange("bean", "name", "old", "new");
        assertEquals(1, counter.size());

        // emulate property change event (listener will remove itself)
        context.propertyChange("bean", "name", "foo", "bar");
        assertEquals(2, counter.size());

        // emulate property change event (context has no listener)
        context.propertyChange("bean", "name", "bar", "baz");
        assertEquals(2, counter.size());
    }

    /**
     * Test the propagation condition of changeProperty.
     */
    @Test
    public void testPropertyChangePropagation01() {
        // initialize listener
        Listener listener = new Listener();
        listener.newValue = 0;

        // register listener
        Context context = new Context();
        context.addListener(null, listener);

        // emulate property change event
        context.propertyChange(null, null, null, null);

        // assert
        assertEquals(0, listener.newValue); // change was not propagated
    }

    /**
     * Test the propagation condition of changeProperty.
     */
    @Test
    public void testPropertyChangePropagation02() {
        // initialize listener
        Listener listener = new Listener();
        listener.newValue = 0;

        // register listener
        Context context = new Context();
        context.addListener(null, listener);

        // emulate property change event
        context.propertyChange(null, null, null, 1);

        // assert
        assertEquals(1, listener.newValue); // change was propagated
    }

    /**
     * Test the propagation condition of changeProperty.
     */
    @Test
    public void testPropertyChangePropagation03() {
        // initialize listener
        Listener listener = new Listener();
        listener.newValue = 0;

        // register listener
        Context context = new Context();
        context.addListener(null, listener);

        // emulate property change event
        context.propertyChange(null, null, 1, null);

        // assert
        assertEquals(null, listener.newValue); // change was propagated
    }

    /**
     * Test the propagation condition of changeProperty.
     */
    @Test
    public void testPropertyChangePropagation04() {
        // initialize listener
        Listener listener = new Listener();
        listener.newValue = 0;

        // register listener
        Context context = new Context();
        context.addListener(null, listener);

        // emulate property change event
        context.propertyChange(null, null, 1, 1);

        // assert
        assertEquals(0, listener.newValue); // change was not propagated
    }

    /**
     * DOCUMENT.
     * 
     * @version 2008/06/13 22:42:11
     */
    private static final class Listener implements PropertyListener {

        /** Store. */
        private Object source = null;

        private String propertyName = null;

        private Object oldObject = null;

        private Object newValue = null;

        /**
         * @see ezbean.PropertyListener#change(java.lang.Object, java.lang.String,
         *      java.lang.Object, java.lang.Object)
         */
        public void change(Object bean, String propertyName, Object oldValue, Object newValue) {
            this.source = bean;
            this.propertyName = propertyName;
            this.oldObject = oldValue;
            this.newValue = newValue;
        }
    }
}
