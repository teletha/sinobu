/*
 * Copyright (C) 2010 Nameless Production Committee.
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

import static ezbean.I.*;

import java.util.HashMap;
import java.util.LinkedList;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.XMLFilterImpl;

import ezbean.model.Codec;
import ezbean.model.Model;
import ezbean.model.Property;

/**
 * <p>
 * XML reader for Object Graph deserialization.
 * </p>
 * <p>
 * We could select to use List implementaion instead of Map for management of implicit object
 * identifier. But it requires linear time to search the existing element. So we should use Map
 * which provides constant-time performance for seaching element.
 * </p>
 * 
 * @version 2010/01/12 22:54:15
 */
class XMLReader extends XMLFilterImpl {

    /** The root object. */
    private final Object root;

    /** The id and object mapping. */
    private final HashMap objects = new HashMap();

    /** The stack of states. */
    private final LinkedList<ModelState> states = new LinkedList<ModelState>();

    /**
     * Create ConfigurationReader instance.
     * 
     * @param root
     */
    XMLReader(Object root) {
        this.root = root;
    }

    /**
     * @see org.xml.sax.helpers.XMLFilterImpl#startElement(java.lang.String, java.lang.String,
     *      java.lang.String, org.xml.sax.Attributes)
     */
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        ModelState state;

        if (states.size() == 0) {
            state = new ModelState(root, Model.load(root.getClass()));
        } else {
            ModelState parent = states.peekLast();

            // Compute property.
            //
            // A name of the current element indicates the hint of the property name.
            // So you can get a valid property from the parent state.
            if (parent.model.isCollection()) {
                localName = attributes.getValue(URI, "key");

                if (localName == null) {
                    localName = String.valueOf(parent.i++);
                }
            }

            Property property = parent.model.getProperty(localName);

            // Compute object
            //
            // Property indicates a object, so you should create a suitable object.
            Object object = null;

            // check attribute model
            Codec codec = property.model.getCodec();

            if (codec != null) {
                String value = attributes.getValue("value");

                if (value == null) {
                    object = null;
                } else {
                    object = codec.decode(value);
                }
            } else {
                // collection model and normal model
                object = make(property.model.type);
            }

            // create next state
            state = new ModelState(object, property.model);
            state.property = property;
        }

        // assign properties which are represented by attributes
        for (int i = 0; i < attributes.getLength(); i++) {
            // check namespace
            if (attributes.getURI(i).equals(URI)) {
                if (attributes.getLocalName(i).equals("id")) {
                    // retrieve identifier for the current object
                    localName = attributes.getValue(i);

                    // retrieve object for the identifier
                    Object object = objects.get(localName);

                    if (object == null) {
                        // Object is not registered for the identifier, so this is first encounter
                        // of thie object.
                        objects.put(localName, state.object);
                    } else {
                        // Object is registered for the identifier, so this is referenced object.
                        state.object = object;
                    }
                }
            } else {
                Property property = state.model.getProperty(attributes.getLocalName(i));

                // ignore deprecated property
                if (property != null) {
                    // restore a property value form an attribute value
                    Codec codec = property.model.getCodec();

                    if (codec != null) {
                        state.model.set(state.object, property, codec.decode(attributes.getValue(i)));
                    }
                }
            }
        }

        // stack current state for reference
        states.offer(state);
    }

    /**
     * @see org.xml.sax.helpers.XMLFilterImpl#endElement(java.lang.String, java.lang.String,
     *      java.lang.String)
     */
    @Override
    public void endElement(String uri, String localName, String qName) {
        ModelState current = states.pollLast();
        ModelState parent = states.peekLast();

        if (parent != null) {
            parent.model.set(parent.object, current.property, current.object);
        }
    }
}
