/**
 * Copyright (C) 2011 Nameless Production Committee.
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
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;

import ezbean.I;

/**
 * <p>
 * This class is a buffer of SAX events.
 * </p>
 * 
 * @version 2010/12/13 8:11:39
 */
class Bits extends XMLFilterImpl {

    /** The event cache. */
    final ArrayList<Object[]> bits = new ArrayList();

    /**
     * Send buffered sax events to the given content handler with the context object.
     * 
     * @param handler A target content handler.
     * @param context A context object.
     */
    void send(XMLScanner handler) {
        for (int i = 0; i < bits.size(); i++) {
            Object[] bit = bits.get(i);

            switch (bit.length) {
            case 1:
                handler.text((String) bit[0]);
                break;

            case 2:
                handler.start((String) bit[0], (Attributes) bit[1]);
                break;

            default:
                try {
                    handler.endElement((String) bit[0], (String) bit[1], (String) bit[2]);
                } catch (SAXException e) {
                    throw I.quiet(e);
                }
            }
        }
    }

    /**
     * @see org.xml.sax.helpers.XMLFilterImpl#characters(char[], int, int)
     */
    @Override
    public void characters(char[] ch, int start, int length) {
        bits.add(new Object[] {new String(ch, start, length)});
    }

    /**
     * @see org.xml.sax.helpers.XMLFilterImpl#startElement(java.lang.String, java.lang.String,
     *      java.lang.String, org.xml.sax.Attributes)
     */
    @Override
    public void startElement(String uri, String local, String name, Attributes atts) {
        bits.add(new Object[] {name, new AttributesImpl(atts)});
    }

    /**
     * @see org.xml.sax.helpers.XMLFilterImpl#endElement(java.lang.String, java.lang.String,
     *      java.lang.String)
     */
    @Override
    public void endElement(String uri, String local, String name) {
        bits.add(new Object[] {uri, local, name});
    }
}
