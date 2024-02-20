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

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import doc.ExtensionTest.Codec;
import doc.ExtensionTest.LocalDateCodec;
import doc.MustacheTest.Person;
import kiss.Extensible;
import kiss.I;
import kiss.Lifestyle;
import kiss.Managed;
import kiss.Singleton;
import kiss.instantiation.ConstructorInjectionTest;
import kiss.instantiation.ConstructorInjectionTest.CircularLifestyleA;
import kiss.instantiation.ConstructorInjectionTest.CircularLifestyleB;
import kiss.json.JSONWriteTest;
import kiss.json.ManipulateTest;
import kiss.lifestyle.PrototypeTest;
import kiss.lifestyle.SingletonTest;

public class DocumentDoc {

    static {
        I.load(DocumentDoc.class);
    }

    @Nested
    public class Introduction {
        /**
         * 
         * Sinobu is not obsolete framework but utility, which can manipulate objects as a
         * extremely-condensed facade.
         * This is extremely lightweight at approximately 106 KB without relying on other libraries,
         * and its various operations are designed to run as fast as other competing libraries.
         * 
         * This library aims to simplify and highly condense the functions related to domains that
         * are frequently encountered in real-world development projects, making them easier to use.
         * Some specific domains are listed below.
         * 
         * - [Dependency Injection](https://en.wikipedia.org/wiki/Dependency_injection)
         * - Object lifecycle management
         * - Property based object modeling
         * - HTTP(S)
         * - [JSON](https://en.wikipedia.org/wiki/JSON)
         * - [HTML](https://en.wikipedia.org/wiki/HTML) (including Tag Soup)
         * - [XML](https://en.wikipedia.org/wiki/XML)
         * - Reactive Programming ([Rx](http://reactivex.io))
         * - Asynchronous & Parallel processing
         * - Multilingualization
         * - Template Engine ([Mustache](https://mustache.github.io/mustache.5.html))
         * - Dynamic plug-in mechanism
         * - Object persistence
         * - Logging
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

    @Nested
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

    @Nested
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
        public class The_concept {

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

    @Nested
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

    @Nested
    public class JSON {

        /**
         * You can read JSON from strings, files, and various inputs. All data will be expanded into
         * memory in a tree format. It is not a streaming format, so please be careful when parsing
         * very large JSON.
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

    @Nested
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

    @Nested
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

    @Nested
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
         * Java19 does not yet have a language built-in template syntax. Therefore, Sinobu provides
         * a mechanism to parse <a href="https://mustache.github.io/">Mustache</a> syntax instead.
         */
        public class Mustache {

            /**
             * To use Mustache, you must first create a Mustache template, which is written using a
             * markup language such as HTML or XML. In template, you use special symbols called
             * Mustache delimiters to specify where the data should be inserted. Mustache delimiters
             * are written in the following format:
             * {@code {placeholder} @}
             * 
             * As you can see, Mustache delimiter is a string of characters enclosed in single
             * brace, such as "&#123;placeholder&#125;". This string specifies the location where
             * the data should be inserted. For example, consider the following Mustache template:
             * {@link MustacheTest#template @}
             * 
             * When using this template, you need to provide data for placeholders. For instance,
             * you might have the following JavaBean object as data:
             * {@link Person @}
             * 
             * Passing the template string and context data to method
             * {@link I#express(String, Object...)}, we get a string in which the various
             * placeholders are replaced by its data.
             * {@linkplain MustacheTest#use() @}
             * 
             * Next, within the program that uses the Mustache template, the Mustache engine is
             * initialized. At this point, the Mustache engine is passed the template and the data.
             * The data is written using data structures such as JavaScript objects.
             * 
             * Finally, the Mustache engine is used to render the Mustache template. At this time,
             * the Mustache engine replaces the Mustache specifier in the template and populates the
             * data to produce a finished HTML, XML, or other markup language document.
             * 
             * The specific usage varies depending on the programming language and framework, but
             * the above steps are a rough outline of the basic procedure for using Mustache.
             */
            public class Syntax {
            }

            /**
             * In SInobu, Mustache can be used by calling the {@link I#express(String, Object...)}
             * method. This method parses the given string, reads the necessary variables from the
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

    @Nested
    public class Plugin {

        /**
         * {@link ExtensionTest @}
         */
        public class Usage {
        }

        /**
         * Sinobu has a general-purpose plug-in mechanism for extending application functions. An
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
         * In the usage example, Codec is the extension point that converts any object to a string
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
         * In the usage example, LocalDateCodec is the extension that is special implementation for
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
         * The key makes easy finding an Extension you need (see also {@link I#find(Class, Class)}).
         * {@link Extension_Key#findExtensionByKey() @}
         */
        @Nested
        public class Extension_Key {
            interface ExtensionPointWithKey<K> extends Extensible {
            }

            class ExtensionWithStringKey implements ExtensionPointWithKey<String> {
                // Associate this Extension with String class.
            }

            class ExtensionWithListKey implements ExtensionPointWithKey<List> {
                // Associate this Extension with List interface.
            }

            @Test
            void findExtensionByKey() {
                assert I.find(ExtensionPointWithKey.class, String.class) instanceof ExtensionWithStringKey;
            }
        }

        /**
         * All extensions are not recognized automatically, you have to load them explicitly using
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

    @Nested
    public class Persistence {
        public class Save_Data {
        }

        public class Save_Automatically {
        }

        public class Config_Location {
        }
    }
}