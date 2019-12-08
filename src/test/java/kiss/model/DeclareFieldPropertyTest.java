/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.model;

import org.junit.jupiter.api.Test;

import kiss.Managed;

@SuppressWarnings("unused")
class DeclareFieldPropertyTest {

    @Test
    void Public() {
        class Declare {
            public int property;
        }

        Model model = Model.of(Declare.class);
        assert model.properties().size() == 1;
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

        Model model = Model.of(Declare.class);
        assert model.properties().size() == 1;
    }

    @Test
    void ManagedProtected() {
        class Declare {
            @Managed
            protected int property;
        }

        Model model = Model.of(Declare.class);
        assert model.properties().size() == 1;
    }

    @Test
    void ManagedPackagePrivate() {
        class Declare {
            @Managed
            int property;
        }

        Model model = Model.of(Declare.class);
        assert model.properties().size() == 1;
    }

    @Test
    void ManagedPrivate() {
        class Declare {
            @Managed
            private int property;
        }

        Model model = Model.of(Declare.class);
        assert model.properties().size() == 1;
    }

    @Test
    void Generic() {
        class Declare<T> {
            public T property;
        }

        Model model = Model.of(Declare.class);
        assert model.properties().size() == 1;
    }

    @Test
    void GenericBounded() {
        class Declare<T extends Comparable> {
            public T property;
        }

        Model model = Model.of(Declare.class);
        assert model.properties().size() == 1;
    }
}
