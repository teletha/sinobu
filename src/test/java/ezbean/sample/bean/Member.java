/*
 * Copyright (C) 2010 Nameless Production Committee.
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

/**
 * DOCUMENT.
 * 
 * @author <a href="mailto:Teletha.T@gmail.com">Teletha Testarossa</a>
 * @version $ Id: Member.java,v 1.0 2006/12/23 21:14:18 Teletha Exp $
 */
public class Member {

    private int id;

    private String name;

    private Address address;

    private Group group;

    /**
     * Get the address property of this {@link Member}.
     * 
     * @return The address prperty.
     */
    public Address getAddress() {
        return address;
    }

    /**
     * Set the address property of this {@link Member}.
     * 
     * @param address The address value to set.
     */
    public void setAddress(Address address) {
        this.address = address;
    }

    /**
     * Get the group property of this {@link Member}.
     * 
     * @return The group prperty.
     */
    public Group getGroup() {
        return group;
    }

    /**
     * Set the group property of this {@link Member}.
     * 
     * @param group The group value to set.
     */
    public void setGroup(Group group) {
        this.group = group;
    }

    /**
     * Get the id property of this {@link Member}.
     * 
     * @return The id prperty.
     */
    public int getId() {
        return id;
    }

    /**
     * Set the id property of this {@link Member}.
     * 
     * @param id The id value to set.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Get the name property of this {@link Member}.
     * 
     * @return The name prperty.
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name property of this {@link Member}.
     * 
     * @param name The name value to set.
     */
    public void setName(String name) {
        this.name = name;
    }

}
