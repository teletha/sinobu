/*
 * Copyright (C) 2014 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss;

import java.util.List;

import kiss.sample.bean.Person;
import kiss.sample.bean.School;
import kiss.sample.bean.Student;

import org.junit.Rule;
import org.junit.Test;

import antibug.PrivateModule;

/**
 * @version 2014/01/13 22:27:16
 */
public class ExtensibleTest {

    @Rule
    public static PrivateModule module = new PrivateModule();

    @Test
    public void listByClass() {
        assert 5 == I.find(EPClass.class).size();
    }

    @Test
    public void listByInterface() {
        assert 4 == I.find(EPInterface.class).size();
    }

    @Test
    public void listByNonExtensionPoint() {
        assert 0 == I.find(EPBoth.class).size();
    }

    @Test(expected = NullPointerException.class)
    public void listByNull() {
        I.find(null);
    }

    @Test
    public void unlist() throws Exception {
        assert 5 == I.find(EPClass.class).size();
        module.unload();
        assert 0 == I.find(EPClass.class).size();
    }

    @Test
    public void key() throws Exception {
        KEPClass extension = I.find(KEPClass.class, Person.class);
        assert extension != null;
        assert KEPClassExtension1.class == extension.getClass();

        extension = I.find(KEPClass.class, String.class);
        assert extension != null;
        assert KEPClassExtension2.class == extension.getClass();
    }

    @Test
    public void keyBySubclass() throws Exception {
        assert I.find(KEPClass.class, Student.class) == null;
    }

    @Test
    public void keyByNonExistence() throws Exception {
        assert I.find(KEPClass.class, School.class) == null;
    }

    @Test
    public void keyByNull() throws Exception {
        assert I.find(null, null) == null;
    }

    @Test
    public void keyByNullKey() throws Exception {
        assert I.find(KEPClass.class, null) == null;
    }

    @Test
    public void keyByNullExtensionPoint() throws Exception {
        assert I.find(null, Person.class) == null;
    }

    @Test
    public void unkey() throws Exception {
        KEPClass extension = I.find(KEPClass.class, Person.class);
        assert extension != null;
        assert KEPClassExtension1.class == extension.getClass();

        module.unload();
        assert I.find(KEPClass.class, Person.class) == null;
    }

    // @Test
    // public void unSameKey() throws Exception {
    // KEPClass extension = I.find(KEPClass.class, Integer.class);
    // assert extension != null;
    // assert SameKEP2.class == extension.getClass();
    //
    // I.make(I.class).unload(SameKEP2.class);
    // extension = I.find(KEPClass.class, Integer.class);
    // assert extension != null;
    // assert SameKEP1.class == extension.getClass();
    //
    // I.make(I.class).unload(SameKEP1.class);
    // assert I.find(KEPClass.class, Integer.class) == null;
    // }

    @Test
    public void findAs() throws Exception {
        List<Class<EPClass>> extensions = I.findAs(EPClass.class);
        assert extensions.size() == 5;

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
    @SuppressWarnings("unused")
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
     * @version 2009/12/30 15:40:55
     */
    @SuppressWarnings("unused")
    private static class SameKEP1 extends KEPClass<Integer> {
    }
}
