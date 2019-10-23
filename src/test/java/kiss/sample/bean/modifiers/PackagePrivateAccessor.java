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

public class PackagePrivateAccessor {

    private String both;

    private String getter;

    private String setter;

    /**
     * Get the both property of this {@link PackagePrivateAccessor}.
     * 
     * @return The both prperty.
     */
    String getBoth() {
        return both;
    }

    /**
     * Set the both property of this {@link PackagePrivateAccessor}.
     * 
     * @param both The both value to set.
     */
    void setBoth(String both) {
        this.both = both;
    }

    /**
     * Get the getter property of this {@link PackagePrivateAccessor}.
     * 
     * @return The getter prperty.
     */
    String getGetter() {
        return getter;
    }

    /**
     * Set the getter property of this {@link PackagePrivateAccessor}.
     * 
     * @param getter The getter value to set.
     */
    public void setGetter(String getter) {
        this.getter = getter;
    }

    /**
     * Get the setter property of this {@link PackagePrivateAccessor}.
     * 
     * @return The setter prperty.
     */
    public String getSetter() {
        return setter;
    }

    /**
     * Set the setter property of this {@link PackagePrivateAccessor}.
     * 
     * @param setter The setter value to set.
     */
    void setSetter(String setter) {
        this.setter = setter;
    }

}
