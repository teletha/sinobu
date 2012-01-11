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

/**
 * @version 2009/07/03 17:33:46
 */
public class ArrayBean {

    private String[] objects;

    private int[] primitives;

    /**
     * Get the objects property of this {@link ArrayBean}.
     * 
     * @return The objects prperty.
     */
    public String[] getObjects() {
        return objects;
    }

    /**
     * Set the objects property of this {@link ArrayBean}.
     * 
     * @param The objects value to set.
     */
    public void setObjects(String[] objects) {
        this.objects = objects;
    }

    /**
     * Get the primitives property of this {@link ArrayBean}.
     * 
     * @return The primitives prperty.
     */
    public int[] getPrimitives() {
        return primitives;
    }

    /**
     * Set the primitives property of this {@link ArrayBean}.
     * 
     * @param The primitives value to set.
     */
    public void setPrimitives(int[] primitives) {
        this.primitives = primitives;
    }

}
