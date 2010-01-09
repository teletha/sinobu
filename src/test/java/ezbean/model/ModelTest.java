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
package ezbean.model;

import static org.junit.Assert.*;

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
import ezbean.sample.bean.StringMap;
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
 * DOCUMENT.
 * 
 * @version 2008/06/16 17:18:44
 */
public class ModelTest {

    /**
     * Helper method to assert property.
     * 
     * @param model
     * @param propertyName
     * @param propertyType
     */
    private void assertProperty(Model model, String propertyName, Class propertyType) {
        assertNotNull(model);
        assertNotNull(propertyName);
        assertNotNull(propertyType);

        Property property = model.getProperty(propertyName);
        assertNotNull(property);
        assertEquals(propertyType, property.model.type);
        assertNotNull(property.accessors[0]);
        assertNotNull(property.accessors[1]);
    }

    /**
     * Test identical equality of {@link Model}.
     */
    @Test
    public void testGetModel() throws Exception {
        // from class
        Model<Person> model = Model.load(Person.class);
        assertNotNull(model);
        assertEquals(Person.class, model.type);

        // from instance
        Person person = I.make(Person.class);
        Model<Person> model2 = Model.load((Class) person.getClass());
        assertNotNull(model);
        assertEquals(Person.class, model.type);

        // identical check
        assertEquals(true, model == model2);
    }

    /**
     * Valid Bean Model.
     */
    @Test
    public void testModel01() {
        Model<Model> model = Model.load(Model.class);
        assertNotNull(model);
        assertEquals(Model.class, model.type);
    }

    /**
     * Valid Bean Model.
     */
    @Test
    public void testModel02() {
        Model model = Model.load(WildcardBean.class);
        assertNotNull(model);

        Property property = model.getProperty("wildcardList");
        assertNotNull(property);
        assertEquals(List.class, property.model.type);
        property = property.model.getProperty("0");
        assertNotNull(property);
        assertEquals(Object.class, property.model.type);

        property = model.getProperty("wildcardMap");
        assertNotNull(property);
        assertEquals(Map.class, property.model.type);
        property = property.model.getProperty("0");
        assertNotNull(property);
        assertEquals(Object.class, property.model.type);
    }

    /**
     * Valid Bean Model.
     */
    @Test
    public void testModel03() {
        Model<InheritanceBean> model = Model.load(InheritanceBean.class);
        assertNotNull(model);
        assertEquals(InheritanceBean.class, model.type);

        Property property = model.getProperty("int");
        assertNotNull(property);

        property = model.getProperty("string");
        assertNotNull(property);

        assertEquals(2, model.properties.size());
    }

    /**
     * Test {@link Map} model.
     */
    @Test
    public void testModel04() {
        Model model = Model.load(StringMap.class);
        assertNotNull(model);

        Property property = model.getProperty("map");
        assertNotNull(property);

        model = property.model;
        assertNotNull(model);
        assertEquals(Map.class, model.type);
        assertEquals(0, model.properties.size());
        assertEquals(true, model.isCollection());
        assertNull(model.getCodec());

        property = model.getProperty("test");
        assertNotNull(property);

        model = property.model;
        assertNotNull(model);
        assertEquals(String.class, model.type);
    }

    /**
     * Test {@link Map} model with the key which can convert to string.
     */
    @Test
    public void compatibleKeyMap() {
        Model model = Model.load(CompatibleKeyMap.class);
        assertNotNull(model);

        Property property = model.getProperty("integerKey");
        assertNotNull(property);

        model = property.model;
        assertNotNull(model);
        assertEquals(Map.class, model.type);
        assertEquals(0, model.properties.size());
        assertEquals(true, model.isCollection());
        assertNull(model.getCodec());

        property = model.getProperty("1");
        assertNotNull(property);

        model = property.model;
        assertNotNull(model);
        assertEquals(Class.class, model.type);
    }

    /**
     * Test {@link Map} model with the key which can convert to string.
     */
    @Test
    public void incompatibleKeyMap() {
        Model model = Model.load(IncompatibleKeyMap.class);
        assertNotNull(model);

        Property property = model.getProperty("incompatible");
        assertNotNull(property);

        model = property.model;
        assertNotNull(model);
        assertEquals(Map.class, model.type);
        assertEquals(0, model.properties.size());
        assertEquals(false, model.isCollection());
        assertNull(model.getCodec());
    }

    /**
     * Valid Bean Model.
     */
    @Test
    public void testModel06() {
        Model model = Model.load(GenericStringBean.class);
        assertNotNull(model);

        Property property = model.getProperty("generic");
        assertNotNull(property);
        assertEquals(String.class, property.model.type);

        property = model.getProperty("genericList");
        assertNotNull(property);
        assertEquals(List.class, property.model.type);
        property = property.model.getProperty("0");
        assertNotNull(property);
        assertEquals(String.class, property.model.type);

        property = model.getProperty("genericMap");
        assertNotNull(property);
        assertEquals(Map.class, property.model.type);
        property = property.model.getProperty("0");
        assertNotNull(property);
        assertEquals(String.class, property.model.type);
    }

    /**
     * Valid Bean Model.
     */
    @Test
    public void testGenericObjectModel() {
        Model model = Model.load(ModelBean.class);
        assertNotNull(model);

        Property property = model.getProperty("generic");
        assertNotNull(property);
        assertEquals(Person.class, property.model.type);

        property = model.getProperty("genericList");
        assertNotNull(property);
        assertEquals(List.class, property.model.type);
        property = property.model.getProperty("0");
        assertNotNull(property);
        assertEquals(Person.class, property.model.type);

        property = model.getProperty("genericMap");
        assertNotNull(property);
        assertEquals(Map.class, property.model.type);
        property = property.model.getProperty("0");
        assertNotNull(property);
        assertEquals(Person.class, property.model.type);
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
        assertNotNull(model);

        Property property = model.getProperty("generic");
        assertNotNull(property);
        assertEquals(List.class, property.model.type);
        property = property.model.getProperty("0");
        assertNotNull(property);
        assertEquals(String.class, property.model.type);

        property = model.getProperty("genericList");
        assertNotNull(property);
        assertEquals(List.class, property.model.type);
        property = property.model.getProperty("0");
        assertNotNull(property);
        assertEquals(List.class, property.model.type);
        property = property.model.getProperty("0");
        assertNotNull(property);
        assertEquals(String.class, property.model.type);

        property = model.getProperty("genericMap");
        assertNotNull(property);
        assertEquals(Map.class, property.model.type);
        property = property.model.getProperty("0");
        assertNotNull(property);
        assertEquals(List.class, property.model.type);
        property = property.model.getProperty("0");
        assertNotNull(property);
        assertEquals(String.class, property.model.type);
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
        assertNotNull(model);

        Property property = model.getProperty("generic");
        assertNotNull(property);
        assertEquals(Map.class, property.model.type);
        property = property.model.getProperty("0");
        assertNotNull(property);
        assertEquals(Integer.class, property.model.type);

        property = model.getProperty("genericList");
        assertNotNull(property);
        assertEquals(List.class, property.model.type);
        property = property.model.getProperty("0");
        assertNotNull(property);
        assertEquals(Map.class, property.model.type);
        property = property.model.getProperty("0");
        assertNotNull(property);
        assertEquals(Integer.class, property.model.type);

        property = model.getProperty("genericMap");
        assertNotNull(property);
        assertEquals(Map.class, property.model.type);
        property = property.model.getProperty("0");
        assertNotNull(property);
        assertEquals(Map.class, property.model.type);
        property = property.model.getProperty("0");
        assertNotNull(property);
        assertEquals(Integer.class, property.model.type);
    }

    /**
     * @version 2009/07/15 12:49:55
     */
    private static class MapBean extends GenericBean<Map<String, Integer>> {
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
        Model<OnlyGetter> model = Model.load(OnlyGetter.class);
        assertNotNull(model);

        List<Property> list = model.properties;
        assertEquals(0, list.size());
    }

    /**
     * Test the class which has no property.
     */
    @Test
    public void testInvalidBean03() {
        Model<OnlySetter> model = Model.load(OnlySetter.class);
        assertNotNull(model);

        List<Property> list = model.properties;
        assertEquals(0, list.size());
    }

    /**
     * Test the class which has no property.
     */
    @Test
    public void testInvalidBean04() {
        Model<FinalAccessor> model = Model.load(FinalAccessor.class);
        assertNotNull(model);

        List<Property> list = model.properties;
        assertEquals(0, list.size());
    }

    /**
     * Test the class which has no property.
     */
    @Test
    public void testInvalidBean05() {
        Model model = Model.load(ProtectedAccessor.class);
        assertNotNull(model);

        List<Property> list = model.properties;
        assertEquals(3, list.size());
    }

    /**
     * Test the class which has no property.
     */
    @Test
    public void testInvalidBean07() {
        Model model = Model.load(OverrideFinalAccessor.class);
        assertNotNull(model);

        List<Property> list = model.properties;
        assertEquals(0, list.size());
    }

    /**
     * Test the class which has no property.
     */
    @Test
    public void testInvalidBean06() {
        Model model = Model.load(StaticAccessor.class);
        assertNotNull(model);

        List<Property> list = model.properties;
        assertEquals(0, list.size());
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
        assertEquals(1, model.get(person, model.getProperty("age")));
    }

    @Test
    public void testGetAtNonAccessibleGenericInstance() {
        GenericStringBean bean = new GenericStringBean();
        bean.setGeneric("value");

        Model model = Model.load(GenericStringBean.class);
        assertEquals("value", model.get(bean, model.getProperty("generic")));
    }

    @Test
    public void testSetAtNonAccessibleInstance() {
        Person person = new Person();
        Model model = Model.load(Person.class);
        model.set(person, model.getProperty("age"), 1);

        assertEquals(1, person.getAge());
    }

    @Test
    public void testSetAtNonAccessibleGenericInstance() {
        GenericStringBean bean = new GenericStringBean();
        Model model = Model.load(GenericStringBean.class);
        model.set(bean, model.getProperty("generic"), "value");

        assertEquals("value", bean.getGeneric());
    }

}