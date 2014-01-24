/*
 * Copyright (C) 2014 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss;

import java.beans.Introspector;
import java.beans.PropertyChangeEvent;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;

import javax.jws.Oneway;

import kiss.model.Model;
import kiss.model.Property;

/**
 * @version 2014/01/24 13:07:40
 */
public class Watch extends Interceptor<Oneway> implements Disposable, Observer<PropertyChangeEvent>, Oneway {

    // =======================================================
    // For Interceptor
    // =======================================================
    /**
     * <p>
     * Initialization as {@link Interceptor}.
     * </p>
     */
    Watch() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object invoke(Object... params) {
        Property property = Model.load(that.getClass()).getProperty(Introspector.decapitalize(name.substring(3)));
        List<Observer> list = context(that).get(property.name);

        if (list.isEmpty()) {
            return super.invoke(params);
        }

        try {
            // Retrieve old value.
            Object old = property.accessor(true).invoke(that);

            Object result = super.invoke(params);

            if (!Objects.equals(old, params[0])) {
                PropertyChangeEvent event = new PropertyChangeEvent(that, property.name, old, params[0]);

                for (Observer observer : list) {
                    observer.onNext(event);
                }
            }

            return result;
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }

    // =======================================================
    // For Property Observer
    // =======================================================
    /** The property path. */
    List<String> path;

    /** The delegation of property change event. */
    Observer observer;

    /** The flag. */
    private boolean whileUpdate = false;

    /** [0] is object(Object), [1] is property name(String) and [2] is old value(Object). */
    private Object[] info = new Object[3];

    /**
     * Initialization as property {@link Observer}.
     * 
     * @param tracer
     * @param observer
     */
    Watch(List tracer, Observer observer) {
        this.that = tracer.remove(0);
        this.path = tracer;
        this.observer = observer;

        traverse(that, path, 2);
    }

    /**
     * <p>
     * Traverse object tree along with the specified property path.
     * </p>
     * 
     * @param object A base object.
     * @param path A property path.
     * @param mode A processing mode.
     * @return
     */
    private Object traverse(Object object, List<String> path, int mode) {
        Model model = Model.load(object.getClass());
        Property property = new Property(model, model.name);

        root: for (int index = 0; index < path.size(); index++) {
            switch (mode) {
            case 1: // search modification
                if (info[0] == object) {
                    mode = 4;
                }
                break;

            case 4: // add listener to the new path
                if (!property.name.equals(info[1])) {
                    mode = 1;
                    break;
                }

                // remove all registered listeners from the sequence of old value
                if (info[2] != null) {
                    // Remove actually
                    //
                    // Only if we remove listeners, we can do like the following
                    // walker.traverse(path.subList(index, length - 1));
                    // But, in the greed, we need the actual old value which is indicated by the
                    // property path at once method call.
                    info[2] = traverse(info[2], path.subList(index, path.size()), 3);
                }

                // don't break, step into the next to add listeners
                mode = 2; // move into add mode

            case 2: // add mode
            case 3: // remove mode
                // The location (index == length) indicates the last property path. It is no need to
                // be aware of property change event.
                Table<String, Observer> list = context(object);

                if (mode == 2) {
                    list.push(path.get(index), this);

                    info[0] = object;
                } else {
                    list.pull(path.get(index), this);
                }
                break;

            default: // normal mode
                break;
            }

            model = property.model;
            property = model.getProperty(path.get(index));

            if (property == null) {
                break root;
            } else {
                object = model.get(object, property);

                if (object == null) {
                    break root;
                }
            }
        }
        return object;
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

        // search new value
        Object value = traverse(that, path, 1);

        if (observer instanceof Watch) {
            Watch pair = (Watch) observer;

            if (!whileUpdate && !pair.whileUpdate) {
                // start synchronizing
                whileUpdate = true;

                // retrieve host object in the pair observer
                Object target = pair.traverse(pair.that, pair.path.subList(0, pair.path.size() - 1), 0);

                if (target != null) {
                    Model model = Model.load(target.getClass());
                    Property property = model.getProperty(pair.path.get(pair.path.size() - 1));
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
        traverse(that, path, 3);

        if (observer instanceof Watch) {
            ((Watch) observer).dispose();
        }
    }

    /**
     * <p>
     * Helper method to access property listener context.
     * </p>
     * 
     * @param object A target bean.
     * @return An associated context.
     */
    private static final Table<String, Observer> context(Object object) {
        try {
            Field field = object.getClass().getField("context");
            Object value = field.get(object);

            if (value == null) {
                value = new Table();
                field.set(object, value);
            }
            return (Table<String, Observer>) value;
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }

    // =======================================================
    // For Annotation Implementation
    // =======================================================
    /**
     * As Marker annotation.
     */
    @Override
    public Class<? extends Annotation> annotationType() {
        return Oneway.class;
    }
}
