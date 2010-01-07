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

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * <p>
 * ModelWalker can walk around in the object graph. Whenever this walker traverses the object graph,
 * the graph walk event will happen and you can receive it by using {@link ModelWalkListener}. To
 * register the {@link ModelWalkListener} to this {@link ModelWalker}, you can use the method
 * {@link #addListener(ModelWalkListener)}.
 * </p>
 * <p>
 * This class is <em>thread-safe</em>. The "snapshot" style traverse method uses a reference to the
 * state of the listener's list at the point that the traverse was started. This list never changes
 * during the traversing of the object graph, so interference is impossible.
 * </p>
 * 
 * @see ModelWalkListener
 * @version 2008/06/12 3:00:09
 */
public class ModelWalker implements PropertyWalker {

    /** The root object. */
    public final Object root;

    /** The model of root object. */
    public final Model rootModel;

    /** The record for traversed objects. */
    protected final Set<Object> record = new HashSet();

    /** The property of root object. */
    private final Property rootProperty;

    /** The list of event listeners. */
    private final List<ModelWalkListener> listeners = new CopyOnWriteArrayList();

    /**
     * Create ModelWalker instance.
     * 
     * @param basePoint
     */
    public ModelWalker(Object basePoint) {
        this.root = basePoint;
        this.rootModel = Model.load(basePoint.getClass());
        this.rootProperty = new Property(rootModel, rootModel.name);
    }

    /**
     * <p>
     * Register the given listener to this model walker.
     * </p>
     * 
     * @param listener A listener to register. <code>null</code> is acceptable.
     */
    public void addListener(ModelWalkListener listener) {
        // check null
        if (listener != null) listeners.add(listener);
    }

    /**
     * <p>
     * Unregister the given listener from this model walker.
     * </p>
     * 
     * @param listener A listener to unregister. <code>null</code> is acceptable.
     */
    public void removeListener(ModelWalkListener listener) {
        listeners.remove(listener);
    }

    /**
     * <p>
     * Traverse this object graph and process each object.
     * </p>
     */
    public void traverse() {
        // traverse all nodes
        traverse(rootModel, rootProperty, root, null);

        // clear walker information
        record.clear();
    }

    /**
     * <p>
     * Traverse this object graph and process each object, then retrieve the object of end point. If
     * the specified property path indicates nonexistent property, <code>null</code> will be
     * returned.
     * </p>
     * 
     * @param path A list of property paths to traverse.
     * @return A object of end point.
     * @throws NullPointerException If the specified path is <code>null</code>.
     */
    public Object traverse(List<String> path) {
        return traverse(rootModel, rootProperty, root, path.iterator());
    }

    /**
     * <p>
     * Traverse this object model graph and retrieve the {@link Model} of end point . If the
     * specified property path indicates nonexistent property name, {@link IllegalArgumentException}
     * will be thrown.
     * </p>
     * 
     * @TODO This method is not used. We can use this in persistence API?
     * @param path A list of property paths to traverse.
     * @return A {@link Model} of end point.
     * @throws IllegalArgumentException If the specified path idicates nonexistent property name.
     */
    public Model traverseModel(List<String> path) {
        Model model = rootModel;

        for (int i = 0; i < path.size(); i++) {
            Property property = model.getProperty(path.get(i));

            if (property == null) {
                throw new IllegalArgumentException(path.get(i) + " property isn't found in " + model);
            }
            model = property.model;
        }
        return model;
    }

    /**
     * @see ezbean.model.PropertyWalker#walk(ezbean.model.Model, ezbean.model.Property,
     *      java.lang.Object)
     */
    public void walk(Model model, Property property, Object node) {
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
        // enter node
        for (ModelWalkListener listener : listeners) {
            listener.enterNode(model, property, node);
        }

        // traverse
        Object value = node;

        if (node != null) {
            if (path == null) {
                // check cyclic node
                if (record.add(node)) property.model.walk(value, this);
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
        for (ModelWalkListener listener : listeners) {
            listener.leaveNode(model, property, node);
        }

        // API definition
        return value;
    }
}
