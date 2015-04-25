/*
 * Copyright (C) 2015 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.icy.model;

import java.util.function.UnaryOperator;

import kiss.icy.Lens;
import kiss.icy.ModelOperator;
import kiss.icy.Operation;

/**
 * @version 2015/04/25 12:01:23
 */
public class Club {

    /** The lens for leader property. */
    private static final Lens<Club, String> NAME = Lens.of(model -> model.model.name, (model, value) -> model.name(value));

    /** The lens for leader property. */
    private static final Lens<Club, Person> LEADER = Lens.of(model -> model.model.leader, (model, value) -> model.leader(value));

    /** The current model. */
    final ClubDef model;

    /**
     * <p>
     * Create model with the specified property holder.
     * </p>
     * 
     * @param model
     */
    private Club(ClubDef model) {
        this.model = model;
    }

    /**
     * <p>
     * Retrieve name property.
     * </p>
     * 
     * @return A name property
     */
    public String name() {
        return model.name;
    }

    /**
     * <p>
     * Create new model with the specified property.
     * </p>
     * 
     * @param value A new property.
     * @return A created model.
     */
    public Club name(String value) {
        if (model.name == value) {
            return this;
        }
        return with(this).name(value).ice();
    }

    /**
     * <p>
     * Getter kind.
     * </p>
     * 
     * @return A property value.
     */
    public Person leader() {
        return model.leader;
    }

    /**
     * <p>
     * Create new model with the specified property.
     * </p>
     * 
     * @param value A new property.
     * @return A created model.
     */
    public Club leader(Person value) {
        if (model.leader == value) {
            return this;
        }
        return with(this).leader(value).ice();
    }

    /**
     * <p>
     * Create new immutable model.
     * </p>
     * 
     * @return An immutable model.
     */
    public final Club ice() {
        return this instanceof Melty ? new Club(model) : this;
    }

    /**
     * <p>
     * Create new mutable model.
     * </p>
     * 
     * @return An immutable model.
     */
    public final Club melt() {
        return this instanceof Melty ? this : new Melty(this);
    }

    /**
     * <p>
     * Create model builder without base model.
     * </p>
     * 
     * @return A new model builder.
     */
    public static final Club with() {
        return with(null);
    }

    /**
     * <p>
     * Create model builder using the specified definition as base model.
     * </p>
     * 
     * @return A new model builder.
     */
    public static final Club with(Club base) {
        return new Melty(base);
    }

    /**
     * @version 2015/04/24 16:41:14
     */
    private static final class Melty extends Club {

        /**
         * @param name
         * @param age
         * @param gender
         * @param club
         */
        private Melty(Club base) {
            super(base == null ? new ClubDef() : base.model);
        }

        /**
         * <p>
         * Assign name property.
         * </p>
         * 
         * @param name A property to assign.
         * @return Chainable API.
         */
        @Override
        public Melty name(String name) {
            model.name = name;

            return this;
        }

        /**
         * <p>
         * Assign leader property.
         * </p>
         * 
         * @param value A property to assign.
         * @return Chainable API.
         */
        @Override
        public Melty leader(Person value) {
            model.leader = value;

            return this;
        }
    }

    /**
     * @version 2015/04/24 16:52:22
     */
    public static final class Operator<M> extends ModelOperator<M, Club> {

        /**
         * @param lens
         */
        private Operator(Lens<M, Club> lens) {
            super(lens);
        }

        /**
         * <p>
         * Operation kind.
         * </p>
         * 
         * @param value
         * @return
         */
        public Operation<M> leader(Person value) {
            return model -> lens.then(LEADER).set(model, value);
        }

        /**
         * <p>
         * Operation kind.
         * </p>
         * 
         * @param value
         * @return
         */
        public Operation<M> leader(UnaryOperator<Person> value) {
            return model -> lens.then(LEADER).set(model, value);
        }

        /**
         * <p>
         * Router kind.
         * </p>
         * 
         * @return
         */
        public static Person.Operator<Club> leader() {
            return new Person.Operator(LEADER);
        }
    }
}
