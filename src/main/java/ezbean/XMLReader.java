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

import java.util.ArrayList;
import java.util.LinkedList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;

import ezbean.model.Codec;
import ezbean.model.Model;
import ezbean.model.Property;

/**
 * @version 2009/07/22 21:36:25
 */
class XMLReader extends XMLFilterImpl {

    /** The root object. */
    private final Object root;

    /** The stack of states. */
    private final LinkedList<ModelState> states = new LinkedList<ModelState>();

    /** The object trace. */
    private final ArrayList objects = new ArrayList();

    /**
     * Create ConfigurationReader instance.
     * 
     * @param root
     */
    XMLReader(Object root) {
        this.root = root;
        objects.add(root);
    }

    /**
     * @see org.xml.sax.helpers.XMLFilterImpl#startElement(java.lang.String, java.lang.String,
     *      java.lang.String, org.xml.sax.Attributes)
     */
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
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
            Object object;

            // check reference
            String ref = attributes.getValue(URI, "ref");

            if (ref != null) {
                object = objects.get(Integer.parseInt(ref));
            } else {
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
            }

            // create next state
            state = new ModelState(object, property.model);
            state.property = property;

            if (!property.isAttribute()) {
                objects.add(state.object);
            }
        }

        // assign properties which are represented by attributes
        for (int i = 0; i < attributes.getLength(); i++) {
            // check namespace
            if (!attributes.getURI(i).equals(URI)) {
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
    public void endElement(String uri, String localName, String qName) throws SAXException {
        ModelState current = states.pollLast();
        ModelState parent = states.peekLast();

        if (parent != null) {
            parent.model.set(parent.object, current.property, current.object);
        }
    }
}
