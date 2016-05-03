/*
 * Copyright (C) 2016 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.sample.bean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @version 2016/05/02 16:37:04
 */
public class DefaultValue {

    public String value = "default";

    public List<String> items = new ArrayList(Collections.singleton("default"));
}
