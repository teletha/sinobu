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

/**
 * @version 2015/04/19 19:20:21
 */
public class Person {

    /** Name property. */
    public final String name;

    /** Age property. */
    public final int age;

    /** Gender property. */
    public final Gender gender;

    /**
     * @param name
     * @param age
     * @param gender
     */
    Person(String name, int age, Gender gender) {
        this.name = name;
        this.age = age;
        this.gender = gender;
    }
}
