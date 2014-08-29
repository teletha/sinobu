/*
 * Copyright (C) 2014 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.sample.bean;

/**
 * DOCUMENT.
 * 
 * @version 2008/06/10 23:40:40
 */
public class ChainBean {

    /** The next bean. */
    private ChainBean next;

    private String name;

    /**
     * Get the next property of this {@link ChainBean}.
     * 
     * @return The next property.
     */
    public ChainBean getNext() {
        return next;
    }

    /**
     * Set the next property of this {@link ChainBean}.
     * 
     * @param next The next value to set.
     */
    public void setNext(ChainBean next) {
        this.next = next;
    }

    public ChainBean setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return name;
    }

}
