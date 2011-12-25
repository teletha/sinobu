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

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * <p>
 * ModelWalker can walk around in the object graph. Whenever this walker traverses the object graph,
 * the graph walk event will happen and you can receive it by overwrite
 * {@link #enter(Model, Property, Object)} and {@link #leave(Model, Property, Object)} methods.
 * </p>
 * <p>
 * This class is <em>not thread-safe</em>.
 * </p>
 * 
 * @version 2010/01/12 20:58:22
 */
public abstract class ModelWalker implements PropertyWalker {

    /** The record for traversed objects. */
    protected final Set nodes = new LinkedHashSet();

    /**
     * <p>
     * Traverse this object graph and process each object.
     * </p>
     * 
     * @param node A point of departure.
     */
    public void walk(Object node) {
        Model model = Model.load(node.getClass());

        // traverse all nodes
        walk(model, new Property(model, model.name), node);

        // clear walker information
        nodes.clear();
    }

    /**
     * <p>
     * Traverse this object graph actually.
     * </p>
     * 
     * @param model A object model of the base node that {@link ModelWalker} started from. This
     *            value must not be <code>null</code>. If the visited node is root, this value will
     *            be a object model of the root node.
     * @param property An arc in object graph. This value must not be <code>null</code>. If the
     *            visited node is root, this value will be a object property of the root node.
     * @param node A current node that {@link ModelWalker} arrives at.
     * @see ezbean.model.PropertyWalker#walk(ezbean.model.Model, ezbean.model.Property,
     *      java.lang.Object)
     */
    public final void walk(Model model, Property property, Object node) {
        if (!property.isTransient()) {
            // enter node
            enter(model, property, node);

            if (node != null) {
                // check cyclic node
                if (nodes.add(node)) property.model.walk(node, this);
            }

            // leave node
            leave(model, property, node);
        }
    }

    /**
     * This method is called whenever the {@link ModelWalker} visits a node in object graph.
     * 
     * @param model A object model of the base node that {@link ModelWalker} started from. This
     *            value must not be <code>null</code>. If the visited node is root, this value will
     *            be a object model of the root node.
     * @param property An arc in object graph. This value must not be <code>null</code>. If the
     *            visited node is root, this value will be a object property of the root node.
     * @param node A current node that {@link ModelWalker} arrives at.
     */
    protected abstract void enter(Model model, Property property, Object node);

    /**
     * This method is called whenever the {@link ModelWalker} leaves a node in object graph.
     * 
     * @param model A object model of the base node that {@link ModelWalker} started from. This
     *            value must not be <code>null</code>. If the visited node is root, this value will
     *            be a object model of the root node.
     * @param property An arc in object graph. This value must not be <code>null</code>. If the
     *            visited node is root, this value will be a object property of the root node.
     * @param node A current node that {@link ModelWalker} arrives at.
     */
    protected abstract void leave(Model model, Property property, Object node);
}
