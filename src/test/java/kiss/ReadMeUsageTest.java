/*
 * Copyright (C) 2022 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss;

import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.System.Logger.Level;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

class ReadMeUsageTest {

    /**
     * Create instance.
     */
    @Test
    void createInstance() {
        class Some {
        }

        assert I.make(Some.class) != I.make(Some.class);;
    }

    /**
     * Create singleton instance. (managed lifestyle)
     */
    @Test
    void createSingleton() {
        @Managed(Singleton.class)
        class Some {
        }

        assert I.make(Some.class) == I.make(Some.class);
    }

    /**
     * Enable dependency injection without configuration. (constructor injection)
     */
    @Test
    @SuppressWarnings("unused")
    void constructorInjection() {
        class Injected {
        }

        class Injectable {
            Injected injected;

            Injectable(Injected injected) {
                this.injected = injected;
            }
        }

        Injectable Injectable = I.make(Injectable.class);
        assert Injectable.injected != null;
    }

    /**
     * Read contents from HTTP.
     */
    @Test
    void http() {
        I.http("https://httpstat.us/200", String.class).to(text -> {
            // read as text
        });

        I.http("https://httpstat.us/200", JSON.class).to(json -> {
            // read as JSON
        });

        I.http("https://httpstat.us/200", XML.class).to(xml -> {
            // read as XML
        });
    }

    /**
     * Parse JSON.
     */
    @Test
    void parseJSON() {
        JSON json = I.json("""
                {
                    "name": "忍",
                    "age": 598
                }
                """);

        // read value as String (shorthand)
        assert json.text("name").equals("忍");

        // read value as int
        assert json.get("age").as(int.class) == 598;
    }

    /**
     * Parse XML/HTML.
     */
    @Test
    void parseXML() {
        XML html = I.xml("""
                <html>
                    <body>
                        <h1>Heading</h1>
                        <div class="date">2022/08/10</div>
                        <p>contents</p>
                        <div class="author">忍</p>
                    </body>
                </html>
                """);

        // select the element by CSS selector and read its text
        assert html.find("p").text().equals("contents");
        assert html.find(".author").text().equals("忍");
    }

    /**
     * Reactive stream. (Rx)
     */
    @Test
    void reactive() {
        List<String> results = I.signal("This", "is", "reactive", "stream").map(String::toUpperCase).toList();

        assert results.get(0).equals("THIS");
        assert results.get(1).equals("IS");
        assert results.get(2).equals("REACTIVE");
        assert results.get(3).equals("STREAM");
    }

    /**
     * Use template engine. (Mustache)
     */
    @Test
    @SuppressWarnings("unused")
    void templateEngine() {
        class Person {
            public String name;
        }

        Person person = new Person();
        person.name = "忍";

        assert I.express("Hello {name}!", person).equals("Hello 忍!");
    }

    /**
     * Use logging.
     */
    @Test
    void logging() {
        I.info("Default logging level");

        I.error("your.logger.name", "Use logger name.");

        I.debug("system", "[system] is default logger name.");
    }

    private PrintStream original;

    @BeforeEach
    void store(TestInfo info) {
        if (info.getDisplayName().startsWith("logging")) {
            original = System.out;
            System.setOut(new NullPrintStream());

            I.env("*.file", Level.OFF);
            I.env("*.console", Level.ALL);
        }
    }

    @AfterEach
    void restore(TestInfo info) {
        if (info.getDisplayName().startsWith("logging")) {
            System.setOut(original);
        }
    }

    /**
     * 
     */
    private static class NullPrintStream extends PrintStream {

        private NullPrintStream() {
            super(OutputStream.nullOutputStream(), true, StandardCharsets.UTF_8);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void print(Object obj) {
        }
    }
}
