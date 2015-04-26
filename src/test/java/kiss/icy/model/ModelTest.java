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

import static kiss.icy.Gender.*;

import org.junit.Test;

import kiss.icy.Lens;

/**
 * @version 2015/04/24 22:05:43
 */
public class ModelTest {

    /** The reusable person. */
    private static Person HIKIGAYA = Person.with().name("Hikigaya").age(17).gender(Male);

    /** The reusable person. */
    private static Person YUKINOSITA = Person.with().name("Yukinosita").age(17).gender(Female);

    /** The reusable club. */
    private static Club MINISTRATION = Club.with().name("Housibu").leader(YUKINOSITA);

    static {
        HIKIGAYA = HIKIGAYA.club(MINISTRATION).ice();
        YUKINOSITA = YUKINOSITA.club(MINISTRATION).ice();
        MINISTRATION = MINISTRATION.ice();
    }

    @Test
    public void changeSingleProperty() {
        Person hikigaya = HIKIGAYA.name("Hatiman");
        assert hikigaya != HIKIGAYA;
        assert hikigaya.name().equals("Hatiman");
        assert hikigaya.age() == HIKIGAYA.age();
        assert hikigaya.gender() == HIKIGAYA.gender();
        assert hikigaya.club() == MINISTRATION;
    }

    @Test
    public void changePropertyWithIdenticalEqualityValue() {
        Person yukinosita = YUKINOSITA.name("Yukinosita");
        assert yukinosita == YUKINOSITA;

        yukinosita = YUKINOSITA.age(17);
        assert yukinosita == YUKINOSITA;
    }

    @Test
    public void changeNestedProperty() throws Exception {
        Lens<Club, String> lens = Club.LEADER.then(Person.NAME);
        Club club = lens.set(MINISTRATION, "Yukino");
        assert club != MINISTRATION;
        assert club.leader() != YUKINOSITA;
        assert club.leader().name().equals("Yukino");
    }
}
