/*
 * Copyright (C) 2012 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package ezbean.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import ezbean.I;
import ezbean.sample.bean.CompatibleKeyMap;
import ezbean.sample.bean.GenericBean;
import ezbean.sample.bean.GenericBoundedTypedBean;
import ezbean.sample.bean.GenericGetterBean;
import ezbean.sample.bean.GenericSetterBean;
import ezbean.sample.bean.GenericStringBean;
import ezbean.sample.bean.IncompatibleKeyMap;
import ezbean.sample.bean.InheritanceBean;
import ezbean.sample.bean.Person;
import ezbean.sample.bean.StringList;
import ezbean.sample.bean.StringMap;
import ezbean.sample.bean.StringMapProperty;
import ezbean.sample.bean.Student;
import ezbean.sample.bean.WildcardBean;
import ezbean.sample.bean.WildcardTypeSetter;
import ezbean.sample.bean.invalid.FinalAccessor;
import ezbean.sample.bean.invalid.OnlyGetter;
import ezbean.sample.bean.invalid.OnlySetter;
import ezbean.sample.bean.invalid.OverrideFinalAccessor;
import ezbean.sample.bean.invalid.ProtectedAccessor;
import ezbean.sample.bean.invalid.StaticAccessor;

/**
 * @version 2011/03/22 17:02:48
 */
public class ModelTest {

    static {
        // dirty code to load I class at first
        assert I.$loader instanceof ClassLoader;
    }

    /**
     * Helper method to assert property.
     * 
     * @param model
     * @param propertyName
     * @param propertyType
     */
    private void assertProperty(Model model, String propertyName, Class propertyType) {
        assert model != null;
        assert propertyName != null;
        assert propertyType != null;

        Property property = model.getProperty(propertyName);
        assert property != null;
        assert propertyType == property.model.type;
        assert property.accessors[0] != null;
        assert property.accessors[1] != null;
    }

    /**
     * Test identical equality of {@link Model}.
     */
    @Test
    public void testGetModel() throws Exception {
        // from class
        Model model = Model.load(Person.class);
        assert model != null;
        assert Person.class == model.type;

        // from instance
        Person person = I.make(Person.class);
        Model model2 = Model.load((Class) person.getClass());
        assert model != null;
        assert Person.class == model.type;

        // identical check
        assert model == model2;
    }

    /**
     * Valid Bean Model.
     */
    @Test
    public void testModel01() {
        Model model = Model.load(Model.class);
        assert model != null;
        assert Model.class == model.type;
    }

    /**
     * Valid Bean Model.
     */
    @Test
    public void testModel02() {
        Model model = Model.load(WildcardBean.class);
        assert model != null;

        Property property = model.getProperty("wildcardList");
        assert property != null;
        assert List.class == property.model.type;
        property = property.model.getProperty("0");
        assert property != null;
        assert Object.class == property.model.type;

        property = model.getProperty("wildcardMap");
        assert property != null;
        assert Map.class == property.model.type;
        property = property.model.getProperty("0");
        assert property != null;
        assert Object.class == property.model.type;
    }

    /**
     * Valid Bean Model.
     */
    @Test
    public void testModel03() {
        Model model = Model.load(InheritanceBean.class);
        assert model != null;
        assert InheritanceBean.class == model.type;

        Property property = model.getProperty("int");
        assert property != null;

        property = model.getProperty("string");
        assert property != null;

        assert 2 == model.properties.size();
    }

    /**
     * Test {@link Map} model.
     */
    @Test
    public void testModel04() {
        Model model = Model.load(StringMapProperty.class);
        assert model != null;

        Property property = model.getProperty("map");
        assert property != null;

        model = property.model;
        assert model != null;
        assert Map.class == model.type;
        assert 0 == model.properties.size();
        assert true == model.isCollection();
        assert model.codec == null;

        property = model.getProperty("test");
        assert property != null;

        model = property.model;
        assert model != null;
        assert String.class == model.type;
    }

    @Test
    public void protectedAccessor() {
        Model model = Model.load(ProtectedAccessor.class);
        assert model != null;

        List<Property> list = model.properties;
        assert 3 == list.size();

        ProtectedAccessor accessor = I.make(ProtectedAccessor.class);
        Property property = model.getProperty("getter");
        accessor.setGetter("test");
        assert model.get(accessor, property).equals("test");

        property = model.getProperty("setter");
        model.set(accessor, property, "aaa");
        assert accessor.getSetter().equals("aaa");
    }

    /**
     * Test {@link Map} model with the key which can convert to string.
     */
    @Test
    public void compatibleKeyMap() {
        Model model = Model.load(CompatibleKeyMap.class);
        assert model != null;

        Property property = model.getProperty("integerKey");
        assert property != null;

        model = property.model;
        assert model != null;
        assert Map.class == model.type;
        assert 0 == model.properties.size();
        assert true == model.isCollection();
        assert model.codec == null;

        property = model.getProperty("1");
        assert property != null;

        model = property.model;
        assert model != null;
        assert Class.class == model.type;
    }

    /**
     * Test {@link Map} model with the key which can convert to string.
     */
    @Test
    public void incompatibleKeyMap() {
        Model model = Model.load(IncompatibleKeyMap.class);
        assert model != null;

        Property property = model.getProperty("incompatible");
        assert property != null;

        model = property.model;
        assert model != null;
        assert Map.class == model.type;
        assert 0 == model.properties.size();
        assert !model.isCollection();
        assert model.codec == null;
    }

    /**
     * Valid Bean Model.
     */
    @Test
    public void testModel06() {
        Model model = Model.load(GenericStringBean.class);
        assert model != null;

        Property property = model.getProperty("generic");
        assert property != null;
        assert String.class == property.model.type;

        property = model.getProperty("genericList");
        assert property != null;
        assert List.class == property.model.type;

        property = property.model.getProperty("0");
        assert property != null;
        assert String.class == property.model.type;

        property = model.getProperty("genericMap");
        assert property != null;
        assert Map.class == property.model.type;

        property = property.model.getProperty("0");
        assert property != null;
        assert String.class == property.model.type;
    }

    /**
     * Valid Bean Model.
     */
    @Test
    public void testGenericObjectModel() {
        Model model = Model.load(ModelBean.class);
        assert model != null;

        Property property = model.getProperty("generic");
        assert property != null;
        assert Person.class == property.model.type;

        property = model.getProperty("genericList");
        assert property != null;
        assert List.class == property.model.type;

        property = property.model.getProperty("0");
        assert property != null;
        assert Person.class == property.model.type;

        property = model.getProperty("genericMap");
        assert property != null;
        assert Map.class == property.model.type;

        property = property.model.getProperty("0");
        assert property != null;
        assert Person.class == property.model.type;
    }

    /**
     * @version 2009/07/15 12:49:55
     */
    private static class ModelBean extends GenericBean<Person> {
    }

    /**
     * Valid Bean Model.
     */
    @Test
    public void testGenericListModel() {
        Model model = Model.load(ListBean.class);
        assert model != null;

        Property property = model.getProperty("generic");
        assert property != null;
        assert List.class == property.model.type;

        property = property.model.getProperty("0");
        assert property != null;
        assert String.class == property.model.type;

        property = model.getProperty("genericList");
        assert property != null;
        assert List.class == property.model.type;

        property = property.model.getProperty("0");
        assert property != null;
        assert List.class == property.model.type;

        property = property.model.getProperty("0");
        assert property != null;
        assert String.class == property.model.type;

        property = model.getProperty("genericMap");
        assert property != null;
        assert Map.class == property.model.type;

        property = property.model.getProperty("0");
        assert property != null;
        assert List.class == property.model.type;

        property = property.model.getProperty("0");
        assert property != null;
        assert String.class == property.model.type;
    }

    /**
     * @version 2009/07/15 12:49:55
     */
    private static class ListBean extends GenericBean<List<String>> {
    }

    /**
     * Valid Bean Model.
     */
    @Test
    public void testGenericMapModel() {
        Model model = Model.load(MapBean.class);
        assert model != null;

        Property property = model.getProperty("generic");
        assert property != null;
        assert Map.class == property.model.type;

        property = property.model.getProperty("0");
        assert property != null;
        assert Integer.class == property.model.type;

        property = model.getProperty("genericList");
        assert property != null;
        assert List.class == property.model.type;

        property = property.model.getProperty("0");
        assert property != null;
        assert Map.class == property.model.type;

        property = property.model.getProperty("0");
        assert property != null;
        assert Integer.class == property.model.type;

        property = model.getProperty("genericMap");
        assert property != null;
        assert Map.class == property.model.type;

        property = property.model.getProperty("0");
        assert property != null;
        assert Map.class == property.model.type;

        property = property.model.getProperty("0");
        assert property != null;
        assert Integer.class == property.model.type;
    }

    /**
     * @version 2009/07/15 12:49:55
     */
    private static class MapBean extends GenericBean<Map<String, Integer>> {
    }

    @Test
    public void list() throws Exception {
        Model model = Model.load(StringList.class);
        assert model instanceof ListModel;
    }

    @Test
    public void map() throws Exception {
        Model model = Model.load(StringMap.class);
        assert model instanceof MapModel;
    }

    @Test
    public void testGenericGetterBean() {
        Model model = Model.load(GenericGetterBean.class);

        assertProperty(model, "generic", String.class);
        assertProperty(model, "genericList", List.class);
        assertProperty(model, "genericMap", Map.class);
    }

    @Test
    public void testGenericSetterBean() {
        Model model = Model.load(GenericSetterBean.class);

        assertProperty(model, "generic", String.class);
        assertProperty(model, "genericList", List.class);
        assertProperty(model, "genericMap", Map.class);
    }

    @Test
    public void testGenericBoundedTypedBean() {
        Model model = Model.load(GenericBoundedTypedBean.class);

        assertProperty(model, "generic", Student.class);
        assertProperty(model, "genericList", List.class);
        assertProperty(model, "genericMap", Map.class);
    }

    @Test
    public void testWildcardTypeSetterBean() throws Exception {
        Model model = Model.load(WildcardTypeSetter.class);

        assertProperty(model, "list", List.class);
    }

    /**
     * Test the class which has no property.
     */
    @Test
    public void testInvalidBean02() {
        Model model = Model.load(OnlyGetter.class);
        assert model != null;

        List<Property> list = model.properties;
        assert 0 == list.size();
    }

    /**
     * Test the class which has no property.
     */
    @Test
    public void testInvalidBean03() {
        Model model = Model.load(OnlySetter.class);
        assert model != null;

        List<Property> list = model.properties;
        assert 0 == list.size();
    }

    /**
     * Test the class which has no property.
     */
    @Test
    public void testInvalidBean04() {
        Model model = Model.load(FinalAccessor.class);
        assert model != null;

        List<Property> list = model.properties;
        assert 0 == list.size();
    }

    /**
     * Test the class which has no property.
     */
    @Test
    public void testInvalidBean07() {
        Model model = Model.load(OverrideFinalAccessor.class);
        assert model != null;

        List<Property> list = model.properties;
        assert 0 == list.size();
    }

    /**
     * Test the class which has no property.
     */
    @Test
    public void testInvalidBean06() {
        Model model = Model.load(StaticAccessor.class);
        assert model != null;

        List<Property> list = model.properties;
        assert 0 == list.size();
    }

    /**
     * Properties are unmodifiable.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testUnmodifiable01() {
        Model model = Model.load(Person.class);
        List<Property> properties = model.properties;

        properties.clear();
    }

    /**
     * Properties are unmodifiable.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testUnmodifiable02() {
        Model model = Model.load(Person.class);
        List<Property> properties = model.properties;

        properties.remove(0);
    }

    /**
     * Properties are unmodifiable.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testUnmodifiable03() {
        Model model = Model.load(Person.class);
        List<Property> properties = model.properties;

        properties.add(new Property(model, "test"));
    }

    /**
     * Properties are unmodifiable.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testUnmodifiable04() {
        Model model = Model.load(Person.class);
        List<Property> properties = model.properties;

        properties.iterator().remove();
    }

    /**
     * Properties are unmodifiable.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testUnmodifiable05() {
        Model model = Model.load(Person.class);
        List<Property> properties = model.properties;

        Collections.sort(properties);
    }

    @Test
    public void testGetAtNonAccessibleInstance() {
        Person person = new Person();
        person.setAge(1);

        Model model = Model.load(Person.class);
        assert model.get(person, model.getProperty("age")).equals(1);
    }

    @Test
    public void testGetAtNonAccessibleGenericInstance() {
        GenericStringBean bean = new GenericStringBean();
        bean.setGeneric("value");

        Model model = Model.load(GenericStringBean.class);
        assert "value" == model.get(bean, model.getProperty("generic"));
    }

    @Test
    public void testSetAtNonAccessibleInstance() {
        Person person = new Person();
        Model model = Model.load(Person.class);
        model.set(person, model.getProperty("age"), 1);

        assert 1 == person.getAge();
    }

    @Test
    public void testSetAtNonAccessibleGenericInstance() {
        GenericStringBean bean = new GenericStringBean();
        Model model = Model.load(GenericStringBean.class);
        model.set(bean, model.getProperty("generic"), "value");

        assert "value" == bean.getGeneric();
    }

}