/*
 * Copyright (C) 2021 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.sample.bean.modifiers;

import kiss.sample.bean.Person;

public class OverrideFinalAccessor extends Person {

    @Override
    public final int getAge() {
        return super.getAge();
    }

    @Override
    public final String getLastName() {
        return super.getLastName();
    }

    @Override
    public final void setAge(int age) {
        super.setAge(age);
    }

    @Override
    public final void setFirstName(String firstName) {
        super.setFirstName(firstName);
    }
}