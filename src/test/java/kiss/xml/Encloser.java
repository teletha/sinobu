/*
 * Copyright (C) 2012 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.xml;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * <p>
 * Enclose root element.
 * </p>
 * 
 * @version 2012/02/14 11:17:45
 */
public class Encloser extends XMLScanner {

    /** The encloser element name. */
    private final String root;

    /** The depth. */
    private int counter = 0;

    /**
     * Create Encloser instance.
     * 
     * @param root
     */
    public Encloser(String root) {
        this.root = root;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startElement(String uri, String localName, String name, Attributes atts) throws SAXException {
        counter++;

        if (counter == 1) {
            super.startElement("", root, root, new AttributesImpl());
        }
        super.startElement(uri, localName, name, atts);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void endElement(String uri, String localName, String name) throws SAXException {
        super.endElement(uri, localName, name);

        if (counter == 1) {
            super.endElement("", root, root);
        }
        counter--;
    }
}
