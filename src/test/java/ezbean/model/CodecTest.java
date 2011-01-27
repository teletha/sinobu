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
package ezbean.model;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.util.Locale;

import org.junit.Test;

/**
 * DOCUMENT.
 * 
 * @version 2008/11/08 4:15:44
 */
public class CodecTest {

    /**
     * BigInteger
     */
    @Test
    public void testBigInteger() {
        Codec converter = new Codec(BigInteger.class);
        BigInteger test = new BigInteger("0");

        assertEquals(new BigInteger("0"), converter.decode(converter.encode(test)));
    }

    /**
     * BigDecimal
     */
    @Test
    public void testBigDecimal() {
        Codec converter = new Codec(BigDecimal.class);
        BigDecimal test = new BigDecimal("0");

        assertEquals(new BigDecimal("0"), converter.decode(converter.encode(test)));
    }

    /**
     * StringBuilder
     */
    @Test
    public void testStringBuilder() {
        Codec converter = new Codec(StringBuilder.class);
        StringBuilder test = new StringBuilder("test");

        assertEquals(new StringBuilder("test").toString(), converter.decode(converter.encode(test)).toString());
    }

    /**
     * StringBuffer
     */
    @Test
    public void testStrinbBuffer() {
        Codec converter = new Codec(StringBuffer.class);
        StringBuffer test = new StringBuffer("test");

        assertEquals(new StringBuffer("test").toString(), converter.decode(converter.encode(test)).toString());
    }

    /**
     * Locale
     */
    @Test
    public void testLocale() {
        Codec converter = new Codec(Locale.class);
        Locale test = new Locale("en");

        assertEquals(new Locale("en"), converter.decode(converter.encode(test)));
    }

    /**
     * URL
     */
    @Test
    public void testURL() throws Exception {
        Codec converter = new Codec(URL.class);
        URL test = new URL("http://sourceforge.net/");

        assertEquals(new URL("http://sourceforge.net/"), converter.decode(converter.encode(test)));
    }

    /**
     * URI
     */
    @Test
    public void testURI() throws Exception {
        Codec converter = new Codec(URI.class);
        URI test = new URI("http://sourceforge.net/");

        assertEquals(new URI("http://sourceforge.net/"), converter.decode(converter.encode(test)));
    }
}
