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
package ezbean.scratchpad;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.Flushable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.CharBuffer;

import org.xml.sax.InputSource;

import ezbean.I;
import ezbean.Manageable;
import ezbean.Singleton;

/**
 * @version 2008/06/18 8:42:37
 */
@Manageable(lifestyle = Singleton.class)
public class EzbeanScratchpad {

    /**
     * <p>
     * Type inference create method. It's cooool!
     * </p>
     * 
     * <pre>
     * Person person = I.create();
     * </pre>
     * 
     * @param <M>
     * @param m
     * @return
     */
    public static <M> M create(M... m) {
        return (M) I.make(m.getClass().getComponentType());
    }

    /**
     * Copy the specified object deeply.
     * 
     * @param <M>
     * @param model
     * @return
     */
    public static <M> M xerox(M model) {
        return model;
    }

    /**
     * <p>
     * Query and calculate the object graph by using the XPath engine which is provided by J2SE.
     * </p>
     * 
     * <pre>
     * School school = I.create(School.class);
     * List&lt;Student&gt; list = new ArrayList();
     * school.setStudents(list);
     * 
     * Student person = I.create(Student.class);
     * person.setAge(1);
     * list.add(person);
     * 
     * person = I.create(Student.class);
     * person.setAge(2);
     * list.add(person);
     * 
     * person = I.create(Student.class);
     * person.setAge(3);
     * list.add(person);
     * 
     * int sum = I.xpath(school, &quot;sum(/School/students/item/@age)&quot;);
     * assertEquals(6, sum);
     * </pre>
     * 
     * @param model
     * @param xpath
     */
    public static void xpath(Object model, String xpath) {
        // create writer
        // SAXBuilder converter = new SAXBuilder();
        // XMLReader reader = XMLUtil.getXMLReader(converter);
        // ContentHandler handler = reader.getContentHandler();

        // xml start
        // handler.startDocument();
        // handler.startPrefixMapping("ez", "http://ez.bean/");

        // ModelWalker walker = new ModelWalker(model);
        // walker.addListener(new ConfigurationWriter(handler));
        // walker.traverse();

        // xml end
        // handler.endDocument();

        // XPath path = XPathFactory.newInstance().newXPath();
        // return path.evaluate(xpath, converter.getDocument());
    }

    /**
     * @param <M>
     * @param file
     * @param config
     * @return
     */
    public static <M> M xml(File file, M config) {
        // read
        try {
            return xml(new InputStreamReader(new FileInputStream(file), I.getEncoding()), config);
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }

    /**
     * @param <M>
     * @param input
     * @param config
     * @return
     */
    public static <M> M xml(Reader input, M config) {
        // read
        InputSource source = new InputSource(input);

        return config;
    }

    /**
     * @param <M>
     * @param input
     * @param config
     * @return
     */
    public static <M> M xml(final Readable input, M config) {
        // read
        InputSource source = new InputSource(new ReadableReader(input));

        return config;
    }

    /**
     * @param config
     * @param output
     */
    public static void xml(Object config, File output) {
        // write
        try {
            xml(config, new OutputStreamWriter(new FileOutputStream(output), I.getEncoding()));
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }

    public static void xml(Object config, Appendable output) {
        // write
    }

    /**
     * @version 2010/01/08 11:47:20
     */
    public static class ReadableReader extends Reader {

        /** The actual input. */
        private final Readable readable;

        /**
         * @param readable
         */
        public ReadableReader(Readable readable) {
            this.readable = readable;
        }

        /**
         * @see java.io.Reader#close()
         */
        @Override
        public void close() throws IOException {
            if (readable instanceof Closeable) {
                ((Closeable) readable).close();
            }
        }

        /**
         * @see java.io.Reader#read(char[], int, int)
         */
        @Override
        public int read(char[] cbuf, int off, int len) throws IOException {
            return readable.read(CharBuffer.wrap(cbuf, off, len));
        }

    }

    /**
     * @version 2010/01/08 11:46:58
     */
    public static class AppendableWriter extends Writer {

        /** The actual output. */
        private final Appendable appendable;

        /**
         * @param appendable
         */
        public AppendableWriter(Appendable appendable) {
            this.appendable = appendable;
        }

        /**
         * @see java.io.Writer#close()
         */
        @Override
        public void close() throws IOException {
            if (appendable instanceof Closeable) {
                ((Closeable) appendable).close();
            }
        }

        /**
         * @see java.io.Writer#flush()
         */
        @Override
        public void flush() throws IOException {
            if (appendable instanceof Flushable) {
                ((Flushable) appendable).flush();
            }
        }

        /**
         * @see java.io.Writer#write(char[], int, int)
         */
        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
            appendable.append(new String(cbuf, off, len));
        }
    }
}
