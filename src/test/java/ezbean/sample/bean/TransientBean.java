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

import java.beans.Transient;

/**
 * @version 2011/02/27 21:42:10
 */
public class TransientBean {

    private int onlyGetter;

    private int onlySetter;

    private int both;

    private int inverse;

    private int none;

    /**
     * Get the onlyGetter property of this {@link TransientBean}.
     * 
     * @return The onlyGetter property.
     */
    @Transient
    public int getOnlyGetter() {
        return onlyGetter;
    }

    /**
     * Set the onlyGetter property of this {@link TransientBean}.
     * 
     * @param onlyGetter The onlyGetter value to set.
     */
    public void setOnlyGetter(int onlyGetter) {
        this.onlyGetter = onlyGetter;
    }

    /**
     * Get the onlySetter property of this {@link TransientBean}.
     * 
     * @return The onlySetter property.
     */
    public int getOnlySetter() {
        return onlySetter;
    }

    /**
     * Set the onlySetter property of this {@link TransientBean}.
     * 
     * @param onlySetter The onlySetter value to set.
     */
    @Transient
    public void setOnlySetter(int onlySetter) {
        this.onlySetter = onlySetter;
    }

    /**
     * Get the both property of this {@link TransientBean}.
     * 
     * @return The both property.
     */
    @Transient
    public int getBoth() {
        return both;
    }

    /**
     * Set the both property of this {@link TransientBean}.
     * 
     * @param both The both value to set.
     */
    @Transient
    public void setBoth(int both) {
        this.both = both;
    }

    /**
     * Get the inverse property of this {@link TransientBean}.
     * 
     * @return The inverse property.
     */
    @Transient
    public int getInverse() {
        return inverse;
    }

    /**
     * Set the inverse property of this {@link TransientBean}.
     * 
     * @param inverse The inverse value to set.
     */
    @Transient(value = false)
    public void setInverse(int inverse) {
        this.inverse = inverse;
    }

    /**
     * Get the none property of this {@link TransientBean}.
     * 
     * @return The none property.
     */
    public int getNone() {
        return none;
    }

    /**
     * Set the none property of this {@link TransientBean}.
     * 
     * @param none The none value to set.
     */
    public void setNone(int none) {
        this.none = none;
    }
}
