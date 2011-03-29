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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import ezbean.model.Model;
import ezbean.model.Property;
import ezbean.model.PropertyWalker;

/**
 * <p>
 * This is dual-purpose implementation class. One is a state recorder for configuration and the
 * other a {@link PropertyWalker} implementation for bean transformation.
 * </p>
 * 
 * @version 2010/01/08 19:42:29
 */
final class ModelState implements PropertyWalker, PropertyListener, Disposable {

    /** The current model. */
    Model model;

    /** The curret object. */
    Object object;

    /** The property for {@link XMLIn} process. */
    Property property;

    /** The current location for {@link XMLIn} process. */
    int i = 0;

    Path file;

    /**
     * Create State instance.
     * 
     * @param object A actual object.
     * @param model A model of the specified object.
     */
    ModelState(Object object, Model model) {
        this.object = object;
        this.model = model;
    }

    /**
     * @see ezbean.model.PropertyWalker#walk(ezbean.model.Model, ezbean.model.Property,
     *      java.lang.Object)
     */
    public void walk(Model model, Property property, Object node) {
        Property dest = this.model.getProperty(property.name);

        // never check null because PropertyWalker traverses existing properties
        this.model.set(object, dest, I.transform(node, dest.model.type));
    }

    /**
     * @see ezbean.PropertyListener#change(java.lang.Object, java.lang.String, java.lang.Object,
     *      java.lang.Object)
     */
    @Override
    public void change(Object bean, String name, Object oldValue, Object newValue) {
        try {
            I.copy(bean, Files.newBufferedWriter(file, I.getEncoding()), false);
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }

    /**
     * @see ezbean.Disposable#dispose()
     */
    @Override
    public void dispose() {
        Listeners<String, PropertyListener> listeners = ((Accessible) object).context();

        for (Property property : ((Model<?>) model).properties) {
            listeners.pull(property.name, this);
        }
    }
}
