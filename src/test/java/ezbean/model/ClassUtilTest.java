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

import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.junit.Test;

/**
 * DOCUMENT.
 * 
 * @version 2008/06/20 12:55:31
 */
public class ClassUtilTest {

    /**
     * Test method for {@link ezbean.model.ClassUtil#getTypes(java.lang.Class)}.
     */
    @Test
    public void testGetAllTypes01() {
        Set<Class> classes = ClassUtil.getTypes(ExtendClass.class);
        assertEquals(11, classes.size());
        assertTrue(classes.contains(ExtendClass.class));
    }

    /**
     * Test method for {@link ezbean.model.ClassUtil#getTypes(java.lang.Class)}.
     */
    @Test
    public void testGetAllTypes02() {
        Set<Class> classes = ClassUtil.getTypes(null);
        assertEquals(0, classes.size());
    }

    /**
     * Test none constructor class.
     */
    @Test
    public void testGetMiniConstructor01() {
        Constructor constructor = ClassUtil.getMiniConstructor(NoneConstructor.class);
        assertNotNull(constructor);
    }

    /**
     * Test one constructor class.
     */
    @Test
    public void testGetMiniConstructor02() {
        Constructor constructor = ClassUtil.getMiniConstructor(OneConstructor.class);
        assertNotNull(constructor);
        assertEquals(1, constructor.getParameterTypes().length);
    }

    /**
     * Test two constructor class.
     */
    @Test
    public void testGetMiniConstructor03() {
        Constructor constructor = ClassUtil.getMiniConstructor(TwoConstructor.class);
        assertNotNull(constructor);
        assertEquals(1, constructor.getParameterTypes().length);
    }

    /**
     * Test two constructor class.
     */
    @Test
    public void testGetMiniConstructor04() {
        Constructor constructor = ClassUtil.getMiniConstructor(HashMap.class);
        assertNotNull(constructor);
        assertEquals(0, constructor.getParameterTypes().length);
    }

    /**
     * Test {@link String} parameter with interface.
     */
    @Test
    public void testGetParameterizedTypes01() {
        Type[] types = ClassUtil.getParameter(ParameterizedStringByInterface.class, ParameterInterface.class);
        assertEquals(1, types.length);
        assertEquals(String.class, types[0]);
    }

    /**
     * Test {@link Object} parameter with interface.
     */
    @Test
    public void testGetParameterizedTypes02() {
        Type[] types = ClassUtil.getParameter(ParameterizedObjectByInterface.class, ParameterInterface.class);
        assertEquals(1, types.length);
        assertEquals(Object.class, types[0]);
    }

    /**
     * Test wildcard parameter with interface.
     */
    @Test
    public void testGetParameterizedTypes14() {
        Type[] types = ClassUtil.getParameter(ParameterizedWildcardByInterface.class, ParameterInterface.class);
        assertEquals(1, types.length);
        assertEquals(Map.class, types[0]);
    }

    /**
     * Test none parameter with interface.
     */
    @Test
    public void testGetParameterizedTypes03() {
        Type[] types = ClassUtil.getParameter(ParameterizedNoneByInterface.class, ParameterInterface.class);
        assertEquals(0, types.length);
    }

    /**
     * Test parent parameter with interface.
     */
    @Test
    public void testGetParameterizedTypes04() {
        Type[] types = ClassUtil.getParameter(ExtendedFromInterface.class, ParameterInterface.class);
        assertEquals(1, types.length);
        assertEquals(String.class, types[0]);
    }

    /**
     * Test parent variable parameter with interface.
     */
    @Test
    public void testGetParameterizedTypes05() {
        Type[] types = ClassUtil.getParameter(TypedExtendedFromInterface.class, ParameterInterface.class);
        assertEquals(1, types.length);
        assertEquals(String.class, types[0]);
    }

    /**
     * Test {@link String} parameter with class.
     */
    @Test
    public void testGetParameterizedTypes06() {
        Type[] types = ClassUtil.getParameter(ParameterizedStringByClass.class, ParameterClass.class);
        assertEquals(1, types.length);
        assertEquals(String.class, types[0]);
    }

    /**
     * Test {@link Object} parameter with class.
     */
    @Test
    public void testGetParameterizedTypes07() {
        Type[] types = ClassUtil.getParameter(ParameterizedObjectByClass.class, ParameterClass.class);
        assertEquals(1, types.length);
        assertEquals(Object.class, types[0]);
    }

    /**
     * Test wildcard parameter with class.
     */
    @Test
    public void testGetParameterizedTypes15() {
        Type[] types = ClassUtil.getParameter(ParameterizedWildcardByClass.class, ParameterClass.class);
        assertEquals(1, types.length);
        assertEquals(Map.class, types[0]);
    }

    /**
     * Test none parameter with class.
     */
    @Test
    public void testGetParameterizedTypes08() {
        Type[] types = ClassUtil.getParameter(ParameterizedNoneByClass.class, ParameterClass.class);
        assertEquals(0, types.length);
    }

    /**
     * Test none parameter with class.
     */
    @Test
    public void testGetParameterizedTypes09() {
        Type[] types = ClassUtil.getParameter(ExtendedFromClass.class, ParameterClass.class);
        assertEquals(1, types.length);
        assertEquals(String.class, types[0]);
    }

    /**
     * Test parent variable parameter with class.
     */
    @Test
    public void testGetParameterizedTypes10() {
        Type[] types = ClassUtil.getParameter(TypedExtendedFromClass.class, ParameterClass.class);
        assertEquals(1, types.length);
        assertEquals(String.class, types[0]);
    }

    /**
     * Test parameter from multiple source.
     */
    @Test
    public void testGetParameterizedTypes11() {
        Type[] types = ClassUtil.getParameter(ParameterFromMultipleSource.class, ParameterInterface.class);
        assertEquals(1, types.length);
        assertEquals(Type.class, types[0]);

        types = ClassUtil.getParameter(ParameterFromMultipleSource.class, ParameterClass.class);
        assertEquals(1, types.length);
        assertEquals(Class.class, types[0]);
    }

    /**
     * Test multiple parameter.
     */
    @Test
    public void testGetParameterizedTypes12() {
        Type[] types = ClassUtil.getParameter(MultipleParameterClass.class, MultipleParameter.class);
        assertEquals(2, types.length);
        assertEquals(Integer.class, types[0]);
        assertEquals(Long.class, types[1]);
    }

    /**
     * Test null input.
     */
    @Test
    public void testGetParameterizedTypes13() {
        Type[] types = ClassUtil.getParameter(null, null);
        assertEquals(0, types.length);
    }

    @Test
    public void subclassHasAnothorParameter() {
        Type[] types = ClassUtil.getParameter(TypedSubClass.class, ParameterClass.class);
        assertEquals(1, types.length);
        assertEquals(String.class, types[0]);
    }

    @Test
    public void test() {
        Type[] types = ClassUtil.getParameter(ConsolesUI.class, StackContainer.class);
        assertEquals(2, types.length);
        assertEquals(Shell.class, types[0]);
        assertEquals(Console.class, types[1]);

    }

    @Test
    public void test2() {
        Type[] types = ClassUtil.getParameter(ConsolesUI.class, SelectableUI.class);
        assertEquals(3, types.length);
        assertEquals(JPanel.class, types[0]);
        assertEquals(Shell.class, types[1]);
        assertEquals(Console.class, types[2]);
    }

    @Test
    public void test3() {
        Type[] types = ClassUtil.getParameter(ConsolesUI.class, UI.class);
        assertEquals(2, types.length);
        assertEquals(JPanel.class, types[0]);
        assertEquals(Shell.class, types[1]);
    }

    private static interface Model {

    }

    private static class SingleSelectableMode<M extends Model> implements Model {

    }

    private static class Console implements Model {

    }

    private static class Shell extends SingleSelectableMode<Console> {

    }

    private static class UI<W extends JComponent, M extends Model> {

    }

    private static class SelectableUI<W extends JComponent, M extends SingleSelectableMode<R>, R extends Model>
            extends UI<W, M> {

    }

    private static class StackContainer<M extends SingleSelectableMode<R>, R extends Model>
            extends SelectableUI<JPanel, M, R> {

    }

    private static class ConsolesUI extends StackContainer<Shell, Console> {

    }

    /**
     * Wrap class.
     */
    @Test
    public void testWrap() {
        assertEquals(Integer.class, ClassUtil.wrap(int.class));
        assertEquals(Long.class, ClassUtil.wrap(long.class));
        assertEquals(Float.class, ClassUtil.wrap(float.class));
        assertEquals(Double.class, ClassUtil.wrap(double.class));
        assertEquals(Boolean.class, ClassUtil.wrap(boolean.class));
        assertEquals(Byte.class, ClassUtil.wrap(byte.class));
        assertEquals(Short.class, ClassUtil.wrap(short.class));
        assertEquals(Character.class, ClassUtil.wrap(char.class));
        assertEquals(String.class, ClassUtil.wrap(String.class));
        assertEquals(null, ClassUtil.wrap(null));
    }

    /**
     * DOCUMENT.
     * 
     * @version 2008/06/20 15:51:49
     */
    private static class ExtendClass extends ArrayList {

        private static final long serialVersionUID = -5962628342667538716L;
    }

    /**
     * DOCUMENT.
     * 
     * @version 2008/06/20 15:22:45
     */
    private static class NoneConstructor {
    }

    /**
     * DOCUMENT.
     * 
     * @version 2008/06/20 15:23:02
     */
    private static class OneConstructor {

        /**
         * Create OneConstructor instance.
         */
        private OneConstructor(int i) {
        }
    }

    /**
     * DOCUMENT.
     * 
     * @version 2008/06/20 15:24:29
     */
    private static class TwoConstructor {

        /**
         * Create TwoConstructor instance.
         */
        private TwoConstructor(int i, String name) {
        }

        /**
         * Create TwoConstructor instance.
         */
        private TwoConstructor(int i) {
        }
    }

    /**
     * DOCUMENT.
     * 
     * @version 2008/06/20 12:56:26
     */
    private static interface ParameterInterface<T> {
    }

    /**
     * DOCUMENT.
     * 
     * @version 2008/06/20 12:56:26
     */
    private static class ParameterClass<T> {
    }

    /**
     * DOCUMENT.
     * 
     * @version 2008/06/20 12:57:04
     */
    private static class ParameterizedStringByInterface implements ParameterInterface<String> {
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
     * DOCUMENT.
     * 
     * @version 2008/06/20 12:57:04
     */
    private static class ParameterizedStringByClass extends ParameterClass<String> {
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
     * DOCUMENT.
     * 
     * @version 2008/06/20 14:43:19
     */
    private static class ExtendableByInterface<T> implements ParameterInterface<T> {
    }

    /**
     * DOCUMENT.
     * 
     * @version 2008/06/20 14:44:09
     */
    private static class TypedExtendedFromInterface extends ExtendableByInterface<String> {
    }

    /**
     * DOCUMENT.
     * 
     * @version 2008/06/20 14:43:19
     */
    private static class ExtendableByClass<T> extends ParameterClass<T> {
    }

    /**
     * DOCUMENT.
     * 
     * @version 2008/06/20 14:44:09
     */
    private static class TypedExtendedFromClass extends ExtendableByClass<String> {
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

}
