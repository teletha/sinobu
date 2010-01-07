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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


import java.io.File;
import java.util.Date;



import org.junit.Test;

import ezbean.I;
import ezbean.sample.bean.ArrayBean;
import ezbean.sample.bean.BuiltinBean;
import ezbean.sample.bean.Person;
import ezbean.sample.bean.Primitive;
import ezbean.sample.bean.SchoolEnum;


/**
 * DOCUMENT.
 * 
 * @author <a href="mailto:Teletha.T@gmail.com">Teletha Testarossa</a>
 * @version $ Id: BeanInstantiateTest.java,v 1.0 2006/12/08 15:17:22 Teletha Exp $
 */
public class BeanInstantiateTest {

    /**
     * Bean.
     */
    @Test
    public void testCreate() {
        Person person = I.make(Person.class);
        assertNotNull(person);
        assertEquals(null, person.getFirstName());

        // set
        person.setFirstName("test");
        assertEquals("test", person.getFirstName());
    }

    /**
     * Bean with primitive property.
     */
    @Test
    public void testCreatePrimitive() {
        Primitive primitive = I.make(Primitive.class);
        assertNotNull(primitive);

        primitive.setBoolean(true);
        assertEquals(true, primitive.isBoolean());

        primitive.setByte((byte) 0);
        assertEquals((byte) 0, primitive.getByte());

        primitive.setChar('a');
        assertEquals('a', primitive.getChar());

        primitive.setDouble(0.1);
        assertEquals(0.1, primitive.getDouble(), 0);

        primitive.setFloat(0.1f);
        assertEquals(0.1f, primitive.getFloat(), 0);

        primitive.setInt(1);
        assertEquals(1, primitive.getInt());

        primitive.setLong(1L);
        assertEquals(1L, primitive.getLong());

        primitive.setShort((short) 1);
        assertEquals((short) 1, primitive.getShort());
    }

    @Test
    public void testCreateArray() {
        ArrayBean bean = I.make(ArrayBean.class);
        bean.setObjects(new String[] {"first", "second"});
        assertEquals(2, bean.getObjects().length);
        assertEquals("first", bean.getObjects()[0]);
        assertEquals("second", bean.getObjects()[1]);

        bean.setPrimitives(new int[] {0, 2, 5});
        assertEquals(3, bean.getPrimitives().length);
    }

// /**
// * No property class.
// */
// @Test
// public void testGetterOnly() {
// OnlyGetter getter = I.create(OnlyGetter.class);
// assertNotNull(getter);
// }
//
// /**
// * Abstract Bean.
// */
// @Test
// public void testAbstract() throws Exception {
// AbstractBean bean = I.create(AbstractBean.class);
// assertNotNull(bean);
// assertEquals(0, bean.getFoo());
//
// // setFoo(1);
// bean.concreateMethod();
// assertEquals(1, bean.getFoo());
// }

    /**
     * Bean with {@link Enum}.
     */
    @Test
    public void testEnum() {
        BuiltinBean bean = I.make(BuiltinBean.class);
        assertNotNull(bean);

        assertEquals(null, bean.getSchoolEnum());
        bean.setSchoolEnum(SchoolEnum.Lulim);
        assertEquals(SchoolEnum.Lulim, bean.getSchoolEnum());
    }

    /**
     * Bean with {@link Date}.
     */
    @Test
    public void testDate() {
        BuiltinBean bean = I.make(BuiltinBean.class);
        assertNotNull(bean);

        assertEquals(null, bean.getDate());
        bean.setDate(new Date(0L));
        assertEquals(new Date(0L), bean.getDate());
    }

    /**
     * Bean with {@link File}.
     */
    @Test
    public void testFile() {
        BuiltinBean bean = I.make(BuiltinBean.class);
        assertNotNull(bean);

        File file = I.locate("test");

        assertEquals(null, bean.getFile());
        bean.setFile(file);
        assertEquals(file, bean.getFile());
    }

    /**
     * Bean with {@link Class}.
     */
    @Test
    public void testClass() {
        BuiltinBean bean = I.make(BuiltinBean.class);
        assertNotNull(bean);

        assertEquals(null, bean.getSomeClass());
        bean.setSomeClass(BeanInstantiateTest.class);
        assertEquals(BeanInstantiateTest.class, bean.getSomeClass());
    }
}
