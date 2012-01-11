/*
 * Copyright (C) 2012 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package ezbean.sample.bean;

import ezbean.sample.RuntimeAnnotation1;

/**
 * @version 2009/04/12 16:26:19
 */
public class Person {

    private int age;

    private String firstName;

    private String lastName;

    /**
     * Get the age property of this {@link Person}.
     * 
     * @return The age prperty.
     */
    public int getAge() {
        return age;
    }

    /**
     * Set the age property of this {@link Person}.
     * 
     * @param age The age value to set.
     */
    @RuntimeAnnotation1
    public void setAge(int age) {
        this.age = age;
    }

    /**
     * Get the firstName property of this {@link Person}.
     * 
     * @return The firstName prperty.
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Set the firstName property of this {@link Person}.
     * 
     * @param firstName The firstName value to set.
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Get the lastName property of this {@link Person}.
     * 
     * @return The lastName prperty.
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Set the lastName property of this {@link Person}.
     * 
     * @param lastName The lastName value to set.
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}
