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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.xml.sax.helpers.AttributesImpl;

import ezbean.model.Model;
import ezbean.model.ModelWalker;
import ezbean.model.Property;
import ezbean.xml.XMLWriter;

/**
 * <p>
 * XML writer for Object Graph serialization.
 * </p>
 * <p>
 * We could select to use List implementaion instead of Map for management of implicit object
 * identifier. But it requires linear time to search the existing element. So we should use Map
 * which provides constant-time performance for seaching element.
 * </p>
 * 
 * @version 2010/12/14 0:23:59
 */
class XMLOut extends ModelWalker {

    /** The content handler. */
    private final XMLWriter writer;

    /** The object and id mapping. */
    private ConcurrentHashMap<Object, Integer> objects = new ConcurrentHashMap();

    /** The current traversing mode. */
    private boolean mode = true;

    /** The current stored node name. */
    private String name;

    /** The attribute for reuse. */
    private AttributesImpl attributes = new AttributesImpl();

    /**
     * Create ConfigurationWriter instance.
     * 
     * @param writer An actual XML writer.
     */
    XMLOut(XMLWriter writer) {
        this.writer = writer;
    }

    /**
     * @see ezbean.model.ModelWalker#traverse(java.lang.Object)
     */
    @Override
    public void traverse(Object node) {
        super.traverse(node);
        mode = false;
        super.traverse(node);
    }

    /**
     * @see ezbean.model.ModelWalker#enter(ezbean.model.Model, ezbean.model.Property,
     *      java.lang.Object)
     */
    protected void enter(Model model, Property property, Object node) {
        if (mode) {
            if (!property.isAttribute() && nodes.contains(node)) {
                objects.putIfAbsent(node, objects.size());
            }
        } else {
            // If the specfied model or property requires new element for serialization, we must
            // write out the previous start element.
            if (model.isCollection()) {
                write();

                // collection item property
                name = property.model.name;

                // collection needs key attribute
                if (model.type == Map.class) {
                    attributes.addAttribute(URI, null, "ez:key", null, property.name);
                }
            } else if (!property.isAttribute()) {
                write();

                name = property.name;
            }

            // If the collection item is attribute node, that is represented as xml value attribute
            // and attribute node that collection node doesn't host is written as xml attribute too.
            if (node != null) {
                if (property.isAttribute()) {
                    attributes.addAttribute(null, null, (model.isCollection()) ? "value" : property.name, null, I.transform(node, String.class));
                } else {
                    Integer integer = objects.get(node);

                    if (integer != null) {
                        // create reference id attribute
                        attributes.addAttribute(URI, null, "ez:id", null, integer.toString());
                    }
                }
            }
        }
    }

    /**
     * @see ezbean.model.ModelWalker#leave(ezbean.model.Model, ezbean.model.Property,
     *      java.lang.Object)
     */
    protected void leave(Model model, Property property, Object node) {
        if (!mode) {
            // If the specfied model or property requires new element for serialization, we must
            // write out the previous start element.
            if (model.isCollection() || !property.isAttribute()) {
                write();

                writer.endElement(null, null, (model.isCollection()) ? property.model.name : property.name);
            }
        }
    }

    /**
     * Helper method to write out the stored sax event.
     */
    private void write() {
        // check node name
        if (name != null) {
            // write start element
            writer.startElement(null, null, name, attributes);

            // clear current state
            name = null;
            attributes.clear();
        }
    }
}
