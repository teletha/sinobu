/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.sample.bean.invalid;

/**
 * DOCUMENT.
 * 
 * @author <a href="mailto:Teletha.NPC@gmail.com">Teletha Testarossa</a>
 * @version $ Id: ProtectedAccessor.java,v 1.0 2007/04/04 11:14:45 Teletha Exp $
 */
public class ProtectedAccessor {

    private String both;

    private String getter;

    private String setter;

    /**
     * Get the both property of this {@link ProtectedAccessor}.
     * 
     * @return The both prperty.
     */
    protected String getBoth() {
        return both;
    }

    /**
     * Set the both property of this {@link ProtectedAccessor}.
     * 
     * @param both The both value to set.
     */
    protected void setBoth(String both) {
        this.both = both;
    }

    /**
     * Get the getter property of this {@link ProtectedAccessor}.
     * 
     * @return The getter prperty.
     */
    protected String getGetter() {
        return getter;
    }

    /**
     * Set the getter property of this {@link ProtectedAccessor}.
     * 
     * @param getter The getter value to set.
     */
    public void setGetter(String getter) {
        this.getter = getter;
    }

    /**
     * Get the setter property of this {@link ProtectedAccessor}.
     * 
     * @return The setter prperty.
     */
    public String getSetter() {
        return setter;
    }

    /**
     * Set the setter property of this {@link ProtectedAccessor}.
     * 
     * @param setter The setter value to set.
     */
    protected void setSetter(String setter) {
        this.setter = setter;
    }

}
