/*
 * Copyright (C) 2020 Nameless Production Committee
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
 * DOCUMENT.
 * 
 * @version 2008/06/18 10:40:57
 */
public class WildcardBean {

    private List<?> wildcardList;

    private Map<String, ?> wildcardMap;

    /**
     * Get the wildcardList property of this {@link WildcardBean}.
     * 
     * @return The wildcardList property.
     */
    public List<?> getWildcardList() {
        return wildcardList;
    }

    /**
     * Set the wildcardList property of this {@link WildcardBean}.
     * 
     * @param wildcardList The wildcardList value to set.
     */
    public void setWildcardList(List<?> wildcardList) {
        this.wildcardList = wildcardList;
    }

    /**
     * Get the wildcardMap property of this {@link WildcardBean}.
     * 
     * @return The wildcardMap property.
     */
    public Map<String, ?> getWildcardMap() {
        return wildcardMap;
    }

    /**
     * Set the wildcardMap property of this {@link WildcardBean}.
     * 
     * @param wildcardMap The wildcardMap value to set.
     */
    public void setWildcardMap(Map<String, ?> wildcardMap) {
        this.wildcardMap = wildcardMap;
    }
}