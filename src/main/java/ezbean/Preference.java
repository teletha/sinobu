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

/**
 * <p>
 * This lifestyle guarantees that only one instance of the specific class exists in Ezbean and all
 * its properties are paersisted automatically as user configuration file.
 * </p>
 * <p>
 * When the instance is initialized, Ezbean restores all properties form the persisted user
 * configuration file. When any instance's property is changed, Ezbean automatically stores it to
 * the user configuration file.
 * </p>
 * 
 * @see Prototype
 * @see Singleton
 * @see ThreadSpecific
 * @version 2011/11/09 21:04:05
 */
public class Preference<M> extends Singleton<M> implements Runnable {

    /** The automatic saving location. */
    protected final Path path;

    /**
     * Create Preference instance.
     * 
     * @param modelClass
     */
    protected Preference(Class<M> modelClass) {
        super(modelClass);

        this.path = I.$working.resolve("preferences").resolve(Model.load(modelClass).type.getName().concat(".xml"));

        if (Files.exists(path)) {
            I.read(path, instance);
        }

        Runtime.getRuntime().addShutdownHook(new Thread(this));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        I.write(instance, path, false);
    }
}
