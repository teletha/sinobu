/*
 * Copyright (C) 2011 Nameless Production Committee.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ezbean.sample.bean.invalid;

import ezbean.sample.bean.Person;

/**
 * @version 2009/12/30 19:17:08
 */
public class OverrideFinalAccessor extends Person {

    /**
     * @see ezbean.sample.bean.Person#getAge()
     */
    @Override
    public final int getAge() {
        return super.getAge();
    }

    /**
     * @see ezbean.sample.bean.Person#getLastName()
     */
    @Override
    public final String getLastName() {
        return super.getLastName();
    }

    /**
     * @see ezbean.sample.bean.Person#setAge(int)
     */
    @Override
    public final void setAge(int age) {
        super.setAge(age);
    }

    /**
     * @see ezbean.sample.bean.Person#setFirstName(java.lang.String)
     */
    @Override
    public final void setFirstName(String firstName) {
        super.setFirstName(firstName);
    }
}
