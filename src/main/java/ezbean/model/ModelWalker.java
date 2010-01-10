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
package ezbean.model;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * <p>
 * ModelWalker can walk around in the object graph. Whenever this walker traverses the object graph,
 * the graph walk event will happen and you can receive it by overwrite
 * {@link #enter(Model, Property, Object, boolean)} and
 * {@link #leave(Model, Property, Object, boolean)} methods.
 * </p>
 * <p>
 * This class is <em>thread-safe</em>. The "snapshot" style traverse method uses a reference to the
 * state of the listener's list at the point that the traverse was started. This list never changes
 * during the traversing of the object graph, so interference is impossible.
 * </p>
 * 
 * @version 2010/01/10 8:30:22
 */
public abstract class ModelWalker implements PropertyWalker {

    /** The record for traversed objects. */
    protected final LinkedHashSet record = new LinkedHashSet();

    /**
     * <p>
     * Traverse this object graph and process each object.
     * </p>
     * 
     * @param base A point of departure.
     */
    public void traverse(Object base) {
        Model model = Model.load(base.getClass());

        // traverse all nodes
        traverse(model, new Property(model, model.name), base, null);

        // clear walker information
        record.clear();
    }

    /**
     * <p>
     * Traverse this object graph and process each object, then retrieve the object in a point of
     * arraival. If the specified property path indicates nonexistent property, <code>null</code>
     * will be returned.
     * </p>
     * 
     * @param base A point of departure.
     * @param path A list of property paths to traverse.
     * @return A point of arrival.
     * @throws NullPointerException If the specified path is <code>null</code>.
     */
    public Object traverse(Object base, List<String> path) {
        Model model = Model.load(base.getClass());

        // start traversing along the path
        return traverse(model, new Property(model, model.name), base, path.iterator());
    }

    /**
     * @see ezbean.model.PropertyWalker#walk(ezbean.model.Model, ezbean.model.Property,
     *      java.lang.Object)
     */
    public final void walk(Model model, Property property, Object node) {
        traverse(model, property, node, null);
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
     * @param path A iterator of property names.
     */
    private Object traverse(Model model, Property property, Object node, Iterator<String> path) {
        // check cyclic reference
        boolean cyclic = !record.add(node);

        // enter node
        enter(model, property, node, cyclic);

        // traverse
        Object value = node;

        if (node != null) {
            if (path == null) {
                // check cyclic node
                if (!cyclic) property.model.walk(value, this);
            } else {
                if (path.hasNext()) {
                    Model nextModel = property.model;
                    Property nextProperty = nextModel.getProperty(path.next());

                    if (nextProperty == null) {
                        value = null;
                    } else {
                        value = traverse(nextModel, nextProperty, nextModel.get(node, nextProperty), path);
                    }
                }
            }
        }

        // leave node
        leave(model, property, node, cyclic);

        // check cyclic reference
        record.remove(node);

        // API definition
        return value;
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
     * @param cyclic TODO
     */
    protected abstract void enter(Model model, Property property, Object node, boolean cyclic);

    /**
     * This method is called whenever the {@link ModelWalker} leaves a node in object graph.
     * 
     * @param model A object model of the base node that {@link ModelWalker} started from. This
     *            value must not be <code>null</code>. If the visited node is root, this value will
     *            be a object model of the root node.
     * @param property An arc in object graph. This value must not be <code>null</code>. If the
     *            visited node is root, this value will be a object property of the root node.
     * @param node A current node that {@link ModelWalker} arrives at.
     * @param cyclic TODO
     */
    protected abstract void leave(Model model, Property property, Object node, boolean cyclic);
}
