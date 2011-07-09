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
package ezbean;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map.Entry;

import ezbean.model.Model;
import ezbean.model.Property;

/**
 * <p>
 * {@link Preference} manipulates user setting information easily.
 * </p>
 * 
 * @version 2011/03/30 7:56:14
 */
@Manageable(lifestyle = Singleton.class)
public abstract class Preference implements Extensible {

    /** The default preference store. */
    private final DefaultStore defaults = new DefaultStore();

    /** The model for this preference. */
    private final Model model;

    /** The automatic saving location. */
    private final Path file;

    /**
     * Initialize the user preference.
     */
    protected Preference() {
        this.model = Model.load((Class) getClass());
        this.file = I.getWorkingDirectory().resolve("preferences/".concat(model.type.getName()));

        restore();

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
    public synchronized void store() {
        try {
            if (Files.notExists(file)) {
                Files.createDirectories(file.getParent());
                Files.createFile(file);
            }

            I.write(this, file, false);
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
        if (Files.exists(file)) {
            I.read(file, this);
        }
    }

    /**
     * <p>
     * Reset all properties of this preference to default values.
     * </p>
     */
    public void reset() {
        for (Entry<String, Object> entry : defaults.entrySet()) {
            model.set(this, model.getProperty(entry.getKey()), entry.getValue());
        }
    }

    /**
     * @version 2011/03/30 12:37:19
     */
    @SuppressWarnings("serial")
    private class DefaultStore extends HashMap<String, Object> implements PropertyListener {

        /**
         * {@inheritDoc}
         */
        public void change(Object bean, String propertyName, Object oldValue, Object newValue) {
            // store default value
            if (!containsKey(propertyName)) {
                put(propertyName, oldValue);
            }

            // save automatically if needed

        }
    }
}
