/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.sample.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @version 2011/03/30 16:33:05
 */
public class School {

    /** The school name. */
    private String name;

    /** The students list. */
    private List<Student> students = new ArrayList();

    /** The teacher list. */
    private Map<String, Person> teachers;

    public void addStudent(Student student) {
        students.add(student);
    }

    /**
     * Get the name property of this {@link School}.
     * 
     * @return The name prperty.
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name property of this {@link School}.
     * 
     * @param name The name value to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the students property of this {@link School}.
     * 
     * @return The students prperty.
     */
    public List<Student> getStudents() {
        return students;
    }

    /**
     * Set the students property of this {@link School}.
     * 
     * @param students The students value to set.
     */
    public void setStudents(List<Student> students) {
        this.students = students;
    }

    /**
     * Get the teachers property of this {@link School}.
     * 
     * @return The teachers property.
     */
    public Map<String, Person> getTeachers() {
        return teachers;
    }

    /**
     * Set the teachers property of this {@link School}.
     * 
     * @param teachers The teachers value to set.
     */
    public void setTeachers(Map<String, Person> teachers) {
        this.teachers = teachers;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return name;
    }
}
