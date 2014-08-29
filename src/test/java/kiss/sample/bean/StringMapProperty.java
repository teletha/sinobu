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

import java.util.Map;

/**
 * DOCUMENT.
 * 
 * @author <a href="mailto:Teletha.T@gmail.com">Teletha Testarossa</a>
 * @version $ Id: StringMap.java,v 1.0 2007/02/18 12:39:08 Teletha Exp $
 */
public class StringMapProperty {

    private Map<String, String> map;

    /**
     * Get the map property of this {@link StringMapProperty}.
     * 
     * @return The map prperty.
     */
    public Map<String, String> getMap() {
        return map;
    }

    /**
     * Set the map property of this {@link StringMapProperty}.
     * 
     * @param map The map value to set.
     */
    public void setMap(Map<String, String> map) {
        this.map = map;
    }
}
