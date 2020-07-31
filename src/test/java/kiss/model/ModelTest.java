/*
 * Copyright (C) 2020 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.model;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import kiss.I;
import kiss.sample.bean.CompatibleKeyMap;
import kiss.sample.bean.GenericBean;
import kiss.sample.bean.GenericBoundedTypedBean;
import kiss.sample.bean.GenericGetterBean;
import kiss.sample.bean.GenericSetterBean;
import kiss.sample.bean.GenericStringBean;
import kiss.sample.bean.IncompatibleKeyMap;
import kiss.sample.bean.Person;
import kiss.sample.bean.StringList;
import kiss.sample.bean.StringMap;
import kiss.sample.bean.StringMapProperty;
import kiss.sample.bean.Student;
import kiss.sample.bean.WildcardBean;
import kiss.sample.bean.WildcardTypeSetter;
import kiss.sample.bean.modifiers.FinalAccessor;
import kiss.sample.bean.modifiers.InheritanceBean;
import kiss.sample.bean.modifiers.OnlyGetter;
import kiss.sample.bean.modifiers.OnlySetter;
import kiss.sample.bean.modifiers.OverrideFinalAccessor;
import kiss.sample.bean.modifiers.PackagePrivateAccessor;
import kiss.sample.bean.modifiers.PrivateAccessor;
import kiss.sample.bean.modifiers.ProtectedAccessor;
import kiss.sample.bean.modifiers.StaticAccessor;

class ModelTest {

    static {
        // dirty code to load I class at first
        assert I.class != null;
    }

    /**
     * Helper method to assert property.
     */
    private void assertProperty(Model model, String propertyName, Class propertyType) {
        assert model != null;
        assert propertyName != null;
        assert propertyType != null;

        Property property = model.property(propertyName);
        assert property != null;
        assert propertyType == property.model.type;
        assert property.getter != null;
        assert property.setter != null;
    }

    @Test
    void identicalCehck() {
        // from class
        Model model = Model.of(Person.class);
        assert model != null;
        assert Person.class == model.type;

        // from instance
        Person person = I.make(Person.class);
        Model model2 = Model.of(person.getClass());
        assert model != null;
        assert Person.class == model.type;

        // identical check
        assert model == model2;
    }

    @Test
    void load() {
        Model model = Model.of(Model.class);
        assert model != null;
        assert Model.class == model.type;
    }

    @Test
    void wildcardProperty() {
        Model model = Model.of(WildcardBean.class);
        assert model != null;

        Property property = model.property("wildcardList");
        assert property != null;
        assert List.class == property.model.type;
        property = property.model.property("0");
        assert property != null;
        assert Object.class == property.model.type;

        property = model.property("wildcardMap");
        assert property != null;
        assert Map.class == property.model.type;
        property = property.model.property("0");
        assert property != null;
        assert Object.class == property.model.type;
    }

    @Test
    void inheritProperty() {
        Model model = Model.of(InheritanceBean.class);
        assert model != null;
        assert InheritanceBean.class == model.type;

        Property property = model.property("int");
        assert property != null;

        property = model.property("string");
        assert property != null;

        assert 2 == model.properties().size();
    }

    @Test
    void stringMap() {
        Model model = Model.of(StringMapProperty.class);
        assert model != null;

        Property property = model.property("map");
        assert property != null;

        model = property.model;
        assert model != null;
        assert Map.class == model.type;
        assert 0 == model.properties().size();

        property = model.property("test");
        assert property != null;

        model = property.model;
        assert model != null;
        assert String.class == model.type;
    }

    @Test
    void noneAttributeFinalFieldProperty() {
        @SuppressWarnings("unused")
        class FinalFieldProperty<T> {

            public final String attribute = "";

            public final Person noneAttribute = new Person();

            {
                noneAttribute.setFirstName("First");
                noneAttribute.setAge(10);
            }
        }

        Model<FinalFieldProperty> model = Model.of(FinalFieldProperty.class);
        assert model.properties().size() == 1;

        Property person = model.properties().get(0);
        assert person.model.type == Person.class;

        FinalFieldProperty instance = I.make(FinalFieldProperty.class);
        assert instance.noneAttribute.getAge() == 10;
        Person other = new Person();

        model.set(instance, person, other);
        assert instance.noneAttribute.getAge() == 10;

        Person retrieved = (Person) model.get(instance, person);
        assert retrieved.getAge() == 10;
    }

    @Test
    void protectedAccessor() {
        Model model = Model.of(ProtectedAccessor.class);
        assert model != null;

        List<Property> list = model.properties();
        assert 3 == list.size();

        ProtectedAccessor accessor = I.make(ProtectedAccessor.class);
        Property property = model.property("getter");
        model.set(accessor, property, "access by protected getter");
        assert model.get(accessor, property).equals("access by protected getter");

        property = model.property("setter");
        model.set(accessor, property, "access by protected setter");
        assert model.get(accessor, property).equals("access by protected setter");

        property = model.property("both");
        model.set(accessor, property, "access by protected accessors");
        assert model.get(accessor, property).equals("access by protected accessors");
    }

    @Test
    void packagePrivateAccessor() {
        Model model = Model.of(PackagePrivateAccessor.class);
        assert model != null;

        List<Property> list = model.properties();
        assert 3 == list.size();

        PackagePrivateAccessor accessor = I.make(PackagePrivateAccessor.class);
        Property property = model.property("getter");
        model.set(accessor, property, "access by package-private getter");
        assert model.get(accessor, property).equals("access by package-private getter");

        property = model.property("setter");
        model.set(accessor, property, "access by package-private setter");
        assert model.get(accessor, property).equals("access by package-private setter");

        property = model.property("both");
        model.set(accessor, property, "access by package-private accessors");
        assert model.get(accessor, property).equals("access by package-private accessors");
    }

    @Test
    void privateAccessor() {
        Model model = Model.of(PrivateAccessor.class);
        assert model != null;

        List<Property> list = model.properties();
        assert 3 == list.size();

        PrivateAccessor accessor = I.make(PrivateAccessor.class);
        Property property = model.property("getter");
        model.set(accessor, property, "access by private getter");
        assert model.get(accessor, property).equals("access by private getter");

        property = model.property("setter");
        model.set(accessor, property, "access by private setter");
        assert model.get(accessor, property).equals("access by private setter");

        property = model.property("both");
        model.set(accessor, property, "access by private accessors");
        assert model.get(accessor, property).equals("access by private accessors");
    }

    @Test
    void finalAccessor() {
        Model model = Model.of(FinalAccessor.class);
        assert model != null;

        List<Property> list = model.properties();
        assert 3 == list.size();

        FinalAccessor accessor = I.make(FinalAccessor.class);
        Property property = model.property("getter");
        model.set(accessor, property, "access by final getter");
        assert model.get(accessor, property).equals("access by final getter");

        property = model.property("setter");
        model.set(accessor, property, "access by final setter");
        assert model.get(accessor, property).equals("access by final setter");

        property = model.property("both");
        model.set(accessor, property, "access by final accessors");
        assert model.get(accessor, property).equals("access by final accessors");
    }

    @Test
    void overrideFinalAccessor() {
        Model model = Model.of(OverrideFinalAccessor.class);
        assert model != null;

        List<Property> list = model.properties();
        assert 3 == list.size();
    }

    /**
     * Test {@link Map} model with the key which can convert to string.
     */
    @Test
    void compatibleKeyMap() {
        Model model = Model.of(CompatibleKeyMap.class);
        assert model != null;

        Property property = model.property("integerKey");
        assert property != null;

        model = property.model;
        assert model != null;
        assert Map.class == model.type;
        assert 0 == model.properties().size();

        property = model.property("1");
        assert property != null;

        model = property.model;
        assert model != null;
        assert Class.class == model.type;
    }

    /**
     * Test {@link Map} model with the key which can convert to string.
     */
    @Test
    void incompatibleKeyMap() {
        Model model = Model.of(IncompatibleKeyMap.class);
        assert model != null;

        Property property = model.property("incompatible");
        assert property != null;

        model = property.model;
        assert model != null;
        assert Map.class == model.type;
        assert 0 == model.properties().size();
    }

    /**
     * Valid Bean Model.
     */
    @Test
    void testModel06() {
        Model model = Model.of(GenericStringBean.class);
        assert model != null;

        Property property = model.property("generic");
        assert property != null;
        assert String.class == property.model.type;

        property = model.property("genericList");
        assert property != null;
        assert List.class == property.model.type;

        property = property.model.property("0");
        assert property != null;
        assert String.class == property.model.type;

        property = model.property("genericMap");
        assert property != null;
        assert Map.class == property.model.type;

        property = property.model.property("0");
        assert property != null;
        assert String.class == property.model.type;
    }

    /**
     * Valid Bean Model.
     */
    @Test
    void testGenericObjectModel() {
        Model model = Model.of(ModelBean.class);
        assert model != null;

        Property property = model.property("generic");
        assert property != null;
        assert Person.class == property.model.type;

        property = model.property("genericList");
        assert property != null;
        assert List.class == property.model.type;

        property = property.model.property("0");
        assert property != null;
        assert Person.class == property.model.type;

        property = model.property("genericMap");
        assert property != null;
        assert Map.class == property.model.type;

        property = property.model.property("0");
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
    void testGenericListModel() {
        Model model = Model.of(ListBean.class);
        assert model != null;

        Property property = model.property("generic");
        assert property != null;
        assert List.class == property.model.type;

        property = property.model.property("0");
        assert property != null;
        assert String.class == property.model.type;

        property = model.property("genericList");
        assert property != null;
        assert List.class == property.model.type;

        property = property.model.property("0");
        assert property != null;
        assert List.class == property.model.type;

        property = property.model.property("0");
        assert property != null;
        assert String.class == property.model.type;

        property = model.property("genericMap");
        assert property != null;
        assert Map.class == property.model.type;

        property = property.model.property("0");
        assert property != null;
        assert List.class == property.model.type;

        property = property.model.property("0");
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
    void testGenericMapModel() {
        Model model = Model.of(MapBean.class);
        assert model != null;

        Property property = model.property("generic");
        assert property != null;
        assert Map.class == property.model.type;

        property = property.model.property("0");
        assert property != null;
        assert Integer.class == property.model.type;

        property = model.property("genericList");
        assert property != null;
        assert List.class == property.model.type;

        property = property.model.property("0");
        assert property != null;
        assert Map.class == property.model.type;

        property = property.model.property("0");
        assert property != null;
        assert Integer.class == property.model.type;

        property = model.property("genericMap");
        assert property != null;
        assert Map.class == property.model.type;

        property = property.model.property("0");
        assert property != null;
        assert Map.class == property.model.type;

        property = property.model.property("0");
        assert property != null;
        assert Integer.class == property.model.type;
    }

    /**
     * @version 2009/07/15 12:49:55
     */
    private static class MapBean extends GenericBean<Map<String, Integer>> {
    }

    @Test
    void list() {
        Model model = Model.of(StringList.class);
        assert model instanceof ListModel;
    }

    @Test
    void map() {
        Model model = Model.of(StringMap.class);
        assert model instanceof MapModel;
    }

    @Test
    void testGenericGetterBean() {
        Model model = Model.of(GenericGetterBean.class);

        assertProperty(model, "generic", String.class);
        assertProperty(model, "genericList", List.class);
        assertProperty(model, "genericMap", Map.class);
    }

    @Test
    void testGenericSetterBean() {
        Model model = Model.of(GenericSetterBean.class);

        assertProperty(model, "generic", String.class);
        assertProperty(model, "genericList", List.class);
        assertProperty(model, "genericMap", Map.class);
    }

    @Test
    void testGenericBoundedTypedBean() {
        Model model = Model.of(GenericBoundedTypedBean.class);

        assertProperty(model, "generic", Student.class);
        assertProperty(model, "genericList", List.class);
        assertProperty(model, "genericMap", Map.class);
    }

    @Test
    void testWildcardTypeSetterBean() {
        Model model = Model.of(WildcardTypeSetter.class);

        assertProperty(model, "list", List.class);
    }

    /**
     * Test the class which has no property.
     */
    @Test
    void testInvalidBean02() {
        Model model = Model.of(OnlyGetter.class);
        assert model != null;

        List<Property> list = model.properties();
        assert 0 == list.size();
    }

    /**
     * Test the class which has no property.
     */
    @Test
    void testInvalidBean03() {
        Model model = Model.of(OnlySetter.class);
        assert model != null;

        List<Property> list = model.properties();
        assert 0 == list.size();
    }

    /**
     * Test the class which has no property.
     */
    @Test
    void testInvalidBean06() {
        Model model = Model.of(StaticAccessor.class);
        assert model != null;

        List<Property> list = model.properties();
        assert 0 == list.size();
    }

    /**
     * Properties are unmodifiable.
     */
    @Test
    void testUnmodifiable01() {
        assertThrows(UnsupportedOperationException.class, () -> {
            Model model = Model.of(Person.class);
            List<Property> properties = model.properties();
            properties.clear();
        });
    }

    /**
     * Properties are unmodifiable.
     */
    @Test
    void testUnmodifiable02() {
        assertThrows(UnsupportedOperationException.class, () -> {
            Model model = Model.of(Person.class);
            List<Property> properties = model.properties();
            properties.remove(0);
        });
    }

    /**
     * Properties are unmodifiable.
     */
    @Test
    void testUnmodifiable03() {
        assertThrows(UnsupportedOperationException.class, () -> {
            Model model = Model.of(Person.class);
            List<Property> properties = model.properties();
            properties.add(new Property(model, "test", null));
        });
    }

    /**
     * Properties are unmodifiable.
     */
    @Test
    void testUnmodifiable04() {
        assertThrows(UnsupportedOperationException.class, () -> {
            Model model = Model.of(Person.class);
            List<Property> properties = model.properties();
            properties.iterator().remove();
        });
    }

    /**
     * Properties are unmodifiable.
     */
    @Test
    void testUnmodifiable05() {
        assertThrows(UnsupportedOperationException.class, () -> {
            Model model = Model.of(Person.class);
            List<Property> properties = model.properties();
            properties.sort(Comparator.comparing(v -> v.name));
        });
    }

    @Test
    void proxy() {
        ProxyModel proxy = I.make(ProxyModel.class, (p, m, a) -> null);
        Model model = Model.of(proxy);
        List<Property> properties = model.properties();

        assert properties.size() == 1;
    }

    /**
     * @version 2017/02/09 20:35:34
     */
    static interface ProxyModel {
        int getValue();

        void setValue(int v);
    }
}