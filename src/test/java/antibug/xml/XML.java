/*
 * Copyright (C) 2012 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package antibug.xml;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import kiss.I;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;
import org.xml.sax.helpers.XMLFilterImpl;

import antibug.SAXBuilder;

import com.sun.org.apache.xml.internal.security.c14n.CanonicalizerSpi;
import com.sun.org.apache.xml.internal.security.c14n.implementations.Canonicalizer20010315ExclOmitComments;
import com.sun.org.apache.xml.internal.security.utils.XMLUtils;

/**
 * @version 2012/01/19 15:05:01
 */
public class XML {

    private Document document;

    public XML() {
    }

    public XML(Path path, XMLFilter... filters) {
        // build xml pipe
        SAXBuilder builder = new SAXBuilder();

        List<XMLFilter> list = new ArrayList();
        list.addAll(Arrays.asList(filters));
        list.add(new IgnoreWhitspaceFilter());
        list.add(builder);

        // start parsing
        I.parse(path, list.toArray(new XMLFilter[list.size()]));

        // retrive result DOM document
        this.document = builder.getDocument();
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();

        try {
            CanonicalizerSpi spi = I.make(Canonicalizer20010315ExclOmitComments.class);

            byte[] b = spi.engineCanonicalizeSubTree(document);
            System.out.println(new String(b));
            builder.append(new String(b, "UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
            throw I.quiet(e);
        }
        return builder.toString();
    }

    /**
     * <p>
     * Helper method to dump XML data to system output.
     * </p>
     * 
     * @param document A target document to dump.
     */
    public static final void dumpXML(Document document) {

        XMLUtils.outputDOM(document, System.out, false);

        System.out.println(" @ @ ");
        System.out.println("");
    }

    /**
     * @version 2011/03/23 8:07:19
     */
    private static class IgnoreWhitspaceFilter extends XMLFilterImpl {

        /**
         * @see org.xml.sax.helpers.XMLFilterImpl#characters(char[], int, int)
         */
        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            for (int i = start; i < start + length; i++) {
                if (!Character.isWhitespace(ch[i])) {
                    super.characters(ch, start, length);
                    return;
                }
            }
        }
    }
}
