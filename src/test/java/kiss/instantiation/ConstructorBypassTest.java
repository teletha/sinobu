/*
 * Copyright (C) 2016 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.instantiation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;

import kiss.I;

import org.junit.Before;
import org.junit.Test;

import antibug.util.UnsafeUtility;

/**
 * @version 2011/03/22 16:54:33
 */
@SuppressWarnings("resource")
public class ConstructorBypassTest {

    private static int call = 0;

    @Before
    public void reset() {
        call = 0;
    }

    @Test
    public void byReflectionFactory() throws Exception {
        // no bypass
        Child child = I.make(Child.class);
        assert child instanceof Child;
        assert 2 == call;

        // bypass
        child = UnsafeUtility.newInstance(Child.class);
        assert child instanceof Child;
        assert 2 == call; // Cool!!!
    }

    @Test
    public void byObjectStreamClass() throws Exception {
        // no bypass
        Child child = I.make(Child.class);
        assert child instanceof Child;
        assert 2 == call;

        // bypass
        Method method = ObjectStreamClass.class.getDeclaredMethod("newInstance", new Class[] {});
        method.setAccessible(true);

        child = (Child) method.invoke(ObjectStreamClass.lookup(Child.class));
        assert child instanceof Child;
        assert 3 == call; // Umm...
    }

    @Test
    public void byMockObjectInputStream() throws Exception {
        // no bypass
        Child child = I.make(Child.class);
        assert child instanceof Child;
        assert 2 == call;

        // bypass
        child = (Child) new Mock(Child.class).readObject();
        assert child instanceof Child;
        assert 3 == call; // Umm...
    }

    /**
     * @version 2010/02/09 18:16:25
     */
    private static class Mock extends ObjectInputStream {

        /** The heading data for serializaed object. */
        // private static final byte[] head = {-84, -19, 0, 5, 115, 114};

        /**
         * The tailing data for serializaed object. Actual byte array should be {2, 0, 0, 120, 112,
         * 115, 113, 0, 126, 0, 0}, but it seems to be able to shrink it.
         */
        // private static final byte[] tail = {2, 0, 0, 120, 112};

        private Mock(Class clazz) throws IOException {
            super(build(clazz));
        }

        private static InputStream build(Class clazz) throws IOException {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            DataOutputStream writer = new DataOutputStream(output);
            writer.writeUTF(clazz.getName());
            writer.writeLong(ObjectStreamClass.lookup(clazz).getSerialVersionUID());

            byte[] data = output.toByteArray();
            // byte[] bytes = new byte[11 + data.length];
            // System.arraycopy(head, 0, bytes, 0, 6);
            // System.arraycopy(data, 0, bytes, 6, data.length);
            // System.arraycopy(tail, 0, bytes, 6 + data.length, 5);

            // API definition
            return new ByteArrayInputStream(ByteBuffer.allocate(11 + data.length)
                    .put(new byte[] {-84, -19, 0, 5, 115, 114})
                    .put(data)
                    .put(new byte[] {2, 0, 0, 120, 112})
                    .array());
        }
    }

    /**
     * @version 2010/02/09 18:03:40
     */
    protected static class Parent {

        private String value;

        /**
         * 
         */
        public Parent() {
            call++;
        }

        /**
         * Get the value property of this {@link ConstructorBypassTest.Parent}.
         * 
         * @return The value property.
         */
        public String getValue() {
            return value;
        }

        /**
         * Set the value property of this {@link ConstructorBypassTest.Parent}.
         * 
         * @param value The value value to set.
         */
        public void setValue(String value) {
            this.value = value;
        }
    }

    /**
     * @version 2010/02/09 18:13:21
     */
    protected static class Child extends Parent implements Serializable {

        private static final long serialVersionUID = 8601055221631424018L;

        /**
         * 
         */
        public Child() {
            call++;
        }
    }
}
