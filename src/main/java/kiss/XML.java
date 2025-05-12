/*
 * Copyright (C) 2024 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss;

import static javax.xml.XMLConstants.*;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A class for parsing, traversing, and manipulating XML and HTML documents
 * with a jQuery-like fluent API.
 * <p>
 * This class wraps the standard {@code org.w3c.dom} API to provide a more
 * convenient way to work with XML structures. It includes a CSS selector engine
 * (which converts CSS selectors to XPath) for finding elements and a lenient
 * HTML parser.
 * </p>
 * This class is not thread-safe for concurrent modifications of the underlying DOM
 * if the same {@code XML} instance (or instances sharing the same {@code Document})
 * is accessed by multiple threads without external synchronization.
 * Traversal and read operations are generally safe if the DOM is not being modified.
 *
 * @see I#xml(String)
 */
public class XML implements Iterable<XML>, Consumer<XML> {

    /** The escapable character pattern. */
    private static final Pattern ESCAPE = Pattern.compile(
            // Group 1: Match named entities that should be preserved (amp, lt, gt, quot, apos)
            "&(amp|lt|gt|quot|apos);"
                    // Match decimal numeric entities (preserve as-is)
                    + "|&#[0-9]+;"
                    // Match hexadecimal numeric entities (preserve as-is)
                    + "|&#x[0-9a-fA-F]+;"
                    // Group 2: Match raw characters that need escaping
                    + "|([&\"'<>])");

    private static final Pattern SELECTOR = Pattern.compile(""
            // Group 1: Combinator
            + "\\s*([>+~<\\s,])\\s*"
            // Group 2: Tag name (tag, or *, namespace is not supported)
            + "|((?:[\\w\\-]+(?:\\\\.[\\w\\-]*)*|\\*))"
            // Group 3: ID
            + "|#((?:[\\w\\-]|\\\\.)+)"
            // Group 4: Class
            + "|\\.((?:[\\w\\-]|\\\\.)+)"
            // Group 5: Attribute selector (namespace is not supported)
            // G5:attrName, G6:op, G7:quote, G8:q_val, G9:unq_val. Backref \\7 is correct.
            + "|\\[\\s?([\\w\\-_]+)(?:\\s*([=~^$*|])?=\\s*(?:(['\"])(.*?)\\7|([^\\]\\s]+)))?\\s*\\]"
            // Group 10: Pseudo-class part
            // G10: Pseudo-class name (e.g., "nth-child", "not")
            // G11: Pseudo-class argument (e.g., "2n+1", "p.foo", null if no arg)
            + "|:([\\w-]+)(?:\\(((?:[^()]+|\\((?:[^()]+)*\\))*?)\\))?");

    /** The cache for compiled selectors. */
    private static final Map<String, XPathExpression> selectors = new ConcurrentHashMap();

    /** The current document. */
    private Document doc;

    /** The current node set. */
    private List<Node> nodes;

    /**
     * Constructs an XML object from a given document and a list of nodes.
     * <p>
     * This constructor is typically used internally or by the {@link I} factory methods.
     * If the document is null, a new empty document is created.
     * If the list of nodes is null, it defaults to a list containing the document itself,
     * which is useful for starting operations on the root of a new or existing document.
     * </p>
     *
     * @param doc A parent W3C DOM Document. This document will be used for creating new nodes.
     * @param nodes A list of W3C DOM Nodes representing the current selection.
     *            Modifications to this list externally after an {@code XML} object
     *            is created may lead to unpredictable behavior.
     */
    XML(Document doc, List nodes) {
        this.doc = doc == null ? I.dom.newDocument() : doc;
        this.nodes = nodes == null ? I.list(this.doc) : nodes;
    }

    /**
     * Inserts this XML as a child element for the specified parent element.
     * This method implements the {@link Consumer} interface, allowing an {@code XML}
     * object to be used in functional contexts, for example, with streams or
     * methods that accept a {@code Consumer}.
     *
     * @param parent A parent {@code XML} element to which this {@code XML} object's
     *            content will be appended.
     */
    @Override
    public void accept(XML parent) {
        parent.append(this);
    }

    /**
     * <p>
     * Insert content, specified by the parameter, to the end of each element in the set of matched
     * elements.
     * </p>
     * <p>
     * The content can be an XML string, another {@code XML} object, a {@code Node},
     * or a {@code NodeList}. If multiple elements are in the current set, the content
     * is cloned for each append operation except for the last one to ensure that
     * the same node instance is not inserted multiple times if it's part of the same document.
     * </p>
     *
     * @param xml An element set, XML string, {@code Node}, or {@code NodeList} to append.
     * @return This {@code XML} object, allowing for chained API calls.
     */
    public XML append(Object xml) {
        Node n = convert(I.xml(doc, xml));

        for (Node node : nodes) {
            node.appendChild(nodes.size() == 1 ? n : n.cloneNode(true));
        }

        // API definition
        return this;
    }

    /**
     * <p>
     * Insert content, specified by the parameter, to the beginning of each element in the set of
     * matched elements.
     * </p>
     * <p>
     * The content can be an XML string, another {@code XML} object, a {@code Node},
     * or a {@code NodeList}. If multiple elements are in the current set, the content
     * is cloned for each prepend operation except for the last one.
     * </p>
     *
     * @param xml An element set, XML string, {@code Node}, or {@code NodeList} to prepend.
     * @return This {@code XML} object, allowing for chained API calls.
     */
    public XML prepend(Object xml) {
        Node n = convert(I.xml(doc, xml));

        for (Node node : nodes) {
            node.insertBefore(nodes.size() == 1 ? n : n.cloneNode(true), node.getFirstChild());
        }

        // API definition
        return this;
    }

    /**
     * <p>
     * Insert content, specified by the parameter, before each element in the set of matched
     * elements.
     * </p>
     * <p>
     * The content can be an XML string, another {@code XML} object, a {@code Node},
     * or a {@code NodeList}. If multiple elements are in the current set, the content
     * is cloned for each insertion operation except for the last one.
     * The parent node of the current elements must exist.
     * </p>
     *
     * @param xml An element set, XML string, {@code Node}, or {@code NodeList} to insert.
     * @return This {@code XML} object, allowing for chained API calls.
     */
    public XML before(Object xml) {
        Node n = convert(I.xml(doc, xml));

        for (Node node : nodes) {
            node.getParentNode().insertBefore(nodes.size() == 1 ? n : n.cloneNode(true), node);
        }

        // API definition
        return this;
    }

    /**
     * <p>
     * Insert content, specified by the parameter, after each element in the set of matched
     * elements.
     * </p>
     * <p>
     * The content can be an XML string, another {@code XML} object, a {@code Node},
     * or a {@code NodeList}. If multiple elements are in the current set, the content
     * is cloned for each insertion operation except for the last one.
     * The parent node of the current elements must exist.
     * </p>
     *
     * @param xml An element set, XML string, {@code Node}, or {@code NodeList} to insert.
     * @return This {@code XML} object, allowing for chained API calls.
     */
    public XML after(Object xml) {
        Node n = convert(I.xml(doc, xml));

        for (Node node : nodes) {
            node.getParentNode().insertBefore(nodes.size() == 1 ? n : n.cloneNode(true), node.getNextSibling());
        }

        // API definition
        return this;
    }

    /**
     * <p>
     * Remove all child nodes of the set of matched elements from the DOM.
     * </p>
     * <p>
     * This method does not remove the matched elements themselves, only their children.
     * </p>
     *
     * @return This {@code XML} object, allowing for chained API calls.
     */
    public XML empty() {
        for (Node node : nodes) {
            while (node.hasChildNodes()) {
                node.removeChild(node.getFirstChild());
            }
        }

        // API definition
        return this;
    }

    /**
     * <p>
     * Remove the set of matched elements from the DOM.
     * </p>
     * <p>
     * Similar to {@link #empty()}, the {@link #remove()} method takes elements out of the DOM. Use
     * {@link #remove()} when you want to remove the element itself, as well as everything inside
     * it. The parent node of the current elements must exist.
     * </p>
     *
     * @return This {@code XML} object, representing the removed elements (though they are no longer
     *         in the DOM). Further operations on this object might have limited effect if they
     *         depend on DOM structure.
     */
    public XML remove() {
        for (Node node : nodes) {
            node.getParentNode().removeChild(node);
        }

        // API definition
        return this;
    }

    /**
     * <p>
     * Wrap an XML or HTML structure around each element in the set of matched elements.
     * </p>
     * <p>
     * The {@code xml} parameter, which specifies the wrapping structure, is cloned for
     * each element in the current set. The current element is then moved inside this
     * cloned structure.
     * </p>
     *
     * @param xml An XML string, {@code XML} object, {@code Node}, or {@code NodeList}
     *            representing the structure to wrap around the elements.
     *            If the structure has multiple root elements, typically the first one is used.
     * @return This {@code XML} object, allowing for chained API calls. The selection still
     *         refers to the original elements, now wrapped.
     */
    public XML wrap(Object xml) {
        XML element = I.xml(doc, xml);

        for (XML e : this) {
            e.wrapAll(element.clone());
        }

        // API definition
        return this;
    }

    /**
     * <p>
     * Wrap an XML or HTML structure around all elements in the set of matched elements.
     * </p>
     * <p>
     * This method takes the first element in the current set, inserts the wrapping
     * structure after it, and then moves all elements from the original set (including
     * the first one) inside the newly inserted wrapping structure.
     * </p>
     *
     * @param xml An XML string, {@code XML} object, {@code Node}, or {@code NodeList}
     *            representing the structure to wrap around all elements.
     *            If the structure has multiple root elements, typically the first one is used.
     * @return This {@code XML} object, allowing for chained API calls. The selection still
     *         refers to the original elements, now wrapped together.
     */
    public XML wrapAll(Object xml) {
        first().after(xml).find("+*").append(this);

        // API definition
        return this;
    }

    /**
     * <p>
     * Create a deep copy of the set of matched elements.
     * </p>
     * <p>
     * The {@code .clone()} method performs a deep copy of the set of matched elements, meaning that
     * it copies the matched elements as well as all of their descendant elements and text nodes.
     * When used in conjunction with one of the insertion methods, {@code .clone()} is a convenient
     * way to duplicate elements on a page. The cloned nodes are not part of any document until
     * inserted.
     * </p>
     *
     * @return A new {@code XML} object containing the cloned elements. The cloned elements
     *         share the same {@code Document} object as the originals but are not initially
     *         attached to the DOM.
     */
    @Override
    public XML clone() {
        List list = new ArrayList();

        for (Node node : nodes) {
            list.add(node.cloneNode(true));
        }
        return new XML(doc, list);

    }

    /**
     * <p>
     * Get the combined text contents of each element in the set of matched elements, including
     * their descendants.
     * </p>
     * <p>
     * For multiple matched elements, their text contents are concatenated together in the order
     * they appear in the {@code nodes} list.
     * </p>
     *
     * @return A {@code String} containing the combined text content. Returns an empty string
     *         if the set of matched elements is empty.
     */
    public String text() {
        StringBuilder text = new StringBuilder();

        for (Node node : nodes) {
            text.append(node.getTextContent());
        }
        return text.toString();
    }

    /**
     * <p>
     * Set the content of each element in the set of matched elements to the specified text.
     * </p>
     * <p>
     * Any existing child nodes of the matched elements will be removed and replaced
     * by a single text node containing the specified text.
     * </p>
     *
     * @param text A text to set. If {@code null}, it might be treated as an empty string
     *            depending on the underlying {@code setTextContent} behavior, or could
     *            potentially cause a {@code NullPointerException} if not handled.
     *            It's safer to pass an empty string if no text is desired.
     * @return This {@code XML} object, allowing for chained API calls.
     */
    public XML text(String text) {
        for (Node node : nodes) {
            node.setTextContent(text);
        }

        // API definition
        return this;
    }

    /**
     * <p>
     * Get the element name (tag name) for the first element in the set of matched elements.
     * </p>
     *
     * @return An element name as a {@code String}. Returns an empty string if the
     *         set of matched elements is empty or the first node is not an {@code Element}.
     */
    public String name() {
        return nodes.isEmpty() ? "" : ((Element) nodes.get(0)).getTagName();
    }

    /**
     * <p>
     * Get the value of an attribute for the first element in the set of matched elements.
     * </p>
     *
     * @param name An attribute name.
     * @return The attribute value as a {@code String}. Returns an empty string if the
     *         set of matched elements is empty, the first node is not an {@code Element},
     *         or the attribute does not exist.
     */
    public String attr(String name) {
        return nodes.isEmpty() ? "" : ((Element) nodes.get(0)).getAttribute(name);
    }

    /**
     * <p>
     * Set one or more attributes for the set of matched elements.
     * </p>
     * <p>
     * If {@code value} is {@code null}, the attribute is removed.
     * Handles namespace attributes (e.g., attributes starting with "xmlns:") correctly
     * by using {@code setAttributeNS} or {@code removeAttributeNS}.
     * </p>
     *
     * @param name An attribute name. If {@code null} or empty, no action is taken.
     * @param value An attribute value. If {@code null}, the attribute is removed.
     *            The value will be converted to a string using {@code toString()}.
     * @return This {@code XML} object, allowing for chained API calls.
     */
    public XML attr(String name, Object value) {
        if (name != null && name.length() != 0) {
            for (Node node : nodes) {
                org.w3c.dom.Element e = (org.w3c.dom.Element) node;

                if (value == null) {
                    if (name.startsWith(XMLNS_ATTRIBUTE)) {
                        // namespace
                        e.removeAttributeNS(XMLNS_ATTRIBUTE_NS_URI, name);
                    } else {
                        // attribute
                        e.removeAttribute(name);
                    }
                } else {
                    try {
                        if (name.startsWith(XMLNS_ATTRIBUTE)) {
                            // namespace
                            e.setAttributeNS(XMLNS_ATTRIBUTE_NS_URI, name, value.toString());
                        } else {
                            // attribute
                            e.setAttribute(name, value.toString());
                        }
                    } catch (DOMException dom) {
                        // user specify invalid attribute name
                    }
                }
            }
        }

        // API definition
        return this;
    }

    /**
     * <p>
     * Adds the specified class(es) to each of the set of matched elements.
     * </p>
     * <p>
     * It's important to note that this method does not replace a class. It simply adds the class,
     * appending it to any which may already be assigned to the elements. More than one class may be
     * added at a time, separated by a space, to the set of matched elements.
     * If a class to be added already exists, it is not added again.
     * </p>
     *
     * @param names One or more space-separated class names to add.
     * @return This {@code XML} object, allowing for chained API calls.
     */
    public XML addClass(String... names) {
        for (XML e : this) {
            String value = " ".concat(e.attr("class")).concat(" ");

            for (String name : names) {
                if (!value.contains(" ".concat(name).concat(" "))) {
                    value = value.concat(name).concat(" ");
                }
            }
            e.attr("class", value.trim());
        }

        // API definition
        return this;
    }

    /**
     * <p>
     * Remove a single class, multiple classes, or all classes from each element in the set of
     * matched elements.
     * </p>
     * <p>
     * If a class name is included as a parameter, then only that class will be removed from the set
     * of matched elements. If no class names are specified in the parameter (i.e., an empty array
     * or no arguments), all classes will be removed (by setting the class attribute to an empty
     * string).
     * More than one class may be removed at a time, specified as separate strings or
     * space-separated within a single string.
     * </p>
     *
     * @param names Space-separated class name list(s) to remove. If empty, all classes are removed.
     * @return This {@code XML} object, allowing for chained API calls.
     */
    public XML removeClass(String... names) {
        for (XML e : this) {
            String value = " ".concat(e.attr("class")).concat(" ");

            for (String name : names) {
                value = value.replace(" ".concat(name).concat(" "), " ");
            }
            e.attr("class", value.trim());
        }

        // API definition
        return this;
    }

    /**
     * <p>
     * Add or remove one or more classes from each element in the set of matched elements,
     * depending on either the class's presence or the value of the switch argument (if provided).
     * This implementation currently only supports toggling a single class name.
     * </p>
     *
     * @param name A single class name to toggle.
     * @return This {@code XML} object, allowing for chained API calls.
     */
    public XML toggleClass(String name) {
        for (XML e : this) {
            if (e.hasClass(name)) {
                e.removeClass(name);
            } else {
                e.addClass(name);
            }
        }

        // API definition
        return this;
    }

    /**
     * <p>
     * Determine whether any of the matched elements are assigned the given class.
     * </p>
     *
     * @param name A class name to search for.
     * @return {@code true} if at least one element in the set has the specified class,
     *         {@code false} otherwise.
     */
    public boolean hasClass(String name) {
        for (XML e : this) {
            String value = " ".concat(e.attr("class")).concat(" ");

            if (value.contains(" ".concat(name).concat(" "))) {
                return true;
            }
        }
        return false;
    }

    // ===============================================
    // Traversing
    // ===============================================

    /**
     * Reduce the set of matched elements to the first in the set.
     * If the current set is empty, an {@code XML} object representing an
     * empty set is returned.
     *
     * @return A new {@code XML} object containing only the first element,
     *         or an empty set if the original set was empty.
     */
    public final XML first() {
        return nodes.isEmpty() ? this : new XML(doc, nodes.subList(0, 1));
    }

    /**
     * Reduce the set of matched elements to the final one in the set.
     * If the current set is empty, an {@code XML} object representing an
     * empty set is returned.
     *
     * @return A new {@code XML} object containing only the last element,
     *         or an empty set if the original set was empty.
     */
    public final XML last() {
        return nodes.isEmpty() ? this : new XML(doc, nodes.subList(nodes.size() - 1, nodes.size()));
    }

    /**
     * Append a new child element with the specified name to each element in the current set,
     * and then return a new {@code XML} object representing these newly created child elements.
     *
     * @param name A child element name.
     * @return A new {@code XML} object representing the created child elements.
     */
    public final XML child(String name) {
        return child(name, null);
    }

    /**
     * Append a new child element with the specified name to each element in the current set,
     * apply a {@link Consumer} function to the new {@code XML} object representing these children,
     * and then return this new {@code XML} object.
     * <p>
     * This is useful for creating and immediately configuring child elements in a fluent manner.
     * </p>
     *
     * @param name A child element name.
     * @param child A {@link Consumer} that accepts the newly created {@code XML}
     *            (representing the children) for further configuration. Can be {@code null}.
     * @return A new {@code XML} object representing the created child elements.
     */
    public final XML child(String name, Consumer<XML> child) {
        // don't use the following codes because of building xml performance
        // return append("<" + name + "/>").lastChild();
        List list = new ArrayList();

        for (Node node : nodes) {
            list.add(node.appendChild(doc.createElementNS(null, name)));
        }
        XML x = new XML(doc, list);
        if (child != null) child.accept(x);
        return x;
    }

    /**
     * Get the children of each element in the current set of matched elements.
     * This is equivalent to {@code find(">*")}. Only element nodes are returned.
     *
     * @return A new {@code XML} object containing all direct child elements.
     */
    public final XML children() {
        return find(">*");
    }

    /**
     * Get the first child element of each element in the current set of matched elements.
     * This is equivalent to {@code find(">*:first-child")}.
     * If an element in the set has no child elements, it contributes nothing to the result.
     * 
     * @return A new {@code XML} object containing the first child element of each matched element.
     */
    public final XML firstChild() {
        return find(">*:first-child");
    }

    /**
     * Get the last child element of each element in the current set of matched elements.
     * This is equivalent to {@code find(">*:last-child")}.
     * If an element in the set has no child elements, it contributes nothing to the result.
     * 
     * @return A new {@code XML} object containing the last child element of each matched element.
     */
    public final XML lastChild() {
        return find(">*:last-child");
    }

    /**
     * Get the parent of each element in the current set of matched elements.
     * If an element has no parent (e.g., it's a document node or a detached element),
     * or its parent is not an {@code Element} node, it does not contribute to the result set.
     * The resulting set contains unique parent elements.
     *
     * @return A new {@code XML} object containing the unique parent elements.
     */
    public final XML parent() {
        CopyOnWriteArrayList<Node> list = new CopyOnWriteArrayList();

        for (Node node : nodes) {
            Node p = node.getParentNode();

            if (p != null && !p.getNodeName().equals("ǃ")) {
                list.addIfAbsent(p instanceof Element ? p : node);
            }
        }
        return new XML(doc, list);
    }

    /**
     * Get the ancestors of each element in the current set of matched elements,
     * up to but not including the element matched by the selector.
     * The elements are returned in order from the closest parent to the furthest.
     *
     * @param selector A CSS selector expression to indicate where to stop matching ancestor
     *            elements. If the selector is empty or null, it might retrieve all ancestors up to
     *            the root.
     * @return A new {@code XML} object containing the matching ancestor elements.
     */
    public final XML parentUntil(String selector) {
        return until(selector, Node::getParentNode);
    }

    /**
     * Get the previous sibling element of each element in the current set of matched elements.
     * This is equivalent to {@code find("<*")}. Only element nodes are returned.
     *
     * @return A new {@code XML} object containing the previous sibling element of each matched
     *         element.
     */
    public final XML prev() {
        return find("<*");
    }

    /**
     * Get all preceding sibling elements of each element in the current set of matched elements,
     * up to but not including the element matched by the selector.
     * The elements are returned in document order (the one closest to the starting element first).
     *
     * @param selector A CSS selector expression to indicate where to stop matching preceding
     *            sibling elements.
     * @return A new {@code XML} object containing the matching preceding sibling elements.
     */
    public final XML prevUntil(String selector) {
        return until(selector, Node::getPreviousSibling);
    }

    /**
     * Get the next sibling element of each element in the current set of matched elements.
     * This is equivalent to {@code find("+*")}. Only element nodes are returned.
     *
     * @return A new {@code XML} object containing the next sibling element of each matched element.
     */
    public final XML next() {
        return find("+*");
    }

    /**
     * Get all following sibling elements of each element in the current set of matched elements,
     * up to but not including the element matched by the selector.
     * The elements are returned in document order.
     *
     * @param selector A CSS selector expression to indicate where to stop matching following
     *            sibling elements.
     * @return A new {@code XML} object containing the matching following sibling elements.
     */
    public final XML nextUntil(String selector) {
        return until(selector, Node::getNextSibling);
    }

    /**
     * Internal helper method to traverse the DOM in a specified direction from each current node,
     * collecting elements until a node matches the given selector or the traversal ends.
     *
     * @param selector A CSS selector. The traversal stops when an element matching this selector is
     *            encountered (exclusive).
     * @param traverse A {@link UnaryOperator} that defines the traversal direction (e.g.,
     *            {@code Node::getNextSibling}).
     * @return A new {@code XML} object containing the collected elements.
     */
    private XML until(String selector, UnaryOperator<Node> traverse) {
        CopyOnWriteArrayList result = new CopyOnWriteArrayList();
        XPathExpression x = compile(selector, "self::");

        for (Node node : nodes) {
            while (true) {
                node = traverse.apply(node);
                if (node == null || node.getNodeName().equals("ǃ")) break;
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    try {
                        if ((Boolean) x.evaluate(node, XPathConstants.BOOLEAN)) {
                            break;
                        }
                    } catch (XPathExpressionException e) {
                        // continue
                    }
                    result.addIfAbsent(node);
                }
            }
        }
        return new XML(doc, result);
    }

    /**
     * Get the descendants of each element in the current set of matched elements, filtered by a CSS
     * selector.
     * <p>
     * The selector is converted to an XPath expression, which is then evaluated against each
     * element in the current set. The results are aggregated into a new {@code XML} object.
     * </p>
     *
     * @param selector A string containing a CSS selector expression to match elements against.
     *            Can also be an XPath expression if prefixed with "xpath:".
     * @return A new {@code XML} object containing the matched descendant elements.
     * @throws RuntimeException if the selector is invalid and causes an
     *             {@link XPathExpressionException}.
     */
    public XML find(String selector) {
        XPathExpression xpath = compile(selector, "descendant::");
        CopyOnWriteArrayList<Node> result = new CopyOnWriteArrayList();

        try {
            for (Node node : nodes) {
                result.addAllAbsent(convert((NodeList) xpath.evaluate(node, XPathConstants.NODESET)));
            }
            return new XML(doc, result);
        } catch (XPathExpressionException e) {
            throw I.quiet(e);
        }
    }

    /**
     * Return size of the current node set. This indicates how many DOM elements
     * are currently matched by this {@code XML} object.
     *
     * @return A size of current node set.
     */
    public int size() {
        return nodes.size();
    }

    /**
     * Apply the specified process to this {@link XML} object itself and return the result
     * of the process. This is helpful for inserting custom operations into a fluent
     * method chain without breaking the chain if the process returns {@code this},
     * or for extracting a different type of result.
     *
     * @param process A {@link WiseFunction} that takes this {@code XML} object as input
     *            and produces a result of type {@code R}.
     * @param <R> The type of the result returned by the process function.
     * @return The result of applying the process function to this {@code XML} object.
     */
    public <R> R effect(WiseFunction<XML, R> process) {
        return process.apply(this);
    }

    /**
     * Returns an iterator over the set of matched elements. Each element
     * in the iteration is an {@code XML} object representing a single DOM node
     * from the current set.
     * <p>
     * This allows the {@code XML} object to be used in enhanced for-loops:
     * </p>
     *
     * @return An {@link Iterator} of {@code XML} objects.
     */
    @Override
    public Iterator<XML> iterator() {
        List<XML> elements = new ArrayList();

        for (Node node : nodes) {
            elements.add(new XML(doc, I.list(node)));
        }
        return elements.iterator();
    }

    /**
     * Convert the first element in the current set to its underlying {@link Node} representation.
     *
     * @return The first {@link Node} in the set, or {@code null} if the set is empty.
     */
    public Node to() {
        return nodes.size() == 0 ? null : nodes.get(0);
    }

    /**
     * <p>
     * Write the XML representation of the matched elements to the specified {@link Appendable}
     * (e.g., {@code StringBuilder}, {@code Writer}) with default formatting (tab indentation).
     * </p>
     * <p>
     * If multiple elements are matched, they are serialized sequentially.
     * </p>
     *
     * @param output An {@link Appendable} to write the XML to.
     */
    public void to(Appendable output) {
        to(output, "\t");
    }

    /**
     * <p>
     * Write this element to the specified output with your format settings.
     * </p>
     * 
     * @param output An output channel.
     * @param indent Specify the indentation string to use when formatting. If null is specified,
     *            formatting will not be performed.
     * @param inlineAndNonEmpty At the time of formatting, the element with the specified name is
     *            regarded as an inline element, and line breaks and indentation are not performed
     *            on the surrounding elements. Also, if an element whose name starts with "&amp;" is
     *            specified, it will not be treated as an empty element and will always have a start
     *            tag and end tag.
     */
    public void to(Appendable output, String indent, String... inlineAndNonEmpty) {
        for (Node node : nodes) {
            to(node, output, indent, 0, false, Set.of(inlineAndNonEmpty));
        }
        I.quiet(output);
    }

    /**
     * Serialize DOM with your pretty format.
     * 
     * @param node A target {@link Node} to serialize.
     * @param output An output channel.
     * @param indent Specify the indentation string to use when formatting. If null is specified,
     *            formatting will not be performed.
     * @param level A current indent level.
     * @param block Indicates whether the previous element is a block element.
     * @param inlines At the time of formatting, the element with the specified name is regarded as
     *            an inline element, and line breaks and indentation are not performed on the
     *            surrounding elements. Also, if an element whose name starts with "&" is specified,
     *            it will not be treated as an empty element and will always have a start tag and
     *            end tag.
     * @return Indicates whether the current element is a block element.
     */
    private boolean to(Node node, Appendable output, String indent, int level, boolean block, Set<String> inlines) {
        try {
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                String text = node.getTextContent();
                if (!text.isBlank()) {
                    output.append(escape(text));
                    return false;
                } else {
                    return block;
                }
            }

            String name = ((Element) node).getTagName();
            boolean isBlock = !inlines.contains(name);

            if (block && isBlock) {
                if (indent != null) output.append("\r\n").append(indent.repeat(level));
            }
            output.append('<').append(name);

            NamedNodeMap attrs = node.getAttributes();
            for (int i = 0; i < attrs.getLength(); i++) {
                Attr attr = (Attr) attrs.item(i);
                output.append(' ').append(attr.getName()).append("=\"").append(escape(attr.getValue())).append('"');
            }

            NodeList children = node.getChildNodes();

            if (children.getLength() == 0 && !inlines.contains("&".concat(name))) {
                output.append("/>");
            } else {
                output.append('>');
                int s = children.getLength();

                for (int i = 0; i < s; i++) {
                    block = to(children.item(i), output, indent, level + 1, isBlock, inlines);
                }

                if (block && indent != null && s != 0) {
                    output.append("\r\n").append(indent.repeat(level));
                }
                output.append("</").append(name).append('>');
            }
            return isBlock;
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }

    /**
     * Escape XML special characters (&amp;, &lt;, %gt;, &quot;, &apos;) in an idempotent way.
     * <p>
     * This method replaces the characters {@code &, <, >, ", '} with their
     * corresponding XML entity references ({@code &amp;, &lt;, &gt;, &quot;, &apos;}).
     * It ensures <strong>idempotency</strong>, meaning the same result is returned
     * even if the method is called multiple times on the same string.
     * <p>
     * Existing entities (both named and numeric, like {@code &amp;} or {@code &#38;})
     * are preserved as-is and are not re-escaped.
     *
     * @param input The input string to escape. Null is treated as an empty string.
     * @return A string with XML-escaped characters, safe for use in XML contexts.
     */
    public static String escape(String input) {
        if (input == null) {
            return "";
        }

        // Use regex matcher to find XML special characters or existing entities
        Matcher matcher = ESCAPE.matcher(input);
        StringBuilder builder = new StringBuilder();
        int pos = 0;

        while (matcher.find()) {
            // Append the segment before the current match
            builder.append(input, pos, matcher.start());

            // Group 2 matches unescaped special characters (e.g., &, <, >, etc.)
            String match = matcher.group(2);
            if (match != null) {
                switch (match.charAt(0)) {
                case '&':
                    builder.append("&amp;");
                    break;
                case '<':
                    builder.append("&lt;");
                    break;
                case '>':
                    builder.append("&gt;");
                    break;
                case '"':
                    builder.append("&quot;");
                    break;
                case '\'':
                    builder.append("&apos;");
                    break;
                }
            } else {
                // Group 1 matched an already escaped entity — preserve it
                builder.append(matcher.group(0));
            }

            // Update position to end of current match
            pos = matcher.end();
        }

        // Append remaining content after the last match
        builder.append(input, pos, input.length());

        return builder.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        to(builder);

        return builder.toString().trim();
    }

    /**
     * Helper method to convert an {@code XML} object (potentially from another document)
     * into a {@link DocumentFragment} owned by the current {@code XML} instance's document.
     * Nodes are imported if they belong to a different document.
     * The original {@code XML} object's document reference is updated to the current document.
     *
     * @param xml The {@code XML} object to convert. Its nodes will be moved or imported.
     * @return A {@link DocumentFragment} containing the nodes from the input {@code xml},
     *         now associated with the current instance's document.
     */
    private Node convert(XML xml) {
        DocumentFragment fragment = doc.createDocumentFragment();

        for (int i = 0; i < xml.nodes.size(); i++) {
            Node node = xml.nodes.get(i);

            if (doc == xml.doc) {
                fragment.appendChild(node);
            } else {
                xml.nodes.set(i, fragment.appendChild(doc.importNode(node, true)));
            }
        }

        // update
        xml.doc = doc;

        // root
        return fragment;
    }

    /**
     * Helper method to convert a {@link NodeList} into a {@code List<Node>},
     * filtering for {@code ELEMENT_NODE}s and ensuring uniqueness if added to
     * a list that checks for absence (like {@code CopyOnWriteArrayList.addIfAbsent}).
     *
     * @param list A {@link NodeList} to convert.
     * @return A {@code List<Node>} containing only unique element nodes from the input list.
     *         The list returned is a {@code CopyOnWriteArrayList}.
     */
    static List<Node> convert(NodeList list) {
        CopyOnWriteArrayList<Node> nodes = new CopyOnWriteArrayList();

        for (int i = 0; i < list.getLength(); i++) {
            Node node = list.item(i);

            if (node.getNodeType() == Node.ELEMENT_NODE) {
                nodes.addIfAbsent(node);
            }
        }
        return nodes;
    }

    /**
     * Compile and cache the specified selector.
     *
     * @param selector A css selector.
     * @return A compiled xpath expression.
     */
    private static XPathExpression compile(String selector, String axis) {
        // check cache
        XPathExpression compiled = selectors.get(selector);

        if (compiled == null) {
            try {
                // compile actually
                compiled = I.xpath.compile(convert(selector, axis));

                // cache it
                selectors.put(selector, compiled);
            } catch (XPathExpressionException e) {
                throw I.quiet(e);
            }
        }

        // API definition
        return compiled;
    }

    /**
     * Helper method to convert a CSS selector string to its equivalent XPath expression string.
     * If the selector starts with "xpath:", it is returned as is (after removing the prefix).
     * <p>
     * This method parses the CSS selector using {@link #SELECTOR} and builds an XPath query
     * based on combinators, type selectors, ID, class, attributes, and pseudo-classes.
     * </p>
     *
     * @param selector A CSS selector string.
     * @return An XPath expression string.
     */
    private static String convert(String selector, String axis) {
        if (selector.startsWith("xpath:")) return selector.substring(6);

        String current = null;
        StringBuilder xpath = new StringBuilder();
        Matcher matcher = SELECTOR.matcher(selector.trim());

        while (matcher.find()) {
            boolean contextual = matcher.start() == 0;

            // =================================================
            // Combinators
            // =================================================
            String match = matcher.group(1);
            if (match == null) {
                if (contextual) {
                    xpath.append(axis).append('*');
                }
            } else {
                match = match.trim();

                if (match.length() == 0) {
                    // Descendant combinator
                    xpath.append("//*");
                } else {
                    switch (match.charAt(0)) {
                    case '>': // Child combinator
                        xpath.append("/*");
                        break;

                    case '~': // General sibling combinator
                        xpath.append("/following-sibling::*");
                        break;

                    case '+': // Adjacent sibling combinator
                        xpath.append("/following-sibling::*[1]");
                        break;

                    case '<': // Adjacent previous sibling combinator (EXTENSION)
                        xpath.append("/preceding-sibling::*[1]");
                        break;

                    case ',': // selector separator
                        xpath.append('|').append(axis).append('*');

                        // reset processing context
                        current = null;
                        break;
                    }

                    if (contextual) {
                        xpath.delete(0, 1);
                    }
                }
                continue;
            }

            // =================================================
            // Type (Universal) Selector
            // =================================================
            match = matcher.group(2);
            if (match != null) {
                if (match.equals("*")) {
                    current = "*";
                } else {
                    xpath.append("[local-name()='").append(current = match.replaceAll("\\\\(.)", "$1")).append("']");
                }
                continue;
            }

            // =================================================
            // ID Selector
            // =================================================
            match = matcher.group(3);
            if (match != null) {
                xpath.append("[@id='").append(match.replaceAll("\\\\(.)", "$1")).append("']");
                continue;
            }

            // =================================================
            // Class Selector
            // =================================================
            match = matcher.group(4);
            if (match != null) {
                xpath.append("[contains(concat(' ',normalize-space(@class),' '),' ")
                        .append(match.replaceAll("\\\\(.)", "$1"))
                        .append(" ')]");
                continue;
            }

            // =================================================
            // Attribute Selector
            // =================================================
            match = matcher.group(5);
            if (match != null) {
                match = "@*[local-name()='".concat(match).concat("']");
                String value = matcher.group(8);
                if (value == null) value = matcher.group(9);

                if (value == null) {
                    // [att]
                    //
                    // Represents an element with the att attribute, whatever the value
                    // of the attribute.
                    xpath.append('[').append(match).append(']');
                } else {
                    String type = matcher.group(6);

                    if (type == null) {
                        // [att=val]
                        //
                        // Represents an element with the att attribute whose value
                        // is exactly "val".
                        xpath.append('[').append(match).append("='").append(value).append("']");
                    } else {
                        switch (type.charAt(0)) {
                        case '~':
                            // [att~=val]
                            //
                            // Represents an element with the att attribute whose value is a
                            // whitespace-separated list of words, one of which is exactly
                            // "val". If "val" contains whitespace, it will never represent
                            // anything (since the words are separated by spaces). Also, if "val"
                            // is the empty string, it will never represent anything.
                            xpath.append("[contains(concat(' ',").append(match).append(",' '),' ").append(value).append(" ')]");
                            break;

                        case '*':
                            // [att*=val]
                            //
                            // Represents an element with the att attribute whose value contains
                            // at least one instance of the substring "val". If "val" is the
                            // empty string then the selector does not represent anything.
                            xpath.append("[contains(").append(match).append(",'").append(value).append("')]");
                            break;

                        case '^':
                            // [att^=val]
                            //
                            // Represents an element with the att attribute whose value begins
                            // with the prefix "val". If "val" is the empty string then the
                            // selector does not represent anything.
                            xpath.append("[starts-with(").append(match).append(",'").append(value).append("')]");
                            break;

                        case '$':
                            // [att$=val]
                            //
                            // Represents an element with the att attribute whose value ends
                            // with the suffix "val". If "val" is the empty string then the
                            // selector does not represent anything.
                            xpath.append("[substring(")
                                    .append(match)
                                    .append(",string-length(")
                                    .append(match)
                                    .append(")-string-length('")
                                    .append(value)
                                    .append("')+1)='")
                                    .append(value)
                                    .append("']");
                            break;

                        case '|':
                            // [att|=val]
                            //
                            // Represents an element with the att attribute, its value either
                            // being exactly "val" or beginning with "val" immediately followed by
                            // "-" (U+002D).
                            xpath.append('[')
                                    .append(match)
                                    .append("='")
                                    .append(value)
                                    .append("' or starts-with(")
                                    .append(match)
                                    .append(",'")
                                    .append(value)
                                    .append("-')]");
                            break;
                        }
                    }
                }
                continue;
            }

            // =================================================
            // Structural Pseudo Classes Selector
            // =================================================
            match = matcher.group(10);
            if (match != null) {
                String arg = matcher.group(11);

                switch (match.hashCode()) {
                case -947996741: // only-child
                    xpath.append("[count(parent::*/*)=1]");
                    break;

                case 1455900751: // only-of-type
                    xpath.append("[count(parent::*/").append(current).append(")=1]");
                    break;

                case 96634189: // empty
                    xpath.append("[not(node())]");
                    break;

                case 109267: // not
                case 103066: // has
                    xpath.append('[');
                    if (match.charAt(0) == 'n') {
                        xpath.append("not");
                    }
                    xpath.append('(').append(convert(arg, axis).replace("descendant::", "descendant-or-self::")).append(")]");
                    break;

                case -995424086: // parent
                    xpath.append("/parent::*");
                    break;

                case 3506402: // root
                    xpath.delete(0, xpath.length()).append("/*");
                    break;

                case -567445985: // contains
                    xpath.append("[contains(text(),'").append(arg).append("')]");
                    break;

                case -2136991809: // first-child
                case 835834661: // last-child
                case 1292941139: // first-of-type
                case 2025926969: // last-of-type
                case -1754914063: // nth-child
                case -1629748624: // nth-last-child
                case -897532411: // nth-of-type
                case -872629820: // nth-last-of-type
                    String coefficient = "0";
                    String remainder = "0";
                    if (match.startsWith("nth")) {
                        int index = arg.indexOf('n');

                        if (arg.equals("even")) {
                            coefficient = "2";
                            // remainder = 0;
                        } else if (arg.equals("odd")) {
                            coefficient = "2";
                            remainder = "1";
                        } else if (index == -1) {
                            remainder = arg;
                        } else {
                            coefficient = arg.substring(0, index).trim();
                            coefficient = coefficient.isEmpty() ? "1" : coefficient.equals("-") ? "-1" : coefficient;

                            String after = arg.substring(index + 1).trim().replace("+", "");
                            if (!after.isEmpty()) remainder = after;
                        }
                    } else {
                        remainder = "1";
                    }

                    // construct xpath
                    String type = match.contains("type") ? current : "*";
                    String expr = "(count(" + (match.contains("last") ? "following" : "preceding") + "-sibling::" + type + ")+1)";

                    xpath.append('[');
                    if (coefficient.equals("0")) {
                        // remainder only
                        xpath.append(expr).append("=").append(remainder);
                    } else {
                        String term = "(" + expr + "-" + remainder + ")";
                        if (coefficient.startsWith("-")) {
                            coefficient = coefficient.replace("-", "");
                            term = "-".concat(term);
                        }
                        xpath.append('(').append(term).append(" mod ").append(coefficient).append("=0)and(");
                        xpath.append(term).append(" div ").append(coefficient).append(">=0)");
                    }
                    xpath.append(']');
                    break;
                }
                continue;
            }
        }

        return xpath.toString();
    }

    // =======================================================================
    // HTML Parser for building XML
    // =======================================================================
    /** The defined empty elements. */
    private static final String[] empties = {"area", "base", "br", "col", "command", "device", "embed", "frame", "hr", "img", "input",
            "keygen", "link", "meta", "param", "source", "track", "wbr"};

    /** The defined data elements. */
    private static final String[] data = {"noframes", "script", "style", "textarea", "title"};

    /** The position for something. */
    private int pos;

    /** The encoded text data. */
    private String html;

    /**
     * Parses the given raw HTML byte data using the specified character encoding
     * and builds the DOM structure represented by this {@code XML} object.
     * <p>
     * This method contains a lenient HTML parser that attempts to handle common
     * HTML structures, including character encoding detection via {@code <meta>} tags.
     * If a different encoding is detected, parsing may be restarted with the new encoding.
     * </p>
     *
     * @param raw The raw byte array of the HTML data.
     * @param encoding The initial character encoding to use for parsing.
     * @return This {@code XML} object, now representing the parsed HTML structure.
     *         If re-parsing occurs due to encoding detection, a new parse cycle is initiated.
     */
    XML parse(byte[] raw, Charset encoding) {
        // ====================
        // Initialization
        // ====================
        XML xml = this;
        html = new String(raw, encoding);
        pos = 0;

        // If crazy html provides multiple meta element for character encoding,
        // we should adopt first one.
        boolean detectable = true;

        // ====================
        // Start Parsing
        // ====================
        nextSpace();

        while (pos != html.length()) {
            if (test("<!--")) {
                // =====================
                // Comment
                // =====================
                xml.append(xml.doc.createComment(next("->")));
            } else if (test("<![CDATA[")) {
                // =====================
                // CDATA
                // =====================
                xml.append(xml.doc.createCDATASection(next("]]>")));
            } else if (test("<!") || test("<?")) {
                // =====================
                // DocType and PI
                // =====================
                // ignore doctype and pi
                next(">");
                nextSpace();
            } else if (test("</")) {
                // =====================
                // End Element
                // =====================
                next(">");

                // update current element into parent
                xml = xml.parent();
            } else if (test("<")) {
                // =====================
                // Start Element
                // =====================
                String name = nextName();
                nextSpace();

                XML child = xml.child(name);

                // parse attributes
                while (html.charAt(pos) != '/' && html.charAt(pos) != '>') {
                    String attr = nextName();
                    nextSpace();

                    if (!test("=")) {
                        // single value attribute
                        child.attr(attr, attr);

                        // step into next
                        pos++;
                    } else {
                        // name-value pair attribute
                        nextSpace();

                        if (test("\"")) {
                            // quote attribute
                            child.attr(attr, next("\""));
                        } else if (test("'")) {
                            // apostrophe attribute
                            child.attr(attr, next("'"));
                        } else {
                            // non-quoted attribute
                            int start = pos;
                            char c = html.charAt(pos);

                            while (c != '>' && !Character.isWhitespace(c)) {
                                c = html.charAt(++pos);
                            }

                            if (html.charAt(pos - 1) == '/') {
                                pos--;
                            }
                            child.attr(attr, html.substring(start, pos));
                        }
                    }
                    nextSpace();
                }

                // close start element
                if (next(">").length() == 0 && Arrays.binarySearch(empties, name) < 0) {
                    // container element
                    if (0 <= Arrays.binarySearch(data, name)) {
                        // text data only element - add contents as text

                        // At first, we find the end element, but some html provides crazy
                        // element pair like <div></DIV>. So we should search by case-insensitive,
                        // don't use find("</" + name + ">").
                        int start = pos;

                        next("</");
                        while (!nextName().equals(name)) {
                            next("</");
                        }
                        next(">");

                        child.text(html.substring(start, pos - 3 - name.length()));
                        // don't update current element
                    } else {
                        // mixed element
                        // update current element into child
                        xml = child;
                    }
                } else {
                    // empty element

                    // check encoding in meta element
                    if (detectable && name.equals("meta")) {
                        String value = child.attr("charset");

                        if (value.length() == 0 && child.attr("http-equiv").equalsIgnoreCase("content-type")) {
                            value = child.attr("content");
                        }

                        if (value.length() != 0) {
                            detectable = false;

                            try {
                                int index = value.lastIndexOf('=');
                                Charset detect = Charset.forName(index == -1 ? value : value.substring(index + 1));

                                if (!encoding.equals(detect)) {
                                    // reset and parse again if the current encoding is wrong
                                    return parse(raw, detect);
                                }
                            } catch (Exception e) {
                                // unknown encoding name
                            }
                        }
                    }
                    // don't update current element
                }
            } else {
                // =====================
                // Text
                // =====================
                String text = next("<");

                if (xml.nodes.size() == 1 && xml.nodes.get(0).getNodeType() == Node.DOCUMENT_NODE) {
                    // Ignore the text node directly below the document.
                } else {
                    xml.append(xml.doc.createTextNode(text));
                }

                // If the current position is not end of document, we should continue to parse
                // next elements. So rollback position(1) for the "<" next start element.
                if (pos != html.length()) {
                    pos--;
                }
            }
        }

        this.nodes = convert(xml.doc.getChildNodes());
        return this;
    }

    /**
     * Helper method for the HTML parser. Checks if the HTML string at the current
     * parsing position ({@link #pos}) starts with the given {@code sequence}.
     * If it matches, {@link #pos} is advanced by the length of the sequence.
     *
     * @param sequence The string sequence to test for.
     * @return {@code true} if a match is found and {@link #pos} is advanced,
     *         {@code false} otherwise.
     */
    private boolean test(String until) {
        if (html.startsWith(until, pos)) {
            pos += until.length();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Helper method for the HTML parser. Consumes characters from the HTML string
     * starting at the current parsing position ({@link #pos}) until the specified
     * {@code terminator} sequence is encountered.
     * {@link #pos} is advanced past the {@code terminator}.
     *
     * @param terminator The string sequence that marks the end of consumption.
     * @return The consumed string, excluding the {@code terminator}.
     *         If the terminator is not found, returns the rest of the HTML string.
     */
    private String next(String until) {
        int start = pos;
        int index = html.indexOf(until, pos);

        if (index == -1) {
            // until last
            pos = html.length();
        } else {
            // until matched sequence
            pos = index + until.length();
        }
        return html.substring(start, pos - until.length());
    }

    /**
     * Helper method for the HTML parser. Consumes an XML/HTML name (tag name or attribute name)
     * from the HTML string starting at the current parsing position ({@link #pos}).
     * A name consists of letters, digits, '_', ':', or '-'.
     * {@link #pos} is advanced past the consumed name.
     * Names are converted to lowercase.
     *
     * @return The consumed name, converted to lowercase.
     */
    private String nextName() {
        int start = pos;
        char c = html.charAt(pos);

        while (Character.isLetterOrDigit(c) || c == '_' || c == ':' || c == '-') {
            c = html.charAt(++pos);
        }
        return html.substring(start, pos).toLowerCase();
    }

    /**
     * Helper method for the HTML parser. Consumes whitespace characters from the HTML string
     * starting at the current parsing position ({@link #pos}).
     * {@link #pos} is advanced past the consumed whitespace.
     */
    private void nextSpace() {
        while (pos < html.length() && Character.isWhitespace(html.charAt(pos))) {
            pos++;
        }
    }
}