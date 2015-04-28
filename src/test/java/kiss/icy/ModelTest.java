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

import static kiss.icy.GroupOp2.*;
import static kiss.icy.PersonOp2.*;
import kiss.I;
import kiss.icy.model.Gender;

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

        // person = person.name("New Name");
        // assert person.name.equals("New Name");
        // assert person.age == 20;
        // assert person.gender == Gender.Male;
    }

    @Test
    public void changeMultipleValues() throws Exception {
        Person person = PersonOp.with("Name", 20, Gender.Male);
        assert person.name.equals("Name");
        assert person.age == 20;
        assert person.gender == Gender.Male;

        person = L.operate(person, name2("New Name"), age2(30));
        assert person.name.equals("New Name");
        assert person.age == 30;
        assert person.gender == Gender.Male;
    }

    @Test
    public void changeNestedValue() throws Exception {
        Person person = PersonOp.with("Name", 20, Gender.Male);
        Group group = GroupOp.with("NPC", person, null);
        assert group.leader.name.equals("Name");

        group = L.operate(group, leader().name("Change"));
        assert group.leader.name.equals("Change");
        assert group.leader.age == 20;
    }

    @Test
    public void testname() throws Exception {
        Person person = PersonOp.with("Name", 20, Gender.Male);
        assert person.name.equals("Name");
        assert person.age == 20;
        assert person.gender == Gender.Male;

        person = L.operate(person, name2("New Name"));
        assert person.name.equals("New Name");
        assert person.age == 20;
        assert person.gender == Gender.Male;

        person = L.operate(person, name2(v -> v.toUpperCase()));
        assert person.name.equals("NEW NAME");
        assert person.age == 20;
        assert person.gender == Gender.Male;
    }

    @Test
    public void changeNestedValue2() throws Exception {
        Person person = PersonOp.with("Name", 20, Gender.Male);
        Group group = GroupOp.with("NPC", person, null);
        assert group.leader.name.equals("Name");

        group = L.operate(group, leader().name("Change"));
        assert group.leader.name.equals("Change");
        assert group.leader.age == 20;
        //
        // group = L.operate(group, LEADER, NAME, "Set");
        // assert group.leader.name.equals("Set");
        //
        // group = L.operate(group, leader(nameIs("Next")));
        // assert group.leader.name.equals("Next");

        // group = L.operate(group, leader().name2("Set"));
        // assert group.leader.name.equals("Set");
    }

    @Test
    public void addToSeq() {
        Person person = PersonOp.with("Name", 20, Gender.Male);
        Group group = GroupOp.with("NPC", person, Seq.of(person));
        assert group.members.head().name.equals("Name");

        Person newer = PersonOp.with("Newer", 21, Gender.Female);

        group = L.operate(group, members().add(0, newer));
        assert group.members.head().name.equals("Newer");
        assert group.members.tail().name.equals("Name");

        group = L.operate(group, members().all(op -> op.name(String::toUpperCase)));
        assert group.members.head().name.equals("NEWER");
        assert group.members.tail().name.equals("NAME");

        group = L.operate(group, members().select(e -> e.gender == Gender.Male, op -> op.name(String::toLowerCase)));
        assert group.members.head().name.equals("NEWER");
        assert group.members.tail().name.equals("name");
    }
}
