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

import java.util.function.Function;
import java.util.function.UnaryOperator;

import kiss.icy.model.Lens;
import kiss.icy.model.ModelOperator;

/**
 * @version 2015/04/21 23:11:36
 */
public class PersonOp2<Model> extends ModelOperator<Model, Person> {

    /** The lens for leader property. */
    private static final Lens<Person, String> NAME = Lens
            .of(model -> model.name, (model, value) -> new Person(value, model.age, model.gender));

    /** The lens for age property. */
    private static final Lens<Person, Integer> AGE = Lens
            .of(model -> model.age, (model, value) -> new Person(model.name, value, model.gender));

    /**
     * @param lens
     */
    public PersonOp2(Lens<Model, Person> lens) {
        super(lens);
    }

    /**
     * <p>
     * Getter kind.
     * </p>
     * 
     * @return
     */
    public Function<Model, String> name() {
        return model -> lens.then(NAME).get(model);
    }

    /**
     * <p>
     * Operation kind.
     * </p>
     * 
     * @param name
     * @return
     */
    public Operation<Model> name(String name) {
        return model -> lens.then(NAME).set(model, name);
    }

    /**
     * <p>
     * Operation kind.
     * </p>
     * 
     * @param name
     * @return
     */
    public Operation<Model> name(UnaryOperator<String> name) {
        return model -> lens.then(NAME).set(model, name);
    }

    /**
     * <p>
     * Operation operation of the name property.
     * </p>
     * 
     * @param name
     * @return
     */
    public static Operation<Person> name2(String name) {
        return model -> NAME.set(model, name);
    }

    /**
     * <p>
     * Operation operation of the age property.
     * </p>
     * 
     * @param value
     * @return
     */
    public static Operation<Person> age2(int value) {
        return model -> AGE.set(model, value);
    }

    /**
     * <p>
     * Operation operation of the name property.
     * </p>
     * 
     * @param name
     * @return
     */
    public static Operation<Person> name2(UnaryOperator<String> name) {
        return model -> NAME.set(model, name.apply(NAME.get(model)));
    }
}
