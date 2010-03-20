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
package ezbean;

/**
 * <p>
 * The Disposable interface is used when components need to deallocate and dispose resources prior
 * to their destruction.
 * </p>
 * 
 * @version 2008/06/10 12:20:54
 */
public interface Disposable {

    /**
     * <p>
     * The dispose operation is called at the end of a components lifecycle. Components use this
     * method to release and destroy any resources that the Component owns.
     * </p>
     */
    void dispose();
}
