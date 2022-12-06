/*
 * Copyright (C) 2022 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package doc;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import doc.ExtensionTest.Codec;
import doc.ExtensionTest.LocalDateCodec;
import kiss.Extensible;
import kiss.I;
import kiss.Lifestyle;
import kiss.Managed;
import kiss.Singleton;
import kiss.json.ManipulateTest;
import kiss.lifestyle.PrototypeTest;
import kiss.lifestyle.SingletonTest;

/**
 * <h2>Document</h2>
 * 
 * @product sinobu
 * @project sinobu
 */
public class DocumentDoc {

    public class Introduction {
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
         * <li>Dynamic plug-in mechanism</li>
         * <li>Domain specific languages</li>
         * <li>Object Persistence</li>
         * <li>Logging</li>
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
        public class Purpose_of_use {
        }

        /**
         * <p>
         * It is probably easiest to use a build tool such as
         * <a href="https://maven.apache.org/">Maven</a> or
         * <a href="https://gradle.org/">Gradle</a>.
         * </p>
         * <pre class="lang-xml">{@code
         * <dependency>
         *     <groupId>{project}</groupId>
         *     <artifactId>{product}</artifactId>
         *     <version>{version}<version>
         * </dependency>
         * }</pre>
         */
        public class How_to_install {
        }
    }

    public class Managing_object_lifestyle {

        /**
         * <p>
         * In Sinobu, lifestyle refers to the way an object is created and managed, corresponding to
         * the scope in terms of DI containers such as SpringFramework and Guice, but without the
         * process of registering with the container or destroying the object.
         * </p>
         */
        public class What_do_you_mean_by_lifestyle_ {

            /**
             * <p>
             * In Java, it is common to use the new operator on the constructor to create a new
             * object. In many cases, this is sufficient, but in the following situations, it is a
             * bit insufficient.
             * </p>
             * <ul>
             * <li>To manage the number of objects to be created.</li>
             * <li>To create objects associated with a specific context.</li>
             * <li>To generate objects with complex dependencies.</li>
             * <li>The type of the object to be generated is not statically determined.</li>
             * </ul>
             * <p>
             * While DI containers such as SpringFramework or Guice are commonly used to deal with
             * such problems, Sinobu comes with its own very simple DI container. The following code
             * shows the creation of an object using DI container.
             * </p>
             * <pre>{@link #createObject()}</pre>
             * <p>
             * As you can see from the above code, there is no actual container object; Sinobu has
             * only one global container in the JVM, and that object cannot be accessed directly. In
             * order to create an object from a container, we need to call {@link I#make(Class)}.
             * </p>
             */
            public class Creating_an_object {

                void createObject() {
                    class Tweet {
                    }

                    Tweet one = I.make(Tweet.class);
                    assert one != null;
                }
            }
        }

        /**
         * <p>
         * In order to define a lifestyle, you need to implement Lifestyle interface. This interface
         * is essentially equivalent to Callable. It is called when container requests the specific
         * type. It makes the following 3 decisions:
         * </p>
         * <ol>
         * <li>Which class to instantiate actually.</li>
         * <li>How to instantiate it.</li>
         * <li>How to manage the instances.</li>
         * </ol>
         * <p>
         * Sinobu defines two lifestyles that are frequently used. One is the prototype pattern and
         * the other is the singleton pattern.
         * </p>
         */
        public class Defining_lifestyle {

            /**
             * <p>
             * The default lifestyle is Prototype, it creates a new instance on demand. This is
             * applied automatically and you have to configure nothing.
             * </p>
             * <pre>{@link PrototypeTest#prototype()}</pre>
             */
            public class Prototype {
            }

            /**
             * <p>
             * The other is the singleton lifestyle, which keeps a single instance in the JVM and
             * always returns it. This time, the lifestyle is applied with annotations when defining
             * the class.
             * </p>
             * <pre>{@link SingletonTest#singleton()}</pre>
             */
            public class Singleton {
            }

            /**
             * <p>
             * You can also implement lifestyles tied to specific contexts. Custom class requires to
             * implement the Lifestyle interface and receive the requested type in the constructor.
             * I'm using {@link I#prototype(Class)} here to make Dependency Injection work, but you
             * can use any instantiation technique.
             * </p>
             * <pre>{@link PerThread}</pre> <pre>{@link #perThread()}</pre>
             */
            public class Custom_lifestyle {

                class PerThread<T> implements Lifestyle<T> {
                    private final ThreadLocal<T> local;

                    PerThread(Class<T> requestedType) {
                        // use sinobu's default instance builder
                        Lifestyle<T> prototype = I.prototype(requestedType);

                        // use ThreadLocal as contextual instance holder
                        local = ThreadLocal.withInitial(prototype::get);
                    }

                    @Override
                    public T call() throws Exception {
                        return local.get();
                    }
                }

                public void perThread() {
                    @Managed(PerThread.class)
                    class User {
                    }

                    // create contextual user
                    User user1 = I.make(User.class);
                    User user2 = I.make(User.class);
                    assert user1 == user2; // same

                    new Thread(() -> {
                        User user3 = I.make(User.class);
                        assert user1 != user3; // different
                    }).run();
                }
            }

            /**
             * <p>
             * Sinobu has built-in defined lifestyles for specific types.
             * </p>
             * <dl>
             * <dt>{@link List}
             * <dd>Create new instance each time. ({@link ArrayList})</dd>
             * <dt>{@link Set}
             * <dd>Create new instance each time. ({@link HashSet})</dd>
             * <dt>{@link Map}
             * <dd>Create new instance each time. ({@link HashMap})</dd>
             * <dt>{@link Locale}
             * <dd>Always returns the instance retrieved from {@link Locale#getDefault()}.</dd>
             * </dl>
             */
            public class Builtin_lifestyles {
            }
        }

        /**
         * <p>
         * To apply a non-prototype lifestyle, you need to configure each class individually. There
         * are two ways to do this.
         * </p>
         */
        public class Applying_lifestyle {

            /**
             * <p>
             * One is to use {@link Managed} annotation. This method is useful if you want to apply
             * lifestyle to classes that are under your control.
             * </p>
             * <pre>{@link UnderYourControl}</pre>
             */
            public class Use_Managed_annotation {

                @Managed(Singleton.class)
                class UnderYourControl {
                }
            }

            /**
             * <p>
             * Another is to load the Lifestyle implementation. Sinobu has a wide variety of
             * extension points, and Lifestyle is one of them. This method is useful if you want to
             * apply lifestyle to classes that are not under your control.
             * </p>
             * <pre>{@link GlobalThreadPool}</pre> <pre>{@link #loadLifestyle()}</pre>
             */
            public class Use_Lifestyle_extension {
                class GlobalThreadPool implements Lifestyle<Executor> {

                    private static final Executor pool = Executors.newCachedThreadPool();

                    @Override
                    public Executor call() throws Exception {
                        return pool;
                    }
                }

                public void loadLifestyle() {
                    I.load(GlobalThreadPool.class);
                }
            }
        }
    }

    public class Dependency_Injection {
        /**
         * <p>
         * Dependency Injection (DI) is a mechanism that solves various problems related to
         * component dependencies in "a nice way". Component dependency refers to the relationship
         * from upper layer to lower layer, such as Controller → Service → Repository in a general
         * layered architecture. "A nice way" means that the framework will take care of the problem
         * without the developer having to work hard manually. In modern Java application
         * development, DI is an almost indispensable mechanism.
         * </p>
         */
        public class The_need_for_Dependency_Injection {
        }

        public class Circular_Reference {
        }
    }

    public class HTTP {
        public class Request_and_Response {
        }

        public class Supported_Type {
        }

        public class Cookie {
        }

        public class Authentication {
        }
    }

    public class JSON {

        /**
         * <p>
         * You can read JSON from strings, files, and various inputs. All data will be expanded into
         * memory in a tree format. It is not a streaming format, so please be careful when parsing
         * very large JSON.
         * </p>
         */
        public class Reading_JSON {

            /**
             * <p>
             * You can access the value by specifying the key.
             * </p>
             * <pre>{@link ManipulateTest#readValue()}</pre>
             */
            public class Access_to_the_value {
            }

            /**
             * <p>
             * You can specify a key multiple times to access nested values.
             * </p>
             * <pre>{@link ManipulateTest#readNestedValue()}</pre>
             * <p>
             * You can also find all values by the sequential keys.
             * </p>
             * <pre>{@link ManipulateTest#readNestedValueBySequentialKeys()}</pre>
             */
            public class Access_to_the_nested_value {
            }
        }

        public class Writing_JSON {
        }

        public class Mapping_to_Model {
        }

        public class Mapping_from_Model {
        }
    }

    public class HTML {
        public class Parsing {
        }

        public class Writing {
        }

        public class Support_CSS_Selector {
        }

        public class Manipulation {
        }

        public class Traverse {
        }
    }

    public class ReactiveX {

        public class Signal {
        }

        public class Subscribe {
        }

        public class Unsubscribe {
        }

        public class Operators {
        }
    }

    public class Template_Literal {

        /**
         * <p>
         * Java19 does not yet have a language built-in template syntax. Therefore, Sinobu provides
         * a mechanism to parse Mustache syntax instead.
         * </p>
         * <p>
         * A Mustache template is a type of template system used in web applications to embed data
         * in HTML. Mustache is supported by a variety of programming languages and frameworks, and
         * can be used to display data in various markup languages such as HTML and XML. In addition
         * to displaying data, Mustache allows for features such as conditional branching and
         * looping, and can even include external templates. As such, Mustache templates are a
         * useful tool for more efficiently developing applications.
         * </p>
         */
        public class What_is_Mustache_ {

            /**
             * <p>
             * To use Mustache, you must first create a Mustache template, which is written using a
             * markup language such as HTML or XML. Within this template, special symbols called
             * Mustache specifiers are used to specify where to embed the data.
             * </p>
             * <p>
             * Next, within the program that uses the Mustache template, the Mustache engine is
             * initialized. At this point, the Mustache engine is passed the template and the data.
             * The data is written using data structures such as JavaScript objects.
             * </p>
             * <p>
             * Finally, the Mustache engine is used to render the Mustache template. At this time,
             * the Mustache engine replaces the Mustache specifier in the template and populates the
             * data to produce a finished HTML, XML, or other markup language document.
             * </p>
             * <p>
             * The specific usage varies depending on the programming language and framework, but
             * the above steps are a rough outline of the basic procedure for using Mustache.
             * </p>
             * <pre>{@code 
             *  <p></p>
             * }</pre>
             */
            public class Usage_in_general {
            }

            /**
             * <p>
             * In SInobu, Mustache can be used by calling the {@link I#express(String, Object...)}
             * method. This method parses the given string, reads the necessary variables from the
             * context, substitutes them, and returns the resulting string.
             * </p>
             */
            public class Usage_at_Sinobu {
            }
        }

        /**
         * <p>
         * The syntax of Mustache is very simple. In a Mustache template, you use special symbols
         * called Mustache delimiters to specify where the data should be inserted. Mustache
         * delimiters are written in the following format:
         * </p>
         * <pre>{@code 
         *  {placeholder}
         * }</pre>
         * <p>
         * As you can see, a Mustache delimiter is a string of characters enclosed in double braces,
         * such as "{placeholder}". This string specifies the location where the data should be
         * inserted. For example, consider the following Mustache template:
         * </p>
         * <pre>{@code 
        *  <h1>{title}</h1>
        * }</pre>
         * <p>
         * In this template, the string "{title}" is a Mustache delimiter that represents a
         * placeholder called "title". When using this template, you need to provide data for the
         * "title" placeholder. For instance, you might have the following JavaScript object as the
         * data:
         * </p>
         * <pre language="js">{@code 
         *  var data = {
         *      title: "Hello, world!"
         *  }
         * }</pre>
         */
        public class Syntax {
        }

        public class Section {
        }

        public class Comment {
        }
    }

    public class Plugin {

        /**
         * <pre>{@link ExtensionTest}</pre>
         */
        public class Usage {
        }

        /**
         * <p>
         * Sinobu has a general-purpose plug-in mechanism for extending application functions. An
         * extensible place is called Extension Point, and its substance is a type (interface or
         * class) marked with the {@link Extensible} interface.
         * </p>
         * <p>
         * We give a definition of <em>Extension Point</em> like the following.
         * </p>
         * <ul>
         * <li>It implements {@link Extensible} interface directly.</li>
         * </ul>
         * <pre>{@link ThisIsExtensionPoint}</pre><pre>{@link ThisIsAlsoExtensionPoint}</pre><pre>{@link ThisIsNotExtensionPoint}</pre>
         * <p>
         * In the usage example, Codec is the extension point that converts any object to a string
         * representation.
         * </p>
         * <pre>{@link Codec}</pre>
         */
        public class Extension_Point {

            interface ThisIsExtensionPoint extends Extensible {
            }

            interface ThisIsNotExtensionPoint extends ThisIsExtensionPoint {
            }

            class ThisIsAlsoExtensionPoint implements Extensible {
                // This is both Extension Point and Extension.
            }
        }

        /**
         * <p>
         * We give a definition of <em>Extension</em> like the following.
         * </p>
         * <ul>
         * <li>It implements any Extension Point or is Extension Point itself.</li>
         * <li>It must be concrete class and has a suitable constructor for Sinobu (see also
         * {@link I#make(Class)} method).</li>
         * </ul>
         * <pre>{@link ThisIsExtension}</pre><pre>{@link ThisIsAlsoExtension}</pre><pre>{@link ThisIsNotExtension}</pre>
         * <p>
         * In the usage example, LocalDateCodec is the extension that is special implementation for
         * {@link LocalDate}.
         * </p>
         * <pre>{@link LocalDateCodec}</pre>
         */
        public class Extension {
            class ThisIsExtension implements Extensible {
                // This is both Extension Point and Extension.
            }

            class ThisIsAlsoExtension extends ThisIsExtension {
                // But not Extension Point.
            }

            class ThisIsNotExtension extends ThisIsExtension {

                public ThisIsNotExtension(NotInjectable object) {
                    // because of invalid constructor
                }
            }

            private class NotInjectable {
            }
        }

        /**
         * <p>
         * You can provide <em>Extension Key</em> for each Extensions by using parameter. The key
         * makes easy finding an Extension you need (see also {@link I#find(Class, Class)}).
         * </p>
         * <pre>{@link ExtensionPointWithKey}</pre><pre>{@link ExtensionWithKey}</pre><pre>{@link ExtensionWithAnotherKey}</pre>
         */
        public class Extension_Key {
            interface ExtensionPointWithKey<K> extends Extensible {
            }

            class ExtensionWithKey implements ExtensionPointWithKey<String> {
                // Associate this Extension with String class.
            }

            class ExtensionWithAnotherKey implements ExtensionPointWithKey<List> {
                // Associate this Extension with List interface.
            }
        }

        /**
         * <p>
         * All extensions are not recognized automatically, you have to load them explicitly using
         * {@link I#load(Class)}.
         * </p>
         * <pre>{@link ExtensionUser}</pre><pre>{@link ApplicationMain}</pre>
         */
        public class Dynamic_Loading {
            class ExtensionUser {
                static {
                    I.load(ExtensionUser.class);
                }

                // write your code
            }

            class ApplicationMain {
                public static void main(String[] args) {
                    I.load(ApplicationMain.class);

                    // start your application
                }
            }
        }

        public class Query_Extension {
        }
    }

    public class DSL {
        public class Tree_Structured_Data {
        }

        public class Node_and_Leaf {
        }

        public class HTML_Sample {
        }
    }

    public class Persistence {
        public class Save_Data {
        }

        public class Save_Automatically {
        }

        public class Config_Location {
        }
    }
}