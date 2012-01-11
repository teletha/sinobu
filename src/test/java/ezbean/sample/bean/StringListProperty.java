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

import java.util.List;

/**
 * DOCUMENT.
 * 
 * @author <a href="mailto:Teletha.T@gmail.com">Teletha Testarossa</a>
 * @version $ Id: StringList.java,v 1.0 2007/01/16 19:08:54 Teletha Exp $
 */
public class StringListProperty {

    private List<String> list;

    /**
     * Get the list property of this {@link StringListProperty}.
     * 
     * @return The list prperty.
     */
    public List<String> getList() {
        return list;
    }

    /**
     * Set the list property of this {@link StringListProperty}.
     * 
     * @param list The list value to set.
     */
    public void setList(List<String> list) {
        this.list = list;
    }

}
