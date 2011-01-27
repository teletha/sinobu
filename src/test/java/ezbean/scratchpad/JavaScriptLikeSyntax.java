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
 * DOCUMENT.
 * 
 * @version 2008/07/01 11:31:09
 */
public class JavaScriptLikeSyntax {

    private String name;

    /**
     * This is property.
     * 
     * @param name
     * @return
     */
    public String name(String... name) {
        if (name.length != 0) {
            this.name = name[0];
        }
        return this.name;
    }

    public static final void main(String[] args) {
        JavaScriptLikeSyntax script = new JavaScriptLikeSyntax();

        String name = script.name();
        System.out.println(name);

        script.name("test");
        System.out.println(script.name());
    }

    /**
     * DOCUMENT.
     * 
     * @version 2008/07/01 11:45:20
     */
    public static abstract class InterfaceClass {

        /**
         * This is property.
         * 
         * @param name
         * @return
         */
        public abstract String name(String... name);

        /**
         * This is property.
         * 
         * @param age
         * @return
         */
        public abstract int age(int... age);

        /**
         * This is method.
         * 
         * @param message
         */
        public abstract void say(Object... message);

        /**
         * This is delegation.
         * 
         * @param message
         */
        protected void say$1(String message) {
            System.out.println(message);
        }

        /**
         * This is delegation.
         * 
         * @param age
         */
        protected void say$2(int age) {
            System.out.println("I'm " + age + " years old.");
        }
    }
}
