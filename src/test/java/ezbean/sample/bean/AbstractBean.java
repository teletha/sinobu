/*
 * Copyright (C) 2010 Nameless Production Committee.
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
package ezbean.sample.bean;

/**
 * DOCUMENT.
 * 
 * @version 2008/06/18 10:39:13
 */
public abstract class AbstractBean {

    /**
     * Getter.
     */
    public abstract int getFoo();

    /**
     * Setter.
     */
    public abstract void setFoo(int foo);

    /**
     * Concreate method.
     */
    public void concreateMethod() throws Exception {
        setFoo(1);
    }
}
