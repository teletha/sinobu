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

class CollectParametersTest {

    private static interface ParamInterface1<T> {
    }

    private static interface ParamInterface2<S, T> {
    }

    private static class ParamClass1<T> {
    }

    private static class ParamClass2<S, T> {
    }

    static {
        // dirty code to load I class at first
        assert I.class != null;
    }

    private boolean collect(Type target, Class api, Class... results) {
        Type[] params = Model.collectParameters(target, api);
        assert params.length == results.length;
        for (int i = 0; i < params.length; i++) {
            assert params[i] == results[i];
        }
        return true;
    }

    @Test
    void parameterizedClass() {
        interface Declare extends ParamInterface1<String> {
        }
        interface Sub extends Declare {
        }
        interface Deep extends Sub {
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
        interface Declare extends ParamInterface1<String[]> {
        }
        interface Sub extends Declare {
        }
        interface Deep extends Sub {
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
        interface Declare extends ParamInterface1 {
        }
        interface Sub extends Declare {
        }
        interface Deep extends Sub {
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
        interface Declare<E> extends ParamInterface1<E> {
        }
        interface Sub extends Declare {
        }
        interface Deep extends Sub {
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
        interface Declare<E> extends ParamInterface1<E> {
        }
        interface Sub extends Declare<String> {
        }

        Type[] types = Model.collectParameters(Sub.class, ParamInterface1.class);
        assert types[0] == String.class;
    }

    @Test
    void parameterizedTypeVariableOnDeep() {
        interface Declare<E> extends ParamInterface1<E> {
        }
        interface Sub<E> extends Declare<E> {
        }
        interface Deep extends Sub<String> {
        }

        assert Model.collectParameters(Deep.class, ParamInterface1.class)[0] == String.class;
        assert Model.collectParameters(Deep.class, Declare.class)[0] == String.class;
        assert Model.collectParameters(Deep.class, Sub.class)[0] == String.class;
    }

    @Test
    void parameterizedWildcard() {
        interface Declare<E extends List> extends ParamInterface1<E> {
        }
        interface Sub extends Declare {
        }
        interface Deep extends Sub {
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
        interface Declare<E> extends ParamInterface1<E> {
        }
        interface Sub<E extends List> extends Declare<E> {
        }

        Type[] types = Model.collectParameters(Sub.class, ParamInterface1.class);
        assert types[0] == List.class;
    }

    @Test
    void parameterizedWildcardOnDeep() {
        interface Declare<E> extends ParamInterface1<E> {
        }
        interface Sub<E> extends Declare<E> {
        }
        interface Deep<E extends List> extends Sub<E> {
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
        interface Declare extends ParamInterface2<String, Integer> {
        }
        interface Sub extends Declare {
        }
        interface Deep extends Sub {
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
        interface Declare extends ParamInterface2 {
        }
        interface Sub extends Declare {
        }
        interface Deep extends Sub {
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
        interface Declare<E, F> extends ParamInterface2<E, F> {
        }
        interface Sub extends Declare {
        }
        interface Deep extends Sub {
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
        interface Declare<E, F> extends ParamInterface2<E, F> {
        }
        interface Sub extends Declare<String, Integer> {
        }

        assert collect(Sub.class, ParamInterface2.class, String.class, Integer.class);
        assert collect(Sub.class, Declare.class, String.class, Integer.class);
    }

    @Test
    void biparameterizedTypeVariableOnDeep() {
        interface Declare<E, F> extends ParamInterface2<E, F> {
        }
        interface Sub<E, F> extends Declare<E, F> {
        }
        interface Deep extends Sub<String, Integer> {
        }

        assert collect(Deep.class, ParamInterface2.class, String.class, Integer.class);
        assert collect(Deep.class, Declare.class, String.class, Integer.class);
        assert collect(Deep.class, Sub.class, String.class, Integer.class);
    }

    @Test
    void biparameterizedWildcard() {
        interface Declare<E extends List, F extends Map> extends ParamInterface2<E, F> {
        }
        interface Sub extends Declare {
        }
        interface Deep extends Sub {
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
        interface Declare<E, F> extends ParamInterface2<E, F> {
        }
        interface Sub<E extends List, F extends Map> extends Declare<E, F> {
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
    void nullType() {
        Type[] types = Model.collectParameters(null, ParamInterface1.class);
        assert types.length == 0;
    }

    @Test
    void nullTarget() {
        Type[] types = Model.collectParameters(List.class, null);
        assert types.length == 0;
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
        class ParameterClassConstructor {
            private ParameterClassConstructor(ParamClass1<String> param) {
            }
        }

        Constructor constructor = Model.collectConstructors(ParameterClassConstructor.class)[0];
        Type[] types = Model.collectParameters(constructor.getGenericParameterTypes()[0], ParamClass1.class);
        assert 1 == types.length;
        assert String.class == types[0];
    }

    @Test
    void constructorHasExtendableByClass() {
        interface Declare<E> extends ParamInterface1<E> {
        }
        interface Sub<E> extends Declare<E> {
        }
        interface Deep extends Sub<String> {
        }
        class X {
            private X(Deep first, Sub<Long> second, Declare<? extends Number> third, Sub<String[]> fourth) {
            }
        }

        Type[] params = Model.collectConstructors(X.class)[0].getGenericParameterTypes();
        assert collect(params[0], ParamInterface1.class, String.class);
        assert collect(params[1], ParamInterface1.class, Long.class);
        assert collect(params[2], ParamInterface1.class, Number.class);
        assert collect(params[3], ParamInterface1.class, String[].class);
    }

    @Test
    void constructorHasMultipleParameter() {
        class MultipleParameterConstructor {
            private MultipleParameterConstructor(Function<Readable, Appendable> param) {
            }
        }

        Constructor constructor = Model.collectConstructors(MultipleParameterConstructor.class)[0];
        Type[] types = Model.collectParameters(constructor.getGenericParameterTypes()[0], Function.class);
        assert 2 == types.length;
        assert Readable.class == types[0];
        assert Appendable.class == types[1];
    }

    @Test
    void list() {
        assert collect(StringList.class, List.class, String.class);
        assert collect(StringList.class, ArrayList.class, String.class);
    }

    @Test
    void map() {
        assert collect(StringMap.class, Map.class, String.class, String.class);
        assert collect(StringMap.class, HashMap.class, String.class, String.class);
    }

    @Test
    void bundedBean() {
        class BoundedBean extends GenericBoundedBean<Student> {
        }

        Type[] types = Model.collectParameters(I.make(BoundedBean.class).getClass(), GenericBoundedBean.class);
        assert 1 == types.length;
        assert Student.class == types[0];
    }

    @Test
    void unaryOpereator() {
        interface Declare extends UnaryOperator<String> {
        }

        assert collect(Declare.class, UnaryOperator.class, String.class);
        assert collect(Declare.class, Function.class, String.class, String.class);
    }

    @Test
    void functionPartially() {
        interface Partial<E> extends Function<String, E> {
        }
        interface Declare extends Partial<Integer> {
        }

        assert collect(Declare.class, Function.class, String.class, Integer.class);
        assert collect(Declare.class, Partial.class, Integer.class);

        assert collect(Partial.class, Function.class, String.class, Object.class);
    }

    @Test
    void complexTypeHierarchy1() {
        class Assoication<M> {
        }
        class Teacher extends Assoication<School> {
        }
        class Root<W extends Person, M> {
        }
        class Child1<W extends Person, M extends Assoication<R>, R> extends Root<W, M> {
        }
        class Child2<M extends Assoication<R>, R> extends Child1<Student, M, R> {
        }
        class Child3 extends Child2<Teacher, School> {
        }
    
        assert collect(Child3.class, Child2.class, Teacher.class, School.class);
        assert collect(Child3.class, Child1.class, Student.class, Teacher.class, School.class);
        assert collect(Child3.class, Root.class, Student.class, Teacher.class);
    }

    @Test
    void complexTypeHierarchy2() {
        class StyleDescriptor {
        }
        class PieceStyle extends StyleDescriptor {
        }
        class UserStyle extends PieceStyle {
        }
        class Widget<S extends StyleDescriptor> {
        }
        class LowLevelWidget<S extends StyleDescriptor, T extends LowLevelWidget<S, T>> extends Widget<S> {
        }
        class MarkedButton<T extends MarkedButton<T, V>, V> extends LowLevelWidget<UserStyle, T> {
        }
        class CheckBox<V> extends MarkedButton<CheckBox<V>, V> {
        }
    
        assert collect(CheckBox.class, Widget.class, UserStyle.class);
        assert collect(CheckBox.class, LowLevelWidget.class, UserStyle.class, CheckBox.class);
    }
}