/*
 * Copyright (C) 2015 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.icy;

/**
 * @version 2015/04/25 12:01:23
 */
public abstract class Club implements Operatable<Club> {

    /** The model operator. */
    public static final Operator<Club> Operator = new Operator(null);

    /** The current model. */
    ClubDef model;

    /**
     * <p>
     * Create model with the specified property holder.
     * </p>
     * 
     * @param model
     */
    private Club() {
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
        if (model.name.equals(value)) {
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
        if (model.leader.equals(value)) {
            return this;
        }
        return with(this).leader(value).ice();
    }

    /**
     * <p>
     * Getter kind.
     * </p>
     * 
     * @return A property value.
     */
    public Seq<Person> members() {
        return model.members;
    }

    /**
     * <p>
     * Create new model with the specified property.
     * </p>
     * 
     * @param value A new property.
     * @return A created model.
     */
    public Club members(Seq<Person> value) {
        if (model.members.equals(value)) {
            return this;
        }
        return with(this).members(value).ice();
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
     * @version 2015/04/26 16:49:59
     */
    private static final class Icy extends Club {

        /**
         * 
         */
        private Icy(Club base) {
            model = new ClubDef();

            if (base != null) {
                model.name = base.name();
                model.leader = base.leader().ice();
                model.members = base.members().ice();
            }
        }

        /**
         * <p>
         * Create new mutable model.
         * </p>
         * 
         * @return An immutable model.
         */
        @Override
        public Club melt() {
            return new Melty(this);
        }
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
            model = new ClubDef();

            if (base != null) {
                model.name = base.name();
                model.leader = base.leader().melt();
                model.members = base.members().melt();
            }
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

        /**
         * <p>
         * Assign memeber property.
         * </p>
         * 
         * @param value A property to assign.
         * @return Chainable API.
         */
        @Override
        public Melty members(Seq<Person> value) {
            model.members = value;

            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Club ice() {
            return new Icy(this);
        }
    }

    /**
     * @version 2015/04/24 16:52:22
     */
    public static final class Operator<M> extends ModelOperator<M, Club> {

        /** The lens for leader property. */
        private static final Lens<Club, String> NAME = Lens.of(Club::name, Club::name);

        /** The lens for leader property. */
        private static final Lens<Club, Person> LEADER = Lens.of(Club::leader, Club::leader);

        /** The lens for leader property. */
        private static final Lens<Club, Seq<Person>> MEMBERS = Lens.of(Club::members, Club::members);

        /**
         * @param lens
         */
        public Operator(Lens<M, Club> lens) {
            super(lens);
        }

        /**
         * <p>
         * Property operator.
         * </p>
         * 
         * @return
         */
        public Lens<M, String> name() {
            return parent.then(NAME);
        }

        /**
         * <p>
         * Property operator.
         * </p>
         * 
         * @return
         */
        public Person.Operator<M> leader() {
            return new Person.Operator(parent.then(LEADER));
        }

        /**
         * <p>
         * Property operator.
         * </p>
         * 
         * @return
         */
        public Seq.Operator<M, Person, Person.Operator<M>> memebers() {
            return new Seq.Operator(parent.then(MEMBERS), new Person.Operator<M>(null));
        }
    }
}
