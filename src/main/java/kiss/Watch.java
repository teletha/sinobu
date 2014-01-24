/*
 * Copyright (C) 2013 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss;

import java.beans.PropertyChangeEvent;
import java.util.List;

import kiss.model.Model;
import kiss.model.Property;

/**
 * @version 2014/01/23 15:57:06
 */
public class Watch implements Disposable, Observer<PropertyChangeEvent> {

    /** The root object. */
    final Object root;

    /** The property path. */
    final List<String> path;

    /** The path length. This value is referred frequently, so cache it. */
    final int length;

    /** The delegation of property change event. */
    Observer observer;

    /** The flag. */
    private boolean whileUpdate = false;

    /** [0] is object(Object), [1] is property name(String) and [2] is old value(Object). */
    private Object[] info = new Object[3];

    /**
     * Initialization : register listener for each nodes on path
     * 
     * @param tracer
     * @param observer
     */
    Watch(List tracer, Observer observer) {
        this.root = tracer.remove(0);
        this.path = tracer;
        this.length = path.size();
        this.observer = observer;

        traverse(root, path, 2);
    }

    private Object traverse(Object node, List<String> route, int mode) {
        Model model = Model.load(node.getClass());
        Property property = new Property(model, model.name);

        root: for (int index = 0; index < route.size(); index++) {
            switch (mode) {
            case 1:
                if (info[0] == node) {
                    mode = 4;
                }
                break;

            case 4:
                if (!property.name.equals(info[1])) {
                    mode = 1;
                    break;
                }

                // remove all registered listeners from the sequence of old value
                if (info[2] != null) {
                    mode = 3; // move into remove mode

                    // Remove actually
                    //
                    // Only if we remove listeners, we can do like the following
                    // walker.traverse(path.subList(index, length - 1));
                    // But, in the greed, we need the actual old value which is indicated by the
                    // property path at once method call.
                    info[2] = traverse(info[2], route.subList(index, length), 3);
                }

                // don't break, step into the next to add listeners
                mode = 2; // move into add mode

            case 2: // add mode
            case 3: // remove mode
                // The location (index == length) indicates the last property path. It is no need to
                // be aware of property change event.
                Table<String, Observer> line = Interceptor.context(node);

                if (mode == 2) {
                    line.push(route.get(index), this);

                    info[0] = node;
                } else {
                    line.pull(route.get(index), this);
                }
                break;

            default: // normal mode
                break;
            }

            model = property.model;
            property = model.getProperty(route.get(index));

            if (property == null) {
                break root;
            } else {
                node = model.get(node, property);

                if (node == null) {
                    break root;
                }
            }
        }
        return node;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onNext(PropertyChangeEvent event) {
        // The property value of the specified object was changed in some property path, but we
        // don't know the location of property change yet. At first, we must search the location
        // (index = n), then remove all registered listeners form the sequence of old value (from
        // n+1 to length-2) and add listeners to the sequence of new value (from n+1 to length-2).

        // On achieving this functionality,
        info[0] = event.getSource();
        info[1] = event.getPropertyName();
        info[2] = event.getOldValue();

        Object value = traverse(root, path, 1);

        if (observer instanceof Watch) {
            Watch pair = (Watch) observer;

            if (!whileUpdate && !pair.whileUpdate) {
                // start synchronizing
                whileUpdate = true;

                // retrieve host object in the pair observer
                Object target = pair.traverse(pair.root, pair.path.subList(0, pair.length - 1), 0);

                if (target != null) {
                    Model model = Model.load(target.getClass());
                    Property property = model.getProperty(pair.path.get(pair.length - 1));
                    model.set(target, property, I.transform(value, property.model.type));
                }

                // finish synchronizing
                whileUpdate = false;
            }
        } else {
            observer.onNext(value);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        traverse(root, path, 3);

        if (observer instanceof Watch) {
            ((Watch) observer).dispose();
        }
    }
}
