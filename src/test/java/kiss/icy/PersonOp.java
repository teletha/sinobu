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
 * @version 2015/04/19 19:23:20
 */
public class PersonOp<M extends Person> implements ModelOperationSet<Person> {

    /** Name property. */
    private String name;

    /** Age property. */
    private int age;

    /** Gender property. */
    private Gender gender;

    /**
     * {@inheritDoc}
     */
    @Override
    public Person build() {
        return new Person(name, age, gender);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void with(Person model) {
        this.name = model.name;
        this.age = model.age;
        this.gender = model.gender;
    }

    /**
     * <p>
     * Assign name property.
     * </p>
     * 
     * @param name
     * @return
     */
    public PersonOp name(String name) {
        this.name = name;

        // Chainable API
        return this;
    }

    /**
     * <p>
     * Assign age property.
     * </p>
     * 
     * @param age
     * @return
     */
    public PersonOp age(int age) {
        this.age = age;

        // Chainable API
        return this;
    }

    /**
     * <p>
     * Assign gender property.
     * </p>
     * 
     * @param age
     * @return
     */
    public PersonOp gender(Gender gender) {
        this.gender = gender;

        // Chainable API
        return this;
    }

    /**
     * <p>
     * Create operation.
     * </p>
     * 
     * @param name
     */
    public static ModelOperation<PersonOp> nameIs(String name) {
        return op -> op.name(name);
    }

    /**
     * <p>
     * Create operation.
     * </p>
     * 
     * @param name
     */
    public static ModelOperation<PersonOp> ageIs(int age) {
        return op -> op.age(age);
    }

    /**
     * @param name
     * @param age
     * @param gender
     * @return
     */
    public static Person with(String name, int age, Gender gender) {
        return new PersonOp().name(name).age(age).gender(gender).build();
    }
}
