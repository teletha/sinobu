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

import java.io.Serializable;
import java.util.List;

/**
 * DOCUMENT.
 * 
 * @version 2007/05/31 23:44:43
 */
public class WildcardTypeSetter {

    public List<Serializable> getList() {
        return null;
    }

    public void setList(List<? extends Serializable> list) {
    }
}