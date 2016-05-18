/*
 * Copyright (C) 2016 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.core;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JPanel;

import javafx.beans.value.ObservableValue;

import org.junit.Test;

import kiss.I;
import kiss.sample.bean.GenericBoundedBean;
import kiss.sample.bean.StringList;
import kiss.sample.bean.StringMap;
import kiss.sample.bean.Student;

/**
 * @version 2016/05/18 9:52:45
 */
public class CollectParametersTest {

    static {
        // dirty code to load I class at first
        assert I.$loader instanceof ClassLoader;
    }

    @Test
    public void parameterIsVariable() {
        Type[] parameters = I.collectParametersOf(ArrayList.class, List.class);
        assert parameters.length == 1;
        assert parameters[0] == Object.class;
    }

    @Test
    public void parameterIsVariableOnInterface() {
        Type[] parameters = I.collectParametersOf(javafx.beans.property.Property.class, ObservableValue.class);
        assert parameters.length == 1;
        assert parameters[0] == Object.class;
    }

    /**
     * Test {@link String} parameter with interface.
     */
    @Test
    public void testGetParameterizedTypes01() {
        Type[] types = I.collectParametersOf(ParameterizedStringByInterface.class, ParameterInterface.class);
        assert 1 == types.length;
        assert String.class == types[0];
    }

    /**
     * Test {@link Object} parameter with interface.
     */
    @Test
    public void testGetParameterizedTypes02() {
        Type[] types = I.collectParametersOf(ParameterizedObjectByInterface.class, ParameterInterface.class);
        assert 1 == types.length;
        assert Object.class == types[0];
    }

    /**
     * Test wildcard parameter with interface.
     */
    @Test
    public void testGetParameterizedTypes14() {
        Type[] types = I.collectParametersOf(ParameterizedWildcardByInterface.class, ParameterInterface.class);
        assert 1 == types.length;
        assert Map.class == types[0];
    }

    /**
     * Test none parameter with interface.
     */
    @Test
    public void testGetParameterizedTypes03() {
        Type[] types = I.collectParametersOf(ParameterizedNoneByInterface.class, ParameterInterface.class);
        assert 0 == types.length;
    }

    /**
     * Test parent parameter with interface.
     */
    @Test
    public void testGetParameterizedTypes04() {
        Type[] types = I.collectParametersOf(ExtendedFromInterface.class, ParameterInterface.class);
        assert 1 == types.length;
        assert String.class == types[0];
    }

    @Test
    public void parameterFromOverriddenInterface() {
        Type[] types = I.collectParametersOf(TypedExtendedFromInterface.class, ParameterInterface.class);
        assert 1 == types.length;
        assert String.class == types[0];
    }

    @Test
    public void parameterFromOverrideInterface() {
        Type[] types = I.collectParametersOf(TypedExtendedFromInterface.class, ExtensibleByInterface.class);
        assert 1 == types.length;
        assert String.class == types[0];
    }

    /**
     * Test {@link String} parameter with class.
     */
    @Test
    public void testGetParameterizedTypes06() {
        Type[] types = I.collectParametersOf(ParameterizedStringByClass.class, ParameterClass.class);
        assert 1 == types.length;
        assert String.class == types[0];
    }

    /**
     * Test {@link Object} parameter with class.
     */
    @Test
    public void testGetParameterizedTypes07() {
        Type[] types = I.collectParametersOf(ParameterizedObjectByClass.class, ParameterClass.class);
        assert 1 == types.length;
        assert Object.class == types[0];
    }

    /**
     * Test wildcard parameter with class.
     */
    @Test
    public void testGetParameterizedTypes15() {
        Type[] types = I.collectParametersOf(ParameterizedWildcardByClass.class, ParameterClass.class);
        assert 1 == types.length;
        assert Map.class == types[0];
    }

    /**
     * Test none parameter with class.
     */
    @Test
    public void testGetParameterizedTypes08() {
        Type[] types = I.collectParametersOf(ParameterizedNoneByClass.class, ParameterClass.class);
        assert 0 == types.length;
    }

    /**
     * Test none parameter with class.
     */
    @Test
    public void testGetParameterizedTypes09() {
        Type[] types = I.collectParametersOf(ExtendedFromClass.class, ParameterClass.class);
        assert 1 == types.length;
        assert String.class == types[0];
    }

    /**
     * Test parent variable parameter with class.
     */
    @Test
    public void testGetParameterizedTypes10() {
        Type[] types = I.collectParametersOf(TypedExtendedFromClass.class, ParameterClass.class);
        assert 1 == types.length;
        assert String.class == types[0];
    }

    /**
     * Test parameter from multiple source.
     */
    @Test
    public void testGetParameterizedTypes11() {
        Type[] types = I.collectParametersOf(ParameterFromMultipleSource.class, ParameterInterface.class);
        assert 1 == types.length;
        assert Type.class == types[0];

        types = I.collectParametersOf(ParameterFromMultipleSource.class, ParameterClass.class);
        assert 1 == types.length;
        assert Class.class == types[0];
    }

    /**
     * Test multiple parameter.
     */
    @Test
    public void testGetParameterizedTypes12() {
        Type[] types = I.collectParametersOf(MultipleParameterClass.class, MultipleParameter.class);
        assert 2 == types.length;
        assert Integer.class == types[0];
        assert Long.class == types[1];
    }

    @Test
    public void parameterIsArrayFromInterface() {
        Type[] types = I.collectParametersOf(ParameterizedStringArrayByInterface.class, ParameterInterface.class);
        assert 1 == types.length;
        assert String[].class == types[0];
    }

    @Test
    public void parameterIsArrayFromClass() {
        Type[] types = I.collectParametersOf(ParameterizedStringArrayByClass.class, ParameterClass.class);
        assert 1 == types.length;
        assert String[].class == types[0];
    }

    @Test
    public void methodGetPrameterAcceptsNullType() {
        Type[] types = I.collectParametersOf(null, ParameterClass.class);
        assert 0 == types.length;
    }

    @Test
    public void methodGetPrameterAcceptsNullTarget() {
        Type[] types = I.collectParametersOf(ParameterizedStringByClass.class, null);
        assert 0 == types.length;
    }

    @Test
    public void subclassHasAnotherParameter() {
        Type[] types = I.collectParametersOf(TypedSubClass.class, ParameterClass.class);
        assert 1 == types.length;
        assert String.class == types[0];
    }

    @Test
    public void constructorHasParameterClass() {
        Constructor constructor = I.collectConstructorsOf(ParameterClassConstructor.class)[0];
        Type[] types = I.collectParametersOf(constructor.getGenericParameterTypes()[0], ParameterClass.class);
        assert 1 == types.length;
        assert String.class == types[0];
    }

    @Test
    public void constructorHasExtendableByClass() {
        Constructor constructor = I.collectConstructorsOf(ExtensibleByClassConstructor.class)[0];
        Type[] types = I.collectParametersOf(constructor.getGenericParameterTypes()[0], ExtensibleByClass.class);
        assert 1 == types.length;
        assert Integer.class == types[0];
    }

    @Test
    public void constructorHasArrayParameter() {
        Constructor constructor = I.collectConstructorsOf(ArrayParameterConstructor.class)[0];
        Type[] types = I.collectParametersOf(constructor.getGenericParameterTypes()[0], ParameterClass.class);
        assert 1 == types.length;
        assert String[].class == types[0];
    }

    @Test
    public void constructorHasMultipleParameter() {
        Constructor constructor = I.collectConstructorsOf(MultipleParameterConstructor.class)[0];
        Type[] types = I.collectParametersOf(constructor.getGenericParameterTypes()[0], MultipleParameter.class);
        assert 2 == types.length;
        assert Readable.class == types[0];
        assert Appendable.class == types[1];
    }

    @Test
    public void constructorHasOverlapParameter() {
        Constructor constructor = I.collectConstructorsOf(ImplicitParameterConstructor.class)[0];
        Type[] types = I.collectParametersOf(constructor.getGenericParameterTypes()[0], ParameterOverlapClass.class);
        assert 1 == types.length;
        assert Map.class == types[0];
    }

    @Test
    public void constructorHasOverlappedParameter() {
        Constructor constructor = I.collectConstructorsOf(ImplicitParameterConstructor.class)[0];
        Type[] types = I.collectParametersOf(constructor.getGenericParameterTypes()[0], ParameterClass.class);
        assert 1 == types.length;
        assert String.class == types[0];
    }

    @Test
    public void parameterVariableFromInterface() {
        Type[] types = I.collectParametersOf(ParameterVariableStringByInterface.class, ParameterVariableInterface.class);
        assert 1 == types.length;
        assert String.class == types[0];
    }

    @Test
    public void parameterVariableFromClass() {
        Type[] types = I.collectParametersOf(ParameterVariableStringByClass.class, ParameterVariableClass.class);
        assert 1 == types.length;
        assert String.class == types[0];
    }

    @Test
    public void list() throws Exception {
        Type[] types = I.collectParametersOf(StringList.class, List.class);
        assert 1 == types.length;
        assert String.class == types[0];

        types = I.collectParametersOf(StringList.class, ArrayList.class);
        assert 1 == types.length;
        assert String.class == types[0];
    }

    @Test
    public void map() throws Exception {
        Type[] types = I.collectParametersOf(StringMap.class, Map.class);
        assert 2 == types.length;
        assert String.class == types[0];

        types = I.collectParametersOf(StringMap.class, HashMap.class);
        assert 2 == types.length;
        assert String.class == types[0];
    }

    @Test
    public void bundedBean() {
        Type[] types = I.collectParametersOf(I.make(BoundedBean.class).getClass(), GenericBoundedBean.class);
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
    private static class ParameterizedStringByInterface implements ParameterInterface<String> {
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
    private static class ExtendedFromInterface extends ParameterizedStringByInterface {
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
    public void complexTypeHierarchy1() {
        Type[] types = I.collectParametersOf(ConsolesUI.class, StackContainer.class);
        assert 2 == types.length;
        assert Shell.class == types[0];
        assert Console.class == types[1];

    }

    @Test
    public void complexTypeHierarchy2() {
        Type[] types = I.collectParametersOf(ConsolesUI.class, SelectableUI.class);
        assert 3 == types.length;
        assert JPanel.class == types[0];
        assert Shell.class == types[1];
        assert Console.class == types[2];
    }

    @Test
    public void complexTypeHierarchy3() {
        Type[] types = I.collectParametersOf(ConsolesUI.class, UI.class);
        assert 2 == types.length;
        assert JPanel.class == types[0];
        assert Shell.class == types[1];
    }

    /**
     * @version 2010/02/15 15:11:48
     */
    private static interface Model {
    }

    /**
     * @version 2010/02/15 15:11:46
     */
    private static class SingleSelectableMode<M extends Model> implements Model {
    }

    /**
     * @version 2010/02/15 15:11:44
     */
    private static class Console implements Model {
    }

    /**
     * @version 2010/02/15 15:11:42
     */
    private static class Shell extends SingleSelectableMode<Console> {
    }

    /**
     * @version 2010/02/15 15:11:40
     */
    private static class UI<W extends JComponent, M extends Model> {
    }

    /**
     * @version 2010/02/15 15:11:38
     */
    private static class SelectableUI<W extends JComponent, M extends SingleSelectableMode<R>, R extends Model> extends UI<W, M> {
    }

    /**
     * @version 2010/02/15 15:11:36
     */
    private static class StackContainer<M extends SingleSelectableMode<R>, R extends Model> extends SelectableUI<JPanel, M, R> {
    }

    /**
     * @version 2010/02/15 15:11:33
     */
    private static class ConsolesUI extends StackContainer<Shell, Console> {
    }
}
