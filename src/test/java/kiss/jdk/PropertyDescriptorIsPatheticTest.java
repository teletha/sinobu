/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.jdk;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

import org.junit.Test;

import kiss.sample.bean.Primitive;
import kiss.sample.bean.PrimitiveWrapper;
import kiss.sample.bean.invalid.ProtectedAccessor;

/**
 * {@link PropertyDescriptor} is not pathetic in JDK7.
 * 
 * @version 2011/03/22 16:55:02
 */
public class PropertyDescriptorIsPatheticTest {

    @Test
    public void primitive() throws Exception {
        PropertyDescriptor descriptor = new PropertyDescriptor("boolean", Primitive.class);
        Method method = descriptor.getReadMethod();
        assert method.getName().equals("isBoolean");
    }

    @Test
    public void wrapper() throws Exception {
        PropertyDescriptor descriptor = new PropertyDescriptor("boolean", PrimitiveWrapper.class);
        Method method = descriptor.getReadMethod();
        assert method.getName().equals("isBoolean");
    }

    @Test(expected = IntrospectionException.class)
    public void protectedProperty() throws Exception {
        new PropertyDescriptor("getter", ProtectedAccessor.class); // lolol
    }
}
