/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.instantiation;

import java.io.File;
import java.util.Date;

import org.junit.Test;

import kiss.I;
import kiss.sample.bean.BuiltinBean;
import kiss.sample.bean.Person;
import kiss.sample.bean.Primitive;
import kiss.sample.bean.SchoolEnum;

/**
 * @version 2014/07/22 12:15:41
 */
public class BeanTest {

    /**
     * Public accessor.
     */
    @Test
    public void beanPublic() {
        Person person = I.make(Person.class);
        assert person != null;
        assert null == person.getFirstName();

        // set
        person.setFirstName("test");
        assert "test" == person.getFirstName();
    }

    /**
     * Bean with primitive property.
     */
    @Test
    public void primitive() {
        Primitive primitive = I.make(Primitive.class);
        assert primitive != null;

        primitive.setBoolean(true);
        assert true == primitive.isBoolean();

        primitive.setByte((byte) 0);
        assert (byte) 0 == primitive.getByte();

        primitive.setChar('a');
        assert 'a' == primitive.getChar();

        primitive.setDouble(0.1);
        assert primitive.getDouble() == 0.1d;

        primitive.setFloat(0.1f);
        assert primitive.getFloat() == 0.1f;

        primitive.setInt(1);
        assert 1 == primitive.getInt();

        primitive.setLong(1L);
        assert 1L == primitive.getLong();

        primitive.setShort((short) 1);
        assert (short) 1 == primitive.getShort();
    }

    /**
     * Bean with {@link Enum}.
     */
    @Test
    public void enumeration() {
        BuiltinBean bean = I.make(BuiltinBean.class);
        assert bean != null;

        assert null == bean.getSchoolEnum();
        bean.setSchoolEnum(SchoolEnum.Lulim);
        assert SchoolEnum.Lulim == bean.getSchoolEnum();
    }

    /**
     * Bean with {@link Date}.
     */
    @Test
    public void date() {
        BuiltinBean bean = I.make(BuiltinBean.class);
        assert bean != null;

        assert null == bean.getDate();
        bean.setDate(new Date(0L));
        assert bean.getDate().equals(new Date(0L));
    }

    /**
     * Bean with {@link File}.
     */
    @Test
    public void file() {
        BuiltinBean bean = I.make(BuiltinBean.class);
        assert bean != null;

        File file = new File("test");

        assert null == bean.getFile();
        bean.setFile(file);
        assert file == bean.getFile();
    }

    /**
     * Bean with {@link Class}.
     */
    @Test
    public void clazz() {
        BuiltinBean bean = I.make(BuiltinBean.class);
        assert bean != null;

        assert null == bean.getSomeClass();
        bean.setSomeClass(BeanTest.class);
        assert BeanTest.class == bean.getSomeClass();
    }
}
