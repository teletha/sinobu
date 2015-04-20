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

import static kiss.icy.GroupOp.*;
import static kiss.icy.PersonOp.*;
import kiss.I;

import org.junit.Test;

/**
 * @version 2015/04/19 19:25:09
 */
public class ModelTest {

    static {
        I.load(ModelTest.class, true);
    }

    @Test
    public void changeSingleValue() throws Exception {
        Person person = PersonOp.with("Name", 20, Gender.Male);
        assert person.name.equals("Name");
        assert person.age == 20;
        assert person.gender == Gender.Male;

        person = L.operate(person, nameIs("New Name"));
        assert person.name.equals("New Name");
        assert person.age == 20;
        assert person.gender == Gender.Male;
    }

    @Test
    public void changeMultipleValues() throws Exception {
        Person person = PersonOp.with("Name", 20, Gender.Male);
        assert person.name.equals("Name");
        assert person.age == 20;
        assert person.gender == Gender.Male;

        // person = L.operate(person, nameIs("New Name"), ageIs(30));
        // assert person.name.equals("New Name");
        // assert person.age == 30;
        // assert person.gender == Gender.Male;
    }

    @Test
    public void changeNestedValue() throws Exception {
        Person person = PersonOp.with("Name", 20, Gender.Male);
        Group group = GroupOp.with("NPC", person);
        assert group.leader.name.equals("Name");

        group = L.operate(group, leader(nameIs("Change")));
        assert group.leader.name.equals("Change");
        assert group.leader.age == 20;

        group = L.operate(group, LEADER, NAME, "Set");
        assert group.leader.name.equals("Set");

        group = L.operate(group, leader(nameIs("Next")));
        assert group.leader.name.equals("Next");
    }
}
