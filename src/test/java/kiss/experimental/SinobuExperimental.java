/*
 * Copyright (C) 2016 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.experimental;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;

import kiss.I;
import kiss.Manageable;
import kiss.Singleton;

/**
 * @version 2017/02/05 12:21:39
 */
@Manageable(lifestyle = Singleton.class)
public class SinobuExperimental {

    /** The accessible internal method for lambda info. */
    private static final Method findConstants;

    static {
        try {
            // reflect lambda info related methods
            findConstants = Class.class.getDeclaredMethod("getConstantPool");
            findConstants.setAccessible(true);
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }

    /**
     * Copy the specified object deeply.
     * 
     * @param <M> A model type.
     * @param model A model type indicator.
     * @return A clone object.
     */
    public static <M> M xerox(M model) {
        return model;
    }

    public static <M> M xml(CharSequence input, Class<M> model) {
        return xml(new StringReader(input.toString()), model);
    }

    public static <M> M xml(Path input, Class<M> model) {
        try {
            return xml(Files.newBufferedReader(input, I.$encoding), model);
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }

    public static <M> M xml(Readable input, Class<M> model) {
        return null;
    }

    public static String xml(Object input, boolean xml) {
        return null;
    }

    public static void xml(Object input, Path output, boolean xml) {
        try {
            if (Files.notExists(output)) {
                Files.createDirectories(output.getParent());
                Files.createFile(output);
            }

            xml(input, Files.newBufferedWriter(output, I.$encoding), xml);
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }

    public static void xml(Object input, Appendable output, boolean xml) {

    }

    /**
     * <p>
     * Query and calculate the object graph by using the XPath engine which is provided by J2SE.
     * </p>
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
