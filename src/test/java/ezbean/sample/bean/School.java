/*
 * Copyright (C) 2011 Nameless Production Committee.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ezbean.sample.bean;

import java.util.List;

/**
 * DOCUMENT.
 * 
 * @author <a href="mailto:Teletha.T@gmail.com">Teletha Testarossa</a>
 * @version $ Id: School.java,v 1.0 2007/01/08 15:42:00 Teletha Exp $
 */
public class School {

    /** The school name. */
    private String name;

    /** The students list. */
    private List<Student> students;

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
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return name;
    }
}
