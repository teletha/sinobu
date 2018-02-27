/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.sample.bean;

/**
 * DOCUMENT.
 * 
 * @version 2008/06/28 15:15:32
 */
public class Student extends Person {

    /** The school. */
    private School school;

    /**
     * Get the school property of this {@link Student}.
     * 
     * @return The school prperty.
     */
    public School getSchool() {
        return school;
    }

    /**
     * Set the school property of this {@link Student}.
     * 
     * @param school The school value to set.
     */
    public void setSchool(School school) {
        this.school = school;
    }
}
