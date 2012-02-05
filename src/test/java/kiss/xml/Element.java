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
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import kiss.I;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * @version 2012/02/05 17:07:25
 */
public class Element implements Iterable<Element> {

    /**
     * Original pattern.
     * 
     * <pre>
     * ([>+~ ])?(\w+|\*)?(#(\w+))?((\.\w+)*)(\[(\w+)\s?([=~^$*|])?=\s?["]([^"]*)["]\])?(:([\w-]+)\((.*)\))?
     * </pre>
     */
    private static final Pattern Selector = Pattern.compile("([>+~ ])?(\\w+|\\*)?(#(\\w+))?((\\.\\w+)*)(\\[(\\w+)\\s?([=~^$*|])?=\\s?[\"]([^\"]*)[\"]\\])?(:([\\w-]+)\\((.*)\\))?");

    private static DocumentBuilder dom;

    private static XPathFactory XPath;

    static {
        try {
            dom = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            XPath = XPathFactory.newInstance();
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }

    public static final Element $(String xml) {
        return new Element(xml);
    }

    private List<Node> nodes = new ArrayList();

    private Element(String xml) {
        try {
            nodes.add(dom.parse(new InputSource(new StringReader(xml))).getDocumentElement());
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }

    private Element(List<Node> nodes) {
        this.nodes = nodes;
    }

    public Element find(String selector) {
        selector = selector.replaceAll("\\s+", " ").replaceAll(" ([>+~]) ", "$1");

        StringBuilder xpath = new StringBuilder();
        Matcher matcher = Selector.matcher(selector);

        while (matcher.find()) {
            // connector
            String suffix = null;
            String match = matcher.group(1);

            if (match == null) {
                if (matcher.start() == 0) {
                    xpath.append("descendant::");
                } else {
                    break;
                }
            } else {
                switch (match.charAt(0)) {
                case '>':
                    xpath.append('/');
                    break;

                case '+':
                    xpath.append("/following-sibling::*[1][self::");
                    suffix = "]";
                    break;

                case '~':
                    break;

                case ' ':
                    xpath.append("//");
                    break;
                }
            }

            // type
            match = matcher.group(2);

            if (match == null) {
                xpath.append("*");
            } else {
                xpath.append(match);
            }

            if (suffix != null) {
                xpath.append(suffix);
            }

            // #id
            match = matcher.group(4);

            if (match != null) {
                xpath.append("[@id='" + match + "']");
            }

            // .class
            match = matcher.group(5);

            if (match != null && match.length() != 0) {
                for (String className : match.substring(1).split("\\.")) {
                    xpath.append("[contains(concat(' ',normalize-space(@class),' '),' " + className + " ')]");
                }
            }

        }

        System.out.println(xpath);
        List<Node> nodes = new ArrayList();

        try {
            for (Node node : this.nodes) {
                NodeList list = (NodeList) XPath.newXPath().evaluate(xpath.toString(), node, XPathConstants.NODESET);

                for (int i = 0; i < list.getLength(); i++) {
                    nodes.add(list.item(i));
                }
            }

            System.out.println(nodes.size());

            return new Element(nodes);
        } catch (XPathExpressionException e) {
            throw I.quiet(e);
        }
    }

    public int size() {
        return nodes.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<Element> iterator() {
        return null;
    }
}
