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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.LinkedList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import kiss.I;

import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import com.sun.org.apache.xml.internal.utils.DOMBuilder;

/**
 * @version 2012/11/17 3:30:05
 */
public class Parser {

    /** The document builder. */
    private static final DocumentBuilder dom;

    static {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

            dom = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw I.quiet(e);
        }
    }

    public DOMBuilder builder = new DOMBuilder(dom.newDocument());

    private static final String SQ = "'";

    private static final String DQ = "\"";

    private static final Tag htmlTag = Tag.valueOf("html");

    private static final Tag bodyTag = Tag.valueOf("body");

    private final LinkedList<Node> stack;

    private final TokenQueue tq;

    private final Document doc;

    private Parser(String html) {
        stack = new LinkedList();
        tq = new TokenQueue(html);

        doc = dom.newDocument();
        stack.add(doc);
    }

    /**
     * Parse HTML into a Document.
     * 
     * @param html HTML to parse
     * @param baseUri base URI of document (i.e. original fetch location), for resolving relative
     *            URLs.
     * @return parsed Document
     */
    public static Document parse(String html) {
        Parser parser = new Parser(html);
        return parser.parse();
    }

    public static Document parseURL(String uri) {
        try {
            URL url = new URL(uri);
            InputStream input = url.openStream();
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            I.copy(input, output, true);
            byte[] bytes = output.toByteArray();

            String text = new String(bytes, 0, bytes.length, "UTF-8");
            return parse(text);
        } catch (Exception e) {
            throw I.quiet(e);
        }

    }

    private Document parse() {
        while (!tq.isEmpty()) {
            if (tq.matchesStartTag()) {
                parseStartTag();
            } else if (tq.matchesCS("</")) {
                parseEndTag();
            } else if (tq.matchesCS("<!--")) {
                parseComment();
            } else if (tq.matches("<![CDATA[")) {
                parseCdata();
            } else if (tq.matchesCS("<?") || tq.matchesCS("<!")) {
                parseXmlDecl();
            } else {
                parseTextNode();
            }
        }
        return (Document) stack.getFirst();
    }

    private void parseComment() {
        tq.consume("<!--");
        String data = tq.chompTo("->");

        if (data.endsWith("-")) // i.e. was -->
            data = data.substring(0, data.length() - 1);

        Comment comment = doc.createComment(data);
        last().appendChild(comment);
    }

    private void parseXmlDecl() {
        tq.consume("<");
        Character firstChar = tq.consume(); // <? or <!, from initial match.
        boolean procInstr = firstChar.toString().equals("!");
        String data = tq.chompTo(">");

        // ProcessingInstruction decl = doc.createProcessingInstruction();
        // XmlDeclaration decl = new XmlDeclaration(data, baseUri, procInstr);
        // last().appendChild(decl);
    }

    private void parseEndTag() {
        tq.consume("</");
        String tagName = tq.consumeTagName();
        tq.chompTo(">");

        if (tagName.length() != 0) {
            Tag tag = Tag.valueOf(tagName);

            popStackToClose(tag);
        }

    }

    private void parseStartTag() {
        tq.consume("<");
        String tagName = tq.consumeTagName();

        tq.consumeWhitespace();

        Element child = doc.createElement(tagName);
        Tag tag = Tag.valueOf(tagName);
        tag(child, tag);

        while (!tq.matchesAny("<", "/>", ">") && !tq.isEmpty()) {
            Attr attribute = parseAttribute();
            if (attribute != null) {
                child.setAttributeNode(attribute);
            }
        }

        boolean isEmptyElement = tag.isEmpty(); // empty element if empty tag (e.g. img) or
                                                // self-closed el (<div/>
        if (tq.matchChomp("/>")) { // close empty element or tag
            isEmptyElement = true;
            if (!tag.isKnownTag()) // if unknown and a self closed, allow it to be self closed on
                                   // output. this doesn't force all instances to be empty
                tag.setSelfClosing();
        } else {
            tq.matchChomp(">");
        }
        addChildToParent(child, isEmptyElement);

        // pc data only tags (textarea, script): chomp to end tag, add content as text node
        if (tag.isData()) {
            String data = tq.chompToIgnoreCase("</" + tagName);
            tq.chompTo(">");
            popStackToClose(tag);

            Node dataNode = doc.createTextNode(data); // data not encoded but raw (for " in
                                                      // script)
            child.appendChild(dataNode);
        }
    }

    private void addChildToParent(Element child, boolean isEmptyElement) {
        Tag childTag = tag(child);
        System.out.println("Before  " + last());
        Node parent = popStackToSuitableContainer(childTag);
        System.out.println(parent + "    " + child);
        parent.appendChild(child);

        if (!isEmptyElement) stack.addLast(child);
    }

    private Node popStackToSuitableContainer(Tag tag) {
        while (!stack.isEmpty()) {
            if (tag(last()).canContain(tag))
                return last();
            else
                stack.removeLast();
        }
        return null;
    }

    private Attr parseAttribute() {
        tq.consumeWhitespace();
        String key = tq.consumeAttributeKey();
        String value = "";
        tq.consumeWhitespace();
        if (tq.matchChomp("=")) {
            tq.consumeWhitespace();

            if (tq.matchChomp(SQ)) {
                value = tq.chompTo(SQ);
            } else if (tq.matchChomp(DQ)) {
                value = tq.chompTo(DQ);
            } else {
                StringBuilder valueAccum = new StringBuilder();
                // no ' or " to look for, so scan to end tag or space (or end of stream)
                while (!tq.matchesAny("<", "/>", ">") && !tq.matchesWhitespace() && !tq.isEmpty()) {
                    valueAccum.append(tq.consume());
                }
                value = valueAccum.toString();
            }
            tq.consumeWhitespace();
        }
        if (key.length() != 0) {
            Attr attr = doc.createAttribute(key);
            attr.setValue(value);
            return attr;
        } else {
            if (value.length() == 0) // no key, no val; unknown char, keep popping so not get stuck
                tq.advance();

            return null;
        }
    }

    private void parseTextNode() {
        Text textNode;
        // special case: handle string like "hello < there". first char will be "<", because of
        // matchStartTag
        if (tq.peek() == '<') {
            tq.advance();
            textNode = doc.createTextNode("<");
        } else {
            String text = tq.consumeTo("<");
            textNode = doc.createTextNode(text);
        }

        if (last().getNodeType() != Node.DOCUMENT_NODE) {
            last().appendChild(textNode);
        }
    }

    private void parseCdata() {
        tq.consume("<![CDATA[");
        String rawText = tq.chompTo("]]>");
        CDATASection textNode = doc.createCDATASection(rawText);
        last().appendChild(textNode);
    }

    private Node popStackToClose(Tag tag) {
        // first check to see if stack contains this tag; if so pop to there, otherwise ignore
        int counter = 0;
        Node elToClose = null;
        for (int i = stack.size() - 1; i > 0; i--) {
            counter++;
            Node el = stack.get(i);
            Tag elTag = tag(el);
            if (elTag.equals(bodyTag) || elTag.equals(htmlTag)) { // once in body, don't close past
                System.out.println(tag); // body
                break;
            } else if (elTag.equals(tag)) {

                elToClose = el;
                break;
            }
        }
        if (elToClose != null) {
            for (int i = 0; i < counter; i++) {
                stack.removeLast();
            }
        }
        return elToClose;
    }

    private Node last() {
        return stack.getLast();
    }

    private Tag tag(Node node) {
        Tag tag = (Tag) node.getUserData(Tag.class.getName());

        if (tag == null) {
            return Tag.valueOf("");
        } else {
            return tag;
        }
    }

    private void tag(Node node, Tag tag) {
        node.setUserData(Tag.class.getName(), tag, null);
    }
}
