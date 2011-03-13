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
package ezbean;

import java.util.List;

import ezbean.model.Model;
import ezbean.model.ModelWalker;
import ezbean.model.Property;

/**
 * <p>
 * This implementation is dirty, but small footprint.
 * </p>
 * 
 * @version 2011/03/13 11:06:24
 */
class Observer extends ModelWalker implements PropertyListener, Disposable {

    /** The root object. */
    final Object root;

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

    /** [0] is object(Object), [1] is property name(String) and [2] is old value(Object). */
    private Object[] info = new Object[3];

    /**
     * Initialization : register listener for each nodes on path
     * 
     * @param tracer
     * @param listener
     */
    Observer(List tracer, PropertyListener listener) {
        this.root = tracer.remove(0);
        this.path = tracer;
        this.length = path.size();
        this.listener = listener;

        path.get(0);

        traverse(root, path);
        mode = 0;
    }

    /**
     * @see ezbean.model.ModelWalker#enter(ezbean.model.Model, ezbean.model.Property,
     *      java.lang.Object)
     */
    protected void enter(Model model, Property property, Object node) {
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
                info[2] = traverse(info[2], path.subList(index, length));
            }

            // don't break, step into the next to add listeners
            mode = 2; // move into add mode

        case 2: // add mode
        case 3: // remove mode
            // The location (index == length) indicates the last property path. It is no need to be
            // aware of property change event.
            if (index < length && node instanceof Accessible) {
                Listeners<String, PropertyListener> line = ((Accessible) node).context();

                if (mode == 2) {
                    line.push(path.get(index), this);

                    info[0] = node;
                } else {
                    line.pull(path.get(index), this);
                }
            }
            break;

        default: // normal mode
            break;
        }
        index++;
    }

    /**
     * @see ezbean.model.ModelWalker#leave(ezbean.model.Model, ezbean.model.Property,
     *      java.lang.Object)
     */
    protected void leave(Model model, Property property, Object node) {
        index--;
    }

    /**
     * @see ezbean.PropertyListener#change(java.lang.Object, java.lang.String, java.lang.Object,
     *      java.lang.Object)
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
        newValue = traverse(root, path);
        mode = 0;

        if (listener instanceof Observer) {
            Observer pair = (Observer) listener;

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
            listener.change(info[0], path.get(length - 1), info[2], newValue);
        }
    }

    /**
     * @see ezbean.Disposable#dispose()
     */
    public void dispose() {
        if (mode != 3) {
            // remove all registered listener from each elements on property path
            mode = 3; // remove mode
            traverse(root, path);

            if (listener instanceof Observer) {
                ((Observer) listener).dispose();
            }
        }
    }
}
