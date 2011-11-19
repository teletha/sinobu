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
package ezbean;

/**
 * <p>
 * NOTE : This is internal interface. A user of Ezbean <em>does not have to use</em> this interface.
 * </p>
 * <p>
 * This interface represents that properties of a object are able to be acccessed by its property
 * name.
 * </p>
 * 
 * @version 2009/12/29 17:26:40
 */
public interface Accessible {

    /**
     * Retrieve the assosiated context with this accessible object.
     * 
     * @return An assosiated context.
     */
    Listeners context();
}
