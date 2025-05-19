/*
 * Copyright (C) 2025 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.sample;

public class Person {
    /**
     * @param name
     * @param age
     */
    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    }

    private String name;

    private int age;

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Person [name=" + name + ", age=" + age + "]";
    }
}