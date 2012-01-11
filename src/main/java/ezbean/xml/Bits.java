/*
 * Copyright (C) 2012 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package ezbean.xml;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * <p>
 * This class is a buffer of SAX events.
 * </p>
 * 
 * @version 2011/04/13 14:49:27
 */
public final class Bits extends XMLFilterImpl {

    /** The event cache. */
    final ArrayList<Object[]> bits = new ArrayList();

    /**
     * <p>
     * Record character event.
     * </p>
     */
    @Override
    public void characters(char[] ch, int start, int length) {
        bits.add(new Object[] {new String(ch, start, length)});
    }

    /**
     * <p>
     * Record start element event.
     * </p>
     */
    @Override
    public void startElement(String uri, String local, String name, Attributes atts) {
        bits.add(new Object[] {uri, local, name, new AttributesImpl(atts)});
    }

    /**
     * <p>
     * Record end element event.
     * </p>
     */
    @Override
    public void endElement(String uri, String local, String name) {
        bits.add(new Object[] {uri, local, name});
    }
}
