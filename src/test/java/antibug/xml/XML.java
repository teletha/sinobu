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

import static antibug.xml.XMLAmbiguity.*;
import static org.w3c.dom.Node.*;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.EnumSet;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import kiss.I;
import kiss.xml.XMLWriter;

import org.w3c.dom.Attr;
import org.w3c.dom.Comment;
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

import com.sun.org.apache.xalan.internal.xsltc.trax.DOM2SAX;
import com.sun.org.apache.xerces.internal.util.DOMUtil;
import com.sun.org.apache.xml.internal.utils.DOMBuilder;

/**
 * @version 2012/02/16 1:01:12
 */
public class XML {

    /** The factory. */
    private static final DocumentBuilder dom;

    /** The xpath evaluator. */
    private static final XPath xpath;

    static {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);

            dom = factory.newDocumentBuilder();
            xpath = XPathFactory.newInstance().newXPath();

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

    /** The actual xml document. */
    private final Document doc;

    /** The current processing element. */
    private Node detail;

    private EnumSet<XMLAmbiguity> ambiguities = EnumSet.allOf(XMLAmbiguity.class);

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
            compare(doc, xml.doc, xml);

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
    public boolean equals(XML xml, EnumSet<XMLAmbiguity>... ambiguities) {
        if (ambiguities != null && ambiguities.length != 0) {
            this.ambiguities = ambiguities[0];
        }

        try {
            compare(doc, xml.doc, xml);

            // no difference
            return true;
        } catch (AssertionError e) {
            // some differences
            return false;
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
    private void compare(Node one, Node other, XML otherXML) {
        assert one.getNodeType() == other.getNodeType();

        switch (one.getNodeType()) {
        case ATTRIBUTE_NODE:
            compare((Attr) one, (Attr) other, otherXML);
            break;

        case ELEMENT_NODE:
            detail = one;
            otherXML.detail = other;

            compare((Element) one, (Element) other, otherXML);
            break;

        case ENTITY_NODE:
            compare((Entity) one, (Entity) other, otherXML);
            break;

        case TEXT_NODE:
            compare((Text) one, (Text) other, otherXML);
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
    private void compare(Document one, Document other, XML otherXML) {
        if (one == null || other == null) {
            assert one == other;
        } else {
            compare(one.getDoctype(), other.getDoctype(), otherXML);
            // Don't use getDocumentElement method to pass compare(Node, Node, XML).
            compare(one.getFirstChild(), other.getFirstChild(), otherXML);
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
    private void compare(DocumentType one, DocumentType other, XML otherXML) {
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
    private void compare(Element one, Element other, XML otherXML) {
        if (one == null || other == null) {
            assert one == other;
        } else {
            assert one.getLocalName().intern() == other.getLocalName().intern();
            assert one.getNamespaceURI() == other.getNamespaceURI();

            if (!ambiguities.contains(Prefix)) {
                assert one.getNodeName() == other.getNodeName();
            }

            compare(one.getAttributes(), other.getAttributes(), otherXML);

            NodeList oneChildren = one.getChildNodes();
            NodeList otherChildren = other.getChildNodes();
            assert oneChildren.getLength() == otherChildren.getLength();

            for (int i = 0; i < oneChildren.getLength(); i++) {
                compare(oneChildren.item(i), otherChildren.item(i), otherXML);
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
    private void compare(NamedNodeMap one, NamedNodeMap other, XML otherXML) {
        if (one == null || other == null) {
            assert one == other;
        } else {
            assert one.getLength() == other.getLength();

            for (int i = 0; i < one.getLength(); i++) {
                compare(one.item(i), other.item(i), otherXML);
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
    private void compare(Attr one, Attr other, XML otherXML) {
        if (one == null || other == null) {
            assert one == other;
        } else {
            assert one.getLocalName().intern() == other.getLocalName().intern();
            assert one.getNamespaceURI() == other.getNamespaceURI();
            assert one.getValue().equals(other.getValue());

            if (!ambiguities.contains(Prefix)) {
                assert one.getName() == other.getName();
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
    private void compare(Text one, Text other, XML otherXML) {
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
    private void compare(Entity one, Entity other, XML otherXML) {
        if (one == null || other == null) {
            assert one == other;
        } else {
            throw new Error();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        try {
            StringBuilder builder = new StringBuilder();

            if (detail == null) {
                DOM2SAX streamer = new DOM2SAX(doc);
                streamer.setContentHandler(new XMLFormatter(builder));
                streamer.parse();
            } else {
                // Create xpath to indicate the error node of the document copy.
                String path = makeXPath(detail);

                // Copy the current document in order to protect the original document because
                // we will append some error message node into document.
                Document copy = (Document) detail.getOwnerDocument().cloneNode(true);

                // Find an error node of the new copied document by the previous xpath.
                detail = (Node) xpath.evaluate(path, copy, XPathConstants.NODE);

                // Shrink big document to be more readable.
                omitFollowingSiblings(detail);
                omitPrecedingSiblings(detail);
                omitChildren(detail);

                // Insert error message node.
                insertErrorMessage(detail);

                DOM2SAX streamer = new DOM2SAX(copy);
                streamer.setContentHandler(new XMLFormatter(builder));
                streamer.parse();
            }
            return builder.toString();
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }

    /**
     * <p>
     * Helper method to insert error message node.
     * </p>
     * 
     * @param node
     */
    private void insertErrorMessage(Node node) {
        Node parent = node.getParentNode();

        if (parent != null) {
            Comment comment = createErrorMessage(node);

            // before
            parent.insertBefore(comment, node);

            // after
            parent.insertBefore(comment.cloneNode(true), node.getNextSibling());
        }
    }

    /**
     * <p>
     * Create error message for the error node.
     * </p>
     * 
     * @param node A error node.
     * @return A created comment.
     */
    private Comment createErrorMessage(Node node) {
        // calcurate size
        char[] message = new char[PowerAssertRenderer.format(node).length()];

        // make as line
        Arrays.fill(message, '-');

        // API definition
        return node.getOwnerDocument().createComment(String.valueOf(message));
    }

    /**
     * <p>
     * Helper method to shrink document.
     * </p>
     * 
     * @param node
     */
    private void omitFollowingSiblings(Node node) {
        // Calcurate following-sibling node.
        Deque<Node> nodes = new ArrayDeque();

        Node next = node.getNextSibling();

        while (next != null) {
            nodes.add(next);

            next = next.getNextSibling();
        }

        if (2 < nodes.size()) {
            Node parent = nodes.pollFirst().getParentNode();

            for (Node n : nodes) {
                parent.removeChild(n);
            }
            parent.appendChild(node.getOwnerDocument().createTextNode(":"));
        }
    }

    /**
     * <p>
     * Helper method to shrink document.
     * </p>
     * 
     * @param node
     */
    private void omitPrecedingSiblings(Node node) {
        // Calcurate following-sibling node.
        Deque<Node> nodes = new ArrayDeque();

        Node next = node.getPreviousSibling();

        while (next != null) {
            nodes.add(next);

            next = next.getPreviousSibling();
        }

        if (2 < nodes.size()) {
            Node first = nodes.pollFirst();
            Node parent = node.getParentNode();

            for (Node n : nodes) {
                parent.removeChild(n);
            }
            parent.insertBefore(node.getOwnerDocument().createTextNode(":"), first);
        }
    }

    /**
     * <p>
     * Helper method to shrink document.
     * </p>
     * 
     * @param node
     */
    private void omitChildren(Node node) {
        // Calcurate following-sibling node.
        Deque<Node> nodes = new ArrayDeque();

        Node child = node.getFirstChild();

        while (child != null) {
            nodes.add(child);

            child = child.getNextSibling();
        }

        if (2 < nodes.size()) {
            nodes.pollFirst();
            Node last = nodes.pollLast();

            for (Node n : nodes) {
                node.removeChild(n);
            }
            node.insertBefore(node.getOwnerDocument().createTextNode(":"), last);
        }
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
                builder.insert(0, "/*" + makeXPathPosition(node) + "[namespace-uri()='" + node.getNamespaceURI() + "'][local-name()='" + node.getLocalName() + "']");
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

        /**
         * {@inheritDoc}
         */
        @Override
        protected boolean asBlock(char[] ch, int start, int length) {
            return length == 1 && ch[start] == ':';
        }
    }
}
