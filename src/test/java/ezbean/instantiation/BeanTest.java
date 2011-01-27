/**
 * Copyright (C) 2011 Nameless Production Committee.
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
package ezbean.instantiation;

import static org.junit.Assert.*;

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
 * @version 2010/02/09 20:39:33
 */
public class BeanTest {

    /**
     * Public accessor.
     */
    @Test
    public void beanPublic() {
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
    public void primitive() {
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
    public void array() {
        ArrayBean bean = I.make(ArrayBean.class);
        bean.setObjects(new String[] {"first", "second"});
        assertEquals(2, bean.getObjects().length);
        assertEquals("first", bean.getObjects()[0]);
        assertEquals("second", bean.getObjects()[1]);

        bean.setPrimitives(new int[] {0, 2, 5});
        assertEquals(3, bean.getPrimitives().length);
    }

    /**
     * Bean with {@link Enum}.
     */
    @Test
    public void enumeration() {
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
    public void date() {
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
    public void file() {
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
    public void clazz() {
        BuiltinBean bean = I.make(BuiltinBean.class);
        assertNotNull(bean);

        assertEquals(null, bean.getSomeClass());
        bean.setSomeClass(BeanTest.class);
        assertEquals(BeanTest.class, bean.getSomeClass());
    }
}
