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

import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.LinkedList;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

/**
 * @version 2012/11/17 14:36:53
 */
class XMLReader {

    private static final String[] empties = {"area", "base", "br", "col", "command", "device", "embed", "frame", "hr",
            "img", "input", "keygen", "link", "meta", "param", "source", "track", "wbr"};

    private static final String[] datas = {"noframes", "script", "style", "textarea", "title"};

    /** The raw text data. */
    private final byte[] row;

    /** The document encoding. */
    private Charset encoding = I.$encoding;

    /** The element stack manager. */
    private LinkedList<Node> stack;

    /** The root document. */
    private Document doc;

    /** The encoded text data. */
    private String html;

    /** The current parsing position. */
    private int pos;

    XMLReader(String uri) {
        try {
            // connect by URL
            URLConnection connection = new URL(uri).openConnection();
            connection.connect();

            // detect encoding from http header
            encoding = detect(connection.getHeaderField("Content-Type"));

            // read actual data
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            I.copy(connection.getInputStream(), output, true);
            row = output.toByteArray();
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }

    /**
     * <p>
     * Parsr html and build {@link Document} tree.
     * </p>
     * 
     * @return
     */
    Document parse() {
        // initialize test
        html = new String(row, 0, row.length, encoding);
        pos = 0;

        // intialize document
        doc = I.dom.newDocument();

        // initialize manager
        stack = new LinkedList();
        stack.add(doc);

        // start parsing
        while (html.length() != pos) {
            // match start tag
            if (html.length() - pos >= 2 && html.charAt(pos) == '<' && Character.isLetter(html.charAt(pos + 1))) {
                pos++; // <
                String name = consumeName().toLowerCase();
                consumeWhitespace();

                Element child = doc.createElement(name);

                while (!matche("<") && !matche("/>") && !matche(">") && html.length() != pos) {
                    consumeWhitespace();
                    Attr attr = doc.createAttribute(consumeName());
                    consumeWhitespace();

                    if (matcheInto("=")) {
                        consumeWhitespace();

                        if (matcheInto("'")) {
                            attr.setValue(chompTo("'"));
                        } else if (matcheInto("\"")) {
                            attr.setValue(chompTo("\""));
                        } else {
                            StringBuilder builder = new StringBuilder();
                            // no ' or " to look for, so scan to end tag or space (or end of stream)
                            while (!matche("<") && !matche("/>") && !matche(">")) {
                                builder.append(html.charAt(pos++));
                            }
                            attr.setValue(builder.toString());
                        }
                        consumeWhitespace();
                    }
                    child.setAttributeNode(attr);
                }
                matcheInto(">");

                stack.getLast().appendChild(child);

                if (Arrays.binarySearch(empties, name) < 0) {
                    stack.addLast(child);

                    // pc data only tags (textarea, script): chomp to end tag, add content as text
                    // node
                    if (0 <= Arrays.binarySearch(datas, name)) {
                        child.appendChild(doc.createTextNode(chompTo("</" + name)));
                        chompTo(">");
                        stack.pollLast();
                    }
                } else {
                    // confirm character encoding
                    if (name.equals("meta")) {
                        if (child.hasAttribute("charset")) {
                            Charset detect = Charset.forName(child.getAttribute("charset"));

                            if (!detect.equals(encoding)) {
                                // reset
                                encoding = detect;
                                return parse();
                            }
                        }

                        if (child.getAttribute("http-equiv").equalsIgnoreCase("content-type")) {
                            Charset detect = detect(child.getAttribute("content"));

                            if (!detect.equals(encoding)) {
                                // reset
                                encoding = detect;
                                return parse();
                            }
                        }
                    }

                    chompTo("/");
                    chompTo(">");
                }

            } else if (matcheInto("</")) {
                String name = chompTo(">").trim();

                if (name.length() != 0) {
                    stack.pollLast();
                }
            } else if (html.startsWith("<!--", pos)) {
                // ignore comment node
                chompTo("->");
            } else if (matcheInto("<![CDATA[")) {
                stack.getLast().appendChild(doc.createCDATASection(chompTo("]]>")));
            } else if (html.startsWith("<?", pos) || html.startsWith("<!", pos)) {
                // ignore processing instruction and doctype declaration
                chompTo(">");
            } else {
                Text textNode = null;
                // special case: handle string like "hello < there". first char will be "<", because
                // of matchStartTag
                if (html.charAt(pos) == '<') {
                    if (html.length() != pos) pos++;
                    textNode = doc.createTextNode("<");
                } else {
                    textNode = doc.createTextNode(consumeTo("<"));
                }

                Node node = stack.getLast();

                if (node.getNodeType() != Node.DOCUMENT_NODE) {
                    node.appendChild(textNode);
                }
            }
        }
        return doc;
    }

    /**
     * Pulls a string off the queue, up to but exclusive of the match sequence, or to the queue
     * running out.
     * 
     * @param seq String to end on (and not include in return, but leave on queue). <b>Case
     *            sensitive.</b>
     * @return The matched data consumed from queue.
     */
    private String consumeTo(String seq) {
        int index = html.indexOf(seq, pos);

        if (index != -1) {
            String consumed = html.substring(pos, index);
            pos += consumed.length();
            return consumed;
        } else {
            int start = pos;
            pos = html.length();
            return html.substring(start, pos);
        }
    }

    /**
     * Consume an tag name off the queue (word or :, _, -)
     * 
     * @return tag name
     */
    private String consumeName() {
        int start = pos;

        while (html.length() != pos && (Character.isLetterOrDigit(html.charAt(pos)) || matche("_") || matche(":") || matche("-"))) {
            pos++;
        }
        return html.substring(start, pos);
    }

    /**
     * Pulls the next run of whitespace characters of the queue.
     */
    private void consumeWhitespace() {
        while (html.length() != pos && Character.isWhitespace(html.charAt(pos))) {
            pos++;
        }
    }

    /**
     * Pulls a string off the queue (like consumeTo), and then pulls off the matched string (but
     * does not return it).
     * <p>
     * If the queue runs out of characters before finding the seq, will return as much as it can
     * (and queue will go isEmpty() == true).
     * 
     * @param seq String to match up to, and not include in return, and to pull off queue. <b>Case
     *            sensitive.</b>
     * @return Data matched from queue.
     */
    private String chompTo(String seq) {
        String data = consumeTo(seq);
        matcheInto(seq);
        return data;
    }

    /**
     * Tests if the next characters on the queue match the sequence. Case insensitive.
     * 
     * @param seq String to check queue for.
     * @return true if the next characters match.
     */
    private boolean matche(String seq) {
        return html.regionMatches(true, pos, seq, 0, seq.length());
    }

    /**
     * Tests if the queue matches the sequence (as with match), and if they do, removes the matched
     * string from the queue.
     * 
     * @param seq String to search for, and if found, remove from queue.
     * @return true if found and removed, false if not found.
     */
    private boolean matcheInto(String seq) {
        if (matche(seq)) {
            pos += seq.length();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Parse out a charset from a content type header. If the charset is not supported, returns null
     * (so the default will kick in.)
     * 
     * @param value e.g. "text/html; charset=EUC-JP"
     * @return "EUC-JP", or null if not found. Charset is trimmed and uppercased.
     */
    private Charset detect(String value) {
        if (value == null || value.length() == 0) {
            return encoding;
        }

        int index = value.lastIndexOf('=');

        return Charset.forName(index == -1 ? value : value.substring(index + 1));
    }
}
