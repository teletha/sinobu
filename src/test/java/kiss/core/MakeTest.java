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

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import kiss.I;
import kiss.sample.bean.Primitive;
import kiss.sample.modifier.Nested.PublicStatic;
import kiss.sample.modifier.Public;

class MakeTest {

    @Test
    void publicClass() {
        assert I.make(Public.class) != null;
    }

    @Test
    void packagePrivate() throws Exception {
        Class clazz = Class.forName("kiss.sample.modifier.PackagePrivate");
        assert clazz != null;
        assert I.make(clazz) != null;
    }

    @Test
    void memberPublicStatic() {
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
    void memberPublicNonStatic() {
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
    void finalClass() {
        final class Final {
        }

        assert I.make(Final.class) != null;
    }

    @Test
    void finalBean() {
        @SuppressWarnings("unused")
        final class FinalBean {
            private int value;

            public int getValue() {
                return value;
            }

            public void setValue(int value) {
                this.value = value;
            }
        }
        assert I.make(FinalBean.class) != null;
    }

    @Test
    void localClass() {
        class Local {
        }

        assert I.make(Local.class) != null;
    }

    @Test
    void abstractClass() {
        abstract class Abstract {
        }

        assertThrows(InstantiationException.class, () -> I.make(Abstract.class));
    }

    @Test
    void interfaceList() {
        assert I.make(List.class) instanceof ArrayList;
    }

    @Test
    void interfaceMap() {
        assert I.make(Map.class) instanceof HashMap;
    }

    @Test
    void throwRuntimeException() {
        class RuntimeThrower {
            private RuntimeThrower() {
                throw new Bug();
            }

            @SuppressWarnings("serial")
            class Bug extends RuntimeException {
            }
        }

        assertThrows(RuntimeThrower.Bug.class, () -> I.make(RuntimeThrower.class));
    }

    @Test
    void throwError() {
        class ErrorThrower {
            private ErrorThrower() {
                throw new Bug();
            }

            @SuppressWarnings("serial")
            class Bug extends Error {
            }
        }

        assertThrows(ErrorThrower.Bug.class, () -> I.make(ErrorThrower.class));
    }

    @Test
    void throwException() {
        class ExceptionThrower {
            private ExceptionThrower() throws Exception {
                throw new Bug();
            }

            @SuppressWarnings("serial")
            class Bug extends Exception {
            }
        }

        assertThrows(ExceptionThrower.Bug.class, () -> I.make(ExceptionThrower.class));
    }

    @Test
    void injectPrimitiveInt() {
        record Primitive(int value) {
        };

        assert I.make(Primitive.class).value == 0;
    }

    @Test
    void injectPrimitiveLong() {
        record Primitive(long value) {
        };

        assert I.make(Primitive.class).value == 0;
    }

    @Test
    void injectPrimitiveFloat() {
        record Primitive(float value) {
        };

        assert I.make(Primitive.class).value == 0;
    }

    @Test
    void injectPrimitiveDouble() {
        record Primitive(double value) {
        };

        assert I.make(Primitive.class).value == 0;
    }

    @Test
    void injectPrimitiveByte() {
        record Primitive(byte value) {
        };

        assert I.make(Primitive.class).value == 0;
    }

    @Test
    void injectPrimitiveShort() {
        record Primitive(short value) {
        };

        assert I.make(Primitive.class).value == 0;
    }

    @Test
    void injectPrimitiveBoolean() {
        record Primitive(boolean value) {
        };

        assert I.make(Primitive.class).value == false;
    }

    @Test
    void injectPrimitiveChar() {
        record Primitive(char value) {
        };

        assert I.make(Primitive.class).value == '\0';
    }

    @Test
    void testReservedKeyword01() {
        Primitive primitive = I.make(Primitive.class);
        assert primitive != null;
        assert 0 == primitive.getInt();

        primitive.setInt(100);
        assert 100 == primitive.getInt();
    }

    @Test
    void testReservedKeyword02() {
        Primitive primitive = I.make(Primitive.class);
        assert primitive != null;
        assert 0L == primitive.getLong();

        primitive.setLong(100);
        assert 100L == primitive.getLong();
    }

    @Test
    void testReservedKeyword03() {
        Primitive primitive = I.make(Primitive.class);
        assert primitive != null;
        assert false == primitive.isBoolean();

        primitive.setBoolean(true);
        assert true == primitive.isBoolean();
    }
}