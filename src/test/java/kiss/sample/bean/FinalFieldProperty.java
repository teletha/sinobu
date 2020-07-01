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

/**
 * @version 2017/12/28 16:06:20
 */
public class FinalFieldProperty<T> {

    public final String attribute = "";

    public final Person noneAttribute = new Person();

    {
        noneAttribute.setFirstName("First");
        noneAttribute.setAge(10);
    }
}