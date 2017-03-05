/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss;

import org.junit.Test;

import kiss.sample.bean.Person;
import kiss.sample.bean.Student;

/**
 * @version 2015/09/06 22:16:21
 */
public class AssociateTest {

    @Test
    public void associate() throws Exception {
        Object host1 = new Object();
        Person person1A = I.associate(host1, Person.class);
        Person person1B = I.associate(host1, Person.class);

        assert person1A == person1B;
    }

    @Test
    public void associateMultiple() throws Exception {
        Object host1 = new Object();
        Person person1A = I.associate(host1, Person.class);
        Person person1B = I.associate(host1, Person.class);

        Student student1A = I.associate(host1, Student.class);
        Student student1B = I.associate(host1, Student.class);

        assert person1A == person1B;
        assert student1A == student1B;
    }
}
