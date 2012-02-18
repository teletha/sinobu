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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
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

import antibug.powerassert.PowerAssertRenderer;

import com.sun.org.apache.xalan.internal.xsltc.trax.DOM2SAX;
import com.sun.org.apache.xerces.internal.util.DOMUtil;

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
        try {
            DOMStreamer builder = new DOMStreamer(dom.newDocument());

            // build pipe
            List<XMLFilter> pipe = new ArrayList();
            pipe.addAll(Arrays.asList(filters));
            pipe.add(builder);

            I.parse(source, pipe.toArray(new XMLFilter[pipe.size()]));

            return xml(builder.m_doc);
        } catch (Exception e) {
            throw I.quiet(e);
        }
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
        DOMStreamer builder = new DOMStreamer(dom.newDocument());

        // make chain
        filter.setContentHandler(builder);

        // API definition
        return new XML(builder.m_doc);
    }

    /** The actual xml document. */
    private final Document doc;

    /** The current processing element. */
    private Node detail;

    /** The exceptions. */
    private Except except = Except.Comment().andPrefix().andWhiteSpace();

    /**
     * @param doc
     */
    private XML(Document doc) {
        this.doc = doc;
    }

    /**
     * <p>
     * Assert this xml has the node which is identified by the specified xpath.
     * </p>
     * 
     * @param xpath A xpath to indicate a node.
     * @param namespaces A list of namespace declarations.
     * @return A reuslt.
     */
    public boolean has(String xpath, String... namespaces) {
        return select(xpath, namespaces) != null;
    }

    /**
     * <p>
     * Select the node which is identified by the specified xpath.
     * </p>
     * 
     * @param xpath A xpath to indicate a node.
     * @param namespaces A list of namespace declarations.
     * @return A reuslt.
     */
    public Node select(String xpath, String... namespaces) {
        try {
            XPath path = XPathFactory.newInstance().newXPath();
            path.setNamespaceContext(new Namespaces(namespaces));
            return (Node) path.evaluate(xpath, doc, XPathConstants.NODE);
        } catch (XPathExpressionException e) {
            throw I.quiet(e);
        }
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
    public boolean isEqualTo(String xml, Except... excepts) {
        return isEqualTo(xml(xml), excepts);
    }

    /**
     * <p>
     * Assert this xml is equal to the specified xml.
     * </p>
     * 
     * @param xml
     * @return
     */
    public boolean isEqualTo(Document xml, Except... excepts) {
        return isEqualTo(xml(xml), excepts);
    }

    /**
     * <p>
     * Assert this xml is equal to the specified xml.
     * </p>
     * 
     * @param xml
     * @return
     */
    public boolean isEqualTo(XML xml, Except... excepts) {
        if (excepts != null && excepts.length != 0) {
            this.except = new Except();

            for (Except except : excepts) {
                this.except.bits.or(except.bits);
            }
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

        case COMMENT_NODE:
            compare((Comment) one, (Comment) other, otherXML);
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
    private void compare(NamedNodeMap one, NamedNodeMap other, XML otherXML) {
        if (one == null || other == null) {
            assert one == other;
        } else {
            assert one.getLength() == other.getLength();

            for (int i = 0; i < one.getLength(); i++) {
                Attr oneAttribute = (Attr) one.item(i);
                Attr otherAttribute = (Attr) other.getNamedItemNS(oneAttribute.getNamespaceURI(), oneAttribute.getLocalName());

                assert otherAttribute != null;
                compare(oneAttribute, otherAttribute, otherXML);
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

            if (!except.ignorePrefix()) {
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
            String oneText = one.getData();
            String otherText = other.getData();

            if (except.ignoreWhiteSpace()) {
                oneText = oneText.trim();
                otherText = otherText.trim();
            }
            assert oneText.equals(otherText);
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
    private void compare(Comment one, Comment other, XML otherXML) {
        if (one == null || other == null) {
            assert one == other;
        } else {
            String oneText = one.getData();
            String otherText = other.getData();

            if (except.ignoreWhiteSpace()) {
                oneText = oneText.trim();
                otherText = otherText.trim();
            }
            assert oneText.equals(otherText);
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

            if (!except.ignorePrefix()) {
                assert one.getNodeName() == other.getNodeName();
            }

            compare(one.getAttributes(), other.getAttributes(), otherXML);

            List<Node> oneChildren = convert(one.getChildNodes());
            List<Node> otherChildren = convert(other.getChildNodes());

            assert oneChildren.size() == otherChildren.size();

            for (int i = 0; i < oneChildren.size(); i++) {
                compare(oneChildren.get(i), otherChildren.get(i), otherXML);
            }
        }
    }

    /**
     * <p>
     * Helper method to conver {@link NodeList} to {@link List}.
     * </p>
     * 
     * @param list
     * @return
     */
    private List<Node> convert(NodeList nodes) {
        List<Node> list = new ArrayList();

        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);

            if (except.ignoreComment() && node.getNodeType() == COMMENT_NODE) {
                continue;
            }

            if (except.ignoreWhiteSpace() && node.getNodeType() == TEXT_NODE && isWhitespace(node.getTextContent())) {
                continue;
            }
            list.add(node);
        }
        return list;
    }

    /**
     * <p>
     * Helper method the specified text is whitespace or not.
     * </p>
     * 
     * @param text
     * @return
     */
    private boolean isWhitespace(String text) {
        for (int i = 0; i < text.length(); i++) {
            if (!Character.isWhitespace(text.charAt(i))) {
                return false;
            }
        }
        return true;
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
                // omitFollowingSiblings(detail);
                // omitPrecedingSiblings(detail);
                // omitChildren(detail);

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
                builder.insert(0, makeXPathPosition(node));
                builder.insert(0, "[local-name()='" + node.getLocalName() + "']");
                if (node.getNamespaceURI() != null) {
                    builder.insert(0, "[namespace-uri()='" + node.getNamespaceURI() + "']");
                }
                builder.insert(0, "/*");

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

    /**
     * @version 2012/02/18 14:17:30
     */
    private static class Namespaces implements NamespaceContext {

        /** The actual mapping. */
        private final Map<String, String> mapping;

        /**
         * @param mapping
         */
        private Namespaces(String... mapping) {
            this.mapping = new HashMap();

            for (int i = 0; i < mapping.length; i++) {
                this.mapping.put(mapping[i++], mapping[i]);
            }
        }

        /**
         * @see javax.xml.namespace.NamespaceContext#getNamespaceURI(java.lang.String)
         */
        public String getNamespaceURI(String prefix) {
            return mapping.get(prefix);
        }

        /**
         * @see javax.xml.namespace.NamespaceContext#getPrefix(java.lang.String)
         */
        public String getPrefix(String namespaceURI) {
            return null;
        }

        /**
         * @see javax.xml.namespace.NamespaceContext#getPrefixes(java.lang.String)
         */
        public Iterator getPrefixes(String namespaceURI) {
            return null;
        }
    }
}
