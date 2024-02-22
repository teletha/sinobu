/*
 * Copyright (C) 2024 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.model;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import kiss.Model;
import kiss.Property;
import kiss.Variable;
import kiss.sample.bean.EnumProperty;
import kiss.sample.bean.GenericStringBean;
import kiss.sample.bean.Person;

public class ModelLensTest {

    record Point(int x, double y) {
    }

    @Test
    public void getProperty() {
        Person person = new Person();
        person.setAge(1);

        Model model = Model.of(Person.class);
        Property property = model.property("age");

        assert model.get(person, property).equals(1);
    }

    @Test
    void getGenericProperty() {
        GenericStringBean bean = new GenericStringBean();
        bean.setGeneric("value");

        Model model = Model.of(GenericStringBean.class);
        assert "value" == model.get(bean, model.property("generic"));
    }

    @Test
    void getAtRecord() {
        Point point = new Point(10, 20);
        Model model = Model.of(Point.class);
        assert (Integer) model.get(point, model.property("x")) == 10;
        assert (Double) model.get(point, model.property("y")) == 20d;
    }

    @Test
    void getNullObject() throws Exception {
        Model model = Model.of(Person.class);
        assert model.get(null, model.property("firstName")) == null;
    }

    @Test
    void getNullProperty() throws Exception {
        Model model = Model.of(Person.class);
        assert model.get(new Person(), (Property) null) == null;
    }

    @Test
    public void setProperty() {
        Person person = new Person();
        Model model = Model.of(Person.class);
        Property property = model.property("age");

        // assign property value
        model.set(person, property, 1);

        assert 1 == person.getAge();
    }

    @Test
    void setGenericProperty() {
        GenericStringBean bean = new GenericStringBean();
        Model model = Model.of(GenericStringBean.class);
        model.set(bean, model.property("generic"), "value");

        assert bean.getGeneric().equals("value");
    }

    @Test
    public void setAtRecord() {
        Point point = new Point(0, 0);
        Model<Point> model = Model.of(Point.class);
        point = model.set(point, model.property("x"), 10);
        assert point.x == 10;
        point = model.set(point, model.property("y"), 20d);
        assert point.y == 20d;
    }

    @Test
    void setNullObject() {
        Model model = Model.of(Person.class);
        Assertions.assertThrows(NullPointerException.class, () -> model.set(null, model.property("firstName"), "ERROR"));
    }

    @Test
    void setNullProperty() {
        Person person = new Person();
        Model model = Model.of(Person.class);
        Assertions.assertThrows(NullPointerException.class, () -> model.set(person, null, "ERROR"));
    }

    @Test
    void setNullValue() {
        Person person = new Person();
        person.setFirstName("OK");
        Model model = Model.of(Person.class);
        model.set(person, model.property("firstName"), null);

        assert person.getFirstName() == null;
    }

    @Test
    void setNullValueOnPrimitive() {
        Person person = new Person();
        person.setAge(10);

        Model model = Model.of(Person.class);
        Assertions.assertThrows(NullPointerException.class, () -> model.set(person, model.property("age"), null));
    }

    @Test
    void setNullValueOnEnum() {
        EnumProperty instance = new EnumProperty();

        Model model = Model.of(instance);
        model.set(instance, model.property("field"), null);
        model.set(instance, model.property("fieldWithDefault"), null);

        assert instance.field == null;
        assert instance.fieldWithDefault == null;
    }

    @Test
    public void observeVariableProperty() {
        class Item {
            public Variable<Integer> count = Variable.of(0);
        }

        Item item = new Item();
        Model model = Model.of(Item.class);
        List<Integer> values = model.observe(item, model.property("count")).toList();
        assert values.isEmpty();

        item.count.set(1);
        item.count.set(2);
        item.count.set(3);
        assert values.get(0) == 1;
        assert values.get(1) == 2;
        assert values.get(2) == 3;
    }

    @Test
    void observeNonVariableProperty() {
        Person instance = new Person();

        Model model = Model.of(instance);
        Variable variable = model.observe(instance, model.property("age")).to();
        assert variable.isAbsent();

        instance.setAge(10);
        assert variable.isAbsent();
    }
}