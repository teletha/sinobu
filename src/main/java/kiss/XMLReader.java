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
        xml = I.xml("m");
        html = new String(row, 0, row.length, encoding);
        pos = 0;

        while (pos != html.length()) {
            if (matche("<!--")) {
                // =====================
                // Comment
                // =====================
                // ignore comment
                consume("->");
            } else if (matche("<![CDATA[")) {
                // =====================
                // CDATA
                // =====================
                xml.append(I.dom.newDocument().createTextNode(consume("]]>")));
            } else if (matche("<!") || matche("<?")) {
                // =====================
                // DocType and PI
                // =====================
                // ignore doctype and pi
                consume(">");
            } else if (matche("</")) {
                // =====================
                // End Element
                // =====================
                consume(">");

                // update current element into parent
                xml = xml.parent();
            } else if (matche("<")) {
                // =====================
                // Start Element
                // =====================
                String name = consumeName();
                consumeSpace();
                XML child = xml.child(name);

                // parse attributes
                while (!html.startsWith("/>", pos) && !html.startsWith(">", pos)) {
                    String attr = consumeName();
                    consumeSpace();

                    if (!matche("=")) {
                        // single value attribute
                    } else {
                        // name-value pair attribute
                        consumeSpace();

                        if (matche("\"")) {
                            // quote attribute
                            child.attr(attr, consume("\""));
                        } else if (matche("'")) {
                            // apostrophe attribute
                            child.attr(attr, consume("'"));
                        } else {
                            // non-quoted attribute
                            child.attr(attr, consumeName());
                        }
                    }
                    consumeSpace();
                }

                // close start element
                if (consume(">").length() == 0 && Arrays.binarySearch(empties, name) < 0) {
                    // container element
                    if (0 <= Arrays.binarySearch(datas, name)) {
                        // text data only element
                        // add contents as text
                        child.text(consume("</".concat(name).concat(">")));
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
                        Charset detect = encoding;
                        String value = child.attr("charset");

                        if (value.length() != 0) {
                            detect = Charset.forName(value);
                        }

                        if (child.attr("http-equiv").equalsIgnoreCase("content-type")) {
                            detect = detect(child.attr("content"));
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
                xml.append(I.dom.newDocument().createTextNode(consume("<")));

                // If the current positon is not end of document, we should continue to parse
                // next elements. So rollback position(1) for the "<" next start element.
                if (pos != html.length()) {
                    pos--;
                }
            }
        }

        return xml.children();
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
    private String consume(String until) {
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
    private String consumeName() {
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
    private void consumeSpace() {
        while (Character.isWhitespace(html.charAt(pos))) {
            pos++;
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
