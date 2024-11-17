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

import org.junit.jupiter.api.Test;

import kiss.I;
import kiss.Model;
import kiss.sample.bean.GenericBoundedBean;
import kiss.sample.bean.Person;
import kiss.sample.bean.School;
import kiss.sample.bean.StringList;
import kiss.sample.bean.StringMap;
import kiss.sample.bean.Student;

class CollectParametersTest {

    static {
        // dirty code to load I class at first
        assert I.class != null;
    }

    private boolean collect(Class target, Class api, Class... results) {
        Type[] params = Model.collectParameters(target, api);
        assert params.length == results.length;
        for (int i = 0; i < params.length; i++) {
            assert params[i] == results[i];
        }
        return true;
    }

    @Test
    void parameterizedClass() {
        class Declare implements ParamInterface1<String> {
        }
        class Sub extends Declare {
        }
        class Deep extends Sub {
        }

        assert Model.collectParameters(Declare.class, ParamInterface1.class)[0] == String.class;
        assert Model.collectParameters(Sub.class, ParamInterface1.class)[0] == String.class;
        assert Model.collectParameters(Deep.class, ParamInterface1.class)[0] == String.class;
    }

    @Test
    void parameterizedClassX() {
        class Declare extends ParamClass1<String> {
        }
        class Sub extends Declare {
        }
        class Deep extends Sub {
        }

        assert Model.collectParameters(Declare.class, ParamClass1.class)[0] == String.class;
        assert Model.collectParameters(Sub.class, ParamClass1.class)[0] == String.class;
        assert Model.collectParameters(Deep.class, ParamClass1.class)[0] == String.class;
    }

    @Test
    void parameterizedArrayClass() {
        class Declare implements ParamInterface1<String[]> {
        }
        class Sub extends Declare {
        }
        class Deep extends Sub {
        }

        assert Model.collectParameters(Declare.class, ParamInterface1.class)[0] == String[].class;
        assert Model.collectParameters(Sub.class, ParamInterface1.class)[0] == String[].class;
        assert Model.collectParameters(Deep.class, ParamInterface1.class)[0] == String[].class;
    }

    @Test
    void parameterizedArrayClassX() {
        class Declare extends ParamClass1<String[]> {
        }
        class Sub extends Declare {
        }
        class Deep extends Sub {
        }

        assert Model.collectParameters(Declare.class, ParamClass1.class)[0] == String[].class;
        assert Model.collectParameters(Sub.class, ParamClass1.class)[0] == String[].class;
        assert Model.collectParameters(Deep.class, ParamClass1.class)[0] == String[].class;
    }

    @Test
    void parameterizedRaw() {
        class Declare implements ParamInterface1 {
        }
        class Sub extends Declare {
        }
        class Deep extends Sub {
        }

        assert Model.collectParameters(Declare.class, ParamInterface1.class).length == 0;
        assert Model.collectParameters(Sub.class, ParamInterface1.class).length == 0;
        assert Model.collectParameters(Deep.class, ParamInterface1.class).length == 0;
    }

    @Test
    void parameterizedRawX() {
        class Declare extends ParamClass1 {
        }
        class Sub extends Declare {
        }
        class Deep extends Sub {
        }

        assert Model.collectParameters(Declare.class, ParamClass1.class).length == 0;
        assert Model.collectParameters(Sub.class, ParamClass1.class).length == 0;
        assert Model.collectParameters(Deep.class, ParamClass1.class).length == 0;
    }

    @Test
    void parameterizedTypeVariable() {
        class Declare<E> implements ParamInterface1<E> {
        }
        class Sub extends Declare {
        }
        class Deep extends Sub {
        }

        assert Model.collectParameters(Declare.class, ParamInterface1.class)[0] == Object.class;
        assert Model.collectParameters(Sub.class, ParamInterface1.class)[0] == Object.class;
        assert Model.collectParameters(Deep.class, ParamInterface1.class)[0] == Object.class;
    }

    @Test
    void parameterizedTypeVariableX() {
        class Declare<E> extends ParamClass1<E> {
        }
        class Sub extends Declare {
        }
        class Deep extends Sub {
        }

        assert Model.collectParameters(Declare.class, ParamClass1.class)[0] == Object.class;
        assert Model.collectParameters(Sub.class, ParamClass1.class)[0] == Object.class;
        assert Model.collectParameters(Deep.class, ParamClass1.class)[0] == Object.class;
    }

    @Test
    void parameterizedTypeVariableOnSub() {
        class Declare<E> implements ParamInterface1<E> {
        }
        class Sub extends Declare<String> {
        }

        Type[] types = Model.collectParameters(Sub.class, ParamInterface1.class);
        assert types[0] == String.class;
    }

    @Test
    void parameterizedTypeVariableOnDeep() {
        class Declare<E> implements ParamInterface1<E> {
        }
        class Sub<E> extends Declare<E> {
        }
        class Deep extends Sub<String> {
        }

        assert Model.collectParameters(Deep.class, ParamInterface1.class)[0] == String.class;
        assert Model.collectParameters(Deep.class, Declare.class)[0] == String.class;
        assert Model.collectParameters(Deep.class, Sub.class)[0] == String.class;
    }

    @Test
    void parameterizedWildcard() {
        class Declare<E extends List> implements ParamInterface1<E> {
        }
        class Sub extends Declare {
        }
        class Deep extends Sub {
        }

        assert Model.collectParameters(Declare.class, ParamInterface1.class)[0] == List.class;
        assert Model.collectParameters(Sub.class, ParamInterface1.class)[0] == List.class;
        assert Model.collectParameters(Deep.class, ParamInterface1.class)[0] == List.class;
    }

    @Test
    void parameterizedWildcardX() {
        class Declare<E extends List> extends ParamClass1<E> {
        }
        class Sub extends Declare {
        }
        class Deep extends Sub {
        }

        assert Model.collectParameters(Declare.class, ParamClass1.class)[0] == List.class;
        assert Model.collectParameters(Sub.class, ParamClass1.class)[0] == List.class;
        assert Model.collectParameters(Deep.class, ParamClass1.class)[0] == List.class;
    }

    @Test
    void parameterizedWildcardOnSub() {
        abstract class Declare<E> implements ParamInterface1<E> {
        }
        class Sub<E extends List> extends Declare<E> {
        }

        Type[] types = Model.collectParameters(Sub.class, ParamInterface1.class);
        assert types[0] == List.class;
    }

    @Test
    void parameterizedWildcardOnDeep() {
        class Declare<E> implements ParamInterface1<E> {
        }
        class Sub<E> extends Declare<E> {
        }
        class Deep<E extends List> extends Sub<E> {
        }

        assert Model.collectParameters(Deep.class, ParamInterface1.class)[0] == List.class;
        assert Model.collectParameters(Deep.class, Declare.class)[0] == List.class;
        assert Model.collectParameters(Deep.class, Sub.class)[0] == List.class;
    }

    @Test
    void multipleClasses() {
        class Declare extends ParamClass1<String> implements ParamInterface1<Number> {
        }

        assert Model.collectParameters(Declare.class, ParamInterface1.class)[0] == Number.class;
        assert Model.collectParameters(Declare.class, ParamClass1.class)[0] == String.class;
    }

    @Test
    void multipleRaws() {
        class Declare extends ParamClass1 implements ParamInterface1 {
        }

        assert Model.collectParameters(Declare.class, ParamInterface1.class).length == 0;
        assert Model.collectParameters(Declare.class, ParamClass1.class).length == 0;
    }

    @Test
    void multipleTypeVariables() {
        class Declare<S, T> extends ParamClass1<S> implements ParamInterface1<T> {
        }

        assert Model.collectParameters(Declare.class, ParamInterface1.class)[0] == Object.class;
        assert Model.collectParameters(Declare.class, ParamClass1.class)[0] == Object.class;
    }

    @Test
    void multipleTypeVariablesOnSub() {
        class Declare<S, T> extends ParamClass1<S> implements ParamInterface1<T> {
        }
        class Sub extends Declare<String, Number> {
        }

        assert Model.collectParameters(Sub.class, ParamClass1.class)[0] == String.class;
        assert Model.collectParameters(Sub.class, ParamInterface1.class)[0] == Number.class;
    }

    @Test
    void multipleTypeVariablesOnDeep() {
        class Declare<S, T> extends ParamClass1<S> implements ParamInterface1<T> {
        }
        class Sub<S, T> extends Declare<S, T> {
        }
        class Deep extends Sub<String, Number> {
        }

        assert Model.collectParameters(Deep.class, ParamClass1.class)[0] == String.class;
        assert Model.collectParameters(Deep.class, ParamInterface1.class)[0] == Number.class;
    }

    @Test
    void multipleWildcards() {
        class Declare<S, T> extends ParamClass1<S> implements ParamInterface1<T> {
        }

        assert Model.collectParameters(Declare.class, ParamInterface1.class)[0] == Object.class;
        assert Model.collectParameters(Declare.class, ParamClass1.class)[0] == Object.class;
    }

    @Test
    void multipleWildcardsOnSub() {
        class Declare<S, T> extends ParamClass1<S> implements ParamInterface1<T> {
        }
        class Sub<S extends List, T extends Map> extends Declare<S, T> {
        }

        assert Model.collectParameters(Sub.class, ParamClass1.class)[0] == List.class;
        assert Model.collectParameters(Sub.class, ParamInterface1.class)[0] == Map.class;
    }

    @Test
    void multipleWildcardsOnDeep() {
        class Declare<S, T> extends ParamClass1<S> implements ParamInterface1<T> {
        }
        class Sub<S, T> extends Declare<S, T> {
        }
        class Deep<S extends List, T extends Map> extends Sub<S, T> {
        }

        assert Model.collectParameters(Deep.class, ParamClass1.class)[0] == List.class;
        assert Model.collectParameters(Deep.class, ParamInterface1.class)[0] == Map.class;
    }

    @Test
    void biparameterizedClass() {
        class Declare implements ParamInterface2<String, Integer> {
        }
        class Sub extends Declare {
        }
        class Deep extends Sub {
        }

        assert collect(Declare.class, ParamInterface2.class, String.class, Integer.class);
        assert collect(Sub.class, ParamInterface2.class, String.class, Integer.class);
        assert collect(Deep.class, ParamInterface2.class, String.class, Integer.class);
    }

    @Test
    void biparameterizedClassX() {
        class Declare extends ParamClass2<String, Integer> {
        }
        class Sub extends Declare {
        }
        class Deep extends Sub {
        }

        assert collect(Declare.class, ParamClass2.class, String.class, Integer.class);
        assert collect(Sub.class, ParamClass2.class, String.class, Integer.class);
        assert collect(Deep.class, ParamClass2.class, String.class, Integer.class);
    }

    @Test
    void biparameterizedRaw() {
        class Declare implements ParamInterface2 {
        }
        class Sub extends Declare {
        }
        class Deep extends Sub {
        }

        assert Model.collectParameters(Declare.class, ParamInterface2.class).length == 0;
        assert Model.collectParameters(Sub.class, ParamInterface2.class).length == 0;
        assert Model.collectParameters(Deep.class, ParamInterface2.class).length == 0;
    }

    @Test
    void biparameterizedRawX() {
        class Declare extends ParamClass2 {
        }
        class Sub extends Declare {
        }
        class Deep extends Sub {
        }

        assert Model.collectParameters(Declare.class, ParamClass2.class).length == 0;
        assert Model.collectParameters(Sub.class, ParamClass2.class).length == 0;
        assert Model.collectParameters(Deep.class, ParamClass2.class).length == 0;
    }

    @Test
    void biparameterizedTypeVariable() {
        class Declare<E, F> implements ParamInterface2<E, F> {
        }
        class Sub extends Declare {
        }
        class Deep extends Sub {
        }

        assert collect(Declare.class, ParamInterface2.class, Object.class, Object.class);
        assert collect(Sub.class, ParamInterface2.class, Object.class, Object.class);
        assert collect(Deep.class, ParamInterface2.class, Object.class, Object.class);
    }

    @Test
    void biparameterizedTypeVariableX() {
        class Declare<E, F> extends ParamClass2<E, F> {
        }
        class Sub extends Declare {
        }
        class Deep extends Sub {
        }

        assert collect(Declare.class, ParamClass2.class, Object.class, Object.class);
        assert collect(Sub.class, ParamClass2.class, Object.class, Object.class);
        assert collect(Deep.class, ParamClass2.class, Object.class, Object.class);
    }

    @Test
    void biparameterizedTypeVariableOnSub() {
        class Declare<E, F> implements ParamInterface2<E, F> {
        }
        class Sub extends Declare<String, Integer> {
        }

        assert collect(Sub.class, ParamInterface2.class, String.class, Integer.class);
        assert collect(Sub.class, Declare.class, String.class, Integer.class);
    }

    @Test
    void biparameterizedTypeVariableOnDeep() {
        class Declare<E, F> implements ParamInterface2<E, F> {
        }
        class Sub<E, F> extends Declare<E, F> {
        }
        class Deep extends Sub<String, Integer> {
        }

        assert collect(Deep.class, ParamInterface2.class, String.class, Integer.class);
        assert collect(Deep.class, Declare.class, String.class, Integer.class);
        assert collect(Deep.class, Sub.class, String.class, Integer.class);
    }

    @Test
    void biparameterizedWildcard() {
        class Declare<E extends List, F extends Map> implements ParamInterface2<E, F> {
        }
        class Sub extends Declare {
        }
        class Deep extends Sub {
        }

        assert collect(Declare.class, ParamInterface2.class, List.class, Map.class);
        assert collect(Sub.class, ParamInterface2.class, List.class, Map.class);
        assert collect(Deep.class, ParamInterface2.class, List.class, Map.class);
    }

    @Test
    void biparameterizedWildcardX() {
        class Declare<E extends List, F extends Map> extends ParamClass2<E, F> {
        }
        class Sub extends Declare {
        }
        class Deep extends Sub {
        }

        assert collect(Declare.class, ParamClass2.class, List.class, Map.class);
        assert collect(Sub.class, ParamClass2.class, List.class, Map.class);
        assert collect(Deep.class, ParamClass2.class, List.class, Map.class);
    }

    @Test
    void biparameterizedWildcardOnSub() {
        class Declare<E, F> implements ParamInterface2<E, F> {
        }
        class Sub<E extends List, F extends Map> extends Declare<E, F> {
        }

        assert collect(Sub.class, ParamInterface2.class, List.class, Map.class);
        assert collect(Sub.class, Declare.class, List.class, Map.class);
    }

    @Test
    void biparameterizedWildcardOnDeep() {
        class Declare<E, F> implements ParamInterface2<E, F> {
        }
        class Sub<E, F> extends Declare<E, F> {
        }
        class Deep<E extends List, F extends Map> extends Sub<E, F> {
        }

        assert collect(Deep.class, ParamInterface2.class, List.class, Map.class);
        assert collect(Deep.class, Declare.class, List.class, Map.class);
        assert collect(Deep.class, Sub.class, List.class, Map.class);
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
        class Declare<E> extends ParamClass1<String> {
        }
        class Sub<S> extends Declare<List> {
        }
        class Deep<D> extends Sub<Integer> {
        }

        assert collect(Declare.class, ParamClass1.class, String.class);
        assert collect(Sub.class, ParamClass1.class, String.class);
        assert collect(Deep.class, ParamClass1.class, String.class);

        assert collect(Sub.class, Declare.class, List.class);
        assert collect(Deep.class, Declare.class, List.class);

        assert collect(Deep.class, Sub.class, Integer.class);
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

    private static interface ParamInterface1<T> {
    }

    private static interface ParamInterface2<S, T> {
    }

    private static class ParamClass1<T> {
    }

    private static class ParamClass2<S, T> {
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
     * @version 2010/02/15 15:04:51
     */
    private static class ExtensibleByClass<T> extends ParameterClass<T> {
    }

    /**
     * DOCUMENT.
     * 
     * @version 2008/06/20 15:19:19
     */
    private static interface MultipleParameter<S, T> {
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