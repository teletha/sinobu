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

import java.util.Arrays;
import java.util.List;

import kiss.model.Model;
import kiss.model.Property;
import kiss.model.PropertyEvent;

/**
 * @version 2014/01/23 15:57:06
 */
public class Watch implements Disposable, Observer<PropertyEvent> {

    /** The root object. */
    final Object root;

    /** The property path. */
    final List<String> path;

    /** The path length. This value is referred frequently, so cache it. */
    final int length;

    /** The delegation of property change event. */
    Observer observer;

    /** The operation mode about application of listeners. */
    private int mode = 2; // initial is add mode

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

        traverse(root, path);
        mode = 0;
    }

    private Object traverse(Object node, List<String> route) {
        Model model = Model.load(node.getClass());
        Property property = new Property(model, model.name);

        for (int index = 0; index < route.size(); index++) {
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
                    info[2] = traverse(info[2], route.subList(index, length));
                }

                // don't break, step into the next to add listeners
                mode = 2; // move into add mode

            case 2: // add mode
            case 3: // remove mode
                // The location (index == length) indicates the last property path. It is no need to
                // be aware of property change event.
                Table<String, Observer> line = Interceptor.context(node);

                if (mode == 2) {
                    System.out.println("set listener at " + path.get(index) + "  " + node);
                    line.push(path.get(index), this);

                    info[0] = node;
                } else {
                    System.out.println("remove listener at " + path.get(index) + "   " + node);
                    line.pull(path.get(index), this);
                }
                break;

            default: // normal mode
                break;
            }

            model = property.model;
            property = model.getProperty(path.get(index));

            if (property == null) {
                return null;
            } else {
                node = model.get(node, property);

                if (node == null) {
                    return null;
                }
            }
        }

        return node;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onNext(PropertyEvent value) {
        System.out.println("onNext " + value);
        // The property value of the specified object was changed in some property path, but we
        // don't know the location of property change yet. At first, we must search the location
        // (index = n), then remove all registered listeners form the sequence of old value (from
        // n+1 to length-2) and add listeners to the sequence of new value (from n+1 to length-2).

        // On achieving this functionality,
        info[0] = value.getSource();
        info[1] = value.getPropertyName();
        info[2] = value.getOldValue();

        mode = 1;
        Object newValue = traverse(root, path);
        mode = 0;

        if (observer instanceof Watch) {
            Watch pair = (Watch) observer;

            if (!whileUpdate && !pair.whileUpdate) {
                // start synchronizing
                whileUpdate = true;

                // retrieve host object in the pair observer
                Object target = pair.traverse(pair.root, pair.path.subList(0, pair.length - 1));

                if (target != null) {
                    Model model = Model.load(target.getClass());
                    Property property = model.getProperty(pair.path.get(pair.length - 1));
                    model.set(target, property, I.transform(newValue, property.model.type));
                }

                // finish synchronizing
                whileUpdate = false;
            }
        } else {
            System.out.println("info2 " + Arrays.toString(info));
            observer.onNext(new PropertyEvent(info[0], path.get(length - 1), info[2], newValue));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        if (mode != 3) {
            // remove all registered listener from each elements on property path
            mode = 3; // remove mode
            traverse(root, path);

            if (observer instanceof Watch) {
                ((Watch) observer).dispose();
            }
        }
    }
}
