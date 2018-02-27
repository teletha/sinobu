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

import java.util.List;
import java.util.Map;

/**
 * @version 2009/07/17 14:52:13
 */
public class GenericGetterBean extends GenericBean<String> {

    /**
     * @see kiss.sample.bean.GenericBean#getGeneric()
     */
    @Override
    public String getGeneric() {
        return super.getGeneric();
    }

    /**
     * @see kiss.sample.bean.GenericBean#getGenericList()
     */
    @Override
    public List<String> getGenericList() {
        return super.getGenericList();
    }

    /**
     * @see kiss.sample.bean.GenericBean#getGenericMap()
     */
    @Override
    public Map<String, String> getGenericMap() {
        return super.getGenericMap();
    }

}
