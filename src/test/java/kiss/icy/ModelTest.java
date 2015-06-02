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

import static kiss.icy.Gender.*;

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
    private static Club MINISTRATION = Club.with()
            .name("Housibu")
            .leader(YUKINOSITA)
            .members(Seq.of(YUKINOSITA, HIKIGAYA))
            .ice();

    @Test
    public void changeSingleProperty() {
        Person hikitani = HIKIGAYA.name("Hikitani");
        assert hikitani != HIKIGAYA;
        assert hikitani.name().equals("Hikitani");
        assert hikitani.age() == HIKIGAYA.age();
        assert hikitani.gender() == HIKIGAYA.gender();
        assert HIKIGAYA.name().equals("Hikigaya");
        assert HIKIGAYA.age() == 17;
        assert HIKIGAYA.gender() == Gender.Male;
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
        assert club1.name() == MINISTRATION.name();
        assert club1.leader() != YUKINOSITA;
        assert club1.leader().name().equals("Yukino");
        assert MINISTRATION.leader().name().equals("Yukinosita");

        Club club2 = Club.Operator.leader().set(MINISTRATION, HIKIGAYA);
        assert club2 != MINISTRATION;
        assert club2.name() == MINISTRATION.name();
        assert club2.leader() == HIKIGAYA;

        Club club3 = Club.Operator.memebers().at(0).name().set(MINISTRATION, "YUKINO");
        assert club3 != MINISTRATION;
        assert club3.name() == MINISTRATION.name();
        assert club3.members().get(0) != YUKINOSITA;
        assert club3.members().get(0).name().equals("YUKINO");
        assert MINISTRATION.members().get(0) == YUKINOSITA;

        Club club4 = Club.Operator.memebers().add().set(MINISTRATION, HIKIGAYA);
        assert club4 != MINISTRATION;
        assert club4.name() == MINISTRATION.name();
        assert club4.members().size() == 3;
        assert club4.members().get(0) == YUKINOSITA;
        assert club4.members().get(1) == HIKIGAYA;
        assert club4.members().get(2) == HIKIGAYA;

        Club club5 = Club.Operator.memebers().remove().set(MINISTRATION, HIKIGAYA);
        assert club5 != MINISTRATION;
        assert club5.name() == MINISTRATION.name();
        assert club5.members().get(0) == YUKINOSITA;
        assert club5.members().size() == 1;

        Club club6 = Club.Operator.memebers().clear().apply(MINISTRATION);
        assert club6 != MINISTRATION;
        assert club6.members().size() == 0;
    }
}
