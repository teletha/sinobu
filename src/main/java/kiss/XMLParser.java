/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss;

import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * @version 2017/03/19 17:02:07
 */
class XMLParser {

    // =======================================================================
    // HTML Parser for building XML
    // =======================================================================
    /** The defiened empty elements. */
    private static final String[] empties = {"area", "base", "br", "col", "command", "device", "embed", "frame", "hr", "img", "input",
            "keygen", "link", "meta", "param", "source", "track", "wbr"};

    /** The defiened data elements. */
    private static final String[] datas = {"noframes", "script", "style", "textarea", "title"};

    /** The position for something. */
    private int pos = 0;

    /** The encoded text data. */
    private String html;

    /**
     * <p>
     * Parse HTML and build {@link XML}.
     * </p>
     * 
     * @return A normalized {@link XML}.
     */
    XML parse(byte[] raw, Charset encoding) {
        // ====================
        // Initialization
        // ====================
        XML xml = I.xml(null);
        html = new String(raw, 0, raw.length, encoding);
        pos = 0;

        // If crazy html provides multiple meta element for character encoding,
        // we should adopt first one.
        boolean detectable = true;

        // ====================
        // Start Parsing
        // ====================
        findSpace();

        while (pos != html.length()) {
            if (test("<!--")) {
                // =====================
                // Comment
                // =====================
                xml.append(xml.doc.createComment(find("->")));
            } else if (test("<![CDATA[")) {
                // =====================
                // CDATA
                // =====================
                xml.append(xml.doc.createCDATASection(find("]]>")));
            } else if (test("<!") || test("<?")) {
                // =====================
                // DocType and PI
                // =====================
                // ignore doctype and pi
                find(">");
            } else if (test("</")) {
                // =====================
                // End Element
                // =====================
                find(">");

                // update current element into parent
                xml = xml.parent();
            } else if (test("<")) {
                // =====================
                // Start Element
                // =====================
                String name = findName();
                findSpace();

                XML child = xml.child(name);

                // parse attributes
                while (html.charAt(pos) != '/' && html.charAt(pos) != '>') {
                    String attr = findName();
                    findSpace();

                    if (!test("=")) {
                        // single value attribute
                        child.attr(attr, attr);

                        // step into next
                        pos++;
                    } else {
                        // name-value pair attribute
                        findSpace();

                        if (test("\"")) {
                            // quote attribute
                            child.attr(attr, find("\""));
                        } else if (test("'")) {
                            // apostrophe attribute
                            child.attr(attr, find("'"));
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
                    findSpace();
                }

                // close start element
                if (find(">").length() == 0 && Arrays.binarySearch(empties, name) < 0) {
                    // container element
                    if (0 <= Arrays.binarySearch(datas, name)) {
                        // text data only element - add contents as text

                        // At first, we find the end element, but the some html provides crazy
                        // element pair like <div></DIV>. So we should search by case-insensitive,
                        // don't use find("</" + name + ">").
                        int start = pos;

                        find("</");
                        while (!findName().equals(name)) {
                            find("</");
                        }
                        find(">");

                        child.text(html.substring(start, pos - 3 - name.length()));
                        // don't update current elment
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

                                // reset and parse again if the current encoding is wrong
                                if (!encoding.equals(detect)) {
                                    return parse(raw, detect);
                                }
                            } catch (Exception e) {
                                // unkwnown encoding name
                            }
                        }
                    }
                    // don't update current elment
                }
            } else {
                // =====================
                // Text
                // =====================
                xml.append(xml.doc.createTextNode(find("<")));

                // If the current positon is not end of document, we should continue to parse
                // next elements. So rollback position(1) for the "<" next start element.
                if (pos != html.length()) {
                    pos--;
                }
            }
            findSpace();
        }
        return I.xml(xml.doc); // return root
    }

    /**
     * <p>
     * Check whether the current sequence is started with the specified characters or not. If it is
     * matched, cursor positioning step into it.
     * </p>
     * 
     * @param until A condition.
     * @return A result.
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
     * <p>
     * Consume the next run of characters until the specified sequence will be appered.
     * </p>
     * 
     * @param until A target character sequence. (include this sequence)
     * @return A consumed character sequence. (exclude the specified sequence)
     */
    private String find(String until) {
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
     * <p>
     * Consume an identical name. (word or :, _, -)
     * </p>
     * 
     * @return An identical name for XML.
     */
    private String findName() {
        int start = pos;
        char c = html.charAt(pos);

        while (Character.isLetterOrDigit(c) || c == '_' || c == ':' || c == '-') {
            c = html.charAt(++pos);
        }
        return html.substring(start, pos).toLowerCase();
    }

    /**
     * <p>
     * Consume the next run of whitespace characters.
     * </p>
     */
    private void findSpace() {
        while (pos < html.length() && Character.isWhitespace(html.charAt(pos))) {
            pos++;
        }
    }
}
