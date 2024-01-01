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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import kiss.Model;
import kiss.Property;
import kiss.Variable;
import kiss.sample.bean.EnumProperty;
import kiss.sample.bean.GenericStringBean;
import kiss.sample.bean.Person;

class ModelLensTest {

    record Point(int x, double y) {
    }

    @Test
    void getAtNonAccessibleInstance() {
        Person person = new Person();
        person.setAge(1);

        Model model = Model.of(Person.class);
        assert model.get(person, model.property("age")).equals(1);
    }

    @Test
    void getAtNonAccessibleGenericInstance() {
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
    void setAtNonAccessibleInstance() {
        Person person = new Person();
        Model model = Model.of(Person.class);
        model.set(person, model.property("age"), 1);

        assert 1 == person.getAge();
    }

    @Test
    void setAtNonAccessibleGenericInstance() {
        GenericStringBean bean = new GenericStringBean();
        Model model = Model.of(GenericStringBean.class);
        model.set(bean, model.property("generic"), "value");

        assert bean.getGeneric().equals("value");
    }

    @Test
    void setAtRecord() {
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
    void observeNonVariableProperty() {
        Person instance = new Person();

        Model model = Model.of(instance);
        Variable variable = model.observe(instance, model.property("age")).to();
        assert variable.isAbsent();

        instance.setAge(10);
        assert variable.isAbsent();
    }
}