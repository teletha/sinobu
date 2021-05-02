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

/**
 * <h2>Document</h2>
 */
public class DocumentDoc {

    /**
     * <p>
     * This library aims to simplify and highly condense the functions related to domains that are
     * frequently encountered in real-world development projects, making them easier to use. Some
     * specific domains are listed below.
     * </p>
     * <ul>
     * <li><a href="https://en.wikipedia.org/wiki/Dependency_injection">Dependency
     * Injection</a></li>
     * <li>Object lifecycle management</li>
     * <li><a href="https://en.wikipedia.org/wiki/JavaBeans">JavaBeans</a>-like property based type
     * modeling</li>
     * <li>HTTP(S)</li>
     * <li><a href="https://en.wikipedia.org/wiki/JSON">JSON</a></li>
     * <li><a href="https://en.wikipedia.org/wiki/HTML">HTML</a>(<a href=
     * "https://en.wikipedia.org/wiki/XML">XML</a>)</li>
     * <li>Reactive Programming (<a href="http://reactivex.io/">Rx</a>)</li>
     * <li>Asynchronous processing</li>
     * <li>Parallel processing</li>
     * <li>Multilingualization</li>
     * <li>Template engine(<a href="https://mustache.github.io/mustache.5.html">Mustache</a>)</li>
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
     * <li><a href="https://en.wikipedia.org/wiki/Less_is_more_(architecture)">Less is more</a></li>
     * <li><a href="https://en.wikipedia.org/wiki/Type_safety">Type safety</a></li>
     * <li>Refactoring safety</li>
     * </ul>
     */
    public DocumentDoc Introduction;

    /**
     * <p>
     * It is probably easiest to use a build tool such as
     * <a href="https://maven.apache.org/">Maven</a> or <a href="https://gradle.org/">Gradle</a>.
     * </p>
     */
    public DocumentDoc How_to_install;

    class Managing_object_lifestyle {
        /**
         * <h2>What do you mean by lifestyle?</h2>
         * <p>
         * In this library, lifestyle refers to the way an object is created and managed,
         * corresponding to the scope in terms of DI containers such as SpringFramework and Guice,
         * but without the process of registering with the container or destroying the object.
         * </p>
         */
        public DocumentDoc What_do_you_mean_by_lifestyle;

        /**
         * <p>
         * SpringのDIコンテナ（≒IoCコンテナ）が起動すると、JavaConfig またはXMLのBean定義に基づいて、Spring Beanがインスタンス化されます。
         * インスタンス化はフレームワークが自動で行ってくれて、最終的にJava Beanが「使用可能な状態」になります。 この「使用可能な状態」になったJava
         * BeanをDIコンテナが管理してくれて、プログラマが書いたコードに注入（DI）してくれるわけです。
         * </p>
         */
        public DocumentDoc Use_a_customized_Lifestyle;
    }

    class Dipendency_Injection {
        public DocumentDoc Injection_Type;

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
