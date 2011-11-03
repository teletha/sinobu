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

import java.nio.file.Files;
import java.nio.file.Path;

import ezbean.model.Model;
import ezbean.model.Property;

/**
 * @version 2011/11/03 20:16:51
 */
public class Preference<M> extends Singleton<M> implements PropertyListener {

    /** The automatic saving location. */
    private final Path file;

    /**
     * @param modelClass
     */
    public Preference(Class<M> modelClass) {
        super(modelClass);

        Model model = Model.load(modelClass);
        this.file = I.getWorkingDirectory().resolve("preferences").resolve(model.type.getName().concat(".xml"));

        if (Files.exists(file)) {
            I.read(file, instance);
        }

        // observe each properties
        for (Property property : model.properties) {
            (((Accessible) instance).context()).push(property.name, this);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void change(Object bean, String name, Object oldValue, Object newValue) {
        I.write(instance, file, false);
    }
}
