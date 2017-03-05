/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.sample.bean;

import java.util.List;

/**
 * @version 2015/07/30 14:09:52
 */
public class ClassListProperty {

    private List<Class> list;

    /**
     * Get the list property of this {@link ClassListProperty}.
     * 
     * @return The list prperty.
     */
    public List<Class> getList() {
        return list;
    }

    /**
     * Set the list property of this {@link ClassListProperty}.
     * 
     * @param list The list value to set.
     */
    public void setList(List<Class> list) {
        this.list = list;
    }

}
