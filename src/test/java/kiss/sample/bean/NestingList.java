/*
 * Copyright (C) 2024 The SINOBU Development Team
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
 * DOCUMENT.
 * 
 * @author <a href="mailto:Teletha.T@gmail.com">Teletha Testarossa</a>
 * @version $ Id: NestingList.java,v 1.0 2007/01/16 19:01:10 Teletha Exp $
 */
public class NestingList {

    private List<List<Integer>> nesting;

    /**
     * Get the nesting property of this {@link NestingList}.
     * 
     * @return The nesting prperty.
     */
    public List<List<Integer>> getNesting() {
        return nesting;
    }

    /**
     * Set the nesting property of this {@link NestingList}.
     * 
     * @param nesting The nesting value to set.
     */
    public void setNesting(List<List<Integer>> nesting) {
        this.nesting = nesting;
    }

}