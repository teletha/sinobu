/*
 * Copyright (C) 2014 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.module;


import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;

import antibug.PrivateModule;


import kiss.I;
import kiss.Interceptor;

/**
 * @version 2011/03/22 17:06:39
 */
public class InterceptorTest {

    @Rule
    public static final PrivateModule module = new PrivateModule();

    @Test
    public void acceptNotNull() throws Exception {
        InterceptedBean bean = I.make(InterceptedBean.class);
        bean.setName("test");

        assert "test" == bean.getName();
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

        assert 4 == bean.order.size();
        assert 11 == bean.order.get(0).intValue();
        assert 21 == bean.order.get(1).intValue();
        assert 22 == bean.order.get(2).intValue();
        assert 12 == bean.order.get(3).intValue();
    }

    @Test
    public void reverse() throws Exception {
        InterceptedBean bean = I.make(InterceptedBean.class);
        bean.setReverse("test");

        assert 4 == bean.order.size();
        assert 21 == bean.order.get(0).intValue();
        assert 11 == bean.order.get(1).intValue();
        assert 12 == bean.order.get(2).intValue();
        assert 22 == bean.order.get(3).intValue();
    }

    @Test
    public void accessAnnotationValueAndModifyParameter() throws Exception {
        InterceptedBean bean = I.make(InterceptedBean.class);
        bean.setValue(1);

        assert 5 == bean.getValue();
    }

    /**
     * @version 2010/11/12 11:49:31
     */
    protected static class InterceptedBean {

        private String name;

        private String sequence;

        private String reverse;

        private int value;

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

        /**
         * Get the value property of this {@link InterceptorTest.InterceptedBean}.
         * 
         * @return The value property.
         */
        public int getValue() {
            return value;
        }

        /**
         * Set the value property of this {@link InterceptorTest.InterceptedBean}.
         * 
         * @param value The value value to set.
         */
        @Value(number = 5)
        public void setValue(int value) {
            this.value = value;

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
         * @see kiss.Interceptor#invoke(java.lang.Object)
         */
        @Override
        protected Object invoke(Object... params) {
            if (params == null || params.length == 0) {
                throw new IllegalArgumentException();
            }

            for (Object param : params) {
                if (param == null) {
                    throw new IllegalArgumentException();
                }
            }
            return super.invoke(params);
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
         * @see kiss.Interceptor#invoke(java.lang.Object)
         */
        @Override
        protected Object invoke(Object... param) {
            InterceptedBean bean = (InterceptedBean) that;

            bean.order.add(11);
            Object result = super.invoke(param);
            bean.order.add(12);
            return result;
        }
    }

    /**
     * @version 2010/11/12 11:46:52
     */
    protected static class SecondInterceptor extends Interceptor<Second> {

        /**
         * @see kiss.Interceptor#invoke(java.lang.Object)
         */
        @Override
        protected Object invoke(Object... param) {
            InterceptedBean bean = (InterceptedBean) that;

            bean.order.add(21);
            Object result = super.invoke(param);
            bean.order.add(22);
            return result;
        }
    }

    /**
     * @version 2010/11/12 11:45:50
     */
    @Retention(RetentionPolicy.RUNTIME)
    private static @interface Value {

        int number();
    }

    /**
     * @version 2010/11/12 11:46:52
     */
    protected static class ValueInterceptor extends Interceptor<Value> {

        /**
         * @see kiss.Interceptor#invoke(java.lang.Object)
         */
        @Override
        protected Object invoke(Object... param) {
            return super.invoke(annotation.number());
        }
    }
}
