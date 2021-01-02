/*
 * Copyright (C) 2021 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.sample.bean;

import java.util.Map;

/**
 * DOCUMENT.
 * 
 * @version 2007/06/01 10:44:18
 */
public class CompatibleKeyMap {

    private Map<Integer, Class> integerKey;

    /**
     * Get the integerKey property of this {@link CompatibleKeyMap}.
     * 
     * @return The integerKey property.
     */
    public Map<Integer, Class> getIntegerKey() {
        return integerKey;
    }

    /**
     * Set the integerKey property of this {@link CompatibleKeyMap}.
     * 
     * @param integerKey The integerKey value to set.
     */
    public void setIntegerKey(Map<Integer, Class> integerKey) {
        this.integerKey = integerKey;
    }
}