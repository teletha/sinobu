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
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * @version 2012/11/17 22:03:00
 */
class XMLReader {

    /** The defiened empty elements. */
    private static final String[] empties = {"area", "base", "br", "col", "command", "device", "embed", "frame", "hr",
            "img", "input", "keygen", "link", "meta", "param", "source", "track", "wbr"};

    /** The defiened data elements. */
    private static final String[] datas = {"noframes", "script", "style", "textarea", "title"};

    /** The raw text data. */
    private final byte[] row;

    /** The document encoding. */
    private Charset encoding = I.$encoding;

    /** The current processing element. */
    private XML xml;

    /** The encoded text data. */
    private String html;

    /** The current parsing position. */
    private int pos;

    /**
     * <p>
     * Constructor.
     * </p>
     * 
     * @param input
     */
    XMLReader(InputStream input) {
        // read actual data
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        I.copy(input, output, true);
        row = output.toByteArray();
    }

    /**
     * <p>
     * Parse HTML and build {@link XML}.
     * </p>
     * 
     * @return A normalized {@link XML}.
     */
    XML parse() {
        // initialize
        xml = I.xml(null);
        html = new String(row, 0, row.length, encoding);
        pos = 0;

        // ====================
        // Start Parsing
        // ====================
        findSpace();

        while (pos != html.length()) {
            if (matche("<!--")) {
                // =====================
                // Comment
                // =====================
                xml.append(xml.doc.createComment(find("->")));
            } else if (matche("<![CDATA[")) {
                // =====================
                // CDATA
                // =====================
                xml.append(xml.doc.createCDATASection(find("]]>")));
            } else if (matche("<!") || matche("<?")) {
                // =====================
                // DocType and PI
                // =====================
                // ignore doctype and pi
                find(">");
            } else if (matche("</")) {
                // =====================
                // End Element
                // =====================
                find(">");

                // update current element into parent
                xml = xml.parent();
            } else if (matche("<")) {
                // =====================
                // Start Element
                // =====================
                String name = findName();
                findSpace();
                XML child = xml.child(name);

                // parse attributes
                while (!html.startsWith("/>", pos) && !html.startsWith(">", pos)) {
                    String attr = findName();
                    findSpace();

                    if (!matche("=")) {
                        // single value attribute
                        child.attr(attr, attr);
                    } else {
                        // name-value pair attribute
                        findSpace();

                        if (matche("\"")) {
                            // quote attribute
                            child.attr(attr, find("\""));
                        } else if (matche("'")) {
                            // apostrophe attribute
                            child.attr(attr, find("'"));
                        } else {
                            // non-quoted attribute
                            child.attr(attr, findName());
                        }
                    }
                    findSpace();
                }

                // close start element
                if (find(">").length() == 0 && Arrays.binarySearch(empties, name) < 0) {
                    // container element
                    if (0 <= Arrays.binarySearch(datas, name)) {
                        // text data only element
                        // add contents as text
                        child.text(find("</".concat(name).concat(">")));
                        // don't update current elment
                    } else {
                        // mixed element
                        // update current element into child
                        xml = child;
                    }
                } else {
                    // empty element

                    // chech encoding in meta element
                    if (name.equals("meta")) {
                        String value = child.attr("charset");

                        if (value.length() == 0 && child.attr("http-equiv").equalsIgnoreCase("content-type")) {
                            value = child.attr("content");
                        }

                        Charset detect = encoding;
                        int index = value.lastIndexOf('=');

                        try {
                            detect = Charset.forName(index == -1 ? value : value.substring(index + 1));
                        } catch (Exception e) {
                            // do nothing, use default
                        }

                        // reset and parse again if the current encoding is wrong
                        if (!encoding.equals(detect)) {
                            encoding = detect;

                            return parse();
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
        }

        return xml;
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
    private boolean matche(String until) {
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
        return html.substring(start, pos);
    }

    /**
     * <p>
     * Consume the next run of whitespace characters.
     * </p>
     */
    private void findSpace() {
        while (Character.isWhitespace(html.charAt(pos))) {
            pos++;
        }
    }
}
