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
package ezbean.model;

/**
 * <p>
 * This class behaves like property iterator.
 * </p>
 * 
 * @see Model#walk(Object, PropertyWalker)
 * @version 2008/06/13 7:35:57
 */
public interface PropertyWalker {

    /**
     * <p>
     * Walk around all properties in the specified model object.
     * </p>
     * 
     * @param model A object model of the base node that {@link Model#walk(Object, PropertyWalker)}
     *            started from. This value must not be <code>null</code>.
     * @param property An arc in object graph. This value must not be <code>null</code>.
     * @param node A current node that {@link Model#walk(Object, PropertyWalker)} arrives at. This
     *            value must not be <code>null</code>.
     */
    void walk(Model model, Property property, Object node);
}
