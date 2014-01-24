/*
 * Copyright (C) 2014 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.core;

import kiss.Disposable;
import kiss.I;
import kiss.sample.bean.ChainBean;
import kiss.sample.bean.GenericBean;
import kiss.sample.bean.GenericGetterBean;
import kiss.sample.bean.GenericSetterBean;
import kiss.sample.bean.GenericStringBean;
import kiss.sample.bean.Person;
import kiss.sample.bean.School;
import kiss.sample.bean.Student;
import kiss.sample.bean.TransientBean;

import org.junit.Test;

/**
 * @version 2014/01/23 16:18:01
 */
public class PropertyBindingTest {

    /**
     * Bind.
     */
    @Test
    public void testBind01() {
        Person source = I.make(Person.class);
        source.setAge(5);
        Person target = I.make(Person.class);
        target.setAge(10);

        assert 5 == source.getAge();
        assert 10 == target.getAge();

        // bind
        I.bind(I.mock(source).getAge(), I.mock(target).getAge(), true);

        assert 5 == source.getAge();
        assert 5 == target.getAge();

        // from source
        source.setAge(1);
        assert 1 == source.getAge();
        assert 1 == target.getAge();

        // from target
        target.setAge(4);
        assert 4 == source.getAge();
        assert 4 == target.getAge();
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

        assert "kuma" == source.getFirstName();
        assert "kuma" == source.getLastName();
        assert "uma" == target.getFirstName();
        assert "uma" == target.getLastName();

        // bind
        I.bind(I.mock(source).getFirstName(), I.mock(target).getLastName(), true);

        assert "kuma" == source.getFirstName();
        assert "kuma" == source.getLastName();
        assert "uma" == target.getFirstName();
        assert "kuma" == target.getLastName();

        // from source
        source.setFirstName("test");
        assert "test" == source.getFirstName();
        assert "kuma" == source.getLastName();
        assert "uma" == target.getFirstName();
        assert "test" == target.getLastName();

        // from target
        target.setLastName("change");
        assert "change" == source.getFirstName();
        assert "kuma" == source.getLastName();
        assert "uma" == target.getFirstName();
        assert "change" == target.getLastName();
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

        assert null == source.getSchool();
        assert null == target.getSchool();

        // bind
        I.bind(I.mock(source).getSchool(), I.mock(target).getSchool(), true);

        assert null == source.getSchool();
        assert null == target.getSchool();

        // from source
        source.setSchool(lulim);
        assert lulim == source.getSchool();
        assert lulim == target.getSchool();

        // from target
        target.setSchool(spica);
        assert spica == source.getSchool();
        assert spica == target.getSchool();
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

        assert null == source.getSchool();
        assert null == target.getSchool();

        // bind
        I.bind(I.mock(source).getSchool().getName(), I.mock(target).getSchool().getName(), true);

        assert null == source.getSchool();
        assert null == target.getSchool();

        // from source
        source.setSchool(lulim);
        assert lulim == source.getSchool();
        assert "lulim" == source.getSchool().getName();
        assert null == target.getSchool();

        // from target
        target.setSchool(spica);
        assert lulim == source.getSchool();
        assert "spica" == source.getSchool().getName();
        assert spica == target.getSchool();
        assert "spica" == target.getSchool().getName();

        // nested property change from source
        lulim.setName("lulim");
        assert lulim == source.getSchool();
        assert "lulim" == source.getSchool().getName();
        assert spica == target.getSchool();
        assert "lulim" == target.getSchool().getName();

        // nested property change from target
        spica.setName("spica");
        assert lulim == source.getSchool();
        assert "spica" == source.getSchool().getName();
        assert spica == target.getSchool();
        assert "spica" == target.getSchool().getName();
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

        assert lulim == source.getSchool();
        assert "lulim" == source.getSchool().getName();
        assert spica == target.getSchool();
        assert "spica" == target.getSchool().getName();

        // bind
        I.bind(I.mock(source).getSchool().getName(), I.mock(target).getSchool().getName(), true);

        assert lulim == source.getSchool();
        assert "lulim" == source.getSchool().getName();
        assert spica == target.getSchool();
        assert "lulim" == target.getSchool().getName();

        // null from source
        source.setSchool(null);
        assert null == source.getSchool();
        assert spica == target.getSchool();
        assert null == target.getSchool().getName();

        assert "lulim" == lulim.getName();
        assert null == spica.getName();

        // change nested property which is removed from binding
        lulim.setName("no effect");
        assert "no effect" == lulim.getName();
        assert null == spica.getName();

        // change nested property which is belong to binding without source
        spica.setName("effect");
        assert "no effect" == lulim.getName();
        assert "effect" == spica.getName();

        // make source graph valid (source has high priority)
        source.setSchool(lulim);
        assert lulim == source.getSchool();
        assert "no effect" == source.getSchool().getName();
        assert spica == target.getSchool();
        assert "no effect" == target.getSchool().getName();

        // change from nested object directly
        spica.setName("change");
        assert lulim == source.getSchool();
        assert "change" == source.getSchool().getName();
        assert spica == target.getSchool();
        assert "change" == target.getSchool().getName();
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

        assert 5 == source.getAge();
        assert "test" == target.getFirstName();

        // bind
        I.bind(I.mock(source).getAge(), I.mock(target).getFirstName(), true);

        assert 5 == source.getAge();
        assert target.getFirstName().equals("5");

        // from source
        source.setAge(1);
        assert 1 == source.getAge();
        assert target.getFirstName().equals("1");

        // from target
        target.setFirstName("23");
        assert 23 == source.getAge();
        assert target.getFirstName().equals("23");
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

        assert 5 == source.getAge();
        assert "test" == target.getFirstName();

        // bind
        I.bind(I.mock(source).getAge(), I.mock(target).getFirstName(), true);

        assert 5 == source.getAge();
        assert target.getFirstName().equals("5");

        // from source
        source.setAge(1);
        assert 1 == source.getAge();
        assert target.getFirstName().equals("1");

        // from target
        target.setFirstName("invalid");
        assert 1 == source.getAge();
        assert "invalid" == target.getFirstName();
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
        assert three == two.getNext();
        assert third == second.getNext();

        // bind
        I.bind(I.mock(one).getNext().getNext(), I.mock(first).getNext().getNext(), true);

        // this is initial binding
        // one - two - three
        // first - second - three
        assert two == one.getNext();
        assert three == two.getNext();
        assert second == first.getNext();
        assert three == second.getNext();

        // from source at last path
        // one - two - third
        // first - second - third
        two.setNext(third);
        assert two == one.getNext();
        assert third == two.getNext();
        assert second == first.getNext();
        assert third == second.getNext();

        // from target at last path
        // one - two - three
        // first - second - three
        second.setNext(three);
        assert two == one.getNext();
        assert three == two.getNext();
        assert second == first.getNext();
        assert three == second.getNext();

        // create anothor chain
        ChainBean tick = I.make(ChainBean.class).setName("tick");
        ChainBean tack = I.make(ChainBean.class).setName("tack");
        tick.setNext(tack);

        // from source at middle path
        // one - tick - tack
        // first - second - tack
        one.setNext(tick);
        assert tick == one.getNext();
        assert tack == tick.getNext();
        assert second == first.getNext();
        assert tack == second.getNext();
        assert three == two.getNext();

        // create anothor chain
        ChainBean foo = I.make(ChainBean.class).setName("foo");
        ChainBean bar = I.make(ChainBean.class).setName("bar");
        foo.setNext(bar);

        // from target at middle path
        // one - tick - bar
        // first - foo - bar
        first.setNext(foo);
        assert tick == one.getNext();
        assert bar == tick.getNext();
        assert foo == first.getNext();
        assert bar == foo.getNext();
        assert three == two.getNext();
        assert tack == second.getNext();

        // no binding change
        two.setNext(third);
        second.setNext(three);
        assert tick == one.getNext(); // as-is
        assert bar == tick.getNext(); // as-is
        assert foo == first.getNext(); // as-is
        assert bar == foo.getNext(); // as-is
        assert third == two.getNext(); // change
        assert three == second.getNext(); // change
    }

    @Test
    public void bindGenericProperty() {
        GenericStringBean source = I.make(GenericStringBean.class);
        GenericStringBean target = I.make(GenericStringBean.class);

        // bind
        I.bind(I.mock(source).getGeneric(), I.mock(target).getGeneric(), true);

        // from source
        source.setGeneric("test");
        assert "test" == source.getGeneric();
        assert "test" == target.getGeneric();
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
        assert "one" == target.getFirstName();

        // from source
        source.setGeneric(two);
        assert "two" == target.getFirstName();
    }

    @Test
    public void bindTransientProperty() {
        TransientBean source = I.make(TransientBean.class);
        source.setBoth(5);
        TransientBean target = I.make(TransientBean.class);
        target.setBoth(10);

        assert 5 == source.getBoth();
        assert 10 == target.getBoth();

        // bind
        I.bind(I.mock(source).getBoth(), I.mock(target).getBoth(), true);

        assert 5 == source.getBoth();
        assert 5 == target.getBoth();

        // from source
        source.setBoth(1);
        assert 1 == source.getBoth();
        assert 1 == target.getBoth();

        // from target
        target.setBoth(4);
        assert 4 == source.getBoth();
        assert 4 == target.getBoth();
    }

    /**
     * @version 2011/03/22 16:42:39
     */
    protected static class GenericNest extends GenericBean<Person> {
    }

    @Test
    public void bindGenericGetterProperty() {
        GenericGetterBean source = I.make(GenericGetterBean.class);
        GenericGetterBean target = I.make(GenericGetterBean.class);

        // bind
        I.bind(I.mock(source).getGeneric(), I.mock(target).getGeneric(), true);

        // from source
        source.setGeneric("test");
        assert "test" == source.getGeneric();
        assert "test" == target.getGeneric();
    }

    @Test
    public void bindGenericSetterProperty() {
        GenericSetterBean source = I.make(GenericSetterBean.class);
        GenericSetterBean target = I.make(GenericSetterBean.class);

        // bind
        I.bind(I.mock(source).getGeneric(), I.mock(target).getGeneric(), true);

        // from source
        source.setGeneric("test");
        assert "test" == source.getGeneric();
        assert "test" == target.getGeneric();
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
    @Test(expected = IllegalArgumentException.class)
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

        assert 5 == source.getAge();
        assert 10 == target.getAge();

        // bind
        I.bind(I.mock(source).getAge(), I.mock(target).getAge(), false);

        assert 5 == source.getAge();
        assert 5 == target.getAge();

        // from source
        source.setAge(1);
        assert 1 == source.getAge();
        assert 1 == target.getAge();

        // from target
        target.setAge(4);
        assert 1 == source.getAge();
        assert 4 == target.getAge();

        // from source
        source.setAge(7);
        assert 7 == source.getAge();
        assert 7 == target.getAge();
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

        assert 5 == source.getAge();
        assert 10 == target.getAge();

        // bind
        Disposable disposable = I.bind(I.mock(source).getAge(), I.mock(target).getAge(), true);

        assert 5 == source.getAge();
        assert 5 == target.getAge();

        // from source
        source.setAge(1);
        assert 1 == source.getAge();
        assert 1 == target.getAge();

        // from target
        target.setAge(4);
        assert 4 == source.getAge();
        assert 4 == target.getAge();

        // unbind
        disposable.dispose();

        // from source
        source.setAge(17);
        assert 17 == source.getAge();
        assert 4 == target.getAge();

        // from target
        target.setAge(71);
        assert 17 == source.getAge();
        assert 71 == target.getAge();
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

        assert 5 == source.getAge();
        assert 10 == target.getAge();

        // bind
        Disposable disposable = I.bind(I.mock(source).getAge(), I.mock(target).getAge(), true);

        assert 5 == source.getAge();
        assert 5 == target.getAge();

        // unbind repeatedly
        disposable.dispose();
        disposable.dispose();
        disposable.dispose();

        // from source
        source.setAge(17);
        assert 17 == source.getAge();
        assert 5 == target.getAge();
    }
}
