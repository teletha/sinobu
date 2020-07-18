/*
 * Copyright (C) 2020 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.core;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import kiss.I;
import kiss.sample.bean.FinalBean;
import kiss.sample.bean.Primitive;
import kiss.sample.modifier.Abstract;
import kiss.sample.modifier.Final;
import kiss.sample.modifier.Nested.PublicStatic;
import kiss.sample.modifier.Public;

class MakeTest {

    @Test
    void publicClass() throws Exception {
        assert I.make(Public.class) != null;
    }

    @Test
    void packagePrivate() throws Exception {
        Class clazz = Class.forName("kiss.sample.modifier.PackagePrivate");
        assert clazz != null;
        assert I.make(clazz) != null;
    }

    @Test
    void memberPublicStatic() throws Exception {
        assert I.make(PublicStatic.class) != null;
    }

    @Test
    void memberProtectedStatic() throws Exception {
        Class clazz = Class.forName("kiss.sample.modifier.Nested$ProtectedStatic");
        assert clazz != null;
        assert I.make(clazz) != null;
    }

    @Test
    void memberPackagePrivateStatic() throws Exception {
        Class clazz = Class.forName("kiss.sample.modifier.Nested$PackagePrivateStatic");
        assert clazz != null;
        assert I.make(clazz) != null;
    }

    @Test
    void memberPrivateStatic() throws Exception {
        Class clazz = Class.forName("kiss.sample.modifier.Nested$PrivateStatic");
        assert clazz != null;
        assert I.make(clazz) != null;
    }

    @Test
    void memberPublicNonStatic() throws Exception {
        assert I.make(kiss.sample.modifier.Nested.Public.class) != null;
    }

    @Test
    void memberProtectedNonStatic() throws Exception {
        Class clazz = Class.forName("kiss.sample.modifier.Nested$Protected");
        assert clazz != null;
        assert I.make(clazz) != null;
    }

    @Test
    void memberPackagePrivateNonStatic() throws Exception {
        Class clazz = Class.forName("kiss.sample.modifier.Nested$PackagePrivate");
        assert clazz != null;
        assert I.make(clazz) != null;
    }

    @Test
    void memberPrivateNonStatic() throws Exception {
        Class clazz = Class.forName("kiss.sample.modifier.Nested$Private");
        assert clazz != null;
        assert I.make(clazz) != null;
    }

    @Test
    void finalClass() throws Exception {
        assert I.make(Final.class) != null;
    }

    void finalBeanLikeClass() throws Exception {
        assert I.make(FinalBean.class) != null;
    }

    @Test
    void abstractClass() throws Exception {
        assertThrows(InstantiationException.class, () -> I.make(Abstract.class));
    }

    @Test
    void recordClass() {
        for (Constructor<?> c : Point.class.getDeclaredConstructors()) {
            System.out.println(c);
        }
        assert I.make(Point.class) != null;
    }

    record Point(int x, int y) {
    }

    @Test
    void throwRuntimeException() {
        assertThrows(RuntimeThrower.Bug.class, () -> I.make(RuntimeThrower.class));
    }

    private static class RuntimeThrower {

        /**
         * Create RuntimeExceptionClass instance.
         */
        private RuntimeThrower() {
            throw new Bug();
        }

        private static class Bug extends RuntimeException {
            private static final long serialVersionUID = 6965448734007115961L;
        }
    }

    @Test
    void throwError() {
        assertThrows(ErrorThrower.Bug.class, () -> I.make(ErrorThrower.class));
    }

    private static class ErrorThrower {

        /**
         * Create ErrorClass instance.
         */
        private ErrorThrower() {
            throw new Bug();
        }

        private static class Bug extends Error {
            private static final long serialVersionUID = 219714084165765163L;
        }
    }

    @Test
    void throwException() {
        assertThrows(ExceptionThrower.Bug.class, () -> I.make(ExceptionThrower.class));
    }

    private static class ExceptionThrower {

        /**
         * Create ExceptionClass instance.
         */
        private ExceptionThrower() throws Bug {
            throw new Bug();
        }

        private static class Bug extends Exception {
            private static final long serialVersionUID = 5333091127457345270L;
        }
    }

    @Test
    void interfaceList() throws Exception {
        assert I.make(List.class) instanceof ArrayList;
    }

    @Test
    void interfaceMap() throws Exception {
        assert I.make(Map.class) instanceof HashMap;
    }

    /**
     * Test int name.
     */
    @Test
    void testReservedName01() {
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
    void testReservedName02() {
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
    void testReservedName03() {
        Primitive primitive = I.make(Primitive.class);
        assert primitive != null;
        assert false == primitive.isBoolean();

        primitive.setBoolean(true);
        assert true == primitive.isBoolean();
    }
}