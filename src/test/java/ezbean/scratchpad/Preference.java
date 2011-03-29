/*
 * Copyright (C) 2011 Nameless Production Committee.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ezbean.scratchpad;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map.Entry;

import ezbean.Accessible;
import ezbean.Extensible;
import ezbean.I;
import ezbean.Listeners;
import ezbean.Manageable;
import ezbean.PropertyListener;
import ezbean.Singleton;
import ezbean.model.Model;
import ezbean.model.Property;

/**
 * @version 2011/03/30 7:56:14
 */
@Manageable(lifestyle = Singleton.class)
public abstract class Preference<P> implements Extensible {

    /** The default preference store. */
    private final DefaultStore defaults = new DefaultStore();

    /** The model for this preference. */
    private final Model<Preference> model;

    /** The saving location. */
    private final Path location;

    /** The flag for automatic saving. */
    private boolean auto;

    /** The state of this preference is now reading or not, */
    private boolean locked = false;

    /**
     * Initialize the user preference.
     */
    protected Preference() {
        model = Model.load((Class) getClass());
        location = I.getWorkingDirectory().resolve(model.name.concat(".xml"));

        // Retrieve the accessible context using internal API.
        Listeners<String, PropertyListener> context = ((Accessible) this).context();

        // observe each properties
        for (Property property : model.properties) {
            context.push(property.name, defaults);
        }
    }

    /**
     * <p>
     * Save user preference.
     * </p>
     */
    public void store() {
        try {
            I.copy(this, Files.newBufferedWriter(location, I.getEncoding()), false);
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }

    /**
     * <p>
     * Restore user preference.
     * </p>
     */
    public void restore() {
        try {
            locked = true;

            I.copy(Files.newBufferedReader(location, I.getEncoding()), this);
        } catch (IOException e) {
            throw I.quiet(e);
        } finally {
            locked = false;
        }
    }

    /**
     * <p>
     * Reset all properties of this preference.
     * </p>
     */
    public void reset() {
        Model<Preference> model = Model.load((Class) getClass());

        for (Entry<Property, Object> entry : defaults.entrySet()) {
            model.set(this, entry.getKey(), entry.getValue());
        }
    }

    /**
     * <p>
     * Automatic saving.
     * </p>
     * 
     * @param auto
     */
    protected void setAutoSave(boolean auto) {
        this.auto = auto;
    }

    /**
     * @version 2011/03/30 7:56:37
     */
    @SuppressWarnings("serial")
    private class DefaultStore extends HashMap<Property, Object> implements PropertyListener {

        /**
         * {@inheritDoc}
         */
        public void change(Object bean, String propertyName, Object oldValue, Object newValue) {
            // store default value
            if (!containsKey(propertyName)) {
                put(Model.load(bean.getClass()).getProperty(propertyName), oldValue);
            }

            if (auto && !locked) {
                store();
            }
        }
    }
}
