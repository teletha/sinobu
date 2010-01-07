/*
 * Copyright (C) 2010 Nameless Production Committee.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *         http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ezbean.xml;


import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;

import javax.xml.XMLConstants;



import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

import ezbean.I;

/**
 * <p>
 * This is the greatest beautiful XML formatter for SAX.
 * </p>
 * 
 * @see ContentHandler
 * @see LexicalHandler
 * @see Attributes
 * @version 2008/08/31 21:55:41
 */
public class XMLFormatter extends XMLScanner implements LexicalHandler {

    /** The line separator character */
    private static final char[] EOL = System.getProperty("line.separator").toCharArray();

    /** The event state for other. */
    private static final int OTHER = 0;

    /** The event state for start element. */
    private static final int START = 1;

    /** The event state for end element. */
    private static final int END = 2;

    /** The event state for character. */
    private static final int CHARACTER = 3;

    /** The output stream. */
    protected final Writer out;

    /** The previous sax event state. */
    private int state = 0;

    /** The number of node depth. */
    private int depth = 0;

    /** The marker of last block element's depth. */
    private int last = 0;

    /** The amount of breaks in a current text node. */
    private int breaks = 0;

    /** The counter of namespace declarations in the current element. */
    private int count = 0;

    /** The namespace list. */
    private final ArrayList<String> namespaces = new ArrayList();

    /**
     * Create XMLFormatter instance.
     */
    public XMLFormatter(OutputStream stream) {
        this(new OutputStreamWriter(stream, I.getEncoding()));
    }

    /**
     * Create XMLFormatter instance.
     */
    public XMLFormatter(Writer writer) {
        this.out = new BufferedWriter(writer);
    }

    /**
     * <p>
     * You can override this method to omit xml declaration.
     * </p>
     * 
     * @see org.xml.sax.ContentHandler#startDocument()
     */
    public void startDocument() throws SAXException {
        try {
            // write xml declaration
            out.write("<?xml version=\"1.0\" encoding=\"");
            out.write(I.getEncoding().name());
            out.write("\"?>");
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    /**
     * @see org.xml.sax.ContentHandler#endDocument()
     */
    public void endDocument() throws SAXException {
        try {
            out.flush();
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    /**
     * @see org.xml.sax.ContentHandler#startPrefixMapping(java.lang.String, java.lang.String)
     */
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        count++;
        namespaces.add(prefix); // add prefix
        namespaces.add(uri); // add uri
    }

    /**
     * @see org.xml.sax.ContentHandler#endPrefixMapping(java.lang.String)
     */
    public void endPrefixMapping(String prefix) throws SAXException {
        namespaces.remove(namespaces.size() - 1); // remove uri
        namespaces.remove(namespaces.size() - 1); // remove prefix
    }

    /**
     * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String,
     *      java.lang.String, org.xml.sax.Attributes)
     */
    public void startElement(String uri, String local, String name, Attributes atts) throws SAXException {
        try {
            int prev = checkEvent(START);

            if (prev == CHARACTER) {
                if (breaks != 0) writeIndent();
            } else {
                if (!asCharacter(uri, local)) {
                    out.write(EOL);
                    writeIndent();

                    // mark position
                    last = depth;
                }
            }

            // start output
            out.write('<');
            out.write(name);

            for (int i = 0; i < atts.getLength(); i++) {
                // exclude xmlns declarations
                if (!XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(atts.getURI(i))) {
                    // decide attribute name
                    name = atts.getQName(i); // attribute name reuses name variable for footprint

                    if (name.length() == 0) name = atts.getLocalName(i);

                    // write attribute
                    out.write(' ');
                    out.write(name);
                    out.write('=');
                    out.write('"');
                    name = atts.getValue(i); // attribute value reuses name variable for footprint
                    if (name != null) write(name.toCharArray(), 0, name.length());
                    out.write('"');
                }
            }

            // write namespace declaration
            if (count != 0) {
                root: for (int i = namespaces.size() - count * 2; i < namespaces.size(); i += 2) {
                    name = namespaces.get(i); // prefix reuses name variable for footprint
                    uri = namespaces.get(i + 1); // uri reuses uri variable for footprint

                    // check duplication namespace declaration
                    for (int j = 0; j < i; j += 2) {
                        if (name == namespaces.get(j) && uri == namespaces.get(j + 1)) continue root;
                    }

                    // start writing namespace declaration
                    out.write(' ');
                    out.write("xmlns");
                    if (name.length() != 0) {
                        out.write(':');
                        out.write(name);
                    }
                    out.write('=');
                    out.write('"');
                    out.write(uri);
                    out.write('"');
                }

                // reset namespace declaration count
                count = 0;
            }
        } catch (IOException e) {
            throw new SAXException(e);
        }
        depth++;
    }

    /**
     * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String,
     *      java.lang.String)
     */
    public void endElement(String uri, String local, String name) throws SAXException {
        depth--;

        try {
            int prev = checkEvent(END, uri, local);

            // if the previous sax event is start element, pass through.
            if (prev == START) return;

            // decide to write a break
            if (last == depth + 1 && !asCharacter(uri, local)) {
                out.write(EOL);
                writeIndent();

                // remark position
                last = depth;
            }

            // start output
            out.write('<');
            out.write('/');
            out.write(name);
            out.write('>');
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    /**
     * @see org.xml.sax.ContentHandler#characters(char[], int, int)
     */
    public void characters(char[] ch, int start, int length) throws SAXException {
        try {
            int prev = checkEvent(CHARACTER);

            if (isIgnorableNode(ch, start, length)) {
                if (breaks == 0) write(ch, start, length);

                // change state
                this.state = (prev == START) ? OTHER : prev;
            } else {
                write(ch, start, length);
            }
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    /**
     * @see org.xml.sax.ContentHandler#processingInstruction(java.lang.String, java.lang.String)
     */
    public void processingInstruction(String target, String data) throws SAXException {
        try {
            checkEvent(OTHER);
            out.write(EOL);

            // start output
            out.write("<?");
            out.write(target);

            if (data != null) {
                out.write(' ');
                out.write(data);
            }
            out.write("?>");
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    /**
     * @see org.xml.sax.ext.LexicalHandler#startDTD(java.lang.String, java.lang.String,
     *      java.lang.String)
     */
    public void startDTD(String name, String publicId, String systemId) throws SAXException {
        try {
            checkEvent(OTHER);
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    /**
     * @see org.xml.sax.ext.LexicalHandler#endDTD()
     */
    public void endDTD() throws SAXException {
        try {
            checkEvent(OTHER);
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    /**
     * @see org.xml.sax.ext.LexicalHandler#startEntity(java.lang.String)
     */
    public void startEntity(String name) throws SAXException {
        // do nothing
    }

    /**
     * @see org.xml.sax.ext.LexicalHandler#endEntity(java.lang.String)
     */
    public void endEntity(String name) throws SAXException {
        // do nothing
    }

    /**
     * @see org.xml.sax.ext.LexicalHandler#startCDATA()
     */
    public void startCDATA() throws SAXException {
        try {
            checkEvent(CHARACTER);
            out.write("<[CDATA[");
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    /**
     * @see org.xml.sax.ext.LexicalHandler#endCDATA()
     */
    public void endCDATA() throws SAXException {
        try {
            checkEvent(CHARACTER);
            out.write("]]>");
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    /**
     * @see org.xml.sax.ext.LexicalHandler#comment(char[], int, int)
     */
    public void comment(char[] ch, int start, int length) throws SAXException {
        try {
            int prev = checkEvent(OTHER);

            if (prev != CHARACTER || breaks != 0) {
                out.write(EOL);
                writeIndent();
            }

            // write comment
            out.write("<!--");

            for (int i = start; i < length; i++) {
                out.write(ch[i]);

                if (ch[i] == '\r' || ch[i] == '\n') {
                    writeIndent();
                }
            }
            out.write("-->");
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    /**
     * Check whether the element is treated as a character or not. You can override this method to
     * manipulate a format of serialization.
     * 
     * @param uri A namespace uri.
     * @param local A locale name.
     * @return Is this element treated as a character?
     */
    protected boolean asCharacter(String uri, String local) {
        return false;
    }

    /**
     * Check whether the element is not abbreviated or not. You can override this method to
     * manipulate a format of serialization.
     * 
     * @param uri A namespace uri.
     * @param local A local name.
     * @return Whether the element is not abbreviated or not.
     */
    protected boolean asPair(String uri, String local) {
        return false;
    }

    /**
     * Check whether the node is a ignorable whitespaceFilter or not, and breaks a number of line
     * feeds.
     * 
     * @param ch A charactor sequence to parse.
     * @param start A start position of parsing.
     * @param length A length.
     * @return Whether this characters is a ignorable whitespaceFilter or not.
     */
    protected boolean isIgnorableNode(char[] ch, int start, int length) {
        // reset
        breaks = 0;

        for (int i = 0; i < length; i++) {
            char c = ch[start + i];

            if (c == '\n') {
                breaks++;
                continue;
            }

            if (c == '\r' || !Character.isWhitespace(c)) return false;
        }
        return true;
    }

    /**
     * Helper method to write an appropriate indentCharcter for current node.
     * 
     * @throws IOException Output error.
     */
    protected void writeIndent() throws IOException {
        for (int i = 0; i < depth * 2; i++) {
            out.write(' ');
        }
    }

    /**
     * DOCUMENT.
     * 
     * @param callState A state where this method is called.
     * @return A previous state.
     * @throws IOException Output error.
     */
    protected int checkEvent(int callState) throws IOException {
        return checkEvent(callState, null, null);
    }

    /**
     * DOCUMENT.
     * 
     * @param callState A state where this method is called.
     * @param uri A namespace uri.
     * @param local A local name.
     * @return A previous state.
     * @throws IOException Output error.
     */
    protected int checkEvent(int callState, String uri, String local) throws IOException {
        // check whether a previous node is a start element or not
        if (state != START) {
            int prev = state;
            state = callState;
            return prev;
        }

        // write a end charactor of a start element.
        if (callState == END) {
            // check the element is pair
            if (asPair(uri, local)) {
                out.write('>');
                state = callState;
                return CHARACTER;
            }
            out.write('/');
            out.write('>');
        } else {
            out.write('>');
        }
        int prev = state;
        state = callState;
        return prev;
    }

    /**
     * <p>
     * Encode predefined entities and write a specific part of an array of characters.
     * </p>
     * 
     * @param data A data to parse and write.
     * @param start A start position.
     * @param length A length.
     * @throws IOException Output error.
     */
    protected void write(char[] data, int start, int length) throws IOException {
        for (int i = start; i < start + length; i++) {
            switch (data[i]) {
            case '&':
                out.write("&amp;");
                break;

            case '<':
                out.write("&lt;");
                break;

            case '>':
                out.write("&gt;");
                break;

            case '"':
                out.write("&quot;");
                break;

            case '\'':
                out.write("&apos;");
                break;

            default:
                out.write(data[i]);
            }
        }
    }
}
