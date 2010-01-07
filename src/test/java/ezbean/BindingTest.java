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


import org.junit.Test;

import ezbean.I;
import ezbean.Disposable;
import ezbean.sample.bean.ChainBean;
import ezbean.sample.bean.GenericBean;
import ezbean.sample.bean.GenericGetterBean;
import ezbean.sample.bean.GenericSetterBean;
import ezbean.sample.bean.GenericStringBean;
import ezbean.sample.bean.Person;
import ezbean.sample.bean.School;
import ezbean.sample.bean.Student;


/**
 * DOCUMENT.
 * 
 * @version 2008/06/17 13:50:21
 */
public class BindingTest {

    /**
     * Bind.
     */
    @Test
    public void testBind01() {
        Person source = I.make(Person.class);
        source.setAge(5);
        Person target = I.make(Person.class);
        target.setAge(10);

        assertEquals(5, source.getAge());
        assertEquals(10, target.getAge());

        // bind
        I.bind(I.mock(source).getAge(), I.mock(target).getAge(), true);

        assertEquals(5, source.getAge());
        assertEquals(5, target.getAge());

        // from source
        source.setAge(1);
        assertEquals(1, source.getAge());
        assertEquals(1, target.getAge());

        // from target
        target.setAge(4);
        assertEquals(4, source.getAge());
        assertEquals(4, target.getAge());
    }

    /**
     * Bind.
     */
    @Test
    public void testBind02() {
        Person source = I.make(Person.class);
        source.setFirstName("kuma");
        source.setLastName("kuma");
        Person target = I.make(Person.class);
        target.setFirstName("uma");
        target.setLastName("uma");

        assertEquals("kuma", source.getFirstName());
        assertEquals("kuma", source.getLastName());
        assertEquals("uma", target.getFirstName());
        assertEquals("uma", target.getLastName());

        // bind
        I.bind(I.mock(source).getFirstName(), I.mock(target).getLastName(), true);

        assertEquals("kuma", source.getFirstName());
        assertEquals("kuma", source.getLastName());
        assertEquals("uma", target.getFirstName());
        assertEquals("kuma", target.getLastName());

        // from source
        source.setFirstName("test");
        assertEquals("test", source.getFirstName());
        assertEquals("kuma", source.getLastName());
        assertEquals("uma", target.getFirstName());
        assertEquals("test", target.getLastName());

        // from target
        target.setLastName("change");
        assertEquals("change", source.getFirstName());
        assertEquals("kuma", source.getLastName());
        assertEquals("uma", target.getFirstName());
        assertEquals("change", target.getLastName());
    }

    /**
     * Bind.
     */
    @Test
    public void testBind03() {
        School lulim = I.make(School.class);
        lulim.setName("lulim");

        School spica = I.make(School.class);
        lulim.setName("spica");

        Student source = I.make(Student.class);
        Student target = I.make(Student.class);

        assertEquals(null, source.getSchool());
        assertEquals(null, target.getSchool());

        // bind
        I.bind(I.mock(source).getSchool(), I.mock(target).getSchool(), true);

        assertEquals(null, source.getSchool());
        assertEquals(null, target.getSchool());

        // from source
        source.setSchool(lulim);
        assertEquals(lulim, source.getSchool());
        assertEquals(lulim, target.getSchool());

        // from target
        target.setSchool(spica);
        assertEquals(spica, source.getSchool());
        assertEquals(spica, target.getSchool());
    }

    /**
     * Bind nested property.
     */
    @Test
    public void testBind04() {
        School lulim = I.make(School.class);
        lulim.setName("lulim");

        School spica = I.make(School.class);
        spica.setName("spica");

        Student source = I.make(Student.class);
        Student target = I.make(Student.class);

        assertEquals(null, source.getSchool());
        assertEquals(null, target.getSchool());

        // bind
        I.bind(I.mock(source).getSchool().getName(), I.mock(target).getSchool().getName(), true);

        assertEquals(null, source.getSchool());
        assertEquals(null, target.getSchool());

        // from source
        source.setSchool(lulim);
        assertEquals(lulim, source.getSchool());
        assertEquals("lulim", source.getSchool().getName());
        assertEquals(null, target.getSchool());

        // from target
        target.setSchool(spica);
        assertEquals(lulim, source.getSchool());
        assertEquals("spica", source.getSchool().getName());
        assertEquals(spica, target.getSchool());
        assertEquals("spica", target.getSchool().getName());

        // nested property change from source
        lulim.setName("lulim");
        assertEquals(lulim, source.getSchool());
        assertEquals("lulim", source.getSchool().getName());
        assertEquals(spica, target.getSchool());
        assertEquals("lulim", target.getSchool().getName());

        // nested property change from target
        spica.setName("spica");
        assertEquals(lulim, source.getSchool());
        assertEquals("spica", source.getSchool().getName());
        assertEquals(spica, target.getSchool());
        assertEquals("spica", target.getSchool().getName());
    }

    /**
     * Bind nested property with null.
     */
    @Test
    public void testBind05() {
        School lulim = I.make(School.class);
        lulim.setName("lulim");

        School spica = I.make(School.class);
        spica.setName("spica");

        Student source = I.make(Student.class);
        source.setSchool(lulim);
        Student target = I.make(Student.class);
        target.setSchool(spica);

        assertEquals(lulim, source.getSchool());
        assertEquals("lulim", source.getSchool().getName());
        assertEquals(spica, target.getSchool());
        assertEquals("spica", target.getSchool().getName());

        // bind
        I.bind(I.mock(source).getSchool().getName(), I.mock(target).getSchool().getName(), true);

        assertEquals(lulim, source.getSchool());
        assertEquals("lulim", source.getSchool().getName());
        assertEquals(spica, target.getSchool());
        assertEquals("lulim", target.getSchool().getName());

        // null from source
        source.setSchool(null);
        assertEquals(null, source.getSchool());
        assertEquals(spica, target.getSchool());
        assertEquals(null, target.getSchool().getName());

        assertEquals("lulim", lulim.getName());
        assertEquals(null, spica.getName());

        // change nested property which is removed from binding
        lulim.setName("no effect");
        assertEquals("no effect", lulim.getName());
        assertEquals(null, spica.getName());

        // change nested property which is belong to binding without source
        spica.setName("effect");
        assertEquals("no effect", lulim.getName());
        assertEquals("effect", spica.getName());

        // make source graph valid (source has high priority)
        source.setSchool(lulim);
        assertEquals(lulim, source.getSchool());
        assertEquals("no effect", source.getSchool().getName());
        assertEquals(spica, target.getSchool());
        assertEquals("no effect", target.getSchool().getName());

        // change from nested object directly
        spica.setName("change");
        assertEquals(lulim, source.getSchool());
        assertEquals("change", source.getSchool().getName());
        assertEquals(spica, target.getSchool());
        assertEquals("change", target.getSchool().getName());
    }

    /**
     * Bind between different types.
     */
    @Test
    public void testBind06() {
        Person source = I.make(Person.class);
        source.setAge(5);
        Person target = I.make(Person.class);
        target.setFirstName("test");

        assertEquals(5, source.getAge());
        assertEquals("test", target.getFirstName());

        // bind
        I.bind(I.mock(source).getAge(), I.mock(target).getFirstName(), true);

        assertEquals(5, source.getAge());
        assertEquals("5", target.getFirstName());

        // from source
        source.setAge(1);
        assertEquals(1, source.getAge());
        assertEquals("1", target.getFirstName());

        // from target
        target.setFirstName("23");
        assertEquals(23, source.getAge());
        assertEquals("23", target.getFirstName());
    }

    /**
     * Bind between different types with invalid value.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testBind07() {
        Person source = I.make(Person.class);
        source.setAge(5);
        Person target = I.make(Person.class);
        target.setFirstName("test");

        assertEquals(5, source.getAge());
        assertEquals("test", target.getFirstName());

        // bind
        I.bind(I.mock(source).getAge(), I.mock(target).getFirstName(), true);

        assertEquals(5, source.getAge());
        assertEquals("5", target.getFirstName());

        // from source
        source.setAge(1);
        assertEquals(1, source.getAge());
        assertEquals("1", target.getFirstName());

        // from target
        target.setFirstName("invalid");
        assertEquals(1, source.getAge());
        assertEquals("invalid", target.getFirstName());
    }

    /**
     * Bind same property name in one property path.
     */
    @Test
    public void testBind08() {
        ChainBean one = I.make(ChainBean.class).setName("one");
        ChainBean two = I.make(ChainBean.class).setName("two");
        ChainBean three = I.make(ChainBean.class).setName("three");
        one.setNext(two);
        two.setNext(three);

        ChainBean first = I.make(ChainBean.class).setName("first");
        ChainBean second = I.make(ChainBean.class).setName("second");
        ChainBean third = I.make(ChainBean.class).setName("third");
        first.setNext(second);
        second.setNext(third);

        // one - two - three
        // first - second - third
        assertEquals(three, two.getNext());
        assertEquals(third, second.getNext());

        // bind
        I.bind(I.mock(one).getNext().getNext(), I.mock(first).getNext().getNext(), true);

        // this is initial binding
        // one - two - three
        // first - second - three
        assertEquals(two, one.getNext());
        assertEquals(three, two.getNext());
        assertEquals(second, first.getNext());
        assertEquals(three, second.getNext());

        // from source at last path
        // one - two - third
        // first - second - third
        two.setNext(third);
        assertEquals(two, one.getNext());
        assertEquals(third, two.getNext());
        assertEquals(second, first.getNext());
        assertEquals(third, second.getNext());

        // from target at last path
        // one - two - three
        // first - second - three
        second.setNext(three);
        assertEquals(two, one.getNext());
        assertEquals(three, two.getNext());
        assertEquals(second, first.getNext());
        assertEquals(three, second.getNext());

        // create anothor chain
        ChainBean tick = I.make(ChainBean.class).setName("tick");
        ChainBean tack = I.make(ChainBean.class).setName("tack");
        tick.setNext(tack);

        // from source at middle path
        // one - tick - tack
        // first - second - tack
        one.setNext(tick);
        assertEquals(tick, one.getNext());
        assertEquals(tack, tick.getNext());
        assertEquals(second, first.getNext());
        assertEquals(tack, second.getNext());
        assertEquals(three, two.getNext());

        // create anothor chain
        ChainBean foo = I.make(ChainBean.class).setName("foo");
        ChainBean bar = I.make(ChainBean.class).setName("bar");
        foo.setNext(bar);

        // from target at middle path
        // one - tick - bar
        // first - foo - bar
        first.setNext(foo);
        assertEquals(tick, one.getNext());
        assertEquals(bar, tick.getNext());
        assertEquals(foo, first.getNext());
        assertEquals(bar, foo.getNext());
        assertEquals(three, two.getNext());
        assertEquals(tack, second.getNext());

        // no binding change
        two.setNext(third);
        second.setNext(three);
        assertEquals(tick, one.getNext()); // as-is
        assertEquals(bar, tick.getNext()); // as-is
        assertEquals(foo, first.getNext()); // as-is
        assertEquals(bar, foo.getNext()); // as-is
        assertEquals(third, two.getNext()); // change
        assertEquals(three, second.getNext()); // change
    }

    @Test
    public void bindGenericProperty() {
        GenericStringBean source = I.make(GenericStringBean.class);
        GenericStringBean target = I.make(GenericStringBean.class);

        // bind
        I.bind(I.mock(source).getGeneric(), I.mock(target).getGeneric(), true);

        // from source
        source.setGeneric("test");
        assertEquals("test", source.getGeneric());
        assertEquals("test", target.getGeneric());
    }

    @Test
    public void bindGenericNestedProperty() {
        Person one = I.make(Person.class);
        one.setFirstName("one");

        Person two = I.make(Person.class);
        two.setFirstName("two");

        GenericNest source = I.make(GenericNest.class);
        source.setGeneric(one);

        Person target = I.make(Person.class);

        // bind
        I.bind(I.mock(source).getGeneric().getFirstName(), I.mock(target).getFirstName(), true);
        assertEquals("one", target.getFirstName());

        // from source
        source.setGeneric(two);
        assertEquals("two", target.getFirstName());
    }

    /**
     * @version 2009/07/17 16:48:16
     */
    protected class GenericNest extends GenericBean<Person> {
    }

    @Test
    public void bindGenericGetterProperty() {
        GenericGetterBean source = I.make(GenericGetterBean.class);
        GenericGetterBean target = I.make(GenericGetterBean.class);

        // bind
        I.bind(I.mock(source).getGeneric(), I.mock(target).getGeneric(), true);

        // from source
        source.setGeneric("test");
        assertEquals("test", source.getGeneric());
        assertEquals("test", target.getGeneric());
    }

    @Test
    public void bindGenericSetterProperty() {
        GenericSetterBean source = I.make(GenericSetterBean.class);
        GenericSetterBean target = I.make(GenericSetterBean.class);

        // bind
        I.bind(I.mock(source).getGeneric(), I.mock(target).getGeneric(), true);

        // from source
        source.setGeneric("test");
        assertEquals("test", source.getGeneric());
        assertEquals("test", target.getGeneric());
    }

    /**
     * Test <code>null</code> source.
     */
    @Test(expected = NullPointerException.class)
    public void testNull01() {
        // create bean
        Person target = I.make(Person.class);

        // bind
        I.bind(null, I.mock(target).getFirstName(), true);
    }

    /**
     * Test <code>null</code> target.
     */
    @Test(expected = NullPointerException.class)
    public void testNull02() {
        // create bean
        Person source = I.make(Person.class);

        // bind
        I.bind(I.mock(source).getFirstName(), null, true);
    }

    /**
     * Test empty source path.
     */
    @Test(expected = IndexOutOfBoundsException.class)
    public void testNull03() {
        // create bean
        Person source = I.make(Person.class);
        Person target = I.make(Person.class);

        // bind
        I.bind(I.mock(source), I.mock(target).getFirstName(), true);
    }

    /**
     * Test empty target path.
     */
    @Test(expected = IndexOutOfBoundsException.class)
    public void testNull04() {
        // create bean
        Person source = I.make(Person.class);
        Person target = I.make(Person.class);

        // bind
        I.bind(I.mock(source).getFirstName(), I.mock(target), true);
    }

    /**
     * Bind one-way.
     */
    @Test
    public void testOneWay1() {
        Person source = I.make(Person.class);
        source.setAge(5);
        Person target = I.make(Person.class);
        target.setAge(10);

        assertEquals(5, source.getAge());
        assertEquals(10, target.getAge());

        // bind
        I.bind(I.mock(source).getAge(), I.mock(target).getAge(), false);

        assertEquals(5, source.getAge());
        assertEquals(5, target.getAge());

        // from source
        source.setAge(1);
        assertEquals(1, source.getAge());
        assertEquals(1, target.getAge());

        // from target
        target.setAge(4);
        assertEquals(1, source.getAge());
        assertEquals(4, target.getAge());

        // from source
        source.setAge(7);
        assertEquals(7, source.getAge());
        assertEquals(7, target.getAge());
    }

    /**
     * Test unbind.
     */
    @Test
    public void testUnbind1() throws Exception {
        Person source = I.make(Person.class);
        source.setAge(5);
        Person target = I.make(Person.class);
        target.setAge(10);

        assertEquals(5, source.getAge());
        assertEquals(10, target.getAge());

        // bind
        Disposable disposable = I.bind(I.mock(source).getAge(), I.mock(target).getAge(), true);

        assertEquals(5, source.getAge());
        assertEquals(5, target.getAge());

        // from source
        source.setAge(1);
        assertEquals(1, source.getAge());
        assertEquals(1, target.getAge());

        // from target
        target.setAge(4);
        assertEquals(4, source.getAge());
        assertEquals(4, target.getAge());

        // unbind
        disposable.dispose();

        // from source
        source.setAge(17);
        assertEquals(17, source.getAge());
        assertEquals(4, target.getAge());

        // from target
        target.setAge(71);
        assertEquals(17, source.getAge());
        assertEquals(71, target.getAge());
    }

    /**
     * Test unbind repeatedly.
     */
    @Test
    public void testUnbind2() throws Exception {
        Person source = I.make(Person.class);
        source.setAge(5);
        Person target = I.make(Person.class);
        target.setAge(10);

        assertEquals(5, source.getAge());
        assertEquals(10, target.getAge());

        // bind
        Disposable disposable = I.bind(I.mock(source).getAge(), I.mock(target).getAge(), true);

        assertEquals(5, source.getAge());
        assertEquals(5, target.getAge());

        // unbind repeatedly
        disposable.dispose();
        disposable.dispose();
        disposable.dispose();

        // from source
        source.setAge(17);
        assertEquals(17, source.getAge());
        assertEquals(5, target.getAge());
    }
}
