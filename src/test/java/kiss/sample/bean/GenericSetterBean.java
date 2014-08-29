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

import java.util.List;
import java.util.Map;

/**
 * @version 2009/07/17 14:52:52
 */
public class GenericSetterBean extends GenericBean<String> {

    /**
     * @see kiss.sample.bean.GenericBean#setGeneric(java.lang.Object)
     */
    @Override
    public void setGeneric(String generic) {
        super.setGeneric(generic);
    }

    /**
     * @see kiss.sample.bean.GenericBean#setGenericList(java.util.List)
     */
    @Override
    public void setGenericList(List<String> genericList) {
        super.setGenericList(genericList);
    }

    /**
     * @see kiss.sample.bean.GenericBean#setGenericMap(java.util.Map)
     */
    @Override
    public void setGenericMap(Map<String, String> genericMap) {
        super.setGenericMap(genericMap);
    }

}
