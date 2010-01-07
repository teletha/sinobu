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

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * <p>
 * This class is a buffer of SAX events.
 * </p>
 * 
 * @version 2008/08/31 11:14:45
 */
class Bits extends XMLFilterImpl {

    /** The event cache. */
    final ArrayList<Object[]> bits = new ArrayList();

    /**
     * Send buffered sax events to the given content handler with the context object.
     * 
     * @param handler A target content handler.
     * @param context A context object.
     * @throws SAXException If the sax event is failed.
     */
    void send(ContentHandler handler) throws SAXException {
        if (handler instanceof Bits) {
            bits.addAll(((Bits) handler).bits);
        } else {
            for (int i = 0; i < bits.size(); i++) {
                Object[] bit = bits.get(i);

                switch (bit.length) {
                case 1:
                    handler.characters(((String) bit[0]).toCharArray(), 0, ((String) bit[0]).length());
                    break;

                case 3:
                    handler.endElement((String) bit[0], (String) bit[1], (String) bit[2]);
                    break;

                default:
                    handler.startElement((String) bit[0], (String) bit[1], (String) bit[2], (Attributes) bit[3]);
                }
            }
        }
    }

    /**
     * @see org.xml.sax.helpers.XMLFilterImpl#characters(char[], int, int)
     */
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        bits.add(new Object[] {new String(ch, start, length)});
    }

    /**
     * @see org.xml.sax.helpers.XMLFilterImpl#startElement(java.lang.String, java.lang.String,
     *      java.lang.String, org.xml.sax.Attributes)
     */
    @Override
    public void startElement(String uri, String local, String name, Attributes atts) throws SAXException {
        bits.add(new Object[] {uri, local, name, new AttributesImpl(atts)});
    }

    /**
     * @see org.xml.sax.helpers.XMLFilterImpl#endElement(java.lang.String, java.lang.String,
     *      java.lang.String)
     */
    @Override
    public void endElement(String uri, String local, String name) throws SAXException {
        bits.add(new Object[] {uri, local, name});
    }
}
