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

import static kiss.icy.model.Gender.*;

import org.junit.Test;

/**
 * @version 2015/04/24 22:05:43
 */
public class ModelTest {

    /** The reusable person. */
    private static Person HIKIGAYA = Person.with().name("Hikigaya").age(17).gender(Male).ice();

    /** The reusable person. */
    private static Person YUKINOSITA = Person.with().name("Yukinosita").age(17).gender(Female).ice();

    /** The reusable club. */
    private static Club MINISTRATION = Club.with().name("Housibu").leader(YUKINOSITA).ice();

    @Test
    public void changeSingleProperty() {
        Person hikigaya = HIKIGAYA.name("Hatiman");
        assert hikigaya != HIKIGAYA;
        assert hikigaya.name().equals("Hatiman");
        assert hikigaya.age() == HIKIGAYA.age();
        assert hikigaya.gender() == HIKIGAYA.gender();
    }

    @Test
    public void changePropertyWithIdenticalEqualityValue() {
        Person yukinosita = YUKINOSITA.name("Yukinosita");
        assert yukinosita == YUKINOSITA;

        yukinosita = YUKINOSITA.age(17);
        assert yukinosita == YUKINOSITA;
    }

    @Test
    public void changeNestedProperty() {
        Club club1 = Club.Operator.leader().name().set(MINISTRATION, "Yukino");
        assert club1 != MINISTRATION;
        assert club1.name().equals("Housibu");
        assert club1.leader() != YUKINOSITA;
        assert club1.leader().name().equals("Yukino");

        Club club2 = Club.Operator.leader().set(MINISTRATION, HIKIGAYA);
        assert club2 != MINISTRATION;
        assert club2.name().equals("Housibu");
        assert club2.leader() == HIKIGAYA;

        // Lens<Club, String> lens = Club.LEADER.then(Person.NAME);
        //
        // Club club = MINISTRATION.operate(changeLeaderName, "Yukino");
        // assert club != MINISTRATION;
        // assert club.name().equals("Housibu");
        // assert club.leader() != YUKINOSITA;
        // assert club.leader().name().equals("Yukino");
    }
}
