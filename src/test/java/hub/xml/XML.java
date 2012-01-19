/*
 * Copyright (C) 2012 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package hub.xml;

import hub.SAXBuilder;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import kiss.I;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * @version 2012/01/19 15:05:01
 */
public class XML {

    private Document document;

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
