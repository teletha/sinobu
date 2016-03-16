/*
 * Copyright (C) 2016 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.json;

import java.util.StringJoiner;

import org.junit.Test;

import kiss.I;
import kiss.sample.bean.Person;

/**
 * @version 2016/03/16 15:27:04
 */
public class SerializeTest {

    @Test
    public void object() {
        Person person = new Person();
        person.setAge(20);
        person.setFirstName("Umi");
        person.setLastName("Sonoda");

        validate(person, //
        "{", //
        "  'age': '20',", //
        "  'firstName': 'Umi',", //
        "  'lastName': 'Sonoda'", //
        "}");
    }

    /**
     * <p>
     * Write JSON.
     * </p>
     * 
     * @param texts
     * @return
     */
    private static void validate(Object model, String... texts) {
        StringBuilder compressed = new StringBuilder();
        StringBuilder formated = new StringBuilder();
        I.write(model, compressed, false);
        I.write(model, formated, true);

        StringJoiner compressedExpectation = new StringJoiner("");
        StringJoiner formattedExpectation = new StringJoiner("\r\n");

        for (String text : texts) {
            text = text.replaceAll("'", "\"");

            compressedExpectation.add(text.trim().replaceAll(" ?: ?", ":"));
            formattedExpectation.add(text.replaceAll("  ", "\t"));
        }

        // validate
        assert compressedExpectation.toString().equals(compressed.toString());
        assert formattedExpectation.toString().equals(formated.toString());
    }
}
