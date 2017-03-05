/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.category;

import org.junit.Test;

import kiss.sample.bean.Person;
import kiss.sample.bean.School;
import kiss.sample.bean.Student;

/**
 * @version 2016/10/18 21:03:22
 */
public class LensTest {

    private final Lens<Person, String> firstName = Lens.of(Person::getFirstName, Person::setFirstName);

    @Test
    public void property() throws Exception {
        Person person = new Person();
        person.setFirstName("test");

        assert firstName.get(person).equals("test");
    }

    private final Lens<Student, School> school = Lens.of(Student::getSchool, Student::setSchool);

    private final Lens<School, String> name = Lens.of(School::getName, School::setName);

    private final Lens<Student, String> schoolName = school.then(name);

    @Test
    public void nested() throws Exception {
        School school = new School();
        school.setName("WoW");

        Student student = new Student();
        student.setSchool(school);

        assert schoolName.get(student).equals("WoW");
        schoolName.set(student, "Change");
        assert student.getSchool().getName().equals("Change");
    }
}
