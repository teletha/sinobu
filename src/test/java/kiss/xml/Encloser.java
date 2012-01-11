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

import kiss.xml.XMLScanner;

/**
 * DOCUMENT.
 * 
 * @version 2008/08/29 21:32:44
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
     * @see org.xml.sax.helpers.XMLFilterImpl#startElement(java.lang.String, java.lang.String,
     *      java.lang.String, org.xml.sax.Attributes)
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
     * @see org.xml.sax.helpers.XMLFilterImpl#endElement(java.lang.String, java.lang.String,
     *      java.lang.String)
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
