/*
 * Copyright (C) 2012 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss;

import static javax.xml.XMLConstants.*;

import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ContentHandler;

import com.sun.org.apache.xerces.internal.util.DOMUtil;
import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XML11Serializer;
import com.sun.org.apache.xml.internal.utils.TreeWalker;

/**
 * @version 2012/11/18 22:33:02
 */
public class XML implements Iterable<XML> {

    /**
     * Original pattern.
     * 
     * <pre>
     * ([>~+\-, ]*)?((?:(?:\w+|\*)\|)?(?:\w+(?:\\.\w+)*|\*))?(?:#(\w+))?((?:\.\w+)*)(?:\[\s?([\w:]+)(?:\s*([=~^$*|])?=\s*["]([^"]*)["])?\s?\])?(?::([\w-]+)(?:\((odd|even|(\d*)(n)?(?:\+(\d+))?|(?:.*))\))?)?
     * </pre>
     */
    private static final Pattern SELECTOR = Pattern.compile("([>~+\\-, ]*)?((?:(?:\\w+|\\*)\\|)?(?:\\w+(?:\\\\.\\w+)*|\\*))?(?:#(\\w+))?((?:\\.\\w+)*)(?:\\[\\s?([\\w:]+)(?:\\s*([=~^$*|])?=\\s*[\"]([^\"]*)[\"])?\\s?\\])?(?::([\\w-]+)(?:\\((odd|even|(\\d*)(n)?(?:\\+(\\d+))?|(?:.*))\\))?)?");

    /** The cache for compiled selectors. */
    private static final Map<String, XPathExpression> selectors = new ConcurrentHashMap();

    /** The xpath evaluator. */
    private static final XPath xpath;

    // initialization
    static {
        try {
            xpath = XPathFactory.newInstance().newXPath();

        } catch (Exception e) {
            throw I.quiet(e);
        }
    }

    /** The current document. */
    Document doc;

    /** The current node set. */
    private final List<Node> nodes;

    /**
     * <p>
     * From node set.
     * </p>
     * 
     * @param nodes
     */
    XML(Document doc, List nodes) {
        this.doc = doc;
        this.nodes = nodes;
    }

    /**
     * <p>
     * Insert content, specified by the parameter, to the end of each element in the set of matched
     * elements.
     * </p>
     * 
     * @param xml
     * @return
     */
    public XML append(Object xml) {
        Node n = convert(I.xml(xml));

        for (Node node : nodes) {
            node.appendChild(n.cloneNode(true));
        }

        // API definition
        return this;
    }

    /**
     * <p>
     * Insert content, specified by the parameter, to the beginning of each element in the set of
     * matched elements.
     * </p>
     * 
     * @param xml
     * @return
     */
    public XML prepend(Object xml) {
        Node n = convert(I.xml(xml));

        for (Node node : nodes) {
            node.insertBefore(n.cloneNode(true), node.getFirstChild());
        }

        // API definition
        return this;
    }

    /**
     * <p>
     * Insert content, specified by the parameter, before each element in the set of matched
     * elements.
     * </p>
     * 
     * @param xml
     * @return
     */
    public XML before(Object xml) {
        Node n = convert(I.xml(xml));

        for (Node node : nodes) {
            node.getParentNode().insertBefore(n.cloneNode(true), node);
        }

        // API definition
        return this;
    }

    /**
     * <p>
     * Insert content, specified by the parameter, after each element in the set of matched
     * elements.
     * </p>
     * 
     * @param xml
     * @return
     */
    public XML after(Object xml) {
        Node n = convert(I.xml(xml));

        for (Node node : nodes) {
            node.getParentNode().insertBefore(n.cloneNode(true), node.getNextSibling());
        }

        // API definition
        return this;
    }

    /**
     * <p>
     * Remove all child nodes of the set of matched elements from the DOM.
     * </p>
     * 
     * @return
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
     * it.
     * </p>
     * 
     * @return
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
     * Wrap an HTML structure around each element in the set of matched elements.
     * </p>
     * 
     * @param xml
     * @return
     */
    public XML wrap(Object xml) {
        XML element = I.xml(xml);

        for (XML e : this) {
            e.wrapAll(element);
        }

        // API definition
        return this;
    }

    /**
     * <p>
     * Wrap an HTML structure around all elements in the set of matched elements.
     * </p>
     * 
     * @param xml
     * @return
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
     * The .clone() method performs a deep copy of the set of matched elements, meaning that it
     * copies the matched elements as well as all of their descendant elements and text nodes. When
     * used in conjunction with one of the insertion methods, .clone() is a convenient way to
     * duplicate elements on a page.
     * </p>
     * {@inheritDoc}
     */
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
     * 
     * @return
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
     * 
     * @param text
     * @return
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
     * Get the value of an attribute for the first element in the set of matched elements.
     * </p>
     * 
     * @param name An attribute name.
     * @return
     */
    public String attr(String name) {
        return ((org.w3c.dom.Element) nodes.iterator().next()).getAttribute(name);
    }

    /**
     * <p>
     * Set one or more attributes for the set of matched elements.
     * </p>
     * 
     * @param name
     * @param value
     * @return
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
     * </p>
     * 
     * @param names
     * @return
     */
    public XML addClass(String names) {
        for (XML e : this) {
            String value = " ".concat(e.attr("class")).concat(" ");

            for (String name : names.split(" ")) {
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
     * of matched elements. If no class names are specified in the parameter, all classes will be
     * removed. More than one class may be removed at a time, separated by a space, from the set of
     * matched elements.
     * </p>
     * 
     * @param names
     * @return
     */
    public XML removeClass(String names) {
        for (XML e : this) {
            String value = " ".concat(e.attr("class")).concat(" ");

            for (String name : names.split(" ")) {
                value = value.replace(" ".concat(name).concat(" "), " ");
            }
            e.attr("class", value.trim());
        }

        // API definition
        return this;
    }

    /**
     * <p>
     * Add or remove one class from each element in the set of matched elements, depending on either
     * the class's presence or the value of the switch argument.
     * </p>
     * 
     * @param name
     * @return
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
     * @param name
     * @return
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
     * <p>
     * Reduce the set of matched elements to the first in the set.
     * </p>
     * 
     * @return
     */
    public XML first() {
        return nodes.isEmpty() ? this : new XML(doc, nodes.subList(0, 1));
    }

    /**
     * <p>
     * Reduce the set of matched elements to the final one in the set.
     * </p>
     * 
     * @return
     */
    public XML last() {
        return nodes.isEmpty() ? this : new XML(doc, nodes.subList(nodes.size() - 1, nodes.size()));
    }

    /**
     * <p>
     * Get all following siblings of each element up to but not including the element matched by the
     * given selector.
     * </p>
     * 
     * @param selector CSS selector.
     * @return A set of matched elements.
     */
    public XML nextUntil(String selector) {
        CopyOnWriteArrayList list = new CopyOnWriteArrayList();

        for (XML xml : this) {
            XML until = xml.find("+".concat(selector));
            Node node = xml.nodes.get(0);
            Node untilNode = until.size() == 0 ? null : until.nodes.get(0);

            do {
                // collect matching node
                list.addIfAbsent(node);

                // search next sibling node
                node = DOMUtil.getNextSiblingElement(node);
            } while (node != untilNode);
        }
        return new XML(doc, list);
    }

    /**
     * <p>
     * Append the given xml as child element and traverse into them.
     * </p>
     * 
     * @param xml A xml element.
     * @return A created child elements.
     */
    public XML child(Object xml) {
        ArrayList list = new ArrayList();
        Node child = convert(I.xml(xml));

        for (Node node : nodes) {
            Node copy = child.cloneNode(true);
            list.addAll(convert(copy.getChildNodes()));
            node.appendChild(copy);
        }
        return new XML(doc, list);

        // Don't use the below code because xpath is too slow.
        // return append(xml).find(">*:last-child");
    }

    /**
     * <p>
     * Get the children of each element in the current set of matched elements.
     * </p>
     * 
     * @return A set of children elements.
     */
    public XML children() {
        CopyOnWriteArrayList list = new CopyOnWriteArrayList();

        for (Node node : nodes) {
            list.addAllAbsent(convert(node.getChildNodes()));
        }
        return new XML(doc, list);
    }

    /**
     * <p>
     * Get the parent of each element in the current set of matched elements.
     * </p>
     * 
     * @return A set of parent elements.
     */
    public XML parent() {
        CopyOnWriteArrayList list = new CopyOnWriteArrayList();

        for (Node node : nodes) {
            Node parent = node.getParentNode();

            if (parent.getNodeType() == Node.ELEMENT_NODE) {
                list.addIfAbsent(parent);
            } else {
                list.addIfAbsent(node);
            }
        }
        return new XML(doc, list);
    }

    /**
     * <p>
     * Get the descendants of each element in the current set of matched elements, filtered by a css
     * selector.
     * </p>
     * 
     * @param selector A string containing a css selector expression to match elements against.
     * @return
     */
    public XML find(String selector) {
        XPathExpression xpath = compile(selector);
        CopyOnWriteArrayList<Node> result = new CopyOnWriteArrayList();

        try {
            for (Node node : nodes) {
                result.addAll(convert((NodeList) xpath.evaluate(node, XPathConstants.NODESET)));
            }
            return new XML(doc, result);
        } catch (XPathExpressionException e) {
            throw I.quiet(e);
        }
    }

    /**
     * <p>
     * Return size of the current node set.
     * </p>
     * 
     * @return
     */
    public int size() {
        return nodes.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<XML> iterator() {
        List<XML> elements = new ArrayList();

        for (Node node : nodes) {
            elements.add(new XML(doc, Collections.singletonList(node)));
        }
        return elements.iterator();
    }

    public void to(ContentHandler handler) {
        try {
            handler.startDocument();
            TreeWalker walker = new TreeWalker(handler);

            for (Node node : nodes) {
                walker.traverseFragment(node);
            }
            handler.endDocument();
        } catch (Exception e) {
            throw I.quiet(e);
        }

    }

    /**
     * <p>
     * Write this elements to the specified output.
     * </p>
     * 
     * @param output A output channel.
     */
    public void to(Appendable output) {
        OutputFormat format = new OutputFormat();
        format.setIndent(2);
        format.setLineWidth(0);
        format.setOmitXMLDeclaration(true);

        XML11Serializer serializer = new XML11Serializer(output instanceof Writer ? (Writer) output
                : new XMLWriter(output), format);

        to(serializer);
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
     * <p>
     * Helper method to convert {@link XML} to single {@link Node}.
     * </p>
     * 
     * @param xml
     * @return
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
     * <p>
     * Helper method to convert {@link NodeList} to single {@link List}.
     * </p>
     * 
     * @param xml
     * @return
     */
    static List convert(NodeList list) {
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
     * <p>
     * Compile and cache the specified selector.
     * </p>
     * 
     * @param selector A css selector.
     * @return A compiled xpath expression.
     */
    private static XPathExpression compile(String selector) {
        // check cache
        XPathExpression compiled = selectors.get(selector);

        if (compiled == null) {
            try {
                // compile actually
                compiled = xpath.compile(convert(selector));

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
     * <p>
     * Helper method to convert css selector to xpath.
     * </p>
     * 
     * @param selector A css selector.
     * @return A xpath.
     */
    private static String convert(String selector) {
        StringBuilder xpath = new StringBuilder();
        Matcher matcher = SELECTOR.matcher(selector.trim());

        while (matcher.find()) {
            // =================================================
            // Combinators
            // =================================================
            String suffix = null;
            String match = matcher.group(1);
            boolean contextual = matcher.start() == 0;

            if (match.length() == 0) {
                // no combinator
                if (contextual) {
                    // first selector
                    xpath.append("descendant::");
                } else {
                    break; // finish parsing
                }
            } else {
                match = match.trim();

                if (match.length() == 0) {
                    // Descendant combinator
                    xpath.append("//");
                } else {
                    switch (match.charAt(0)) {
                    case '>': // Child combinator
                        xpath.append('/');
                        break;

                    case '~': // General sibling combinator
                        xpath.append("/following-sibling::");
                        break;

                    case '+': // Adjacent sibling combinator
                        xpath.append("/following-sibling::");
                        suffix = "[1]";
                        break;

                    case '-': // Adjacent previous sibling combinator (EXTENSION)
                        xpath.append("/preceding-sibling::");
                        suffix = "[1]";
                        break;

                    case ',': // selector separator
                        xpath.append("|descendant::");
                        break;
                    }

                    if (contextual) {
                        xpath.delete(0, 1);
                    }
                }
            }

            // =================================================
            // Type (Universal) Selector
            // =================================================
            match = matcher.group(2);

            if (match == null || match.equals("*")) {
                xpath.append("*");
            } else {
                xpath.append("*[name()='").append(match.replace('|', ':').replaceAll("\\\\", "")).append("']");
            }

            // =================================================
            // ID Selector
            // =================================================
            match = matcher.group(3);

            if (match != null) {
                xpath.append("[@id='" + match + "']");
            }

            // =================================================
            // Class Selector
            // =================================================
            match = matcher.group(4);

            if (match != null && match.length() != 0) {
                for (String className : match.substring(1).split("\\.")) {
                    xpath.append("[contains(concat(' ',normalize-space(@class),' '),' " + className + " ')]");
                }
            }

            // =================================================
            // Attribute Selector
            // =================================================
            match = matcher.group(5);

            if (match != null) {
                String value = matcher.group(7);

                if (value == null) {
                    // [att]
                    //
                    // Represents an element with the att attribute, whatever the value
                    // of the attribute.
                    xpath.append("[@").append(match).append("]");
                } else {
                    String type = matcher.group(6);

                    if (type == null) {
                        // [att=val]
                        //
                        // Represents an element with the att attribute whose value
                        // is exactly "val".
                        xpath.append("[@").append(match).append("='").append(value).append("']");
                    } else {
                        switch (type.charAt(0)) {
                        case '~':
                            // [att~=val]
                            //
                            // Represents an element with the att attribute whose value is a
                            // whitespace-separated list of words, one of which is exactly
                            // "val". If "val" contains whitespace, it will never represent
                            // anything (since the words are separated by spaces). Also if "val"
                            // is the empty string, it will never represent anything.
                            xpath.append("[contains(concat(' ',@")
                                    .append(match)
                                    .append(",' '),' ")
                                    .append(value)
                                    .append(" ')]");
                            break;

                        case '*':
                            // [att*=val]
                            //
                            // Represents an element with the att attribute whose value contains
                            // at least one instance of the substring "val". If "val" is the
                            // empty string then the selector does not represent anything.
                            xpath.append("[contains(@").append(match).append(",'").append(value).append("')]");
                            break;

                        case '^':
                            // [att^=val]
                            //
                            // Represents an element with the att attribute whose value begins
                            // with the prefix "val". If "val" is the empty string then the
                            // selector does not represent anything.
                            xpath.append("[starts-with(@").append(match).append(",'").append(value).append("')]");
                            break;

                        case '$':
                            // [att$=val]
                            //
                            // Represents an element with the att attribute whose value ends
                            // with the suffix "val". If "val" is the empty string then the
                            // selector does not represent anything.
                            xpath.append("[substring(@")
                                    .append(match)
                                    .append(", string-length(@")
                                    .append(match)
                                    .append(") - string-length('")
                                    .append(value)
                                    .append("') + 1) = '")
                                    .append(value)
                                    .append("']");
                            break;

                        case '|':
                            // [att|=val]
                            //
                            // Represents an element with the att attribute, its value either
                            // being exactly "val" or beginning with "val" immediately followed
                            // by "-" (U+002D).
                            break;
                        }
                    }
                }
            }

            // =================================================
            // Structural Pseudo Classes Selector
            // =================================================
            match = matcher.group(8);

            if (match != null) {
                switch (match.hashCode()) {
                case -947996741: // only-child
                    xpath.append("[count(../*) = 1]");
                    break;

                case 1455900751: // only-of-type
                    xpath.append("[count(../").append(matcher.group(2)).append(")=1]");
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
                    xpath.append('(');

                    String sub = convert(matcher.group(9));

                    if (sub.startsWith("descendant::")) {
                        sub = sub.replace("descendant::", "descendant-or-self::");
                    }
                    xpath.append(sub).append(")]");
                    break;

                case -995424086: // parent
                    xpath.append("/..");
                    break;

                case 3506402: // root
                    xpath.delete(0, xpath.length()).append("/*");
                    break;

                case -567445985: // contains
                    xpath.append("[contains(text(),'").append(matcher.group(9)).append("')]");
                    break;

                case 835834661: // last-child
                    xpath.append("[not(following-sibling::*)]");
                    break;

                case -2136991809: // first-child
                case 1292941139: // first-of-type
                case 2025926969: // last-of-type
                case -1754914063: // nth-child
                case -1629748624: // nth-last-child
                case -897532411: // nth-of-type
                case -872629820: // nth-last-of-type
                    String coefficient = matcher.group(10);
                    String remainder = matcher.group(9);

                    if (remainder == null) {
                        // coefficient = null; // coefficient is already null
                        remainder = "1";
                    } else if (matcher.group(11) == null) {
                        coefficient = null;
                        // remainder = matcher.group(9); // remainder is already assigned

                        if (remainder.equals("even")) {
                            coefficient = "2";
                            remainder = "0";
                        } else if (remainder.equals("odd")) {
                            coefficient = "2";
                            remainder = "1";
                        }
                    } else {
                        // coefficient = matcher.group(10); // coefficient is already assigned
                        remainder = matcher.group(12);

                        if (remainder == null) {
                            remainder = "0";
                        }
                    }

                    xpath.append("[(count(");

                    if (match.contains("last")) {
                        xpath.append("following");
                    } else {
                        xpath.append("preceding");
                    }

                    xpath.append("-sibling::");

                    if (match.endsWith("child")) {
                        xpath.append("*");
                    } else {
                        xpath.append(matcher.group(2));
                    }
                    xpath.append(")+1)");

                    if (coefficient != null) {
                        if (coefficient.length() == 0) {
                            coefficient = "1";
                        }
                        xpath.append(" mod ").append(coefficient);
                    }
                    xpath.append('=').append(remainder).append("]");
                    break;
                }
            }

            if (suffix != null) {
                xpath.append(suffix);
            }
        }
        return xpath.toString();
    }
}
