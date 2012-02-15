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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

import antibug.powerassert.PowerAssertRenderer;

import com.sun.org.apache.xerces.internal.util.DOMUtil;
import com.sun.org.apache.xml.internal.security.c14n.CanonicalizerSpi;
import com.sun.org.apache.xml.internal.security.c14n.implementations.Canonicalizer11_OmitComments;
import com.sun.org.apache.xml.internal.utils.DOMBuilder;

/**
 * @version 2012/01/19 15:05:01
 */
public class XML {

    /** The factory. */
    private static final DocumentBuilder dom;

    /** The error details. */
    private static final Map<XML, Node> details = new ConcurrentHashMap();

    static {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);

            dom = factory.newDocumentBuilder();

            PowerAssertRenderer.register(XMLRenderer.class);
            PowerAssertRenderer.register(ElementRenderer.class);
            PowerAssertRenderer.register(AttributeRenderer.class);
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
     * Wrap xml document.
     * </p>
     * 
     * @param filter
     * @return
     */
    public static XML xml(XMLFilter filter) {
        DOMBuilder builder = new DOMBuilder(dom.newDocument());

        // make chain
        filter.setContentHandler(builder);

        // API definition
        return new XML(builder.m_doc);
    }

    /**
     * <p>
     * Helper method to build xpath expression of the specified node.
     * </p>
     * 
     * @param node A target node.
     * @return A xpath expression.
     */
    private static String makeXPath(Node node) {
        StringBuilder builder = new StringBuilder();

        while (node != null) {
            switch (node.getNodeType()) {
            case ATTRIBUTE_NODE:
                builder.insert(0, "/@" + node.getNodeName());
                node = ((Attr) node).getOwnerElement();
                break;

            case ELEMENT_NODE:
                builder.insert(0, "/" + node.getNodeName() + makeXPathPosition(node));
                node = node.getParentNode();
                break;

            case TEXT_NODE:
                builder.insert(0, "/text()");
                node = node.getParentNode();
                break;

            default:
                node = node.getParentNode();
                break;
            }
        }
        return builder.toString();
    }

    /**
     * <p>
     * Helper method to calcurate xpath position.
     * </p>
     * 
     * @param node A target element node.
     * @return A position.
     */
    private static String makeXPathPosition(Node node) {
        int i = 1;
        Element current = DOMUtil.getFirstChildElement(node.getParentNode(), node.getNodeName());

        while (current != node) {
            i++;
            current = DOMUtil.getNextSiblingElement(current, node.getNodeName());
        }

        // if the current element is only typed child in this context, we can omit a position
        // syntax sugar of the xpath expression.
        if (i == 1 && DOMUtil.getNextSiblingElement(current, node.getNodeName()) == null) {
            return "";
        } else {
            return "[" + i + "]";
        }
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
    public boolean equals(String xml) {
        return equals(xml(xml));
    }

    /**
     * <p>
     * Assert this xml is equal to the specified xml.
     * </p>
     * 
     * @param xml
     * @return
     */
    public boolean equals(Document xml) {
        return equals(xml(xml));
    }

    /**
     * <p>
     * Assert this xml is equal to the specified xml.
     * </p>
     * 
     * @param xml
     * @return
     */
    public boolean equals(XML xml) {
        try {
            compare(doc, xml.doc);

            // no difference
            return true;
        } catch (AssertionError e) {
            // some differences
            Throwable cause = e.getCause();

            if (cause instanceof Detail) {
                Detail detail = (Detail) cause;

                details.put(this, detail.one);
                details.put(xml, detail.other);
            }
            return false;
        }
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
            assert one.getNodeName() == other.getNodeName() : new Detail(one, other);
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
            assert one.getValue().equals(other.getValue()) : new Detail(one, other);
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
            XMLFormatter formatter = new XMLFormatter(builder);

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
     * @version 2012/02/15 14:15:10
     */
    private static final class XMLFormatter extends XMLWriter {

        /**
         * @param out
         */
        public XMLFormatter(Appendable out) {
            super(out);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void startDocument() {
            // ignore
        }
    }

    /**
     * @version 2012/02/15 1:49:33
     */
    @SuppressWarnings("serial")
    private static final class Detail extends Throwable {

        /** The test node. */
        private final Node one;

        /** The expect node. */
        private final Node other;

        /**
         * <p>
         * Show error in detail.
         * </p>
         * 
         * @param one
         * @param other
         */
        private Detail(Node one, Node other) {
            this.one = one;
            this.other = other;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();

            builder.append(makeXPath(one)).append(" is ").append(PowerAssertRenderer.format(one)).append("\n");
            builder.append(makeXPath(other)).append(" is ").append(PowerAssertRenderer.format(other)).append("\n");

            return builder.toString();
        }
    }

    /**
     * @version 2012/02/15 14:35:59
     */
    private static final class XMLRenderer extends PowerAssertRenderer<XML> {

        /**
         * {@inheritDoc}
         */
        @Override
        protected String render(XML value) {
            StringBuilder builder = new StringBuilder();

            Node detail = details.get(value);

            if (detail != null) {
                builder.append(makeXPath(detail))
                        .append(" is ")
                        .append(PowerAssertRenderer.format(detail))
                        .append("\n");
                Node node = detail instanceof Attr ? ((Attr) detail).getOwnerElement() : detail;

                node.getParentNode().insertBefore(node.getOwnerDocument().createComment("----------"), node);
                node.getParentNode()
                        .insertBefore(node.getOwnerDocument().createComment("----------"), node.getNextSibling());
            }
            builder.append(value);

            return builder.toString();
        }
    }

    /**
     * @version 2012/02/15 11:55:46
     */
    private static final class ElementRenderer extends PowerAssertRenderer<Element> {

        /**
         * {@inheritDoc}
         */
        @Override
        protected String render(Element value) {
            StringBuilder builder = new StringBuilder();
            builder.append('<').append(value.getNodeName());

            for (Attr attribute : DOMUtil.getAttrs(value)) {
                builder.append(' ').append(attribute.getName()).append("=\"").append(attribute.getValue()).append('"');
            }
            if (!value.hasChildNodes()) {
                builder.append('/');
            }
            builder.append('>');

            return builder.toString();
        }
    }

    /**
     * @version 2012/02/15 11:55:46
     */
    private static final class AttributeRenderer extends PowerAssertRenderer<Attr> {

        /**
         * {@inheritDoc}
         */
        @Override
        protected String render(Attr attr) {
            Element value = attr.getOwnerElement();

            StringBuilder builder = new StringBuilder();
            builder.append('<').append(value.getNodeName());
            builder.append(' ').append(attr.getName()).append("=\"").append(attr.getValue()).append('"');

            if (!value.hasChildNodes()) {
                builder.append('/');
            }
            builder.append('>');

            return builder.toString();
        }
    }
}
