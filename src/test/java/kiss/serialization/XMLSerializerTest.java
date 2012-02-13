/*
 * Copyright (C) 2012 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.serialization;

import java.nio.file.Path;

import kiss.I;
import kiss.XMLizer;
import kiss.sample.bean.School;
import kiss.sample.bean.Student;

import org.junit.Rule;
import org.junit.Test;

import antibug.CleanRoom;

/**
 * @version 2012/02/13 13:54:14
 */
public class XMLSerializerTest {

    /** The temporaries. */
    @Rule
    public static final CleanRoom room = new CleanRoom();

    /** The serialization file. */
    private static final Path config = room.locateFile("config.xml");

    @Test
    public void xml() throws Exception {
        Student sena = I.make(Student.class);
        sena.setAge(16);
        sena.setName("柏崎 星奈");

        School school = new School();
        school.setName("聖クロニカ学園高等部");
        school.addStudent(sena);

        XMLizer lizer = new XMLizer();
        lizer.write(school);
    }
}
