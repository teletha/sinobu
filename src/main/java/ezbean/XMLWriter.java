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
import java.util.Map;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import ezbean.model.Model;
import ezbean.model.ModelWalkListener;
import ezbean.model.Property;

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
 * @version 2009/07/23 15:55:48
 */
@SuppressWarnings("serial")
class XMLWriter extends HashMap<Object, Integer> implements ModelWalkListener {

    /** The content handler. */
    private final ContentHandler handler;

    /** The implicit object identifier. */
    private int identifier = 0;

    /** The current stored node name. */
    private String name;

    /** The attribute for reuse. */
    private AttributesImpl attributes = new AttributesImpl();

    /**
     * Create ConfigurationWriter instance.
     * 
     * @param handler
     */
    XMLWriter(ContentHandler handler) {
        this.handler = handler;
    }

    /**
     * @see ezbean.model.ModelWalkListener#enterNode(ezbean.model.Model, ezbean.model.Property,
     *      java.lang.Object)
     */
    public void enterNode(Model model, Property property, Object node) {
        // If the specfied model or property requires new element for serialization, we must write
        // out the previous start element.
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

        // If the collection item is attribute node, that is represented as xml value attribute and
        // attribute node that collection node doesn't host is written as xml attribute too.
        if (node != null) {
            if (property.isAttribute()) {
                attributes.addAttribute(null, null, (model.isCollection()) ? "value" : property.name, null, I.transform(node, String.class));
            } else {
                // check cyclic node
                Integer i = get(node);

                if (i != null) {
                    // create reference id attribute
                    attributes.addAttribute(URI, null, "ez:ref", null, i.toString());
                }

                // record node with identifier
                put(node, identifier++);
            }
        }
    }

    /**
     * @see ezbean.model.ModelWalkListener#leaveNode(ezbean.model.Model, ezbean.model.Property,
     *      java.lang.Object)
     */
    public void leaveNode(Model model, Property property, Object node) {
        // If the specfied model or property requires new element for serialization, we must write
        // out the previous start element.
        if (model.isCollection() || !property.isAttribute()) {
            write();

            try {
                handler.endElement(null, null, (model.isCollection()) ? property.model.name : property.name);
            } catch (SAXException e) {
                // If this exception will be thrown, it is bug of this program. So we must rethrow
                // the wrapped error in here.
                throw new Error(e);
            }
        }
    }

    /**
     * Helper method to write out the stored sax event.
     */
    private void write() {
        // check node name
        if (name != null) {
            try {
                // write start element
                handler.startElement(null, null, name, attributes);

                // clear current state
                name = null;
                attributes.clear();
            } catch (SAXException e) {
                // If this exception will be thrown, it is bug of this program. So we must rethrow
                // the wrapped error in here.
                throw new Error(e);
            }
        }
    }
}
