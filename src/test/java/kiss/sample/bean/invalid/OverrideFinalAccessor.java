/*
 * Copyright (C) 2012 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.sample.bean.invalid;

import kiss.sample.bean.Person;

/**
 * @version 2009/12/30 19:17:08
 */
public class OverrideFinalAccessor extends Person {

    /**
     * @see kiss.sample.bean.Person#getAge()
     */
    @Override
    public final int getAge() {
        return super.getAge();
    }

    /**
     * @see kiss.sample.bean.Person#getLastName()
     */
    @Override
    public final String getLastName() {
        return super.getLastName();
    }

    /**
     * @see kiss.sample.bean.Person#setAge(int)
     */
    @Override
    public final void setAge(int age) {
        super.setAge(age);
    }

    /**
     * @see kiss.sample.bean.Person#setFirstName(java.lang.String)
     */
    @Override
    public final void setFirstName(String firstName) {
        super.setFirstName(firstName);
    }
}
