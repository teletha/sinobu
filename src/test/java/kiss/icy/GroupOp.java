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

import kiss.Binary;
import kiss.I;

/**
 * @version 2015/04/19 21:53:51
 */
public class GroupOp<M extends Group> implements ModelOperationSet<Group> {

    /** The lens for leader property. */
    public static final Lens<Group, Person> LEADER = Lens.of(model -> model.leader, (model, value) -> new Group(value, model.name));

    /** Name property. */
    private String name;

    /** Leader property. */
    private Person leader;

    /**
     * {@inheritDoc}
     */
    @Override
    public Group build() {
        return new Group(leader, name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void with(Group model) {
        this.name = model.name;
        this.leader = model.leader;
    }

    /**
     * <p>
     * Assign name property.
     * </p>
     * 
     * @param name
     * @return
     */
    public GroupOp name(String name) {
        this.name = name;

        // Chainable API
        return this;
    }

    /**
     * <p>
     * Assign leader property.
     * </p>
     * 
     * @param leader
     * @return
     */
    public GroupOp leader(Person leader) {
        this.leader = leader;

        // Chainable API
        return this;
    }

    /**
     * <p>
     * Assign leader property.
     * </p>
     * 
     * @param leader
     * @return
     */
    public GroupOp leader(ModelOperationSet<Person> leader) {
        this.leader = leader.build();

        // Chainable API
        return this;
    }

    /**
     * <p>
     * Create operation.
     * </p>
     * 
     * @param value
     */
    public static ModelOperation<GroupOp> leaderIs(Person value) {
        return op -> op.leader(value);
    }

    /**
     * <p>
     * Create operation.
     * </p>
     * 
     * @param value
     */
    public static ModelOperation<GroupOp> leader(ModelOperation<PersonOp> operation) {
        return op -> {
            PersonOp person = (PersonOp) I.find(ModelOperationSet.class, Person.class);
            person.with(op.leader);

            operation.apply(person);

            op.leader(person.build());
        };
    }

    public static <P> Binary<Lens<Group, P>, P> leader(Binary<Lens<Person, P>, P> property) {
        return property.a(LEADER.then(property.a));
    }

    /**
     * @param name
     * @param leader
     * @return
     */
    public static Group with(String name, Person leader) {
        return new GroupOp().name(name).leader(leader).build();
    }
}
