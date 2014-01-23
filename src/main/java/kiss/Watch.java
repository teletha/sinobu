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

import java.util.List;

import javax.accessibility.Accessible;

import jdk.nashorn.internal.runtime.Context;
import jdk.nashorn.internal.runtime.PropertyListener;
import kiss.model.Model;
import kiss.model.Property;

/**
 * @version 2014/01/23 15:57:06
 */
public class Watch implements Disposable {

    /** The property path. */
    final List<String> path;

    /** The path length. This value is referred frequently, so cache it. */
    final int length;

    /** The delegation of property change event. */
    PropertyListener listener;

    /** The operation mode about application of listeners. */
    private int mode = 2; // initial is add mode

    /** The current index of the property path. */
    private int index = 0;

    /** The flag. */
    private boolean whileUpdate = false;

    private Object[] info = new Object[3];

    /**
     * Create Observer instance.
     * 
     * @param base
     * @param path
     */
    Watch(List tracer, PropertyListener listener) {
        super(tracer.remove(0));

        // register model walk listener
        addListener(this);

        this.path = tracer;
        this.length = path.size();
        this.listener = listener;

        path.get(0);

        traverse(path);
        mode = 0;
    }

    /**
     * @see net.sf.easybean.model.ModelWalkListener#enterNode(net.sf.easybean.model.Model,
     *      net.sf.easybean.model.Property, java.lang.Object)
     */
    public void enterNode(Model model, Property property, Object node) {
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

                // remove actually
                ModelWalker walker = new ModelWalker(info[2]);
                walker.addListener(this);

                // Only if we remove listeners, we can do like the following
                // walker.traverse(path.subList(index, length - 1));
                // But, in the greed, we need the actual old value which is indicated by the
                // property path at once method call.
                info[2] = walker.traverse(path.subList(index, length));
            }

            // don't break, step into the next to add listeners
            mode = 2; // move into add mode

        case 2: // add mode
        case 3: // remove mode
            // The location (index == length) indicates the last property path. It is no need to be
            // aware of property change event.
            if (index < length && node instanceof Accessible) {
                Context context = ((Accessible) node).ezContext();

                if (mode == 2) {
                    context.addListener(path.get(index), this);

                    info[0] = node;

                } else {
                    context.removeListener(path.get(index), this);
                }
            }
            break;

        default: // normal mode
            break;
        }
        index++;
    }

    /**
     * @see net.sf.easybean.model.ModelWalkListener#leaveNode(net.sf.easybean.model.Model,
     *      net.sf.easybean.model.Property, java.lang.Object)
     */
    public void leaveNode(Model model, Property property, Object node) {
        index--;
    }

    /**
     * @see net.sf.easybean.PropertyListener#change(java.lang.Object, java.lang.String,
     *      java.lang.Object, java.lang.Object)
     */
    public void change(Object object, String propertyName, Object oldValue, Object newValue) {
        // The property value of the specified object was changed in some property path, but we
        // don't know the location of property change yet. At first, we must search the location
        // (index = n), then remove all registered listeners form the sequence of old value (from
        // n+1 to length-2) and add listeners to the sequence of new value (from n+1 to length-2).

        // On achieving this functionality,
        info[0] = object;
        info[1] = propertyName;
        info[2] = oldValue;

        mode = 1;
        newValue = traverse(path);
        mode = 0;

        if (listener instanceof Observer) {
            Observer pair = (Observer) listener;

            if (!whileUpdate && !pair.whileUpdate) {
                // start synchronizing
                whileUpdate = true;

                // retrieve host object in the pair observer
                Object target = pair.traverse(pair.path.subList(0, pair.length - 1));

                if (target != null) {
                    Model model = Model.load(target.getClass());
                    Property property = model.getProperty(pair.path.get(pair.length - 1));
                    model.set(target, property, EasyBean.transform(newValue, property.model.type));
                }

                // finish synchronizing
                whileUpdate = false;
            }
        } else {
            listener.change(info[0], path.get(length - 1), info[2], newValue);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void dispose() {
        if (mode != 3) {
            // remove all registered listener from each elements on property path
            mode = 3; // remove mode
            traverse(path);

            if (listener instanceof Watch) {
                ((Watch) listener).dispose();
            }
        }
    }

}
