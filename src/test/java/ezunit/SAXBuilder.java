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
package ezunit;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * This class is a simple data format converter form SAX events to DOM model.
 * 
 * @version 2007/06/04 13:16:44
 */
public class SAXBuilder extends XMLFilterImpl {

    /** The result */
    private DOMResult result = new DOMResult();

    /**
     * Construct a new instance of this SAXBuilder.
     */
    public SAXBuilder() {
        try {
            TransformerHandler handler = ((SAXTransformerFactory) TransformerFactory.newInstance()).newTransformerHandler();
            handler.setResult(result);

            setContentHandler(handler);
        } catch (TransformerConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Return the newly generated DOM model.
     * 
     * @return A DOM document.
     */
    public Document getDocument() {
        Node node = result.getNode();

        if (node == null) {
            return null;
        }
        return (node.getNodeType() == Node.DOCUMENT_NODE) ? (Document) node : node.getOwnerDocument();
    }
}
