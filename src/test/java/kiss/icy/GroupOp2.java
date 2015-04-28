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

import kiss.icy.model.ModelOperator;

/**
 * @version 2015/04/22 0:19:22
 */
public class GroupOp2<M> extends ModelOperator<M, Group> {

    /** The lens for leader property. */
    private static final Lens<Group, Person> LEADER = Lens.of(model -> model.leader, (model, value) -> new Group(value, model.name, model.members));

    /** The lens for member property. */
    private static final Lens<Group, Seq<Person>> MEMBERS = Lens.of(model -> model.members, (model, value) -> new Group(model.leader, model.name, value));

    /**
     * @param lens
     */
    public GroupOp2(Lens<M, Group> lens) {
        super(lens);
    }

    /**
     * <p>
     * Getter kind.
     * </p>
     * 
     * @return
     */
    public static PersonOp2<Group> leader() {
        return new PersonOp2(LEADER);
    }

    public static SeqOp<Group, Person, PersonOp2<Person>, PersonOp2<Group>> members() {
        return new SeqOp(MEMBERS, new PersonOp2(Lens.Φ));
    }
}
