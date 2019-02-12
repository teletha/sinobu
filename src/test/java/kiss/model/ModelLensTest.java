/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.model;

import org.junit.jupiter.api.Test;

import kiss.Variable;
import kiss.sample.bean.EnumProperty;
import kiss.sample.bean.EnumProperty.Value;
import kiss.sample.bean.GenericStringBean;
import kiss.sample.bean.Person;
import kiss.sample.bean.VariablePropertyAtField;

class ModelLensTest {

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

        assert "value" == bean.getGeneric();
    }

    @Test
    void setNullObject() {
        Person person = new Person();
        Model model = Model.of(Person.class);
        model.set(null, model.property("firstName"), "ERROR");

        assert person.getFirstName() == null;
    }

    @Test
    void setNullProperty() {
        Person person = new Person();
        Model model = Model.of(Person.class);
        model.set(person, null, "ERROR");

        assert person.getFirstName() == null;
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
        model.set(person, model.property("age"), null);

        assert person.getAge() == 10;
    }

    @Test
    void setNullValueOnEnum() {
        EnumProperty instance = new EnumProperty();

        Model model = Model.of(instance);
        model.set(instance, model.property("field"), null);
        model.set(instance, model.property("fieldWithDefault"), null);

        assert instance.field == null;
        assert instance.fieldWithDefault == Value.One;
    }

    @Test
    void observeVariableProperty() {
        VariablePropertyAtField instance = new VariablePropertyAtField();

        Model model = Model.of(instance);
        Variable variable = model.observe(instance, model.property("string")).to();
        assert variable.isAbsent();

        instance.string.set("update");
        assert variable.is("update");
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
