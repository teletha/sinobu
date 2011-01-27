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
package ezbean.scratchpad;

/**
 * @version 2009/12/27 17:20:01
 */
public class ASMfierDebugBase {

    private String name;

    private int age;

    /**
     * Get the name property of this {@link ASMfierDebugBase}.
     * 
     * @return The name property.
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name property of this {@link ASMfierDebugBase}.
     * 
     * @param name The name value to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the age property of this {@link ASMfierDebugBase}.
     * 
     * @return The age property.
     */
    public int getAge() {
        return age;
    }

    /**
     * Set the age property of this {@link ASMfierDebugBase}.
     * 
     * @param age The age value to set.
     */
    public void setAge(int age) {
        this.age = age;
    }

    public void talk(String message, int count) {

    }

    public boolean isHungry() {
        return false;
    }
}
