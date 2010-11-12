/*
 * Copyright (C) 2010 Nameless Production Committee.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ezbean;

import static org.junit.Assert.*;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;

import ezunit.PrivateModule;

/**
 * @version 2010/11/12 11:45:21
 */
public class InterceptorTest {

    @Rule
    public static final PrivateModule module = new PrivateModule();

    @Test
    public void acceptNotNull() throws Exception {
        InterceptedBean bean = I.make(InterceptedBean.class);
        bean.setName("test");

        assertEquals("test", bean.getName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectNull() {
        InterceptedBean bean = I.make(InterceptedBean.class);
        bean.setName(null);
    }

    @Test
    public void sequence() throws Exception {
        InterceptedBean bean = I.make(InterceptedBean.class);
        bean.setSequence("test");

        assertEquals(4, bean.order.size());
        assertEquals(11, bean.order.get(0).intValue());
        assertEquals(21, bean.order.get(1).intValue());
        assertEquals(22, bean.order.get(2).intValue());
        assertEquals(12, bean.order.get(3).intValue());
    }

    @Test
    public void reverse() throws Exception {
        InterceptedBean bean = I.make(InterceptedBean.class);
        bean.setReverse("test");

        assertEquals(4, bean.order.size());
        assertEquals(21, bean.order.get(0).intValue());
        assertEquals(11, bean.order.get(1).intValue());
        assertEquals(12, bean.order.get(2).intValue());
        assertEquals(22, bean.order.get(3).intValue());
    }

    /**
     * @version 2010/11/12 11:49:31
     */
    protected static class InterceptedBean {

        private String name;

        private String sequence;

        private String reverse;

        private List<Integer> order = new ArrayList();

        /**
         * Get the name property of this {@link InterceptorTest.InterceptedBean}.
         * 
         * @return The name property.
         */
        public String getName() {
            return name;
        }

        /**
         * Set the name property of this {@link InterceptorTest.InterceptedBean}.
         * 
         * @param name The name value to set.
         */
        @CheckNull
        public void setName(String name) {
            this.name = name;
        }

        /**
         * Get the sequence property of this {@link InterceptorTest.InterceptedBean}.
         * 
         * @return The sequence property.
         */
        public String getSequence() {
            return sequence;
        }

        /**
         * Set the sequence property of this {@link InterceptorTest.InterceptedBean}.
         * 
         * @param sequence The sequence value to set.
         */
        @First
        @Second
        public void setSequence(String sequence) {
            this.sequence = sequence;
        }

        /**
         * Get the reverse property of this {@link InterceptorTest.InterceptedBean}.
         * 
         * @return The reverse property.
         */
        public String getReverse() {
            return reverse;
        }

        /**
         * Set the reverse property of this {@link InterceptorTest.InterceptedBean}.
         * 
         * @param reverse The reverse value to set.
         */
        @Second
        @First
        public void setReverse(String reverse) {
            this.reverse = reverse;
        }
    }

    /**
     * @version 2010/11/12 11:45:50
     */
    @Retention(RetentionPolicy.RUNTIME)
    private static @interface CheckNull {
    }

    /**
     * @version 2010/11/12 11:46:52
     */
    protected static class CheckNullInterceptor extends Interceptor<CheckNull> {

        /**
         * @see ezbean.Interceptor#invoke(java.lang.Object)
         */
        @Override
        protected void invoke(Object param) {
            if (param == null) {
                throw new IllegalArgumentException();
            }
            super.invoke(param);
        }
    }

    /**
     * @version 2010/11/12 11:45:50
     */
    @Retention(RetentionPolicy.RUNTIME)
    private static @interface First {
    }

    /**
     * @version 2010/11/12 11:45:50
     */
    @Retention(RetentionPolicy.RUNTIME)
    private static @interface Second {
    }

    /**
     * @version 2010/11/12 11:46:52
     */
    protected static class FirstInterceptor extends Interceptor<First> {

        /**
         * @see ezbean.Interceptor#invoke(java.lang.Object)
         */
        @Override
        protected void invoke(Object param) {
            InterceptedBean bean = (InterceptedBean) that;

            bean.order.add(11);
            super.invoke(param);
            bean.order.add(12);
        }
    }

    /**
     * @version 2010/11/12 11:46:52
     */
    protected static class SecondInterceptor extends Interceptor<Second> {

        /**
         * @see ezbean.Interceptor#invoke(java.lang.Object)
         */
        @Override
        protected void invoke(Object param) {
            InterceptedBean bean = (InterceptedBean) that;

            bean.order.add(21);
            super.invoke(param);
            bean.order.add(22);
        }
    }
}
