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

import kiss.Variable;

public class TransientBean {

    public transient String field;

    public String noneField;

    public transient Variable<String> variable = Variable.empty();

    public Variable<String> noneVariable = Variable.empty();
}