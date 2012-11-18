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
import java.io.Flushable;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import kiss.model.Model;
import kiss.model.Property;
import kiss.model.PropertyWalker;

/**
 * <p>
 * XML writer for Object Graph serialization.
 * </p>
 * <p>
 * We could select to use List implementaion instead of Map for management of implicit object
 * identifier. But it requires linear time to search the existing element. So we should use Map
 * which provides constant-time performance for seaching element.
 * </p>
 * <p>
 * This is also {@link Appendable} {@link Writer}.
 * </p>
 * 
 * @version 2012/11/07 21:01:06
 */
class XMLWriter extends Writer implements PropertyWalker {

    // =======================================================================
    // General Fields
    // =======================================================================
    /** The current processing element. */
    XML xml;

    /** The position for something. */
    private int pos = 0;

    // =======================================================================
    // AppendableWriter
    // =======================================================================
    /** The actual output. */
    private Appendable output;

    /**
     * <p>
     * Constructor for AppendableWriter.
     * </p>
     */
    XMLWriter(Appendable output) {
        this.output = output;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        I.quiet(output);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void flush() throws IOException {
        if (output instanceof Flushable) {
            ((Flushable) output).flush();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        output.append(new String(cbuf, off, len));
    }

    // =======================================================================
    // PropertyWalker for XML Serialization
    // =======================================================================
    /** The record for traversed objects. */
    private final ConcurrentHashMap<Object, XML> reference = new ConcurrentHashMap();

    /**
     * {@inheritDoc}
     */
    @Override
    public void walk(Model model, Property property, Object node) {
        if (!property.isTransient()) {
            // ========================================
            // Enter Node
            // ========================================
            if (model.isCollection()) {
                // collection item property
                xml = xml.child(property.model.name);

                // collection needs key attribute
                if (Map.class.isAssignableFrom(model.type)) {
                    xml.attr("ss:key", property.name);
                }
            } else if (!property.isAttribute()) {
                xml = xml.child(property.name);
            }

            // If the collection item is attribute node, that is represented as xml value attribute
            // and attribute node that collection node doesn't host is written as xml attribute too.
            if (node != null) {
                if (property.isAttribute()) {
                    xml.attr(model.isCollection() ? "value" : property.name, I.transform(node, String.class));
                } else {
                    XML ref = reference.get(node);

                    if (ref == null) {
                        // associate node object with element
                        reference.put(node, xml);

                        // assign new id
                        xml.attr("ss:id", pos++);

                        // ========================================
                        // Traverse Child Node
                        // ========================================
                        property.model.walk(node, this);
                    } else {
                        // share id
                        xml.attr("ss:id", ref.attr("ss:id"));
                    }
                }
            }

            // ========================================
            // Leave Node
            // ========================================
            if (model.isCollection() || !property.isAttribute()) {
                xml = xml.parent();
            }
        }
    }

    // =======================================================================
    // HTML Parser for building XML
    // =======================================================================
    /** The defiened empty elements. */
    private static final String[] empties = {"area", "base", "br", "col", "command", "device", "embed", "frame", "hr",
            "img", "input", "keygen", "link", "meta", "param", "source", "track", "wbr"};

    /** The defiened data elements. */
    private static final String[] datas = {"noframes", "script", "style", "textarea", "title"};

    /** The raw text data. */
    private byte[] row;

    /** The document encoding. */
    private Charset encoding;

    /** The encoded text data. */
    private String html;

    /**
     * <p>
     * Constructor.
     * </p>
     * 
     * @param input
     */
    XMLWriter(InputStream input) {
        // read actual data
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        I.copy(input, output, true);
        row = output.toByteArray();
        encoding = I.$encoding;
    }

    /**
     * <p>
     * Parse HTML and build {@link XML}.
     * </p>
     * 
     * @return A normalized {@link XML}.
     */
    XML parse() {
        // ====================
        // Initialization
        // ====================
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
            findSpace();
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
        while (pos < html.length() && Character.isWhitespace(html.charAt(pos))) {
            pos++;
        }
    }
}
