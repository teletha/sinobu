/*
 * Copyright (C) 2015 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.sample.bean;

import java.util.List;

/**
 * DOCUMENT.
 * 
 * @author <a href="mailto:Teletha.T@gmail.com">Teletha Testarossa</a>
 * @version $ Id: Group.java,v 1.0 2006/12/09 9:26:07 Teletha Exp $
 */
public class Group {
    
    /** The group name. */
    private String name;
    
    /** The group member list. */
    private List<Person> members;

    
    /**
     * Get the members property of this {@link Group}.
     *
     * @return The members prperty.
     */
    public List<Person> getMembers() {
        return members;
    }

    
    /**
     * Set the members property of this {@link Group}.
     *
     * @param members The members value to set.
     */
    public void setMembers(List<Person> members) {
        this.members = members;
    }

    
    /**
     * Get the name property of this {@link Group}.
     *
     * @return The name prperty.
     */
    public String getName() {
        return name;
    }

    
    /**
     * Set the name property of this {@link Group}.
     *
     * @param name The name value to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    
}
