/*
 * Copyright (C) 2024 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package doc;

import java.io.Writer;
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
import java.util.function.Consumer;

import org.w3c.dom.Node;

import doc.ExtensionTest.Codec;
import doc.ExtensionTest.LocalDateCodec;
import doc.MustacheTest.Person;
import kiss.Disposable;
import kiss.Extensible;
import kiss.I;
import kiss.Lifestyle;
import kiss.Managed;
import kiss.Model;
import kiss.Observer;
import kiss.Signal;
import kiss.Singleton;
import kiss.Variable;
import kiss.XML;
import kiss.instantiation.ConstructorInjectionTest;
import kiss.instantiation.ConstructorInjectionTest.CircularLifestyleA;
import kiss.instantiation.ConstructorInjectionTest.CircularLifestyleB;
import kiss.json.JSONWriteTest;
import kiss.json.ManipulateTest;
import kiss.lifestyle.PrototypeTest;
import kiss.lifestyle.SingletonTest;
import kiss.model.ModelLensTest;
import kiss.xml.HTMLSoupTest;
import kiss.xml.ReadTest;
import kiss.xml.XMLFindTest;
import kiss.xml.XMLWriterTest;

public class DocumentDoc {

    static {
        I.load(DocumentDoc.class);
    }

    public class Introduction {
        /**
         * 
         * Sinobu is not obsolete framework but utility, which can manipulate objects as a
         * extremely-condensed facade.
         * This is extremely lightweight at approximately 120 KB without relying on other libraries,
         * and its various operations are designed to run as fast as other competing libraries.
         * 
         * This library aims to simplify and highly condense the functions related to domains that
         * are frequently encountered in real-world development projects, making them easier to use.
         * 
         * - [Dependency Injection](https://en.wikipedia.org/wiki/Dependency_injection)
         * - Object lifecycle management
         * - Property based object modeling
         * - HTTP(S) Client
         * - Web Socket Client
         * - [JSON](https://en.wikipedia.org/wiki/JSON)
         * - [HTML](https://en.wikipedia.org/wiki/HTML) (including Tag Soup)
         * - [XML](https://en.wikipedia.org/wiki/XML)
         * - Reactive Programming ([Rx](http://reactivex.io))
         * - Asynchronous & Parallel processing
         * - Multilingualization
         * - Template Engine ([Mustache](https://mustache.github.io/mustache.5.html))
         * - Dynamic plug-in mechanism
         * - Object persistence
         * - Logging (Garbage-Free)
         * - Virtual Job Scheduler
         * - [Cron](https://en.wikipedia.org/wiki/Cron) Scheduling
         * 
         * With a few exceptions, Sinobu and its APIs are designed to be simple to use and easy to
         * understand by adhering to the following principles.
         * 
         * - [Keep it stupid simple](https://en.wikipedia.org/wiki/KISS_principle)
         * - [Less is more](https://en.wikipedia.org/wiki/Less_is_more_(architecture))
         * - [Type safety](https://en.wikipedia.org/wiki/Type_safety)
         * - Refactoring safety
         */
        public class Purpose_of_use {
        }

        /**
         * It is probably easiest to use a build tool such as
         * [Maven](https://maven.apache.org) or [Gradle](https://gradle.org).
         * 
         * {@snippet lang = xml :
         * <dependency>
         *     <groupId>{@var project}</groupId>
         *     <artifactId>{@var product}</artifactId>
         *     <version>{@var version}<version>
         * </dependency>
         * }
         */
        public class How_to_install {
        }
    }

    public class Managing_object_lifestyle {

        /**
         * In Sinobu, lifestyle refers to the way an object is created and managed, corresponding to
         * the scope in terms of DI containers such as SpringFramework and Guice, but without the
         * process of registering with the container or destroying the object.
         */
        public class What_do_you_mean_by_lifestyle_ {

            /**
             * In Java, it is common to use the new operator on the constructor to create a new
             * object. In many cases, this is sufficient, but in the following situations, it is a
             * bit insufficient.
             * 
             * - To manage the number of objects to be created.
             * - To create objects associated with a specific context.
             * - To generate objects with complex dependencies.
             * - The type of the object to be generated is not statically determined.
             * 
             * While DI containers such as SpringFramework or Guice are commonly used to deal with
             * such problems, Sinobu comes with its own very simple DI container. The following code
             * shows the creation of an object using DI container.
             * 
             * {@link #createObject() @}
             * 
             * As you can see from the above code, there is no actual container object; Sinobu has
             * only one global container in the JVM, and that object cannot be accessed directly. In
             * order to create an object from a container, we need to call {@link I#make(Class)}.
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
         * In order to define a lifestyle, you need to implement Lifestyle interface. This interface
         * is essentially equivalent to Callable. It is called when container requests the specific
         * type. It makes the following 3 decisions:
         * 
         * 1. Which class to instantiate actually.
         * 2. How to instantiate it.
         * 3. How to manage the instances.
         * 
         * Sinobu defines two lifestyles that are frequently used. One is the prototype pattern and
         * the other is the singleton pattern.
         */
        public class Defining_lifestyle {

            /**
             * The default lifestyle is Prototype, it creates a new instance on demand. This is
             * applied automatically and you have to configure nothing.
             * 
             * {@link PrototypeTest#prototype() @}
             */
            public class Prototype {
            }

            /**
             * The other is the singleton lifestyle, which keeps a single instance in the JVM and
             * always returns it. This time, the lifestyle is applied with annotations when defining
             * the class.
             * 
             * {@link SingletonTest#singleton() @}
             */
            public class Singleton {
            }

            /**
             * You can also implement lifestyles tied to specific contexts. Custom class requires to
             * implement the Lifestyle interface and receive the requested type in the constructor.
             * I'm using {@link I#prototype(Class)} here to make Dependency Injection work, but you
             * can use any instantiation technique.
             * 
             * {@link PerThread @}
             * {@link #perThread() @}
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
                    }).start();
                }
            }

            /**
             * Sinobu has built-in defined lifestyles for specific types.
             * 
             * - {@link List} - Create new instance each time. ({@link ArrayList})
             * - {@link Set} - Create new instance each time. ({@link HashSet})
             * - {@link Map} - Create new instance each time. ({@link HashMap})
             * - {@link Locale} - Always returns the instance retrieved from
             * {@link Locale#getDefault()}.
             */
            public class Builtin_lifestyles {
            }
        }

        /**
         * To apply a non-prototype lifestyle, you need to configure each class individually. There
         * are two ways to do this.
         */
        public class Applying_lifestyle {

            /**
             * One is to use {@link Managed} annotation. This method is useful if you want to apply
             * lifestyle to classes that are under your control.
             * 
             * {@link UnderYourControl @}
             */
            public class Use_Managed_annotation {

                @Managed(Singleton.class)
                class UnderYourControl {
                }
            }

            /**
             * Another is to load the Lifestyle implementation. Sinobu has a wide variety of
             * extension points, and Lifestyle is one of them. This method is useful if you want to
             * apply lifestyle to classes that are not under your control.
             * 
             * {@link GlobalThreadPool @}
             * {@link #loadLifestyle() @}
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
         * Dependency Injection (DI) is a mechanism that solves various problems related to
         * component dependencies in 'a nice way'. Component dependency refers to the relationship
         * from upper layer to lower layer, such as Controller → Service → Repository in a general
         * layered architecture.
         * 'A nice way' means that the framework will take care of the problem without the developer
         * having to work hard manually.
         * 
         * In modern Java application development, DI is an almost indispensable mechanism.
         * The detailed explanation of the DI concept is left to [another
         * website](https://en.wikipedia.org/wiki/Dependency_injection).
         */
        public class Concept {

            /**
             * Unlike other DI frameworks, there is no explicit DI container in Sinobu. It has only
             * one container implicitly inside, but the user is not aware of it. Therefore, there is
             * also no need to define dependencies explicitly by means of code or external files.
             * All dependencies are automatically resolved based on type.
             */
            public class DI_Container {
            }

            /**
             * Commonly, there are four main types in which a client can receive injected services.
             * 
             * - Constructor injection, where dependencies are provided through a client's class
             * constructor.
             * - Setter injection, where the client exposes a setter method which accepts the
             * dependency.
             * - Field injection, where the client exposes a field which accepts the dependency.
             * - Interface injection, where the dependency's interface provides an injector method
             * that will inject the dependency into any client passed to it.
             * 
             * Of these, **only constructor injection is supported** by Sinobu. Other injection
             * types will **not be supported in the future** due to their significant disruption
             * to object safety.
             */
            public class Injection_Type {
            }
        }

        /**
         * The most common form of dependency injection is for a class to request its
         * dependencies through its constructor. This ensures the client is always in a valid
         * state, since it cannot be instantiated without its necessary dependencies.
         * 
         * {@link ConstructorInjectionTest#objectInjection() @}
         */
        public class Constructor_Injection {

            /**
             * Any type can be injectable, but there are a few types that receive special treatment.
             * 
             * - Primitives - A default value (0 for int, false for boolean) is assigned.
             * {@link ConstructorInjectionTest#primitiveIntInjection() @}
             * {@link ConstructorInjectionTest#primitiveBooleanInjection() @}
             * - {@link Lifestyle} - The resolution of dependencies can be delayed until the user
             * actually needs it. Type variables must be correctly specified.
             * {@link ConstructorInjectionTest#objectLazyInjection() @}
             * - {@link Class} - The currently processing model type. This feature is mainly
             * available when implementing the special generic {@link Lifestyle}.
             * {@link ConstructorInjectionTest#typeInjection() @}
             * 
             */
            public class Injectable_Type {
            }

            /**
             * If only one constructor is defined for the class being injected, it is used. If more
             * than one constructor is defined, it must detect which constructor is to be used.
             * 
             * The constructor with the {@link Managed} annotation has the highest priority.
             * {@link ConstructorInjectionTest#priorityManaged() @}
             * 
             * Next priority is given to constructors with the Inject annotation. The Inject
             * annotation targets all annotations with the simple name 'Inject', so annotations such
             * as jakarta.inject.Inject used in JSR330 etc. can also be used.
             * {@link ConstructorInjectionTest#priorityInject() @}
             * 
             * If no annotation is found, the constructor with the lowest number of arguments is
             * used.
             * {@link ConstructorInjectionTest#priorityMinParam() @}
             * 
             * If several constructors with the smallest number of arguments are defined, the first
             * constructor found among them is used. (which is implementation-dependent in the JDK)
             * {@link ConstructorInjectionTest#priorityMinParams() @}
             */
            public class Priority {
            }

            /**
             * One of the problems with constructor injection is that it cannot resolve circular
             * dependencies. To partially solve this problem, Sinobu provides a delayed dependency
             * injection method, but it does not completely solve any situation. If a solution is
             * not possible, an error will occur.
             * 
             * {@link CircularLifestyleA @}
             * {@link CircularLifestyleB @}
             * {@link ConstructorInjectionTest#circularDependenciesWithProvider() @}
             */
            public class Circular_Reference {
            }
        }
    }

    public class Model_and_Property {
        /**
         * In the context of Java programming, a 'property' generally refers to the characteristics
         * or attributes of a class or object. These properties define the state of an object and
         * encapsulate the data associated with it. In Java, properties are typically represented as
         * private fields within a class, with corresponding getter and setter methods to access and
         * modify their values.
         * 
         * Here's an example of a Java class with properties:
         * {@link Person @}
         * 
         * In this example, the Person class has two properties: name and age. These properties are
         * declared as private fields within the class to encapsulate their implementation details
         * and prevent direct access from outside the class. Getter and setter methods (getName(),
         * setName(), getAge(), setAge()) are provided to allow controlled access to these
         * properties.
         * 
         * Using properties in Java classes promotes encapsulation, which is one of the fundamental
         * principles of object-oriented programming (OOP). Encapsulation hides the internal state
         * of an object and exposes only the necessary interfaces for interacting with it, improving
         * code maintainability, reusability, and flexibility.
         */
        public class Concept {

            class Person {
                private String name; // Property: name

                private int age; // Property: age

                // Constructor
                public Person(String name, int age) {
                    this.name = name;
                    this.age = age;
                }

                // Getter method for name property
                public String getName() {
                    return name;
                }

                // Setter method for name property
                public void setName(String name) {
                    this.name = name;
                }

                // Getter method for age property
                public int getAge() {
                    return age;
                }

                // Setter method for age property
                public void setAge(int age) {
                    this.age = age;
                }
            }
        }

        /**
         * In Sinobu, model is a set of properties. If a class you define has properties, it is
         * already a model.
         * 
         * Models can be retrieved from a class using the {@link kiss.Model#of(Class)} method, but
         * there should not be many situations where the end user needs to retrieve the model
         * directly.
         * {@link Models#model @}
         */
        public class Models {

            Model model = Model.of(Person.class);
        }

        /**
         * In Sinobu, a property refers to a value accessible by name and defined by a field or
         * method. Property name is arbitrary.
         */
        public class Property {

            /**
             * Property definition by field.
             * {@link By_Field#filedProperty @}
             * 
             * With the final modifier, the value cannot be changed and is therefore not treated as
             * a property.
             * {@link By_Field#finalField @}
             * 
             * Access modifiers other than public are not treated as properties.
             * {@link By_Field#nonPublicField @}
             * 
             * If you want to treat fields with access modifiers other than public as properties,
             * use the {@link Managed} annotation.
             * {@link By_Field#managedField @}
             */
            public class By_Field {

                public String filedProperty = "This is property";

                public final String finalField = "This is NOT property";

                protected String nonPublicField = "This is NOT property";

                @Managed
                protected String managedField = "This is property";
            }

            /**
             * Property definition by {@link Variable} field.
             * {@link By_Variable_Field#variableField @}
             * 
             * Unlike normal fields, they are treated as properties even if they have final
             * modifier.
             * {@link By_Variable_Field#finalField @}
             * 
             * Access modifiers other than public are not treated as properties.
             * {@link By_Variable_Field#nonPublicField @}
             * 
             * If you want to treat fields with access modifiers other than public as properties,
             * use the {@link Managed} annotation.
             * {@link By_Variable_Field#managedField @}
             */
            public class By_Variable_Field {

                public Variable<String> variableField = Variable.of("This is property");

                public final Variable<String> finalField = Variable.of("This is property");

                protected Variable<String> nonPublicField = Variable.of("This is NOT property");

                @Managed
                protected Variable<String> managedField = Variable.of("This is property");
            }

            /**
             * Property definition by method. The getter and setter methods must be defined
             * according to the Java Bean naming conventions.
             * {@link GetterAndSetter @}
             * 
             * The getter and setter methods do not need to have a public modifier.
             * {@link NonPublicGetterAndSetter @}
             */
            public class By_Method {

                class GetterAndSetter {
                    private String property = "This is property";

                    public String getProperty() {
                        return property;
                    }

                    public void setProperty(String property) {
                        this.property = property;
                    }
                }

                class NonPublicGetterAndSetter {
                    private String property = "This is property";

                    String getProperty() {
                        return property;
                    }

                    void setProperty(String property) {
                        this.property = property;
                    }
                }
            }
        }

        /**
         * Normally, end users do not use {@link Model} API to manipulate or traverse properties.
         * The following is mainly required for framework and library production.
         * 
         * Models and properties can be used to get, set and monitor property values.
         */
        public class Manipulation {

            /**
             * Get the value by name.
             * {@link ModelLensTest#getProperty() @}
             */
            public class Get {
            }

            /**
             * Set the value by name.
             * {@link ModelLensTest#setProperty() @}
             *
             * If you set a property value, it is recommended that you reassign the return value to
             * the object. This is necessary because the Record is immutable.
             * {@link ModelLensTest#setAtRecord() @}
             */
            public class Set {
            }

            /**
             * Monitor the {@link Variable} value.
             * {@link ModelLensTest#observeVariableProperty() @}
             */
            public class Monitor {
            }
        }
    }

    public class ReactiveX {
        /**
         * The concept of ReactiveX is very well summarized on the official website, so it is better
         * to read there.
         * 
         * - [ReactiveX](https://reactivex.io/intro.html)
         * - [Observable](https://reactivex.io/documentation/observable.html)
         * - [Operators](https://reactivex.io/documentation/operators.html)
         * 
         * The {@link Signal} class that implements the Reactive Pattern. This class provides
         * methods for subscribing to the {@link Signal} as well as delegate methods to the various
         * observers.
         * 
         * In Reactive Pattern an observer subscribes to a {@link Signal}. Then that observer reacts
         * to whatever item or sequence of items the {@link Signal} emits. This pattern facilitates
         * concurrent operations because it does not need to block while waiting for the
         * {@link Signal}
         * to emit objects, but instead it creates a sentry in the form of an observer that stands
         * ready to react appropriately at whatever future time the {@link Signal} does so.
         * 
         * The subscribe method is how you connect an {@link Observer} to a {@link Signal}. Your
         * {@link Observer} implements some subset of the following methods:
         * 
         * - {@link Observer#accept(Object)} - A {@link Signal} calls this method whenever the
         * {@link Signal} emits an item. This method takes as a parameter the item emitted by the
         * {@link Signal}.
         * - {@link Observer#error(Throwable)} - A {@link Signal} calls this method to indicate that
         * it has failed to generate the expected data or has encountered some other error. It will
         * not make further calls to {@link Observer#error(Throwable)} or
         * {@link Observer#complete()}. The {@link Observer#error(Throwable)} method takes as its
         * parameter an indication of what caused the error.
         * - {@link Observer#complete()} - A {@link Signal} calls this method after it has called
         * {@link Observer#accept(Object)} for the final time, if it has not encountered any errors.
         * 
         * By the terms of the {@link Signal} contract, it may call {@link Observer#accept(Object)}
         * zero or more times, and then may follow those calls with a call to either
         * {@link Observer#complete()} or {@link Observer#error(Throwable)} but not both, which will
         * be its last call. By convention, in this document, calls to
         * {@link Observer#accept(Object)} are usually called &ldquo;emissions&rdquo; of items,
         * whereas calls to {@link Observer#complete()} or {@link Observer#error(Throwable)} are
         * called &ldquo;notifications.&rdquo;
         */
        public class Concept {
        }

        public class Subscribe {
        }

        public class Unsubscribe {
        }

        public class Operators {
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
         * You can read JSON from strings, files, and various inputs. All data will be expanded
         * into memory in a tree format. It is not a streaming format, so please be careful when
         * parsing very large JSON.
         */
        public class Reading_JSON {

            /**
             * You can access the value by specifying the key.
             * 
             * {@link ManipulateTest#readValue() @}
             */
            public class Access_to_the_value {
            }

            /**
             * You can specify a key multiple times to access nested values.
             * {@link ManipulateTest#readNestedValue() @}
             * 
             * You can also find all values by the sequential keys.
             * {@link ManipulateTest#readNestedValueBySequentialKeys() @}
             */
            public class Access_to_the_nested_value {
            }
        }

        /**
         * You can write JSON from property-based model.
         * 
         * {@link JSONWriteTest#testName() @}
         */
        public class Writing_JSON {
        }

        public class Mapping_to_Model {
        }

        public class Mapping_from_Model {
        }
    }

    public class HTML {
        /**
         * Sinobu provides functionality for handling HTML and XML documents.
         * It uses a built-in, lenient parser (often called a tag soup parser)
         * that can handle malformed HTML, similar to how web browsers do.
         * The core class for this is {@link XML}, which offers a jQuery-like
         * API for traversing and manipulating the document structure.
         * This approach allows for robust parsing of real-world web content.
         */
        public class Concept {
        }

        /**
         * You can parse HTML/XML from various sources using the {@link I} class utility methods.
         * Sinobu automatically detects whether the input is a URL, file path, or raw string.
         * The parser is designed to be tolerant of errors and can handle common issues
         * found in real-world HTML, such as missing tags or incorrect nesting.
         * This makes it suitable for scraping or processing potentially messy markup.
         *
         * - {@link I#xml(String)}
         * - {@link I#xml(java.nio.file.Path)}
         * - {@link I#xml(java.io.InputStream)}
         * - {@link I#xml(java.io.Reader)}
         * - {@link I#xml(Node)}
         * 
         * {@link ReadTest#htmlLiteral() @}
         * 
         * And can parse the invalid structure.
         * {@link HTMLSoupTest#caseInconsistencyLowerUpper() @}
         * {@link HTMLSoupTest#slipOut() @}
         */
        public class Reading {
        }

        /**
         * You can serialize the XML structure back into a string representation or write it to an
         * {@link Appendable} (like {@link Writer} or {@link StringBuilder}).
         * Sinobu offers both compact and formatted output options.
         */
        public class Writing {

            /**
             * The {@link XML#toString()} method provides a compact string representation without
             * extra formatting.
             * {@link XMLWriterTest#format() @}
             */
            public class By_toString {
            }

            /**
             * The {@link XML#to(Appendable, String, String...)} method allows for pretty-printing
             * the XML structure with configurable indentation for improved readability.
             * You can specify the indentation string (e.g., a tab character `\t` or spaces).
             * {@link XMLWriterTest#specialIndent() @}
             * 
             * You can also specify tag names that should be treated as inline elements (no line
             * breaks around them), preserving the original formatting for elements like `<span>` or
             * `<a>`.
             * {@link XMLWriterTest#inlineElement() @}
             *
             * By using the special prefix `&` before a tag name, you can also specify tags that
             * should always be treated as non-empty elements (always have a closing tag like
             * `<script></script>`, even if empty).
             * {@link XMLWriterTest#nonEmptyElement() @}
             * 
             * By passing `null` as the indent character, formatting (indentation and line breaks)
             * is disabled, similar to {@code toString()} but writing to an {@link Appendable}.
             * {@link XMLWriterTest#withoutFormat() @}
             */
            public class By_formatter {
            }
        }

        /**
         * Sinobu leverages CSS selectors for querying elements within the document structure using
         * the {@link XML#find(String)} method. This provides a powerful and familiar way to select
         * nodes, similar to JavaScript's `document.querySelectorAll`.
         * This library supports many standard CSS3 selectors and includes some useful extensions.
         * {@link XMLFindTest#type() @}
         * {@link XMLFindTest#attribute() @}
         * {@link XMLFindTest#clazz() @}
         * {@link XMLFindTest#child() @}
         */
        public class CSS_Selector {

            /// Combinators define the relationship between selectors.
            ///
            /// | Combinator | Description | Notes |
            /// | :---------- | :----------- | :------------ |
            /// | ` ` (Space) | Descendant | Default combinator between selectors |
            /// | `>` | Child | Direct children |
            /// | `+` | Adjacent Sibling | Immediately following sibling |
            /// | `~` | General Sibling | All following siblings |
            /// | `<` | Adjacent Previous Sibling | **Sinobu Extension** |
            /// | `,` | Selector List | Groups multiple selectors |
            /// | `\|` | Namespace Separator | Used with type and attribute selectors |
            ///
            public class Combinators {
            }

            /**
             * Basic selectors target elements based on their type, class, or ID.
             * 
             * | Selector Type | Example | Description |
             * | :------------ | :--------- | :------------------- |
             * | Type | `div` | By element name |
             * | Class | `.warning` | By `class` attribute |
             * | ID | `#main` | By `id` attribute |
             * | Universal | `*` | All elements |
             */
            public class Basic_Selectors {
            }

            /**
             * Attribute selectors target elements based on the presence or value of their
             * attributes.
             * 
             * | Selector Syntax | Description |
             * | :----------------- | :------------------------------------------- |
             * | `[attr]` | Elements with an `attr` attribute |
             * | `[attr=value]` | Elements where `attr` equals `value` |
             * | `[attr~=value]` | Elements where `attr` contains the word `value` |
             * | `[attr*=value]` | Elements where `attr` contains substring `value`|
             * | `[attr^=value]` | Elements where `attr` starts with `value` |
             * | `[attr$=value]` | Elements where `attr` ends with `value` |
             * | `[ns:attr]` | Elements with attribute `attr` in namespace `ns` |
             * | `[ns:attr=value]` | Elements with namespaced attribute and value |
             */
            public class Attribute_Selectors {
            }

            /**
             * Pseudo-classes select elements based on their state, position, or characteristics not
             * reflected by simple selectors. The table includes standard pseudo-classes and kiss
             * library specific extensions (marked as Sinobu Extension).
             * 
             * | Pseudo-Class | Description | Notes |
             * | :------------ | :----------------- | :------------------- |
             * | `:first-child` | First element among siblings | |
             * | `:last-child` | Last element among siblings | |
             * | `:only-child` | Element that is the only child | |
             * | `:first-of-type` | First element of its type among siblings | |
             * | `:last-of-type` | Last element of its type among siblings | |
             * | `:only-of-type` | Element that is the only one of its type | |
             * | `:nth-child(n)` | n-th element among siblings | keyword |
             * | `:nth-last-child(n)` | n-th element among siblings, from last | keyword |
             * | `:nth-of-type(n)` | n-th element of its type among siblings | keyword |
             * | `:nth-last-of-type(n)`| n-th element of type among siblings from last | keyword |
             * | `:empty` | Elements with no children (incl. text) | |
             * | `:not(selector)` | Elements not matching the inner `selector` | |
             * | `:has(selector)` | Elements having a descendant matching `selector` | |
             * | `:root` | Document's root element | |
             * | `:contains(text)` | Elements containing `text` directly | **Sinobu Extension** |
             * | `:parent` | Parent element | **Sinobu Extension** |
             * 
             * Note: User interface state pseudo-classes (like `:hover`, `:focus`, `:checked`) are
             * generally not supported as they relate to browser interactions rather than static
             * document structure analysis.
             */
            public class Pseudo_Class_Selectors {
            }
        }

        /**
         * The {@link XML} object provides a fluent API, similar to jQuery, for modifying the
         * document structure.
         * **Important:** These manipulation methods modify the underlying DOM structure directly;
         * the {@link XML} object itself is mutable in this regard.
         * Explore the nested classes for specific manipulation categories.
         */
        public class Manipulation {
            /**
             * Methods for inserting new content (elements, text, or other XML structures)
             * relative to the selected elements in the document.
             *
             * | Method Link | Description |
             * | :--------------- | :---------------- |
             * | {@link XML#append(Object)} | Insert content at the end of each element. |
             * | {@link XML#prepend(Object)} | Insert content at the beginning of each element.|
             * | {@link XML#before(Object)} | Insert content before each element. |
             * | {@link XML#after(Object)} | Insert content after each element. |
             * | {@link XML#child(String)} | Create and append a new child element. |
             * | {@link XML#child(String, Consumer)} | Create, append, and configure a new child. |
             */
            public class Adding_Content {
            }

            /**
             * Methods for removing content or elements from the document.
             *
             * | Method Link | Description |
             * | :--------------- | :---------------------------------------- |
             * | {@link XML#empty()} | Remove all child nodes from elements. |
             * | {@link XML#remove()} | Remove the selected elements from the DOM.|
             */
            public class Removing_Content {
            }

            /**
             * Methods for wrapping selected elements with new HTML structures.
             *
             * | Method Link | Description |
             * | :------------------- | :----------------------------------------- |
             * | {@link XML#wrap(Object)} | Wrap each selected element individually. |
             * | {@link XML#wrapAll(Object)}| Wrap all elements together with one structure.|
             */
            public class Wrapping {
            }

            /**
             * Methods for getting or setting the text content of elements.
             * Text content represents the combined text of an element and its descendants.
             *
             * | Method Link | Description |
             * | :------------------- | :---------------------------------------- |
             * | {@link XML#text()} | Get the combined text content of elements.|
             * | {@link XML#text(String)}| Set text content, replacing existing. |
             */
            public class Text_Content {
            }

            /**
             * Methods for managing element attributes (e.g., `href`, `src`, `id`).
             *
             * | Method Link | Description |
             * | :-------------------------------- | :------------------------------------------- |
             * | {@link XML#attr(String)} | Get attribute value for the first element. |
             * | {@link XML#attr(String, Object)} | Set attribute value; `null` removes attribute.|
             */
            public class Attributes {
            }

            /**
             * Methods for managing CSS classes on elements. Since the `class` attribute is
             * frequently manipulated, dedicated helper methods are provided for convenience.
             *
             * | Method Link | Description |
             * | :------------------------------- | :------------------------------------- |
             * | {@link XML#addClass(String...)} | Add one or more classes. |
             * | {@link XML#removeClass(String...)}| Remove one or more classes. |
             * | {@link XML#toggleClass(String)} | Add or remove a class based on presence.|
             * | {@link XML#hasClass(String)} | Check if any element has the class. |
             */
            public class CSS_Classes {
            }

            /**
             * Method for duplicating elements, creating a deep copy.
             *
             * | Method Link | Description |
             * | :-------------- | :------------------------------------ |
             * | {@link XML#clone()} | Create a deep copy of selected elements.|
             */
            public class Cloning {
            }
        }

        /**
         * Navigate the DOM tree relative to the currently selected elements.
         * Most traversal methods return a new {@link XML} object containing the resulting elements,
         * allowing for method chaining without modifying the original selection (unless intended).
         * Explore the nested classes for specific traversal categories.
         */
        public class Traversing {

            /**
             * Methods for filtering the current set of selected elements or finding new ones within
             * the current context.
             *
             * | Method Link | Description |
             * | :-------------------- | :-------------------------------------- |
             * | {@link XML#first()} | Reduce the set to the first element. |
             * | {@link XML#last()} | Reduce the set to the last element. |
             * | {@link XML#find(String)}| Find descendants matching the selector. |
             */
            public class Filtering {
            }

            /**
             * Methods for moving vertically up or down the DOM tree, relative to the current
             * elements.
             *
             * | Method Link | Description |
             * | :---------------- | :---------------------------------------- |
             * | {@link XML#parent()} | Get the direct parent of each element in the current set. |
             * | {@link XML#children()} | Get the direct children of each element in the set. |
             * | {@link XML#firstChild()} | Get the first direct child of each element in the set. |
             * | {@link XML#lastChild()} | Get the last direct child of each element in the set. |
             */
            public class Vertical_Movement {
            }

            /**
             * Methods for moving sideways to sibling elements (elements at the same level in the
             * DOM tree).
             *
             * | Method Link | Description |
             * | :-------------- | :----------------------------------------- |
             * | {@link XML#prev()} | Get the immediately preceding sibling. |
             * | {@link XML#next()} | Get the immediately following sibling. |
             */
            public class Horizontal_Movement {
            }

            /**
             * The {@link XML} object implements {@link Iterable}, allowing easy iteration
             * over each selected DOM element individually using a standard Java for-each loop.
             * This is useful for processing each element in a selection.
             * {@link #iterate() @}
             */
            public class Iteration {

                void iterate() {
                    XML elements = I.xml("<div><p>1</p><p>2</p></div>").find("p");

                    for (XML p : elements) {
                        System.out.println(p.text());
                    }
                }
            }
        }
    }

    public class Template_Literal {

        /**
         * {@link MustacheTest @}
         */
        public class Usage {
        }

        /**
         * Mustache can be used for HTML, config files, source code - anything. It works by
         * expanding tags in a template using values provided in a hash or object. We call it
         * "logic-less" because there are no if statements, else clauses, or for loops. Instead
         * there are only tags. Some tags are replaced with a value, some nothing, and others a
         * series of values.
         * 
         * Java19 does not yet have a language built-in template syntax. Therefore, Sinobu
         * provides
         * a mechanism to parse <a href="https://mustache.github.io/">Mustache</a> syntax
         * instead.
         */
        public class Mustache {

            /**
             * To use Mustache, you must first create a Mustache template, which is written
             * using a
             * markup language such as HTML or XML. In template, you use special symbols called
             * Mustache delimiters to specify where the data should be inserted. Mustache
             * delimiters
             * are written in the following format:
             * {@code {placeholder} @}
             * 
             * As you can see, Mustache delimiter is a string of characters enclosed in single
             * brace, such as "&#123;placeholder&#125;". This string specifies the location
             * where
             * the data should be inserted. For example, consider the following Mustache
             * template:
             * {@link MustacheTest#template @}
             * 
             * When using this template, you need to provide data for placeholders. For
             * instance,
             * you might have the following JavaBean object as data:
             * {@link Person @}
             * 
             * Passing the template string and context data to method
             * {@link I#express(String, Object...)}, we get a string in which the various
             * placeholders are replaced by its data.
             * {@linkplain MustacheTest#use() @}
             * 
             * Next, within the program that uses the Mustache template, the Mustache engine is
             * initialized. At this point, the Mustache engine is passed the template and the
             * data.
             * The data is written using data structures such as JavaScript objects.
             * 
             * Finally, the Mustache engine is used to render the Mustache template. At this
             * time,
             * the Mustache engine replaces the Mustache specifier in the template and populates
             * the
             * data to produce a finished HTML, XML, or other markup language document.
             * 
             * The specific usage varies depending on the programming language and framework,
             * but
             * the above steps are a rough outline of the basic procedure for using Mustache.
             */
            public class Syntax {
            }

            /**
             * In SInobu, Mustache can be used by calling the
             * {@link I#express(String, Object...)}
             * method. This method parses the given string, reads the necessary variables from
             * the
             * context, substitutes them, and returns the resulting string.
             */
            public class Usage_at_Sinobu {
            }
        }

        public class Section {
        }

        public class Comment {
        }
    }

    public class Plugin {

        /**
         * {@link ExtensionTest @}
         */
        public class Usage {
        }

        /**
         * Sinobu has a general-purpose plug-in mechanism for extending application functions.
         * An
         * extensible place is called Extension Point, and its substance is a type (interface or
         * class) marked with the {@link Extensible} interface.
         * 
         * We give a definition of <em>Extension Point</em> like the following.
         * 
         * - It implements {@link Extensible} interface directly.
         * 
         * {@link ThisIsExtensionPoint @}
         * {@link ThisIsAlsoExtensionPoint @}
         * {@link ThisIsNotExtensionPoint @}
         * 
         * In the usage example, Codec is the extension point that converts any object to a
         * string
         * representation.
         * {@link Codec @}
         */
        public class Extension_Point {

            interface ThisIsExtensionPoint extends Extensible {
            }

            interface ThisIsNotExtensionPoint extends ThisIsExtensionPoint {
            }

            class ThisIsAlsoExtensionPoint implements Extensible {
                // This class is both Extension Point and Extension.
            }
        }

        /**
         * We give a definition of <em>Extension</em> like the following.
         * 
         * - It implements any Extension Point or is Extension Point itself.
         * - It must be concrete class and has a suitable constructor for Sinobu (see also
         * {@link I#make(Class)} method).
         * 
         * {@link ThisIsExtension @}
         * {@link ThisIsAlsoExtension @}
         * {@link ThisIsNotExtension @}
         * 
         * In the usage example, LocalDateCodec is the extension that is special implementation
         * for
         * {@link LocalDate}.
         * {@link LocalDateCodec @}
         */
        public class Extension {
            class ThisIsExtension implements Extensible {
                // This class is both Extension Point and Extension.
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
         * You can provide <em>Extension Key</em> for each Extensions by using parameter.
         * 
         * {@link ExtensionPointWithKey @}
         * {@link ExtensionWithStringKey @}
         * {@link ExtensionWithListKey @}
         * 
         * The key makes easy finding an Extension you need (see also
         * {@link I#find(Class, Class)}).
         * {@link Extension_Key#findExtensionByKey() @}
         */

        public class Extension_Key {
            interface ExtensionPointWithKey<K> extends Extensible {
            }

            class ExtensionWithStringKey implements ExtensionPointWithKey<String> {
                // Associate this Extension with String class.
            }

            class ExtensionWithListKey implements ExtensionPointWithKey<List> {
                // Associate this Extension with List interface.
            }

            void findExtensionByKey() {
                assert I.find(ExtensionPointWithKey.class, String.class) instanceof ExtensionWithStringKey;
            }
        }

        /**
         * All extensions are not recognized automatically, you have to load them explicitly
         * using
         * {@link I#load(Class)}.
         * {@link ExtensionUser @}
         * {@link ApplicationMain @}
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
    }

    public class Persistence {
        public class Save_Data {
        }

        public class Save_Automatically {
        }

        public class Config_Location {
        }
    }

    public class Cron {

        /**
         * Cron is a job scheduler for UNIX-like operating systems, developed by Ken Thompson of
         * the Bell Labs in 1975. The name derives from the Greek word ‘chronos’ (time). It was
         * initially created to automate administrative tasks on UNIX systems, but is now used
         * in a wide range of applications.
         * 
         * Cron has the following characteristics:
         * 
         * - Routine tasks such as system administration tasks, backups and log rotation can be
         * automated.
         * - It is a standard feature of UNIX/Linux-based systems and has developed into an
         * important element in system administration.
         * - Complex execution schedules, from minutes to years, can be expressed concisely.
         * 
         * Cron is used in modern systems in a variety of situations, including
         * 
         * - It is also used by AWS CloudWatch Events, Google Cloud Scheduler and other cloud
         * services.
         * - It is also standard in many modern tools, such as Jenkins and Kubernetes CronJob.
         * - It is used to control batch processing and scheduled tasks in microservice
         * architectures.
         */
        public class Features_and_Roles {
        }

        ///  ## Format
        /// 
        /// A Cron expression consists of five or six fields, separated by spaces.
        /// From left to right, they represent seconds, minutes, hours, days, months, and days of the week, with seconds being optional.
        /// The possible values for each field are as follows, and some special function characters can also be used.
        ///
        /// If you want to specify 9:00 every morning, it would look like this
        /// ```
        /// 0 0 9 * * *
        /// ```
        ///
        /// Seconds field is optional and can be omitted.
        /// ```
        /// 0 9 * * *
        /// ```
        ///
        /// | Field           | Required | Acceptable Values                                                     | Special Characters        |
        /// |------------|--------|------------------------------------------------------|-------------------------|
        /// | Seconds     | No       | 0 ~ 59                                                                              | `,` `-` `*` `/` `R`                  |
        /// | Minutes     | Yes       | 0 ~ 59                                                                              | `,` `-` `*` `/` `R`                  |
        /// | Hours         | Yes      | 0 ~ 23                                                                               | `,` `-` `*` `/` `R`                  |
        /// | Days           | Yes       | 1 ~ 31                                                                              | `,` `-` `*` `/` `?` `L` `W` `R`  |
        /// | Months      | Yes      | 1 ~ 12 or JAN ~ DEC                                                      | `,` `-` `*` `/` `R`                   |
        /// | Weekdays  | Yes      | 0 ~ 7 or SUN ~ SAT<br/>0 and 7 represent Sunday | `,` `-` `*` `/` `?` `L` `#` `R`    |
        public class Format {

            /// The seconds field can be a number from 0 to 59. It is optional and does not have to be specified.
            ///
            /// | Expression | Description    | Example        | Execution Timing            |
            /// |------------|----------------|----------------|-----------------------------|
            /// | `*`        | Every minute    | `* * * * *`    |  every minute        |
            /// | `*/5`      | Every 5 minutes | `*/5 * * * *`  |  every 5 minutes     |
            /// | `5`        | Specific minute | `5 * * * *`    |  at the 5th minute   |
            /// | `1-30`     | Range           | `1-30 * * * *` |  from minute 1 to 30 |
            /// | `0,30`     | Multiple values | `0,30 * * * *` |  at minutes 0 and 30 |
            public class Second {
            }

            /// The time field can be a number from 0 to 23.
            ///
            /// | Expression   | Description           | Example        | Execution Timing                   |
            /// |--------------|-----------------------|----------------|------------------------------------|
            /// | `*`          | Every hour            | `0 * * * *`    |  at the 0th minute of every hour |
            /// | `*/3`        | Every 3 hours         | `0 */3 * * *`  |  every 3 hours at the 0th minute |
            /// | `0`          | Midnight              | `0 0 * * *`    |  at 12:00 AM every day         |
            /// | `9-17`       | Business hours        | `0 9-17 * * *` |  at the 0th minute between 9 AM and 5 PM |
            /// | `8,12,18`    | Multiple times        | `0 8,12,18 * * *` |  at 8 AM, 12 PM, and 6 PM | 
            public class Hour {
            }

            /// The date field can be a number between 1 and 31.
            ///
            /// | Expression | Description        | Example         | Execution Timing                       |
            /// |------------|--------------------|-----------------|----------------------------------------|
            /// | `*`        | Every day           | `0 0 * * *`     |  at 12:00 AM every day         |
            /// | `1`        | First day of month  | `0 0 1 * *`     |  at 12:00 AM on the 1st of every month |
            /// | `L`        | Last day of month   | `0 0 L * *`     |  at 12:00 AM on the last day of every month |
            /// | `*/2`      | Every other day     | `0 0 */2 * *`   |  at 12:00 AM every other day   |
            /// | `1,15`     | Multiple days       | `0 0 1,15 * *`  |  at 12:00 AM on the 1st and 15th of every month |
            /// | `15W`      | Nearest weekday     | `0 0 15W * *`   |  at 12:00 AM on the nearest weekday to the 15th |
            public class Day {
            }

            /// The month field can be a number from 1 to 12.
            /// It can also be specified in English abbreviations such as `JUN`, `FEB`, `MAR`, etc.
            ///
            /// | Expression         | Description          | Example           | Execution Timing                           |
            /// |--------------------|----------------------|-------------------|--------------------------------------------|
            /// | `*`                | Every month          | `0 0 1 * *`       |  at 12:00 AM on the 1st of every month |
            /// | `1` or `JAN`       | January              | `0 0 1 1 *`       |  at 12:00 AM on January 1st        |
            /// | `*/3`              | Quarterly            | `0 0 1 */3 *`     |  at 12:00 AM on the 1st every 3 months |
            /// | `3-5`              | Specified period     | `0 0 1 3-5 *`     |  at 12:00 AM on the 1st from March to May |
            /// | `1,6,12`           | Multiple months      | `0 0 1 1,6,12 *`  |  at 12:00 AM on January 1st, June 1st, and December 1st |
            public class Month {
            }

            /// The day of the week field can be a number from 0 to 7, where 0 is Sunday, 1 is Monday, 2 is Tuesday, and so on, returning to Sunday at 7.
            /// You can also specify the English abbreviation of `SUN`, `MON`, `TUE`, etc.
            ///
            /// | Expression | Description | Example | Execution Timing |
            /// |----------|-------------|---------|------------------|
            /// | `*`      | Every day   | `0 0 * * *` |  every day at 00:00 |
            /// | `1-5`    | Weekdays only | `0 0 * * 1-5` |  at 00:00 on weekdays |
            /// | `0,6`    | Weekends only | `0 0 * * 0,6` |  at 00:00 on Saturday and Sunday |
            /// | `1#1`    | Nth weekday | `0 0 * * 1#1` |  at 00:00 on the first Monday of each month |
            /// | `5L`     | Last weekday | `0 0 * * 5L` |  at 00:00 on the last Friday of each month |
            public class Day_of_Week {
            }
        }

        /// In addition to numbers, each field can contain characters with special meanings and functions.
        ///
        /// | Character | Description | Example | Execution Timing |
        /// |-----------|-------------|---------|------------------|
        /// | `*`       | All values  | `* * * * *` |  every minute |
        /// | `,`       | List of values | `1,15,30 * * * *` |  at 1 minute, 15 minutes, and 30 minutes past every hour |
        /// | `-`       | Range       | `9-17 * * * *` |  every minute from 9 AM to 5 PM |
        /// | `/`       | Interval    | `*/15 * * * *` |  every 15 minutes |
        /// | `L`       | Last        | `0 0 L * *` |  at 00:00 on the last day of each month |
        /// | `W`       | Nearest weekday | `0 0 15W * *` |  at 00:00 on the nearest weekday to the 15th |
        /// | `#`       | Nth occurrence | `0 0 * * 1#1` |  at 00:00 on the first Monday of each month |
        /// | `R`       | Random                       | `R 10 * * *` |  once at a random minute in 10:00 a.m |
        /// | `?`       | No date specification | `0 0 ? * MON` |  at 00:00 every Monday |
        /// 
        /// 
        /// Complex time specifications can be made by combining several special characters as follows
        ///
        /// | Example                       | Execution Timing                                          |
        /// |-------------------------------|---------------------------------------------------------|
        /// | `30 17 * * 5L`               |  at 17:30 on the last Friday of each month     |
        /// | `*/30 9-17 * * 1-5`          |  every 30 minutes from 9 AM to 5 PM on weekdays |
        /// | `0 0 1,15,L * *`              |  at 00:00 on the 1st, 15th, and last day of each month |
        /// | `0 12 * * 1#2,1#4`          |  at 12:00 on the 2nd and 4th Monday of each month |
        /// | `0 9 15W * *`                 |  at 09:00 on the nearest weekday to the 15th   |
        /// | `0 22 * 1-3 0L`              |  at 22:00 on the last Sunday of each month from January to March |
        /// | `15 9-17/2 * * 1,3,5`      |  every 2 hours from 09:15 to 17:15 on Mondays, Wednesdays, and Fridays |
        /// | `0 0-5/2 * * *`              |  every 2 hours between 00:00 and 05:00 every day |
        /// | `0 18-22 * * 1-5`            |  at every hour from 18:00 to 22:00 on weekdays |
        /// | `0 0 1 * 2`                  |  at 00:00 on the first day of every month, but only if it is a Tuesday |
        /// | `0 0 15 * 1#3`               |  at 00:00 on the third Monday of every month when it falls on the 15th |
        /// | `0 12 1 1 *`                 |  at 12:00 on January 1st every year            |
        /// | `*/10 * * * 6#3`             |  every 10 minutes on the third Saturday of each month |
        /// | `0 8-18/3 * * 0`             |  every 3 hours from 08:00 to 18:00 on Sundays  |
        /// | `0 0-23/6 * * *`             |  every 6 hours, at 00:00, 06:00, 12:00, and 18:00 every day |
        /// | `0 15 1 1,7 *`               |  at 15:00 on January 1st and July 1st every year |
        /// | `0 20 * * 1#2`               |  at 20:00 on the second Monday of each month    |
        /// | `0-30R 10,22 * * *`               |  once each at random times between 0 and 30 minutes at 10:00 or 22:00    |
        /// | `0 0 1-5,10-15 * *`          |  at 00:00 on the 1st to 5th and the 10th to 15th day of each month |
        /// | `0 1-5/2,10-15 * * *`        |  at every 2 hours from 01:00 to 05:00 and every hour from 10:00 to 15:00 every day |
        public class Special_Characters {
        }

        /**
         * You can use {@link I#schedule(String)} to schedule jobs by cron expression.
         * {@link API#scheduling() @}
         * 
         * To stop continuous task scheduling, execute the return value {@link Disposable}.
         * {@link API#stopScheduling() @}
         * 
         * Since the return value is {@link Signal}, it is easy to stop after five executions.
         * {@link API#stopSchedulingAfter5Executions() @}
         */
        public class API {

            void scheduling() {
                I.schedule("0 0 * * *").to(() -> {
                    // execute your job
                });
            }

            void stopScheduling() {
                Disposable disposer = I.schedule("0 0 * * *").to(() -> {
                    // execute your job
                });

                // stop scheduling
                disposer.dispose();
            }

            void stopSchedulingAfter5Executions() {
                I.schedule("0 0 * * *").take(5).to(() -> {
                    // execute your job
                });
            }
        }
    }
}