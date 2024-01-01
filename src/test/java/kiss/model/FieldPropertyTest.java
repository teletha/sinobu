/*
 * Copyright (C) 2024 The SINOBU Development Team
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

@SuppressWarnings("unused")
class FieldPropertyTest {

    @Test
    void Public() {
        class Declare {
            public int property;
        }

        assert validatePropertyAccess(new Declare(), 0, 10);
    }

    @Test
    void Protected() {
        class Declare {
            protected int notProperty;
        }

        Model model = Model.of(Declare.class);
        assert model.properties().size() == 0;
    }

    @Test
    void PackagePrivate() {
        class Declare {
            int notProperty;
        }

        Model model = Model.of(Declare.class);
        assert model.properties().size() == 0;
    }

    @Test
    void Private() {
        class Declare {
            private int notProperty;
        }

        Model model = Model.of(Declare.class);
        assert model.properties().size() == 0;
    }

    @Test
    void ManagedPublic() {
        class Declare {
            @Managed
            public int property;
        }

        assert validatePropertyAccess(new Declare(), 0, 10);
    }

    @Test
    void ManagedProtected() {
        class Declare {
            @Managed
            protected int property;
        }

        assert validatePropertyAccess(new Declare(), 0, 10);
    }

    @Test
    void ManagedPackagePrivate() {
        class Declare {
            @Managed
            int property;
        }

        assert validatePropertyAccess(new Declare(), 0, 10);
    }

    @Test
    void ManagedPrivate() {
        class Declare {
            @Managed
            private int property;
        }

        assert validatePropertyAccess(new Declare(), 0, 10);
    }

    @Test
    void PrimitiveInt() {
        class Declare {
            public int property;
        }

        assert validatePropertyAccess(new Declare(), 0, 10);
    }

    @Test
    void PrimitiveLong() {
        class Declare {
            public long property;
        }

        assert validatePropertyAccess(new Declare(), 0L, 10L);
    }

    @Test
    void PrimitiveFloat() {
        class Declare {
            public float property;
        }

        assert validatePropertyAccess(new Declare(), 0F, 10F);
    }

    @Test
    void PrimitiveDouble() {
        class Declare {
            public double property;
        }

        assert validatePropertyAccess(new Declare(), 0D, 10D);
    }

    @Test
    void PrimitiveShort() {
        class Declare {
            public short property;
        }

        assert validatePropertyAccess(new Declare(), (short) 0, (short) 10);
    }

    @Test
    void PrimitiveByte() {
        class Declare {
            public byte property;
        }

        assert validatePropertyAccess(new Declare(), (byte) 0, (byte) 10);
    }

    @Test
    void PrimitiveBoolean() {
        class Declare {
            public boolean property;
        }

        assert validatePropertyAccess(new Declare(), false, true);
    }

    @Test
    void PrimitiveChar() {
        class Declare {
            public char property;
        }

        assert validatePropertyAccess(new Declare(), '\u0000', 'A');
    }

    @Test
    void WrappedInt() {
        class Declare {
            public Integer property;
        }

        assert validatePropertyAccess(new Declare(), null, 10);
    }

    @Test
    void WrappedLong() {
        class Declare {
            public Long property;
        }

        assert validatePropertyAccess(new Declare(), null, 10L);
    }

    @Test
    void WrappedFloat() {
        class Declare {
            public Float property;
        }

        assert validatePropertyAccess(new Declare(), null, 10F);
    }

    @Test
    void WrappedDouble() {
        class Declare {
            public Double property;
        }

        assert validatePropertyAccess(new Declare(), null, 10D);
    }

    @Test
    void WrappedShort() {
        class Declare {
            public Short property;
        }

        assert validatePropertyAccess(new Declare(), null, (short) 10);
    }

    @Test
    void WrappedByte() {
        class Declare {
            public Byte property;
        }

        assert validatePropertyAccess(new Declare(), null, (byte) 10);
    }

    @Test
    void WrappedBoolean() {
        class Declare {
            public Boolean property;
        }

        assert validatePropertyAccess(new Declare(), null, true);
    }

    @Test
    void WrappedChar() {
        class Declare {
            public Character property;
        }

        assert validatePropertyAccess(new Declare(), null, 'A');
    }

    @Test
    void AttributeString() {
        class Declare {
            public String property;
        }

        assert validatePropertyAccess(new Declare(), null, "A");
    }

    @Test
    void AttributeClass() {
        class Declare {
            public Class property;
        }

        assert validatePropertyAccess(new Declare(), null, String.class);
    }

    @Test
    void AttributeLocalDateTime() {
        class Declare {
            public LocalDateTime property;
        }

        assert validatePropertyAccess(new Declare(), null, LocalDateTime.now());
    }

    @Test
    void Map() {
        class Declare {
            public Map<String, String> property;
        }

        assert validatePropertyAccess(new Declare(), null, Map.of());
    }

    @Test
    void List() {
        class Declare {
            public List<String> property;
        }

        assert validatePropertyAccess(new Declare(), null, List.of());
    }

    @Test
    void Generic() {
        class Declare<T> {
            public T property;
        }

        assert validatePropertyAccess(new Declare(), null, new Object());
    }

    @Test
    void GenericSpecialized() {
        class Generic<T> {
            public T property;
        }

        class Declare extends Generic<String> {
        }

        assert validatePropertyAccess(new Declare(), null, "specialized");
    }

    @Test
    void GenericSpecializedList() {
        class Generic<T> {
            public T property;
        }

        class Declare extends Generic<List<String>> {
        }

        assert validatePropertyAccess(new Declare(), null, List.of("string"));
    }

    @Test
    void GenericSpecializedGenericList() {
        class Generic<T> {
            public T property;
        }

        class Declare<E> extends Generic<List<E>> {
        }

        assert validatePropertyAccess(new Declare(), null, List.of("string"));
    }

    @Test
    void GenericSpecializedMap() {
        class Generic<T> {
            public T property;
        }

        class Declare extends Generic<Map<String, String>> {
        }

        assert validatePropertyAccess(new Declare(), null, Map.of("key", "value"));
    }

    @Test
    void GenericSpecializedGenericMap() {
        class Generic<T> {
            public T property;
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
        if (!p.model.type.isPrimitive()) {
            assert p.model.type.isInstance(newValue);
        }

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