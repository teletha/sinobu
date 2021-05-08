/*
 * Copyright (C) 2021 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package doc;

import java.util.Locale;
import java.util.concurrent.Callable;

import kiss.Disposable;
import kiss.I;
import kiss.Lifestyle;
import kiss.Managed;
import kiss.Singleton;
import kiss.lifestyle.PrototypeTest;
import kiss.lifestyle.SingletonTest;
import kiss.sample.bean.Person;

/**
 * <h2>Document</h2>
 */
public class DocumentDoc {

    class Introduction {
        /**
         * <p>
         * This library aims to simplify and highly condense the functions related to domains that
         * are frequently encountered in real-world development projects, making them easier to use.
         * Some specific domains are listed below.
         * </p>
         * <ul>
         * <li><a href="https://en.wikipedia.org/wiki/Dependency_injection">Dependency
         * Injection</a></li>
         * <li>Object lifecycle management</li>
         * <li><a href="https://en.wikipedia.org/wiki/JavaBeans">JavaBeans</a>-like property based
         * type modeling</li>
         * <li>HTTP(S)</li>
         * <li><a href="https://en.wikipedia.org/wiki/JSON">JSON</a></li>
         * <li><a href="https://en.wikipedia.org/wiki/HTML">HTML</a>(<a href=
         * "https://en.wikipedia.org/wiki/XML">XML</a>)</li>
         * <li>Reactive Programming (<a href="http://reactivex.io/">Rx</a>)</li>
         * <li>Asynchronous processing</li>
         * <li>Parallel processing</li>
         * <li>Multilingualization</li>
         * <li>Template
         * engine(<a href="https://mustache.github.io/mustache.5.html">Mustache</a>)</li>
         * <li>Dynamic plug-in mechanisms</li>
         * <li>Domain specific languages</li>
         * <li>Persistence</li>
         * </ul>
         * <p>
         * With a few exceptions, Sinobu and its APIs are designed to be simple to use and easy to
         * understand by adhering to the following principles.
         * </p>
         * <ul>
         * <li><a href="https://en.wikipedia.org/wiki/KISS_principle">Keep it stupid simple</a></li>
         * <li><a href="https://en.wikipedia.org/wiki/Less_is_more_(architecture)">Less is
         * more</a></li>
         * <li><a href="https://en.wikipedia.org/wiki/Type_safety">Type safety</a></li>
         * <li>Refactoring safety</li>
         * </ul>
         */
        public DocumentDoc Purpose_of_use;

        /**
         * <p>
         * It is probably easiest to use a build tool such as
         * <a href="https://maven.apache.org/">Maven</a> or
         * <a href="https://gradle.org/">Gradle</a>.
         * </p>
         */
        public DocumentDoc How_to_install;
    }

    class Managing_object_lifestyle {

        /**
         * <h2>What do you mean by lifestyle?</h2>
         * <p>
         * In Sinobu, lifestyle refers to the way an object is created and managed, corresponding to
         * the scope in terms of DI containers such as SpringFramework and Guice, but without the
         * process of registering with the container or destroying the object.
         * </p>
         * <h3>Creating an object</h3>
         * <p>
         * In Java, it is common to use the new operator on the constructor to create a new object.
         * In many cases, this is sufficient, but in the following situations, it is a bit
         * insufficient.
         * </p>
         * <ul>
         * <li>To manage the number of objects to be created.</li>
         * <li>To create objects associated with a specific context.</li>
         * <li>To generate objects with complex dependencies.</li>
         * <li>The type of the object to be generated is not statically determined.</li>
         * </ul>
         * <p>
         * While DI containers such as SpringFramework or Guice are commonly used to deal with such
         * problems, Sinobu comes with its own very simple DI container. The following code shows
         * the creation of an object using DI container.
         * </p>
         * <pre>{@link #createObject()}</pre>
         * <p>
         * As you can see from the above code, there is no actual container object; Sinobu has only
         * one global container in the JVM, and that object cannot be accessed directly. In order to
         * create an object from a container, we need to call {@link I#make(Class)}.
         * </p>
         */
        public DocumentDoc What_do_you_mean_by_lifestyle;

        void createObject() {
            Person someone = I.make(Person.class);
            assert someone != null;
        }

        /**
         * <h2>Defining lifestyle</h2>
         * <p>
         * In order to define a lifestyle, we need to write a {@link Lifestyle} interface. This
         * interface is essentially equivalent to {@link Callable}, but it is called when a specific
         * Type (class or interface) is requested for a container, and it makes the following 3
         * decisions:
         * </p>
         * <ol>
         * <li>Which class to instantiate actually.</li>
         * <li>How to instantiate it.</li>
         * <li>How to manage the instances.</li>
         * </ol>
         * <p>
         * As the simplest example, let's consider a prototype pattern that creates a new instance
         * using the new operator each time it is requested. The following implementation is called
         * every time an instance of Person is requested for the container, and creates and returns
         * a new instance.
         * </p>
         * <pre>{@link Prototype}</pre>
         * <p>
         * This kind of prototype pattern is set as the default lifestyle in Sinobu because it is
         * expected to be used most often. Also, you can use {@link I#prototype(Class)} to generate
         * prototypical lifestyles for any type, so there is no need to actually write such an
         * implementation.
         * </p>
         * <h3>Pre-defined lifestyle</h3>
         * <p>
         * Sinobu comes with two pre-defined lifestyles. One is the prototype lifestyle described
         * earlier, which generates a new instant every time it is requested. This is the default
         * lifestyle in Sinobu, so you do not need to make any special settings to use it.
         * </p>
         * <pre>{@link PrototypeTest#prototype()}</pre>
         * <p>
         * The other is the singleton lifestyle, which keeps a single instance in the JVM and always
         * returns it.
         * </p>
         * <pre>{@link SingletonTest#singleton()}</pre>
         * <h3>Custom lifestyle</h3>
         * <p>
         * You can also define new lifestyles based on arbitrary contexts by implementing the
         * {@link Lifestyle} interface and defining a constructor to receive the requested type.
         * </p>
         * <pre>{@link PerThread}</pre>
         */
        public DocumentDoc Defining_lifestyle;

        class Prototype implements Lifestyle<Person> {

            @Override
            public Person call() throws Exception {
                return new Person();
            }
        }

        class PerThread<T> implements Lifestyle<T> {

            private final ThreadLocal<T> local;

            PerThread(Class<T> requestedType) {
                local = ThreadLocal.withInitial(() -> {
                    try {
                        return requestedType.getDeclaredConstructor().newInstance();
                    } catch (Exception e) {
                        throw new Error(e);
                    }
                });
            }

            @Override
            public T call() throws Exception {
                return local.get();
            }
        }

        /**
         * <h2>Registering lifestyle</h2>
         * <p>
         * To use a lifestyle other than the prototype lifestyle, you need to individually configure
         * the lifestyle to be used in the class. There are two ways to do this, one is to use
         * {@link Managed} annotation. This way is useful if you want to specify a lifestyle for a
         * class that is under your control. The following is an example of using the
         * {@link Singleton} lifestyle.
         * </p>
         * <pre>{@link Earth}</pre>
         * <p>
         * {@link Managed} annotation specifies the implementation of {@link Lifestyle} you want to
         * use, but if none is specified, it is treated as if a prototype lifestyle is specified.
         * </p>
         * <p>
         * The other is defining custom {@link Lifestyle}. Sinobu recognizes it automatically if
         * your custom lifestyle class is loaded or unloaded by {@link I#load(Class)} and
         * {@link Disposable#dispose()}methods. The following is example.
         * </p>
         * <pre>{@link SingletonLocale}</pre>
         */
        public DocumentDoc Registering_lifestyle;

        @Managed(Singleton.class)
        class Earth {
        }

        class SingletonLocale implements Lifestyle<Locale> {

            private static final Locale singleton = Locale.forLanguageTag("language-tag");

            @Override
            public Locale call() throws Exception {
                return singleton;
            }
        }
    }

    class Dipendency_Injection {
        /**
         * <h2>The need for Dependency Injection</h2>
         * <p>
         * Dependency Injection (DI) is a mechanism that solves various problems related to
         * component dependencies in "a nice way". Component dependency refers to the relationship
         * from upper layer to lower layer, such as Controller → Service → Repository in a general
         * layered architecture. "A nice way" means that the framework will take care of the problem
         * without the developer having to work hard manually. In modern Java application
         * development, DI is an almost indispensable mechanism.
         * </p>
         */
        public DocumentDoc The_need_for_Dependency_Injection;

        public DocumentDoc Circular_Reference;
    }

    class HTTP {
        public DocumentDoc Request_and_Response;

        public DocumentDoc Supported_Type;

        public DocumentDoc Cookie;

        public DocumentDoc Authentication;
    }

    class JSON {

        public DocumentDoc Parsing;

        public DocumentDoc Writing;

        public DocumentDoc Mapping_to_Model;

        public DocumentDoc Mapping_from_Model;
    }

    class HTML {
        public DocumentDoc Parsing;

        public DocumentDoc Writing;

        public DocumentDoc Support_CSS_Selector;

        public DocumentDoc Manipulation;

        public DocumentDoc Traverse;
    }

    class ReactiveX {

        public DocumentDoc Signal;

        public DocumentDoc Subscribe;

        public DocumentDoc Unsubscribe;

        public DocumentDoc Operators;
    }

    class Template_Engine {
        public DocumentDoc Variable;

        public DocumentDoc Section;

        public DocumentDoc Comment;
    }

    class Plugin {

        public DocumentDoc Extension_Point;

        public DocumentDoc Extension;

        public DocumentDoc Dynamic_Loading;

        public DocumentDoc Query_Extension;
    }

    class DSL {
        public DocumentDoc Tree_Structured_Data;

        public DocumentDoc Node_and_Leaf;

        public DocumentDoc HTML_Sample;
    }

    class Persistence {
        public DocumentDoc Save_Data;

        public DocumentDoc Save_Automatically;

        public DocumentDoc Config_Location;
    }
}
