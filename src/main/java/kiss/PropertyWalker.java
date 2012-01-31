/*
 * Copyright (C) 2012 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss;

import kiss.model.Model;
import kiss.model.Property;

/**
 * <p>
 * This class behaves like property iterator.
 * </p>
 * <p>
 * PropertyWalker can walk around in the object graph. Whenever this walker traverses the object
 * graph, the graph walk event will happen and you can receive it by overwrite method.
 * </p>
 * 
 * @see Model#walk(Object, PropertyWalker)
 * @version 2011/12/26 13:05:59
 */
public interface PropertyWalker {

    /**
     * <p>
     * Walk around all properties in the specified model object.
     * </p>
     * 
     * @param model A object model of the base node that started from. This value must not be
     *            <code>null</code>.
     * @param property An arc in object graph. This value must not be <code>null</code>.
     * @param node A current node that arrives at. This value must not be <code>null</code>.
     */
    void walk(Model model, Property property, Object node);
}
