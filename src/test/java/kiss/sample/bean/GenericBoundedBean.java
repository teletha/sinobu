/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.sample.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @version 2009/07/14 15:52:16
 */
public class GenericBoundedBean<M extends Person> {

    private M generic;

    /** The list model. */
    private List<M> genericList = new ArrayList();

    /** The map model. */
    private Map<String, M> genericMap = new HashMap();

    /**
     * Get the generic property of this {@link GenericBoundedBean}.
     * 
     * @return The generic property.
     */
    public M getGeneric() {
        return generic;
    }

    /**
     * Set the generic property of this {@link GenericBoundedBean}.
     * 
     * @param generic The generic value to set.
     */
    public void setGeneric(M generic) {
        this.generic = generic;
    }

    /**
     * Get the genericList property of this {@link GenericBoundedBean}.
     * 
     * @return The genericList property.
     */
    public List<M> getGenericList() {
        return genericList;
    }

    /**
     * Set the genericList property of this {@link GenericBoundedBean}.
     * 
     * @param genericList The genericList value to set.
     */
    public void setGenericList(List<M> genericList) {
        this.genericList = genericList;
    }

    /**
     * Get the genericMap property of this {@link GenericBoundedBean}.
     * 
     * @return The genericMap property.
     */
    public Map<String, M> getGenericMap() {
        return genericMap;
    }

    /**
     * Set the genericMap property of this {@link GenericBoundedBean}.
     * 
     * @param genericMap The genericMap value to set.
     */
    public void setGenericMap(Map<String, M> genericMap) {
        this.genericMap = genericMap;
    }

}
