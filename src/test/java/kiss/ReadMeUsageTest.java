/*
 * Copyright (C) 2024 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss;

import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.System.Logger.Level;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

class ReadMeUsageTest {

    /**
     * Create instant from type.
     */
    @Test
    void createInstance() {
        class Some {
        }

        assert I.make(Some.class) != I.make(Some.class);
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
     * Parse XML/HTML. (accept tag soup)
     */
    @Test
    void parseXML() {
        XML html = I.xml("""
                <html>
                    <body>
                        <h1>Heading</h1>
                        <div class="age">598</div>
                        <p>contents</p>
                        <div class="author">忍</p>
                    </body>
                </html>
                """);

        // select the element by CSS selector and read its text content
        assert html.find("p").text().equals("contents");
        assert html.find(".author").text().equals("忍");
    }

    /**
     * Reactive stream. (Rx)
     */
    @Test
    void reactive() {
        String result = I.signal("This", "is", "reactive", "stream")
                .skip(2)
                .map(String::toUpperCase)
                .scan(Collectors.joining(" "))
                .to()
                .exact();

        assert result.equals("REACTIVE STREAM");
    }

    /**
     * Evaluate expression language. (Mustache-like syntax)
     */
    @Test
    void templateEngine() {
        Person person = new Person();
        person.name = "忍";
        person.age = 598;

        assert I.express("{name} is {age} years old.", person).equals("忍 is 598 years old.");
    }

    class Person {
        public String name;

        public int age;
    }

    /**
     * Write log message on console, file and user-defined appender.
     */
    @Test
    void logging() {
        I.trace("Default logging level is INFO.");

        I.debug("your.logger.name", "Different logger names can be used for different output settings.");

        I.info("system", "The default logger name is [system].");

        I.warn("""
                The following settings can be changed for each logger:
                    * log level
                    * displying caller location
                    * output directory of log file
                    * whether the log file is overwritten or appended
                    * the number of days the log file is kept
                """);

        I.error((Supplier) () -> "Use a lambda expression to delay message building.");
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