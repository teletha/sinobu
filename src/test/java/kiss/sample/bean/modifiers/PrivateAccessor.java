/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.sample.bean.modifiers;

@SuppressWarnings("unused")
public class PrivateAccessor {

    private String both;

    private String getter;

    private String setter;

    /**
     * Get the both property of this {@link PrivateAccessor}.
     * 
     * @return The both prperty.
     */
    private String getBoth() {
        return both;
    }

    /**
     * Set the both property of this {@link PrivateAccessor}.
     * 
     * @param both The both value to set.
     */
    private void setBoth(String both) {
        this.both = both;
    }

    /**
     * Get the getter property of this {@link PrivateAccessor}.
     * 
     * @return The getter prperty.
     */
    private String getGetter() {
        return getter;
    }

    /**
     * Set the getter property of this {@link PrivateAccessor}.
     * 
     * @param getter The getter value to set.
     */
    public void setGetter(String getter) {
        this.getter = getter;
    }

    /**
     * Get the setter property of this {@link PrivateAccessor}.
     * 
     * @return The setter prperty.
     */
    public String getSetter() {
        return setter;
    }

    /**
     * Set the setter property of this {@link PrivateAccessor}.
     * 
     * @param setter The setter value to set.
     */
    private void setSetter(String setter) {
        this.setter = setter;
    }

}
