/*
 * Copyright (C) 2012 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.xml;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import kiss.I;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * @version 2012/02/06 16:24:40
 */
public class Element implements Iterable<Element> {

    /**
     * Original pattern.
     * 
     * <pre>
     * ([>+~ ])?(\w+|\*)?(#(\w+))?((\.\w+)*)(\[\s?(\w+)(\s?([=~^$*|])?=\s?["]([^"]*)["])?\s?\])?(:([\w-]+)\((.*)\))?
     * </pre>
     */
    private static final Pattern SELECTOR = Pattern.compile("([>+~ ])?(\\w+|\\*)?(#(\\w+))?((\\.\\w+)*)(\\[\\s?(\\w+)(\\s?([=~^$*|])?=\\s?[\"]([^\"]*)[\"])?\\s?\\])?(:([\\w-]+)\\((.*)\\))?");

    /** The cache for compiled selectors. */
    private static final Map<String, XPathExpression> selectors = new ConcurrentHashMap();

    /** The document builder. */
    private static DocumentBuilder DOM;

    /** The xpath evaluator. */
    private static XPath XPATH;

    // initialization
    static {
        try {
            DOM = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            XPATH = XPathFactory.newInstance().newXPath();
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }

    public static final Element $(String xml) {
        return new Element(xml);
    }

    /** The current node set. */
    private final Set<Node> nodes;

    /**
     * <p>
     * From xml text.
     * </p>
     * 
     * @param xml
     */
    private Element(String xml) {
        try {
            nodes = Collections.singleton(DOM.parse(new InputSource(new StringReader(xml))).getFirstChild());
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }

    /**
     * <p>
     * From node set.
     * </p>
     * 
     * @param nodes
     */
    private Element(Set<Node> nodes) {
        this.nodes = nodes;
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
    public Element find(String selector) {
        XPathExpression xpath = compile(selector);
        Set<Node> result = new HashSet();

        try {
            for (Node node : nodes) {
                NodeList list = (NodeList) xpath.evaluate(node, XPathConstants.NODESET);

                for (int i = 0; i < list.getLength(); i++) {
                    result.add(list.item(i));
                }
            }

            return new Element(result);
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
    public Iterator<Element> iterator() {
        List<Element> elements = new ArrayList();

        for (Node node : nodes) {
            elements.add(new Element(Collections.singleton(node)));
        }
        return elements.iterator();
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
            // normalize space
            String pattern = selector.replaceAll("\\s+", " ").replaceAll(" ([>+~]) ", "$1");

            // compile css selector to xpath
            StringBuilder xpath = new StringBuilder();
            Matcher matcher = SELECTOR.matcher(pattern);

            while (matcher.find()) {
                // =================================================
                // Combinators
                // =================================================
                String suffix = null;
                String match = matcher.group(1);

                if (match == null) {
                    // no combinator
                    if (matcher.start() == 0) {
                        // first selector
                        xpath.append("descendant::");
                    } else {
                        break; // finish parsing
                    }
                } else {
                    switch (match.charAt(0)) {
                    case '>': // Child combinator
                        xpath.append('/');
                        break;

                    case ' ': // Descendant combinator
                        xpath.append("//");
                        break;

                    case '~': // General sibling combinator
                        xpath.append("/following-sibling::");
                        break;

                    case '+': // Adjacent sibling combinator
                        xpath.append("/following-sibling::*[1][self::");
                        suffix = "]";
                        break;
                    }
                }

                // =================================================
                // Type (Universal) Selector
                // =================================================
                match = matcher.group(2);

                if (match == null) {
                    xpath.append("*");
                } else {
                    xpath.append(match);
                }

                if (suffix != null) {
                    xpath.append(suffix);
                }

                // =================================================
                // ID Selector
                // =================================================
                match = matcher.group(4);

                if (match != null) {
                    xpath.append("[@id='" + match + "']");
                }

                // =================================================
                // Class Selector
                // =================================================
                match = matcher.group(5);

                if (match != null && match.length() != 0) {
                    for (String className : match.substring(1).split("\\.")) {
                        xpath.append("[contains(concat(' ',normalize-space(@class),' '),' " + className + " ')]");
                    }
                }

                // =================================================
                // Attribute Selector
                // =================================================
                match = matcher.group(8);

                if (match != null) {
                    String value = matcher.group(11);

                    if (value == null) {
                        // [att]
                        //
                        // Represents an element with the att attribute, whatever the value
                        // of the attribute.
                        xpath.append("[@").append(match).append("]");
                    } else {
                        String type = matcher.group(10);

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
            }

            try {
                // compile actually
                compiled = XPATH.compile(xpath.toString());

                // cache it
                selectors.put(selector, compiled);
            } catch (XPathExpressionException e) {
                throw I.quiet(e);
            }
        }

        // API definition
        return compiled;
    }
}
