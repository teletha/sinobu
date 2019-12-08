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
import kiss.Variable;

@SuppressWarnings("unused")
class DeclareVariableFieldPropertyTest {

    @Test
    void Public() {
        class Declare {
            public Variable<String> property;
        }

        Model model = Model.of(Declare.class);
        assert model.properties().size() == 1;
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
            public Variable<String> property;
        }

        Model model = Model.of(Declare.class);
        assert model.properties().size() == 1;
    }

    @Test
    void ManagedProtected() {
        class Declare {
            @Managed
            protected Variable<String> property;
        }

        Model model = Model.of(Declare.class);
        assert model.properties().size() == 1;
    }

    @Test
    void ManagedPackagePrivate() {
        class Declare {
            @Managed
            Variable<String> property;
        }

        Model model = Model.of(Declare.class);
        assert model.properties().size() == 1;
    }

    @Test
    void ManagedPrivate() {
        class Declare {
            @Managed
            private Variable<String> property;
        }

        Model model = Model.of(Declare.class);
        assert model.properties().size() == 1;
    }

    @Test
    void ExtendedVariable() {
        class Declare {
            public DoubleVariable property;
        }

        Model model = Model.of(Declare.class);
        assert model.properties().size() == 1;
    }

    /**
     * Specialized {@link Variable}.
     */
    private static class DoubleVariable extends Variable<Double> {

        public DoubleVariable(Double value) {
            super(value);
        }
    }
}
