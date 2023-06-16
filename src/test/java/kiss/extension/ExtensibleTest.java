/*
 * Copyright (C) 2023 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.extension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import kiss.Extensible;
import kiss.I;
import kiss.LoadableTestBase;
import kiss.sample.bean.Person;
import kiss.sample.bean.School;
import kiss.sample.bean.Student;

class ExtensibleTest extends LoadableTestBase {

    @Test
    void listByClass() {
        assert 5 == I.find(EPClass.class).size();
    }

    @Test
    void listByInterface() {
        assert 4 == I.find(EPInterface.class).size();
        assert 0 == I.find(KEPInterface.class).size();
    }

    @Test
    void listByNonExtensionPoint() {
        assert 0 == I.find(EPBoth.class).size();
    }

    @Test
    void listByNull() {
        Assertions.assertThrows(NullPointerException.class, () -> I.find(null));
    }

    @Test
    void key() {
        KEPClass extension = I.find(KEPClass.class, Person.class);
        assert extension != null;
        assert KEPClassExtension1.class == extension.getClass();

        extension = I.find(KEPClass.class, String.class);
        assert extension != null;
        assert KEPClassExtension2.class == extension.getClass();
    }

    @Test
    void keyBySubclass() {
        KEPClass extension = I.find(KEPClass.class, Student.class);
        assert extension != null;
        assert KEPClassExtension1.class == extension.getClass();
    }

    @Test
    void keyBySubinterface() {
        KEPClass extension = I.find(KEPClass.class, ArrayList.class);
        assert extension != null;
        assert KEPClassExtension3.class == extension.getClass();
    }

    @Test
    void keyByNonExistence() {
        assert I.find(KEPClass.class, School.class) == null;
    }

    @Test
    void keyByNull() {
        assert I.find(null, null) == null;
    }

    @Test
    void keyByNullKey() {
        assert I.find(KEPClass.class, null) == null;
    }

    @Test
    void keyByNullExtensionPoint() {
        assert I.find(null, Person.class) == null;
    }

    @Test
    void findAs() {
        List<Class<EPClass>> extensions = I.findAs(EPClass.class);
        assert extensions.size() == 6;

        extensions.clear(); // we can modify
        assert extensions.size() == 0;
        assert I.find(EPClass.class).size() == 5;
    }

    /**
     * @version 2009/12/30 15:40:55
     */
    private static interface EPInterface extends Extensible {
    }

    /**
     * @version 2009/12/30 15:40:55
     */
    private static class EPClass implements Extensible {
    }

    /**
     * @version 2009/12/30 15:40:55
     */
    @SuppressWarnings("unused")
    private static class EPClassExtension1 extends EPClass {
    }

    /**
     * @version 2009/12/30 15:40:55
     */
    @SuppressWarnings("unused")
    private static class EPClassExtension2 extends EPClass {
    }

    /**
     * @version 2009/12/30 15:40:55
     */
    private static abstract class EPClassExtension3 extends EPClass {
    }

    /**
     * @version 2009/12/30 15:40:55
     */
    @SuppressWarnings("unused")
    private static class EPClassExtension4 extends EPClassExtension3 {
    }

    /**
     * @version 2009/12/30 15:40:55
     */
    @SuppressWarnings("unused")
    private static class EPInterfaceExtension1 implements EPInterface {
    }

    /**
     * @version 2009/12/30 15:40:55
     */
    @SuppressWarnings("unused")
    private static class EPInterfaceExtension2 implements EPInterface {
    }

    /**
     * @version 2009/12/30 15:40:55
     */
    private static abstract class EPInterfaceExtension3 implements EPInterface {
    }

    /**
     * @version 2009/12/30 15:40:55
     */
    @SuppressWarnings("unused")
    private static class EPInterfaceExtension4 extends EPInterfaceExtension3 {
    }

    /**
     * @version 2009/12/30 15:40:55
     */
    private static class EPBoth extends EPClass implements EPInterface {
    }

    /**
     * @version 2009/12/30 15:40:55
     */
    private static interface KEPInterface<K> extends Extensible {
    }

    /**
     * @version 2009/12/30 15:40:55
     */
    private static class KEPClass<K> implements Extensible {
    }

    /**
     * @version 2009/12/30 15:40:55
     */
    private static class KEPClassExtension1 extends KEPClass<Person> {
    }

    /**
     * @version 2009/12/30 15:40:55
     */
    private static class KEPClassExtension2 extends KEPClass<String> {
    }

    /**
     * @version 2016/03/29 17:12:03
     */
    private static class KEPClassExtension3 extends KEPClass<List> {
    }

    /**
     * @version 2009/12/30 15:40:55
     */
    @SuppressWarnings("unused")
    private static class SameKEP1 extends KEPClass<Integer> {
    }

    /**
     * @return
     */
    @SuppressWarnings("unused")
    private static KEPInterface<Map> anonymousKEPConcreatClass() {
        return new KEPInterface<Map>() {
        };
    }

    /**
     * @return
     */
    @SuppressWarnings("unused")
    private static <A> KEPInterface<A> anonymousKEPGenericClass() {
        return new KEPInterface<A>() {
        };
    }

    @Test
    void findEnum() {
        List<EnumExtensionPoint> extensions = I.find(EnumExtensionPoint.class);
        assert extensions.size() == 3;
        assert extensions.get(0) == EnumExtension.A;
        assert extensions.get(1) == EnumExtension.B;
        assert extensions.get(2) == EnumExtension.C;
    }

    @Test
    void findAsEnum() {
        List<Class<EnumExtensionPoint>> extensions = I.findAs(EnumExtensionPoint.class);
        assert extensions.size() == 2;
        assert extensions.get(0).equals(EnumExtensionPoint.class);
        assert extensions.get(1).equals(EnumExtension.class);
    }

    private interface EnumExtensionPoint extends Extensible {
    }

    private static enum EnumExtension implements EnumExtensionPoint {
        A, B, C;
    }
}