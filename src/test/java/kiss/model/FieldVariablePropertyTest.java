/*
 * Copyright (C) 2023 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.junit.jupiter.api.Test;

import kiss.Managed;
import kiss.Model;
import kiss.Property;
import kiss.Variable;

@SuppressWarnings("unused")
class FieldVariablePropertyTest {

    @Test
    void Public() {
        class Declare {
            public Variable<Integer> property = Variable.empty();
        }

        assert validatePropertyAccess(new Declare(), null, 10);
    }

    @Test
    void Protected() {
        class Declare {
            protected Variable<String> notProperty;
        }

        Model model = Model.of(Declare.class);
        assert model.properties().size() == 0;
    }

    @Test
    void PackagePrivate() {
        class Declare {
            Variable<String> notProperty;
        }

        Model model = Model.of(Declare.class);
        assert model.properties().size() == 0;
    }

    @Test
    void Private() {
        class Declare {
            private Variable<String> notProperty;
        }

        Model model = Model.of(Declare.class);
        assert model.properties().size() == 0;
    }

    @Test
    void ManagedPublic() {
        class Declare {
            @Managed
            public Variable<Integer> property = Variable.empty();
        }

        assert validatePropertyAccess(new Declare(), null, 10);
    }

    @Test
    void ManagedProtected() {
        class Declare {
            @Managed
            protected Variable<Integer> property = Variable.empty();
        }

        assert validatePropertyAccess(new Declare(), null, 10);
    }

    @Test
    void ManagedPackagePrivate() {
        class Declare {
            @Managed
            Variable<Integer> property = Variable.empty();
        }

        assert validatePropertyAccess(new Declare(), null, 10);
    }

    @Test
    void ManagedPrivate() {
        class Declare {
            @Managed
            private Variable<Integer> property = Variable.empty();
        }

        assert validatePropertyAccess(new Declare(), null, 10);
    }

    @Test
    void WrappedInt() {
        class Declare {
            public Variable<Integer> property = Variable.empty();
        }

        assert validatePropertyAccess(new Declare(), null, 10);
    }

    @Test
    void WrappedLong() {
        class Declare {
            public Variable<Long> property = Variable.empty();
        }

        assert validatePropertyAccess(new Declare(), null, 10L);
    }

    @Test
    void WrappedFloat() {
        class Declare {
            public Variable<Float> property = Variable.empty();
        }

        assert validatePropertyAccess(new Declare(), null, 10F);
    }

    @Test
    void WrappedDouble() {
        class Declare {
            public Variable<Double> property = Variable.empty();
        }

        assert validatePropertyAccess(new Declare(), null, 10D);
    }

    @Test
    void WrappedShort() {
        class Declare {
            public Variable<Short> property = Variable.empty();
        }

        assert validatePropertyAccess(new Declare(), null, (short) 10);
    }

    @Test
    void WrappedByte() {
        class Declare {
            public Variable<Byte> property = Variable.empty();
        }

        assert validatePropertyAccess(new Declare(), null, (byte) 10);
    }

    @Test
    void WrappedBoolean() {
        class Declare {
            public Variable<Boolean> property = Variable.empty();
        }

        assert validatePropertyAccess(new Declare(), null, true);
    }

    @Test
    void WrappedChar() {
        class Declare {
            public Variable<Character> property = Variable.empty();
        }

        assert validatePropertyAccess(new Declare(), null, 'A');
    }

    @Test
    void AttributeString() {
        class Declare {
            public Variable<String> property = Variable.empty();
        }

        assert validatePropertyAccess(new Declare(), null, "A");
    }

    @Test
    void AttributeClass() {
        class Declare {
            public Variable<Class> property = Variable.empty();
        }

        assert validatePropertyAccess(new Declare(), null, String.class);
    }

    @Test
    void AttributeLocalDateTime() {
        class Declare {
            public Variable<LocalDateTime> property = Variable.empty();
        }

        assert validatePropertyAccess(new Declare(), null, LocalDateTime.now());
    }

    @Test
    void Map() {
        class Declare {
            public Variable<Map<String, String>> property = Variable.empty();
        }

        assert validatePropertyAccess(new Declare(), null, Map.of());
    }

    @Test
    void List() {
        class Declare {
            public Variable<List<String>> property = Variable.empty();
        }

        assert validatePropertyAccess(new Declare(), null, List.of());
    }

    @Test
    void ExtendedVariable() {
        class DoubleVariable extends Variable<Double> {

            public DoubleVariable(Double value) {
                super(value);
            }
        }

        class Declare {
            public DoubleVariable property = new DoubleVariable(0D);
        }

        assert validatePropertyAccess(new Declare(), 0D, 10D);
        assert Model.of(Declare.class).property("property").model.type == Double.class;
    }

    @Test
    void MultiExtendedVariable() {
        class Generic<V> extends Variable<V> {
            public Generic(V value) {
                super(value);
            }
        }

        class ForDouble extends Generic<Double> {
            public ForDouble(double value) {
                super(value);
            }
        }

        class Declare {
            public ForDouble property = new ForDouble(0D);
        }

        assert validatePropertyAccess(new Declare(), 0D, 10D);
        assert Model.of(Declare.class).property("property").model.type == Double.class;
    }

    @Test
    void Generic() {
        class Declare<T> {
            public Variable<T> property = Variable.empty();
        }

        assert validatePropertyAccess(new Declare(), null, new Object());
    }

    @Test
    void GenericSpecialized() {
        class Generic<T> {
            public Variable<T> property = Variable.empty();
        }

        class Declare extends Generic<String> {
        }

        assert validatePropertyAccess(new Declare(), null, "specialized");
        assert Model.of(Declare.class).property("property").model.type == String.class;
    }

    @Test
    void GenericSpecializedList() {
        class Generic<T> {
            public Variable<T> property = Variable.empty();
        }

        class Declare extends Generic<List<String>> {
        }

        assert validatePropertyAccess(new Declare(), null, List.of("string"));
    }

    @Test
    void GenericSpecializedGenericList() {
        class Generic<T> {
            public Variable<T> property = Variable.empty();
        }

        class Declare<E> extends Generic<List<E>> {
        }

        assert validatePropertyAccess(new Declare(), null, List.of("string"));
    }

    @Test
    void GenericSpecializedMap() {
        class Generic<T> {
            public Variable<T> property = Variable.empty();
        }

        class Declare extends Generic<Map<String, String>> {
        }

        assert validatePropertyAccess(new Declare(), null, Map.of("key", "value"));
    }

    @Test
    void GenericSpecializedGenericMap() {
        class Generic<T> {
            public Variable<T> property = Variable.empty();
        }

        class Declare<K, V> extends Generic<Map<K, V>> {
        }

        assert validatePropertyAccess(new Declare(), null, Map.of("key", "value"));
    }

    /**
     * Test property access. (Getter and Setter)
     * 
     * @param model A target model to test.
     * @param instance A model's instance.
     * @param expectedCurrentValue The expected current value.
     * @param newValue The new value to assign.
     * @return
     */
    private boolean validatePropertyAccess(Object instance, Object expectedCurrentValue, Object newValue) {
        Model model = Model.of(instance);
        assert model.properties().size() == 1;
        Property p = (Property) model.properties().iterator().next();

        // get current value
        Object propertyValue = model.get(instance, p);
        assert Objects.equals(propertyValue, expectedCurrentValue);

        // set new value
        model.set(instance, p, newValue);
        propertyValue = model.get(instance, p);
        assert Objects.equals(propertyValue, newValue);

        return true;
    }
}