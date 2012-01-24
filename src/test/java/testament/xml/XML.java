/*
 * Copyright (C) 2012 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package testament.xml;


import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;

import kiss.I;
import kiss.xml.XMLWriter;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;
import org.xml.sax.helpers.XMLFilterImpl;

import testament.SAXBuilder;

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
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        DOMImplementation domImpl = document.getImplementation();
        DOMImplementationLS domImplLS = (DOMImplementationLS) domImpl.getFeature("LS", "3.0");
        LSOutput lsOutput = domImplLS.createLSOutput();
        LSSerializer lsSer = domImplLS.createLSSerializer();

        lsOutput.setByteStream(System.out);

        // LSSerializerのDOMConfigurationにパラメータをセットする
        lsSer.getDomConfig().setParameter("format-pretty-print", Boolean.TRUE);
        lsSer.write(document, lsOutput);

        StringBuilder builder = new StringBuilder();

        try {
            XMLWriter formatter = new XMLWriter(builder);
            // create SAX result
            SAXResult result = new SAXResult(formatter);
            result.setLexicalHandler(formatter);

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.transform(new DOMSource(document), result);
        } catch (Exception e) {
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
        try {
            XMLWriter formatter = new XMLWriter(System.out);
            // create SAX result
            SAXResult result = new SAXResult(formatter);
            result.setLexicalHandler(formatter);

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.transform(new DOMSource(document), result);

            System.out.println("");
            System.out.println("");
        } catch (TransformerConfigurationException e) {
            throw new RuntimeException(e);
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }
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
