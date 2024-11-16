/*
 * Copyright (C) 2024 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.core;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import org.junit.jupiter.api.Test;

import kiss.I;
import kiss.Model;
import kiss.sample.bean.GenericBoundedBean;
import kiss.sample.bean.Person;
import kiss.sample.bean.School;
import kiss.sample.bean.StringList;
import kiss.sample.bean.StringMap;
import kiss.sample.bean.Student;

@SuppressWarnings("unused")
class CollectParametersTest {

    static {
        // dirty code to load I class at first
        assert I.class != null;
    }

    @Test
    void parameterIsVariable() {
        Type[] parameters = Model.collectParameters(ArrayList.class, List.class);
        assert parameters.length == 1;
        assert parameters[0] == Object.class;
    }

    @Test
    void parameterizedClass() {
        class Definition implements ParameterInterface<String> {
        }

        Type[] types = Model.collectParameters(Definition.class, ParameterInterface.class);
        assert types.length == 1;
        assert types[0] == String.class;
    }

    @Test
    void parameterizedClassFromSub() {
        class Definition implements ParameterInterface<String> {
        }

        class Sub extends Definition {
        }

        Type[] types = Model.collectParameters(Sub.class, ParameterInterface.class);
        assert types.length == 1;
        assert types[0] == String.class;
    }

    @Test
    void parameterizedRaw() throws Exception {
        class Definition {
            UnaryOperator op;
        }

        Type[] types = Model.collectParameters(Definition.class.getDeclaredField("op").getGenericType(), Function.class);
        assert types.length == 2;
        assert types[0] == Object.class;
        assert types[1] == Object.class;
    }

    @Test
    void parameterizedTypeVariable2() throws Exception {
        abstract class Definition<E> implements UnaryOperator<E> {
        }

        Type[] types = Model.collectParameters(Definition.class, Function.class);
        assert types.length == 2;
        assert types[0] == Object.class;
        assert types[1] == Object.class;
    }

    void parameterizedTypeVariable() throws Exception {
        class Definition<E> {
            UnaryOperator<E> op;
        }

        Type[] types = Model.collectParameters(Definition.class.getDeclaredField("op").getGenericType(), Function.class);
        assert types.length == 2;
        assert types[0] == Object.class;
        assert types[1] == Object.class;
    }

    /**
     * Test {@link Object} parameter with interface.
     */
    @Test
    void testGetParameterizedTypes02() {
        Type[] types = Model.collectParameters(ParameterizedObjectByInterface.class, ParameterInterface.class);
        assert 1 == types.length;
        assert Object.class == types[0];
    }

    /**
     * Test wildcard parameter with interface.
     */
    @Test
    void testGetParameterizedTypes14() {
        Type[] types = Model.collectParameters(ParameterizedWildcardByInterface.class, ParameterInterface.class);
        assert 1 == types.length;
        assert Map.class == types[0];
    }

    /**
     * Test none parameter with interface.
     */
    @Test
    void testGetParameterizedTypes03() {
        Type[] types = Model.collectParameters(ParameterizedNoneByInterface.class, ParameterInterface.class);
        assert 0 == types.length;
    }

    @Test
    void parameterFromOverriddenInterface() {
        Type[] types = Model.collectParameters(TypedExtendedFromInterface.class, ParameterInterface.class);
        assert 1 == types.length;
        assert String.class == types[0];
    }

    @Test
    void parameterFromOverrideInterface() {
        Type[] types = Model.collectParameters(TypedExtendedFromInterface.class, ExtensibleByInterface.class);
        assert 1 == types.length;
        assert String.class == types[0];
    }

    /**
     * Test {@link String} parameter with class.
     */
    @Test
    void testGetParameterizedTypes06() {
        Type[] types = Model.collectParameters(ParameterizedStringByClass.class, ParameterClass.class);
        assert 1 == types.length;
        assert String.class == types[0];
    }

    /**
     * Test {@link Object} parameter with class.
     */
    @Test
    void testGetParameterizedTypes07() {
        Type[] types = Model.collectParameters(ParameterizedObjectByClass.class, ParameterClass.class);
        assert 1 == types.length;
        assert Object.class == types[0];
    }

    /**
     * Test wildcard parameter with class.
     */
    @Test
    void testGetParameterizedTypes15() {
        Type[] types = Model.collectParameters(ParameterizedWildcardByClass.class, ParameterClass.class);
        assert 1 == types.length;
        assert Map.class == types[0];
    }

    /**
     * Test none parameter with class.
     */
    @Test
    void testGetParameterizedTypes08() {
        Type[] types = Model.collectParameters(ParameterizedNoneByClass.class, ParameterClass.class);
        assert 0 == types.length;
    }

    /**
     * Test none parameter with class.
     */
    @Test
    void testGetParameterizedTypes09() {
        Type[] types = Model.collectParameters(ExtendedFromClass.class, ParameterClass.class);
        assert 1 == types.length;
        assert String.class == types[0];
    }

    /**
     * Test parent variable parameter with class.
     */
    @Test
    void testGetParameterizedTypes10() {
        Type[] types = Model.collectParameters(TypedExtendedFromClass.class, ParameterClass.class);
        assert 1 == types.length;
        assert String.class == types[0];
    }

    /**
     * Test parameter from multiple source.
     */
    @Test
    void testGetParameterizedTypes11() {
        Type[] types = Model.collectParameters(ParameterFromMultipleSource.class, ParameterInterface.class);
        assert 1 == types.length;
        assert Type.class == types[0];

        types = Model.collectParameters(ParameterFromMultipleSource.class, ParameterClass.class);
        assert 1 == types.length;
        assert Class.class == types[0];
    }

    /**
     * Test multiple parameter.
     */
    @Test
    void testGetParameterizedTypes12() {
        Type[] types = Model.collectParameters(MultipleParameterClass.class, MultipleParameter.class);
        assert 2 == types.length;
        assert Integer.class == types[0];
        assert Long.class == types[1];
    }

    @Test
    void parameterIsArrayFromInterface() {
        Type[] types = Model.collectParameters(ParameterizedStringArrayByInterface.class, ParameterInterface.class);
        assert 1 == types.length;
        assert String[].class == types[0];
    }

    @Test
    void parameterIsArrayFromClass() {
        Type[] types = Model.collectParameters(ParameterizedStringArrayByClass.class, ParameterClass.class);
        assert 1 == types.length;
        assert String[].class == types[0];
    }

    @Test
    void methodGetPrameterAcceptsNullType() {
        Type[] types = Model.collectParameters(null, ParameterClass.class);
        assert 0 == types.length;
    }

    @Test
    void methodGetPrameterAcceptsNullTarget() {
        Type[] types = Model.collectParameters(ParameterizedStringByClass.class, null);
        assert 0 == types.length;
    }

    @Test
    void subclassHasAnotherParameter() {
        Type[] types = Model.collectParameters(TypedSubClass.class, ParameterClass.class);
        assert 1 == types.length;
        assert String.class == types[0];
    }

    @Test
    void constructorHasParameterClass() {
        Constructor constructor = Model.collectConstructors(ParameterClassConstructor.class)[0];
        Type[] types = Model.collectParameters(constructor.getGenericParameterTypes()[0], ParameterClass.class);
        assert 1 == types.length;
        assert String.class == types[0];
    }

    @Test
    void constructorHasExtendableByClass() {
        Constructor constructor = Model.collectConstructors(ExtensibleByClassConstructor.class)[0];
        Type[] types = Model.collectParameters(constructor.getGenericParameterTypes()[0], ExtensibleByClass.class);
        assert 1 == types.length;
        assert Integer.class == types[0];
    }

    @Test
    void constructorHasArrayParameter() {
        Constructor constructor = Model.collectConstructors(ArrayParameterConstructor.class)[0];
        Type[] types = Model.collectParameters(constructor.getGenericParameterTypes()[0], ParameterClass.class);
        assert 1 == types.length;
        assert String[].class == types[0];
    }

    @Test
    void constructorHasMultipleParameter() {
        Constructor constructor = Model.collectConstructors(MultipleParameterConstructor.class)[0];
        Type[] types = Model.collectParameters(constructor.getGenericParameterTypes()[0], MultipleParameter.class);
        assert 2 == types.length;
        assert Readable.class == types[0];
        assert Appendable.class == types[1];
    }

    @Test
    void constructorHasOverlapParameter() {
        Constructor constructor = Model.collectConstructors(ImplicitParameterConstructor.class)[0];
        Type[] types = Model.collectParameters(constructor.getGenericParameterTypes()[0], ParameterOverlapClass.class);
        assert 1 == types.length;
        assert Map.class == types[0];
    }

    @Test
    void constructorHasOverlappedParameter() {
        Constructor constructor = Model.collectConstructors(ImplicitParameterConstructor.class)[0];
        Type[] types = Model.collectParameters(constructor.getGenericParameterTypes()[0], ParameterClass.class);
        assert 1 == types.length;
        assert String.class == types[0];
    }

    @Test
    void parameterVariableFromInterface() {
        Type[] types = Model.collectParameters(ParameterVariableStringByInterface.class, ParameterVariableInterface.class);
        assert 1 == types.length;
        assert String.class == types[0];
    }

    @Test
    void parameterVariableFromClass() {
        Type[] types = Model.collectParameters(ParameterVariableStringByClass.class, ParameterVariableClass.class);
        assert 1 == types.length;
        assert String.class == types[0];
    }

    @Test
    void list() throws Exception {
        Type[] types = Model.collectParameters(StringList.class, List.class);
        assert 1 == types.length;
        assert String.class == types[0];

        types = Model.collectParameters(StringList.class, ArrayList.class);
        assert 1 == types.length;
        assert String.class == types[0];
    }

    @Test
    void map() throws Exception {
        Type[] types = Model.collectParameters(StringMap.class, Map.class);
        assert 2 == types.length;
        assert String.class == types[0];

        types = Model.collectParameters(StringMap.class, HashMap.class);
        assert 2 == types.length;
        assert String.class == types[0];
    }

    @Test
    void bundedBean() {
        Type[] types = Model.collectParameters(I.make(BoundedBean.class).getClass(), GenericBoundedBean.class);
        assert 1 == types.length;
        assert Student.class == types[0];
    }

    /**
     * @version 2010/02/19 22:37:01
     */
    private static interface ParameterInterface<T> {
    }

    /**
     * @version 2010/02/19 22:37:01
     */
    private static interface ParameterVariableInterface<T extends Serializable> {
    }

    /**
     * @version 2010/02/19 22:50:39
     */
    private static class ParameterClass<T> {
    }

    /**
     * @version 2010/02/19 22:50:39
     */
    private static class ParameterVariableClass<T extends Serializable> {
    }

    /**
     * @version 2010/02/20 0:10:12
     */
    private static class ParameterOverlapClass<S> extends ParameterClass<String> {
    }

    /**
     * @version 2010/02/19 22:46:18
     */
    private static class ParameterVariableStringByInterface implements ParameterVariableInterface<String> {
    }

    /**
     * @version 2010/02/15 15:34:39
     */
    private static class ParameterizedStringArrayByInterface implements ParameterInterface<String[]> {
    }

    /**
     * DOCUMENT.
     * 
     * @version 2008/06/20 12:57:04
     */
    private static class ParameterizedObjectByInterface implements ParameterInterface<Object> {
    }

    /**
     * DOCUMENT.
     * 
     * @version 2008/06/20 12:57:04
     */
    private static class ParameterizedWildcardByInterface<T extends Map> implements ParameterInterface<T> {
    }

    /**
     * DOCUMENT.
     * 
     * @version 2008/06/20 12:57:04
     */
    private static class ParameterizedNoneByInterface implements ParameterInterface {
    }

    /**
     * @version 2010/02/19 22:54:31
     */
    private static class ParameterizedStringByClass extends ParameterClass<String> {
    }

    /**
     * @version 2010/02/19 22:54:31
     */
    private static class ParameterVariableStringByClass extends ParameterVariableClass<String> {
    }

    /**
     * @version 2010/02/15 15:34:39
     */
    private static class ParameterizedStringArrayByClass extends ParameterClass<String[]> {
    }

    /**
     * DOCUMENT.
     * 
     * @version 2008/06/20 12:57:04
     */
    private static class ParameterizedObjectByClass extends ParameterClass<Object> {
    }

    /**
     * DOCUMENT.
     * 
     * @version 2008/06/20 12:57:04
     */
    private static class ParameterizedWildcardByClass<T extends Map> extends ParameterClass<T> {
    }

    /**
     * DOCUMENT.
     * 
     * @version 2008/06/20 12:57:04
     */
    private static class ParameterizedNoneByClass extends ParameterClass {
    }

    /**
     * DOCUMENT.
     * 
     * @version 2008/06/20 12:57:04
     */
    private static class ExtendedFromClass extends ParameterizedStringByClass {
    }

    /**
     * @version 2010/02/15 15:04:45
     */
    private static class ExtensibleByInterface<T> implements ParameterInterface<T> {
    }

    /**
     * DOCUMENT.
     * 
     * @version 2008/06/20 14:44:09
     */
    private static class TypedExtendedFromInterface extends ExtensibleByInterface<String> {
    }

    /**
     * @version 2010/02/15 15:04:51
     */
    private static class ExtensibleByClass<T> extends ParameterClass<T> {
    }

    /**
     * DOCUMENT.
     * 
     * @version 2008/06/20 14:44:09
     */
    private static class TypedExtendedFromClass extends ExtensibleByClass<String> {
    }

    /**
     * DOCUMENT.
     * 
     * @version 2008/06/20 15:15:56
     */
    private static class ParameterFromMultipleSource extends ParameterClass<Class> implements ParameterInterface<Type> {
    }

    /**
     * DOCUMENT.
     * 
     * @version 2008/06/20 15:19:19
     */
    private static interface MultipleParameter<S, T> {
    }

    /**
     * DOCUMENT.
     * 
     * @version 2008/06/20 15:20:00
     */
    private static class MultipleParameterClass implements MultipleParameter<Integer, Long> {
    }

    /**
     * @version 2009/07/19 18:55:16
     */
    @SuppressWarnings("hiding")
    private static class TypedSubClass<Boolean> extends ParameterClass<String> {
    }

    /**
     * @version 2010/02/15 12:55:43
     */
    private static class ParameterClassConstructor {

        private ParameterClassConstructor(ParameterClass<String> param) {
        }
    }

    /**
     * @version 2010/02/15 12:55:43
     */
    private static class ExtensibleByClassConstructor {

        private ExtensibleByClassConstructor(ExtensibleByClass<Integer> param) {
        }
    }

    /**
     * @version 2010/02/15 12:55:43
     */
    private static class ArrayParameterConstructor {

        private ArrayParameterConstructor(ParameterClass<String[]> param) {
        }
    }

    /**
     * @version 2010/02/15 15:06:14
     */
    private static class MultipleParameterConstructor {

        private MultipleParameterConstructor(MultipleParameter<Readable, Appendable> param) {
        }
    }

    /**
     * @version 2010/02/20 0:05:01
     */
    private static class ImplicitParameterConstructor {

        private ImplicitParameterConstructor(ParameterOverlapClass<Map> param) {
        }
    }

    /**
     * @version 2010/02/19 23:43:53
     */
    protected static class BoundedBean extends GenericBoundedBean<Student> {
    }

    @Test
    void complexTypeHierarchy1() {
        Type[] types = Model.collectParameters(Child3.class, Child2.class);
        assert 2 == types.length;
        assert Teacher.class == types[0];
        assert School.class == types[1];

    }

    @Test
    void complexTypeHierarchy2() {
        Type[] types = Model.collectParameters(Child3.class, Child1.class);
        assert 3 == types.length;
        assert Student.class == types[0];
        assert Teacher.class == types[1];
        assert School.class == types[2];
    }

    @Test
    void complexTypeHierarchy3() {
        Type[] types = Model.collectParameters(Child3.class, Root.class);
        assert 2 == types.length;
        assert Student.class == types[0];
        assert Teacher.class == types[1];
    }

    /**
     * @version 2010/02/15 15:11:46
     */
    private static class Assoication<M> {
    }

    /**
     * @version 2010/02/15 15:11:42
     */
    private static class Teacher extends Assoication<School> {
    }

    /**
     * @version 2010/02/15 15:11:40
     */
    private static class Root<W extends Person, M> {
    }

    /**
     * @version 2010/02/15 15:11:38
     */
    private static class Child1<W extends Person, M extends Assoication<R>, R> extends Root<W, M> {
    }

    /**
     * @version 2010/02/15 15:11:36
     */
    private static class Child2<M extends Assoication<R>, R> extends Child1<Student, M, R> {
    }

    /**
     * @version 2010/02/15 15:11:33
     */
    private static class Child3 extends Child2<Teacher, School> {
    }

    @Test
    void complexTypeHierarchy4() {
        Type[] types = Model.collectParameters(CheckBox.class, Widget.class);
        assert 1 == types.length;
        assert UserStyle.class == types[0];
    }

    /**
     * @version 2016/05/27 22:33:12
     */
    static class StyleDescriptor {
    }

    /**
     * @version 2016/09/11 2:47:58
     */
    static class PieceStyle extends StyleDescriptor {
    }

    /**
     * @version 2016/05/27 22:32:52
     */
    static class UserStyle extends PieceStyle {
    }

    /**
     * @version 2016/05/27 22:34:05
     */
    static interface Declarable {
    }

    /**
     * @version 2016/05/27 22:33:47
     */
    static abstract class Widget<Styles extends StyleDescriptor> implements Declarable {
    }

    /**
     * @version 2016/05/27 22:33:36
     */
    static abstract class LowLevelWidget<Styles extends StyleDescriptor, T extends LowLevelWidget<Styles, T>> extends Widget<Styles> {
    }

    /**
     * @version 2016/05/27 22:33:01
     */
    static abstract class MarkedButton<T extends MarkedButton<T, V>, V> extends LowLevelWidget<UserStyle, T> {
    }

    /**
     * @version 2016/05/27 22:31:56
     */
    static class CheckBox<V> extends MarkedButton<CheckBox<V>, V> {
    }
}