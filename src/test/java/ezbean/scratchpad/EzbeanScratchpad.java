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

import ezbean.I;
import ezbean.Manageable;
import ezbean.Singleton;
import ezbean.scratchpad.ExtensionKeyTest.Extension;
import ezbean.scratchpad.ExtensionKeyTest.ExtensionKeyProvider;

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
     * @param <E>
     * @param extensionPoint
     * @param key
     * @return
     */
    public static <E extends Extension> E find(Class<E> extensionPoint, Class<?> key) {
        return null;
    }

    /**
     * @param <E>
     * @param extensionPoint
     * @param key
     * @return
     */
    public static <E extends Extension<? extends ExtensionKeyProvider<? super K>>, K> E find(Class<E> extensionPoint, K key) {
        return null;
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
}
