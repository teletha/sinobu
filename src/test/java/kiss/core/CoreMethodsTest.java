/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.core;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import antibug.Code;
import kiss.I;
import kiss.sample.bean.FinalBean;
import kiss.sample.bean.Primitive;
import kiss.sample.modifier.Abstract;
import kiss.sample.modifier.Final;
import kiss.sample.modifier.Nested.PublicStatic;
import kiss.sample.modifier.Public;

/**
 * @version 2018/03/31 22:50:28
 */
public class CoreMethodsTest {

    /**
     * Test public class. (top-level class)
     */
    @Test
    public void testInstantiate01() throws Exception {
        assert I.make(Public.class) != null;
    }

    /**
     * Test package private class.(top-level class)
     */
    @Test
    public void testInstantiate02() throws Exception {
        Class clazz = Class.forName("kiss.sample.modifier.PackagePrivate");
        assert clazz != null;
        assert I.make(clazz) != null;
    }

    /**
     * Test Nested public static class. (static member type)
     */
    @Test
    public void testInstantiate03() throws Exception {
        assert I.make(PublicStatic.class) != null;
    }

    /**
     * Test Nested protected static class. (static member type)
     */
    @Test
    public void testInstantiate04() throws Exception {
        Class clazz = Class.forName("kiss.sample.modifier.Nested$ProtectedStatic");
        assert clazz != null;
        assert I.make(clazz) != null;
    }

    /**
     * Test Nested package private static class. (static member type)
     */
    @Test
    public void testInstantiate05() throws Exception {
        Class clazz = Class.forName("kiss.sample.modifier.Nested$PackagePrivateStatic");
        assert clazz != null;
        assert I.make(clazz) != null;
    }

    /**
     * Test Nested private static class. (static member type)
     */
    @Test
    public void testInstantiate06() throws Exception {
        Class clazz = Class.forName("kiss.sample.modifier.Nested$PrivateStatic");
        assert clazz != null;
        assert I.make(clazz) != null;
    }

    /**
     * Test Nested public class. (non-static member type)
     */
    @Test
    public void testInstantiate07() throws Exception {
        assert I.make(kiss.sample.modifier.Nested.Public.class) != null;
    }

    /**
     * Test Nested protected class. (non-static member type)
     */
    @Test
    public void testInstantiate08() throws Exception {
        Class clazz = Class.forName("kiss.sample.modifier.Nested$Protected");
        assert clazz != null;
        assert I.make(clazz) != null;
    }

    /**
     * Test Nested package private class. (non-static member type)
     */
    @Test
    public void testInstantiate09() throws Exception {
        Class clazz = Class.forName("kiss.sample.modifier.Nested$PackagePrivate");
        assert clazz != null;
        assert I.make(clazz) != null;
    }

    /**
     * Test Nested private class. (non-static member type)
     */
    @Test
    public void testInstantiate10() throws Exception {
        Class clazz = Class.forName("kiss.sample.modifier.Nested$Private");
        assert clazz != null;
        assert I.make(clazz) != null;
    }

    @Test
    public void instatiateFinal() throws Exception {
        assert I.make(Final.class) != null;
    }

    public void instatiateFinalBean() throws Exception {
        assert I.make(FinalBean.class) != null;
    }

    @Test
    public void instatiateAbstract() throws Exception {
        assert Code.catches(() -> I.make(Abstract.class)) instanceof InstantiationException;
    }

    @Test
    public void instatiateThrowable() throws Exception {
        assert Code.catches(() -> I.make(ExceptionThrower.class)) instanceof ExceptionThrower.Bug;
        assert Code.catches(() -> I.make(RuntimeThrower.class)) instanceof RuntimeThrower.Bug;
        assert Code.catches(() -> I.make(ErrorThrower.class)) instanceof ErrorThrower.Bug;
    }

    /**
     * Test List.
     */
    @Test
    public void testCollection01() throws Exception {
        assert I.make(List.class) != null;
    }

    /**
     * Test Map.
     */
    @Test
    public void testCollection02() throws Exception {
        assert I.make(Map.class) != null;
    }

    /**
     * Test int name.
     */
    @Test
    public void testReservedName01() {
        Primitive primitive = I.make(Primitive.class);
        assert primitive != null;
        assert 0 == primitive.getInt();

        primitive.setInt(100);
        assert 100 == primitive.getInt();
    }

    /**
     * Test long name.
     */
    @Test
    public void testReservedName02() {
        Primitive primitive = I.make(Primitive.class);
        assert primitive != null;
        assert 0L == primitive.getLong();

        primitive.setLong(100);
        assert 100L == primitive.getLong();
    }

    /**
     * Test boolean name.
     */
    @Test
    public void testReservedName03() {
        Primitive primitive = I.make(Primitive.class);
        assert primitive != null;
        assert false == primitive.isBoolean();

        primitive.setBoolean(true);
        assert true == primitive.isBoolean();
    }

    /**
     * @version 2018/04/02 9:30:58
     */
    private static class RuntimeThrower {

        /**
         * Create RuntimeExceptionClass instance.
         */
        private RuntimeThrower() {
            throw new Bug();
        }

        /**
         * @version 2018/04/02 9:30:55
         */
        private static class Bug extends RuntimeException {

            private static final long serialVersionUID = 6965448734007115961L;
        }
    }

    /**
     * @version 2018/04/02 9:30:52
     */
    private static class ErrorThrower {

        /**
         * Create ErrorClass instance.
         */
        private ErrorThrower() {
            throw new Bug();
        }

        /**
         * @version 2018/04/02 9:30:48
         */
        private static class Bug extends Error {

            private static final long serialVersionUID = 219714084165765163L;
        }
    }

    /**
     * @version 2018/04/02 9:30:43
     */
    private static class ExceptionThrower {

        /**
         * Create ExceptionClass instance.
         */
        private ExceptionThrower() throws Bug {
            throw new Bug();
        }

        /**
         * @version 2018/04/02 9:30:40
         */
        private static class Bug extends Exception {

            private static final long serialVersionUID = 5333091127457345270L;
        }
    }

    // ===============================================================
    // Test Quiet Method
    // ===============================================================
    /**
     * Quiet with <code>null</code> exception.
     */
    @Test
    public void testQuietWithNull() {
        I.quiet((Object) null);
    }

    /**
     * Test checked exception.
     */
    @Test
    public void testCatchException() {
        try {
            throwError();
        } catch (Exception e) {
            assert e instanceof ClassNotFoundException;
        }
    }

    /**
     * Throw error.
     */
    private void throwError() {
        try {
            throw new ClassNotFoundException();
        } catch (ClassNotFoundException e) {
            throw I.quiet(e);
        }
    }

    @Test
    public void testExceptionQuietly() {
        assert Code.catches(() -> I.quiet(new ClassNotFoundException())) instanceof ClassNotFoundException;
        assert Code.catches(() -> I.quiet(new UnsupportedOperationException())) instanceof UnsupportedOperationException;
        assert Code.catches(() -> I.quiet(new LinkageError())) instanceof LinkageError;
    }
}
