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

import static org.w3c.dom.Node.*;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;

import kiss.I;
import kiss.xml.XMLWriter;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Entity;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.XMLFilter;
import org.xml.sax.helpers.XMLFilterImpl;

import antibug.Ezunit;

import com.sun.org.apache.xml.internal.security.c14n.CanonicalizerSpi;
import com.sun.org.apache.xml.internal.security.c14n.implementations.Canonicalizer11_OmitComments;
import com.sun.org.apache.xml.internal.utils.DOMBuilder;

/**
 * @version 2012/01/19 15:05:01
 */
public class XML {

    public static final DOMBuilder builder;

    /** The factory. */
    private static final DocumentBuilder dom;

    static {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);

            dom = factory.newDocumentBuilder();

            builder = new DOMBuilder(dom.newDocument());
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }

    /**
     * <p>
     * Parse the given xml text and build document using the given filters.
     * </p>
     * 
     * @param text A xml text.
     * @param filters A xml event filter.
     * @return
     */
    public static XML xml(String text, XMLFilter... filters) {
        return xml(new InputSource(new StringReader(text)), filters);
    }

    /**
     * <p>
     * Parse the given xml text and build document using the given filters.
     * </p>
     * 
     * @param path A xml source path.
     * @param filters A xml event filter.
     * @return
     */
    public static XML xml(Path path, XMLFilter... filters) {
        try {
            return xml(new InputSource(Files.newBufferedReader(path, I.$encoding)), filters);
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }

    /**
     * <p>
     * Parse the given xml text and build document using the given filters.
     * </p>
     * 
     * @param source A xml source.
     * @param filters A xml event filter.
     * @return
     */
    public static XML xml(InputSource source, XMLFilter... filters) {
        DOMBuilder builder = new DOMBuilder(dom.newDocument());
        XMLFilterImpl filter = new XMLFilterImpl();
        filter.setContentHandler(builder);

        // build pipe
        List<XMLFilter> pipe = new ArrayList();
        pipe.addAll(Arrays.asList(filters));
        pipe.add(filter);

        I.parse(source, pipe.toArray(new XMLFilter[pipe.size()]));

        return xml(builder.m_doc);
    }

    /**
     * <p>
     * Wrap xml document.
     * </p>
     * 
     * @param doc A parsed document.
     * @return
     */
    public static XML xml(Document doc) {
        return new XML(doc);
    }

    /**
     * <p>
     * Build document from the specified filter.
     * </p>
     * 
     * @param filter
     * @return
     */
    public static Document build(XMLFilter filter) {
        DOMBuilder builder = new DOMBuilder(dom.newDocument());

        // make chain
        filter.setContentHandler(builder);

        // API definition
        return builder.m_doc;
    }

    private final Document doc;

    /**
     * @param doc
     */
    private XML(Document doc) {
        this.doc = doc;
    }

    /**
     * <p>
     * Assert this xml is identical to the specified xml.
     * </p>
     * 
     * @param text
     * @return
     */
    public boolean isIdenticalTo(String xml) {
        return isIdenticalTo(xml(xml));
    }

    /**
     * <p>
     * Assert this xml is identical to the specified xml.
     * </p>
     * 
     * @param xml
     * @return
     */
    public boolean isIdenticalTo(Document xml) {
        return isIdenticalTo(xml(xml));
    }

    /**
     * <p>
     * Assert this xml is identical to the specified xml.
     * </p>
     * 
     * @param xml
     * @return
     */
    public boolean isIdenticalTo(XML xml) {
        try {
            compare(doc, xml.doc);

            return true;
        } catch (AssertionError e) {
            return false;
        }
    }

    /**
     * <p>
     * Assert this xml is equal to the specified xml.
     * </p>
     * 
     * @param text
     * @return
     */
    public boolean isEqualTo(String xml) {
        return isEqualTo(xml(xml));
    }

    /**
     * <p>
     * Assert this xml is equal to the specified xml.
     * </p>
     * 
     * @param xml
     * @return
     */
    public boolean isEqualTo(Document xml) {
        return isEqualTo(xml(xml));
    }

    /**
     * <p>
     * Assert this xml is equal to the specified xml.
     * </p>
     * 
     * @param xml
     * @return
     */
    public boolean isEqualTo(XML xml) {
        compare(canonicalize(), xml.canonicalize());

        return false;
    }

    /**
     * <p>
     * Canonicalize this xml.
     * </p>
     * 
     * @return
     */
    private Document canonicalize() {
        CanonicalizerSpi spi = I.make(Canonicalizer11_OmitComments.class);

        try {
            return xml(new String(spi.engineCanonicalizeSubTree(doc))).doc;
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }

    /**
     * <p>
     * Assert document equality.
     * </p>
     * 
     * @param one
     * @param other
     * @return
     */
    private void compare(Node one, Node other) {
        assert one.getNodeType() == other.getNodeType();

        switch (one.getNodeType()) {
        case ATTRIBUTE_NODE:
            compare((Attr) one, (Attr) other);
            break;

        case ELEMENT_NODE:
            compare((Element) one, (Element) other);
            break;

        case ENTITY_NODE:
            compare((Entity) one, (Entity) other);
            break;

        case TEXT_NODE:
            compare((Text) one, (Text) other);
            break;
        }
    }

    /**
     * <p>
     * Check node equality.
     * </p>
     * 
     * @param one A test node.
     * @param other A test node.
     */
    private void compare(Document one, Document other) {
        if (one == null || other == null) {
            assert one == other;
        } else {
            compare(one.getDoctype(), other.getDoctype());
            compare(one.getDocumentElement(), other.getDocumentElement());
        }
    }

    /**
     * <p>
     * Check node equality.
     * </p>
     * 
     * @param one A test node.
     * @param other A test node.
     */
    private void compare(DocumentType one, DocumentType other) {
        if (one == null || other == null) {
            assert one == other;
        } else {
            assert one.getName() == other.getName();
            assert one.getPublicId() == other.getPublicId();
            assert one.getSystemId() == other.getSystemId();
        }
    }

    /**
     * <p>
     * Check node equality.
     * </p>
     * 
     * @param one A test node.
     * @param other A test node.
     */
    private void compare(Element one, Element other) {
        if (one == null || other == null) {
            assert one == other;
        } else {
            assert one.getNodeName() == other.getNodeName();
            assert one.getLocalName().intern() == other.getLocalName().intern();
            assert one.getNamespaceURI() == other.getNamespaceURI();

            compare(one.getAttributes(), other.getAttributes());

            NodeList oneChildren = one.getChildNodes();
            NodeList otherChildren = other.getChildNodes();
            assert oneChildren.getLength() == otherChildren.getLength();

            for (int i = 0; i < oneChildren.getLength(); i++) {
                compare(oneChildren.item(i), otherChildren.item(i));
            }
        }
    }

    /**
     * <p>
     * Check node equality.
     * </p>
     * 
     * @param one A test node.
     * @param other A test node.
     */
    private void compare(NamedNodeMap one, NamedNodeMap other) {
        if (one == null || other == null) {
            assert one == other;
        } else {
            assert one.getLength() == other.getLength();

            for (int i = 0; i < one.getLength(); i++) {
                compare(one.item(i), other.item(i));
            }
        }
    }

    /**
     * <p>
     * Check node equality.
     * </p>
     * 
     * @param one A test node.
     * @param other A test node.
     */
    private void compare(Attr one, Attr other) {
        if (one == null || other == null) {
            assert one == other;
        } else {
            assert one.getName() == other.getName();
            assert one.getLocalName().intern() == other.getLocalName().intern();
            assert one.getNamespaceURI() == other.getNamespaceURI();
            assert one.getValue().equals(other.getValue());
        }
    }

    /**
     * <p>
     * Check node equality.
     * </p>
     * 
     * @param one A test node.
     * @param other A test node.
     */
    private void compare(Text one, Text other) {
        if (one == null || other == null) {
            assert one == other;
        } else {
            assert one.getData().equals(other.getData());
        }
    }

    /**
     * <p>
     * Check node equality.
     * </p>
     * 
     * @param one A test node.
     * @param other A test node.
     */
    private void compare(Entity one, Entity other) {
        if (one == null || other == null) {
            assert one == other;
        } else {
            throw new Error();
        }
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        try {
            StringBuilder builder = new StringBuilder();
            XMLWriter formatter = new XMLWriter(builder);

            SAXResult result = new SAXResult(formatter);
            result.setLexicalHandler(formatter);

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.transform(new DOMSource(doc), result);

            return builder.toString();
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }

    /**
     * <p>
     * Helper method to dump XML data to system output.
     * </p>
     * 
     * @param document A target document to dump.
     */
    public static final void dumpXML(Document document) {

        Ezunit.dumpXML(document);

        System.out.println(" @ @ ");
        System.out.println("");
    }
}
