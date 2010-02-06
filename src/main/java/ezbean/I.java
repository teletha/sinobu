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
package ezbean;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.ResourceBundle.Control;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;

import sun.org.mozilla.javascript.internal.IdScriptableObject;
import sun.org.mozilla.javascript.internal.NativeArray;
import sun.org.mozilla.javascript.internal.NativeObject;
import sun.reflect.ReflectionFactory;
import ezbean.io.FileSystem;
import ezbean.model.ClassUtil;
import ezbean.model.Codec;
import ezbean.model.Model;
import ezbean.model.Property;
import ezbean.module.ModuleLoader;
import ezbean.module.Modules;
import ezbean.xml.XMLFormatter;

/**
 * <p>
 * Ezbean is extremely-condensed facade to manipulate objects (especially JavaBeans).
 * </p>
 * <dl>
 * <dt>Instantiation and Management</dt>
 * <dd>
 * <p>
 * The method {@link #make(Class)} offers general-purpose elucidation for generation and management
 * of the instance.
 * </p>
 * <p>
 * When many containers which manage objects provide the functionality like this, they use the
 * method which name starts with "get". But Ezbean uses the method {@link #make(Class)}. We
 * attribute this difference to the thought to the object management. This difference has much
 * effect on the default lifestyle (many other containers call as Scope) too.
 * </p>
 * <p>
 * Ezbean doesn't provide any features for the object lifecycle because we stand by the principles
 * that we make the most of the function which is originally equipped in Java. If you want the
 * initialization callbacks, you can utilize the constructor. If you want the destruction callbacks,
 * you can utilize the {@link java.lang.Object#finalize()} method.
 * </p>
 * </dd>
 * <dt>Dependency Injection</dt>
 * <dd>
 * <p>
 * Ezbean supports Constructor Injection <em>only</em>. The Constructor Injection is a dependency
 * injection variant where an object gets all its dependencies via the constructor. We can say that
 * there is no possibility that Setter Injection, Field Injection and Interface Injection will be
 * supported in the days to come. This is one of the most important policy in Ezbean. The following
 * is a benefit of Constructor Injection:
 * </p>
 * <ul>
 * <li>It makes a strong dependency contract</li>
 * <li>It makes effective use of constructor in object lifecycle</li>
 * <li>It makes JavaBeans property clean</li>
 * <li>It makes testing easy, since dependencies can be passed in as Mock Object</li>
 * </ul>
 * <p>
 * The following is a deficit of Constructor Injection:
 * </p>
 * <ul>
 * <li>It can't resolve circular dependency</li>
 * </ul>
 * </dd>
 * </dl>
 * <h1 id="ConfigurationService">Configuration Service</h1>
 * <p>
 * Ezbean provides some enviroment variables that you can configure.
 * </p>
 * <ul>
 * <li><a href="#encoding">Character Encoding</a></li>
 * <li><a href="#parentClassLoader">Parent Class Loader</a></li>
 * <li><a href="#workingDirectory">Working Directory</a></li>
 * </ul>
 * <p>
 * When you want to initialize these enviroment variables and your application environment related
 * to Ezbean (e.g. external class annotation by {@link #annotate(Class, Annotation...)}, dynamic
 * class loading by {@link #load(File)}), you must implement subclass of Ezbean and declare as <a
 * href="http://java.sun.com/javase/6/docs/technotes/guides/jar/jar.html#Service%20Provider">service
 * provider</a>.
 * </p>
 * <p>
 * A service provider is identified by placing a <em>provider-configuration file</em> in the
 * resource directory META-INF/services. The file's name is the fully-qualified binary name of the
 * service's type (in this case, "ezbean.Ezbean"). The file contains a list of fully-qualified of
 * concrete provider classes, one per line. Space and tab characters surrounding each name, as well
 * as blank lines, are ignored. The comment character is '#' ('&#92;u0023', NUMBER SIGN); on each
 * line all characters following the first comment character are ignored. The file must be encoded
 * in UTF-8.
 * </p>
 * <p>
 * For more details on declaring service providers, and the JAR format in general, see the <a
 * href="http://java.sun.com/javase/6/docs/technotes/guides/jar/jar.html">JAR File
 * Specification</a>.
 * </p>
 * <h2>Example</h2>
 * <p>
 * If <code>your.application.Config</code> class is a provider of Ezbean configuration service then
 * its jar file would contain the file <code>META-INF/services/ezbean.I</code>. This file would
 * contain the single line like the following:
 * </p>
 * 
 * <pre>
 * your.application.Config
 * </pre>
 * <p>
 * and <code>our.application.Config</code> class would be like the following:
 * </p>
 * 
 * <pre>
 * public class Config extend I {
 *   
 *   public Config() {
 *     // initialize Ezbean environment variables for your application
 *     workingDirectory = new File("path/to/directory");
 *   }
 * }
 * </pre>
 * 
 * @see ServiceLoader
 * @version 2010/02/05 1:53:29
 */
public class I implements ClassLoadListener<Extensible> {

    // Candidates of Method Name
    //
    // annotate
    // bind
    // create class copy
    // delete define
    // edit
    // find
    // get
    // hash have
    // i18n include
    // json
    // k
    // locate load
    // make mock
    // n
    // observe
    // parse
    // quiet
    // read
    // save staple
    // transform
    // unload use
    // v
    // write weave
    // xml xerox
    // yield
    // zip

    /**
     * <p>
     * The configuration of charcter encoding in Ezbean, default value is <em>UTF-8</em>. It is
     * encouraged to use this encoding instead of platform default encoding when file I/O under the
     * Ezbean environment.
     * </p>
     * <p>
     * You can retrieve this value by using the method {@link #getEncoding()}. You can configure
     * this value by using <a href="#ConfigurationService">Configuration Service</a>.
     * </p>
     */
    protected static Charset encoding = Charset.forName("UTF-8");

    /**
     * <p>
     * The configuration of parent class loader in Ezbean, default value is
     * <code><em>I.class.getClassLoader()</em></code>.
     * </p>
     * <p>
     * You can retrieve this value by using the method {@link #getParentClassLoader()}. You can
     * configure this value by using <a href="#ConfigurationService">Configuration Service</a>.
     * </p>
     */
    protected static ClassLoader parentClassLoader = I.class.getClassLoader();

    /**
     * <p>
     * The configuration of working directory in Ezbean, default value is <em>current directory</em>
     * .
     * </p>
     * <p>
     * You can retrieve this value by using the method {@link #getWorkingDirectory()}. You can
     * configure this value by using <a href="#ConfigurationService">Configuration Service</a>.
     * </p>
     */
    protected static File workingDirectory = new File("").getAbsoluteFile();

    /** The namespace uri of Ezbean. */
    static final String URI = "http://ez.bean/";

    /** The cache between Model and Lifestyle. */
    private static final Listeners<Class, Lifestyle> lifestyles = Modules.aware(new Listeners<Class, Lifestyle>());

    /** The circularity dependency graph per thread. */
    private static final ThreadSpecific<Deque<Class>> dependencies = new ThreadSpecific(ArrayDeque.class);

    /** The mapping from extension point to extensions. */
    private static final Listeners<Class, Class> extensions = new Listeners();

    /** The mapping from extension point to assosiated extension mapping. */
    private static final Listeners<Integer, Class> keys = new Listeners();

    /**
     * The tracer context per thread. The tracer context consists of the following. The first
     * element is must be a source object that you want to trace the property paths. The other
     * elements are property names as {@link java.lang.String}.
     */
    private static final ThreadSpecific<Deque<List>> tracers = new ThreadSpecific(ArrayDeque.class);

    /** The lock for configurations. */
    private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    /** The sax parser factory for reuse. */
    private static final SAXParserFactory sax = SAXParserFactory.newInstance();

    /** The javascript engine for reuse. */
    private static final ScriptEngine script;

    /** The locale name resolver. */
    private static final Control control = Control.getControl(Control.FORMAT_CLASS);

    /**
     * This instantiator instantiates an object with out any side effects caused by the constructor.
     */
    private static final Constructor instantiator;

    // initialization
    static {
        // load all Ezbean configuration service providers
        for (@SuppressWarnings("unused")
        I config : ServiceLoader.load(I.class)) {
            // each configuration classes (subclass of Ezbean) are instantiated and configure
            // itself in it's constructor, so we do nothing here.
        }

        // built-in lifestyles
        lifestyles.put(List.class, new Prototype(ArrayList.class));
        lifestyles.put(Map.class, new Prototype(HashMap.class));
        lifestyles.put(Prototype.class, new Prototype(Prototype.class));

        try {
            // Instantiates a class by using reflection to make a call to private method
            // ObjectStreamClass.newInstance, present in many JVM implementations.
            instantiator = Object.class.getConstructor();

            // configure sax parser
            sax.setNamespaceAware(true);
            sax.setXIncludeAware(true);
            sax.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            // sax.setFeature("http://xml.org/sax/features/external-general-entities", false);
            // sax.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            // sax.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
            // sax.setFeature("http://xml.org/sax/features/xmlns-uris", true);
        } catch (Exception e) {
            // If this exception will be thrown, it is bug of this program. So we must rethrow the
            // wrapped error in here.
            throw new Error(e);
        }

        // configure javascript engine
        script = new ScriptEngineManager(parentClassLoader).getEngineByName("js");

        // Load myself as module. All built-in classload listeners and extension points will be
        // loaded and activated.
        load(ClassUtil.getArchive(I.class));
    }

    /**
     * <p>
     * Subclass of Ezbean can use the constructor to initialize environment.
     * </p>
     */
    protected I() {
        // do nothing here for environment initialization by subclass
    }

    /**
     * <p>
     * Bind between two properties. You can specify the binding type (one-way and two-way). This
     * binding supports the following binding types :
     * </p>
     * <dl>
     * <dt>Oneway</dt>
     * <dd>The change to the target property is not transmitted to the data source though the value
     * with a new data source is forwarded to the target property.</dd>
     * <dt>Twoway</dt>
     * <dd>The change on source and target is interactively transmitted.</dd>
     * </dl>
     * 
     * @param sourcePath A property path from the source object. Multiple path (e.g. parent.name) is
     *            acceptable.
     * @param targetPath A property path from the target object. Multiple path (e.g. parent.name) is
     *            acceptable.
     * @param twoway A flag for binding type. If <code>true</code> is specified, source and target
     *            properties bind each other. If <code>false</code> is specified, only the source
     *            property binds to the target property.
     * @return A {@link Disposable} object for this binding. You can unbind this binding to call the
     *         method {@link Disposable#dispose()} of the returned object.
     * @throws NullPointerException If the specified source's path or target's path is
     *             <code>null</code>.
     * @throws IndexOutOfBoundsException If the specified source's path or target's path is empty.
     */
    public static <S> Disposable bind(S sourcePath, S targetPath, boolean twoway) {
        // retrieve a tracer of the current processing thread
        Deque<List> tracer = tracers.resolve();

        // create target binding
        Observer targetBind = new Observer(tracer.poll(), null);

        // create source binding
        Observer subjectBind = new Observer(tracer.poll(), targetBind);

        if (twoway) {
            // bind each other
            targetBind.listener = subjectBind;
        } else {
            // dispose target binding
            targetBind.dispose();
        }

        // initial property binding
        subjectBind.change(null, subjectBind.path.get(0), null, null);

        // API definition
        return subjectBind;
    }

    /**
     * <p>
     * Find all <a href="Extensible.html#Extension">Extensions</a> which are specified by the given
     * <a href="Extensible#ExtensionPoint">Extension Point</a>.
     * </p>
     * <p>
     * The returned list will be "safe" in that no references to it are maintained by Ezbean. (In
     * other words, this method must allocate a new list). The caller is thus free to modify the
     * returned list.
     * </p>
     * 
     * @param <E> An Extension Point.
     * @param extensionPoint An extension point class. The <a
     *            href="Extensible#ExtensionPoint">Extension Point</a> class is only accepted,
     *            otherwise this method will return empty list.
     * @return All Extensions of the given Extension Point or empty list.
     * @throws NullPointerException If the Extension Point is <code>null</code>.
     */
    public static <E extends Extensible> List<E> find(Class<E> extensionPoint) {
        // Skip null check because this method can throw NullPointerException.
        List<Class> classes = extensions.get(extensionPoint);

        if (classes != null) {
            // instantiate all found extesions
            List list = new ArrayList(classes.size());

            for (Class extension : classes) {
                list.add(make(extension));
            }
            return list;
        }

        // we can not find suitable extensions
        return Collections.EMPTY_LIST;
    }

    /**
     * <p>
     * Find the <a href="Extensible.html#Extension">Extension</a> which are specified by the given
     * <a href="Extensible#ExtensionPoint">Extension Point</a> and the given key.
     * </p>
     * 
     * @param <E> An Extension Point.
     * @param extensionPoint An Extension Point class. The <a
     *            href="Extensible#ExtensionPoint">Extension Point</a> class is only accepted,
     *            otherwise this method will return <code>null</code>.
     * @param key An <a href="Extensible.html#ExtensionKey">Extension Key</a> class.
     * @return A associated Extension of the given Extension Point and the given Extension Key or
     *         <code>null</code>.
     */
    public static <E extends Extensible> E find(Class<E> extensionPoint, Class key) {
        Class<E> clazz = keys.find(hash(extensionPoint, key));

        return clazz == null ? null : make(clazz);
    }

    /**
     * <p>
     * Retrieve the specified character encoding under the user environment. You can configure this
     * value by using <a href="#ConfigurationService">Configuration Service</a>.
     * </p>
     * 
     * @return A character encoding.
     * @see #encoding
     */
    public static Charset getEncoding() {
        return encoding;
    }

    /**
     * <p>
     * Retrieve the specified parent class loader under the user environment. You can configure this
     * value by using <a href="#ConfigurationService">Configuration Service</a>.
     * </p>
     * 
     * @return A parent class loader.
     * @see #parentClassLoader
     */
    public static ClassLoader getParentClassLoader() {
        return parentClassLoader;
    }

    /**
     * <p>
     * Retrieve the specified working directory under the user environment. You can configure this
     * value by using <a href="#ConfigurationService">Configuration Service</a>.
     * </p>
     * 
     * @return A working directory.
     * @see #workingDirectory
     */
    public static File getWorkingDirectory() {
        return workingDirectory;
    }

    /**
     * <p>
     * Generates a hash code for multiple values. The hash code is generated by calling
     * {@link Arrays#hashCode(Object[])}.
     * </p>
     * <p>
     * This is useful for implementing {@link Object#hashCode()}. For example, in an object that has
     * three properties, {@code x}, {@code y}, and {@code z}, one could write:
     * </p>
     * 
     * <pre>
     * public int hashCode() {
     *     return I.hash(getX(), getY(), getZ());
     * }
     * </pre>
     * 
     * <b>Warning</b>: When a single object is supplied, the returned hash code does not equal the
     * hash code of that object.
     */
    public static int hash(Object... params) {
        return Arrays.hashCode(params);
    }

    /**
     * <p>
     * Gets a <em>type-safe and refactoring-safe</em> resource bundle (<em>not</em>
     * {@link java.util.ResourceBundle}) corresponding to the specified resource bundle class.
     * </p>
     * <p>
     * Conceptually, i18n method uses the following strategy for locating and instantiating resource
     * bundles:
     * </p>
     * <p>
     * i18n method uses the bundle class name and the default locale (obtained from
     * <code>I.make(Locale.class)</code>)) to generate a sequence of candidate bundle names. If the
     * default locale's language, country, and variant are all empty strings, then the bundle class
     * name is the only candidate bundle name. Otherwise, the following sequence is generated from
     * the attribute values of the default locale (language, country, and variant):
     * </p>
     * <ol>
     * <li>bundleClassSimpleName + "_" + language + "_" + country + "_" + variant</li>
     * <li>bundleClassSimpleName + "_" + language + "_" + country</li>
     * <li>bundleClassSimpleName + "_" + language</li>
     * <li>bundleClassSimpleName</li>
     * </ol>
     * <p>
     * Candidate bundle names where the final component is an empty string are omitted. For example,
     * if country is an empty string, the second candidate bundle name is omitted.
     * </p>
     * <p>
     * i18n method then iterates over the candidate bundle names to find the first one for which it
     * can instantiate an actual resource bundle. For each candidate bundle name, it attempts to
     * create a resource bundle:
     * </p>
     * <ol>
     * <li>First, it attempts to find a class using the candidate bundle name. If such a class can
     * be found and loaded using {@link I#find(Class)}, is assignment compatible with the given
     * bundle class, and can be instantiated, i18n method creates a new instance of this class and
     * uses it as the result resource bundle.</li>
     * </ol>
     * <p>
     * If the following classes are provided:
     * </p>
     * <ul>
     * <li>MyResources.class</li>
     * <li>MyResources_fr.class</li>
     * <li>MyResources_fr_CH.class</li>
     * </ul>
     * <p>
     * The contents of all files are valid (that is non-abstract subclasses of {@link Extensible}
     * for the ".class" files). The default locale is Locale("en", "GB").
     * </p>
     * <p>
     * Calling i18n method with the shown locale argument values instantiates resource bundles from
     * the following sources:
     * </p>
     * <ol>
     * <li>Locale("fr", "CH"): result MyResources_fr_CH.class</li>
     * <li>Locale("fr", "FR"): result MyResources_fr.class</li>
     * <li>Locale("es"): result MyResources.class</li>
     * </ol>
     * 
     * @param <B> A resource bundle.
     * @param bundleClass A resource bundle class. <code>null</code> will throw
     *            {@link NullPointerException}.
     * @return A suitable resource bundle class for the given bundle class and locale.
     * @throws NullPointerException If the bundle class is <code>null</code>.
     */
    public static <B extends Extensible> B i18n(Class<B> bundleClass) {
        root: for (Locale locale : control.getCandidateLocales("", I.make(Locale.class))) {
            String name = control.toBundleName(bundleClass.getSimpleName(), locale);
            List<Class> list = extensions.get(bundleClass);

            if (list != null) {
                for (Class clazz : list) {
                    if (clazz.getSimpleName().equals(name)) {
                        bundleClass = clazz;
                        break root;
                    }
                }
            }
        }
        return make(bundleClass);
    }

    /**
     * <p>
     * Write out the given configuration object as JSON.
     * </p>
     * 
     * @param model A configuration object. <code>null</code> is acceptable, but this method will do
     *            nothing (don't throw {@link java.lang.NullPointerException}).
     */
    public static String json(Object model) {
        StringBuilder builder = new StringBuilder();

        // jsonize
        new JSON(builder).traverse(model);

        // API definition
        return builder.toString();
    }

    /**
     * <p>
     * Read the given configuration as json and create the configuration object.
     * </p>
     * 
     * @param modelClass A model class. <code>null</code> will throw
     *            {@link java.lang.NullPointerException}.
     * @param input A configuration as json to be red. If <code>null</code>, empty string or invalid
     *            json string is passed, this method ignores this input and returns the default
     *            object which equals to <code>I.create(configClass);</code>
     * @return A configuration object to be created.
     * @throws NullPointerException If the config class is null.
     * @throws IllegalArgumentException If the config class is non-accessible or final class.
     * @throws UnsupportedOperationException If the config class is inner-class.
     * @throws ClassCircularityError If the config class has circular dependency.
     */
    public static <M> M json(Class<M> modelClass, String input) {
        try {
            return json(Model.load(modelClass), script.eval("a=" + input));
        } catch (ScriptException e) {
            return make(modelClass);
        }
    }

    /**
     * <p>
     * Helper method to traverse json structure using Java Object {@link Model}.
     * </p>
     * 
     * @param <M> A current model type.
     * @param model A java object model.
     * @param js A javascript value.
     * @return A restored java object.
     */
    private static <M> M json(Model<M> model, Object js) {
        // create new java object to restore values
        M java = make(model.type);

        if (js instanceof IdScriptableObject) {
            for (Object id : ((IdScriptableObject) js).getIds()) {
                // compute property
                Property property = model.getProperty(id.toString());

                // calculate value
                Object value = model.type == List.class ? ((NativeArray) js).get((Integer) id, null)
                        : ((NativeObject) js).get((String) id, null);

                // convert value
                if (property.isAttribute()) {
                    value = transform(value, property.model.type);
                } else {
                    value = json(property.model, value);
                }

                // assign value
                model.set(java, property, value);
            }
        }

        // API definition
        return java;
    }

    /**
     * <p>
     * Locate the specified file URL and return the plain {@link File} object.
     * </p>
     * 
     * @param filePath A location path.
     * @return A located {@link File}.
     * @throws NullPointerException If the given file path is null.
     */
    public static File locate(URL filePath) {
        try {
            // Use File constructor with URI to resolve escaped character.
            return locate(new File(filePath.toURI()).getPath());
        } catch (URISyntaxException e) {
            return locate(filePath.getPath());
        }
    }

    /**
     * Locate the specified file path and return the plain {@link File} object.
     * 
     * @param filePath A location path.
     * @return A located {@link File}.
     * @throws NullPointerException If the given file path is null.
     */
    public static File locate(String filePath) {
        return make(FileSystem.class).locate(filePath);
    }

    /**
     * Locate the specified file path and return the plain {@link File} object.
     * 
     * @param base
     * @param filePath A location path.
     * @return A located {@link File}.
     * @throws NullPointerException If the given file path is null.
     */
    public static File locate(File base, String filePath) {
        return locate(base.toString() + FileSystem.SEPARATOR + filePath);
    }

    /**
     * <p>
     * Returns a new or cached instance of the model class.
     * </p>
     * <p>
     * This method supports the top-level class and the member type. If the local class or the
     * anonymous class is passed to this argument, {@link UnsupportedOperationException} will be
     * thrown. There is a possibility that a part of this limitation will be removed in the future.
     * </p>
     * 
     * @param <M>
     * @param modelClass
     * @return A instance of the specified model class. This instance is managed by Ezbean.
     * @throws NullPointerException If the model class is <code>null</code>.
     * @throws IllegalArgumentException If the model class is non-accessible or final class.
     * @throws UnsupportedOperationException If the model class is inner-class.
     * @throws ClassCircularityError If the model has circular dependency.
     */
    public static <M> M make(Class<M> modelClass) {
        // At first, we must confirm the cached lifestyle associated with the model class. If
        // there is no such cache, we will try to create it.
        Lifestyle<M> lifestyle = lifestyles.find(modelClass);

        if (lifestyle != null) return lifestyle.resolve(); // use cache

        // Skip null check because this method can throw NullPointerException.
        // if (modelClass == null) throw new NullPointerException("NPE");

        // Retrieve the circularity dependency graph for the current processing thread.
        Deque<Class> graph = dependencies.resolve();

        // The Class model is special.
        if (modelClass == Class.class) {
            modelClass = graph.pollLast();

            if (!Prototype.class.isAssignableFrom(modelClass)) {
                graph.add(modelClass);
            }
            return (M) modelClass;
        }

        // The model class have some preconditions to have to meet.
        if (modelClass.isLocalClass() || modelClass.isAnonymousClass()) {
            throw new UnsupportedOperationException(modelClass + " is  inner class.");
        }

        int modifier = modelClass.getModifiers();

        // In the second place, we must find the model provider class which is associated with the
        // model povider interface. If the model provider interface is a concreate class, we can
        // use it as a provider.
        Class<M> providerClass = modelClass;

        if (((Modifier.ABSTRACT | Modifier.INTERFACE) & modifier) != 0) {
            // TODO model provider finding strategy
            // This strategy is decided at execution phase.
            providerClass = make(Modules.class).find(modelClass);

            // updata to the provider's modifier
            modifier = providerClass.getModifiers();
        }

        // We can obtain the model about the model provider class.
        Model<?> model = Model.load(providerClass);
        boolean shouldEnhance = model.properties.size() != 0;

        // If this model is non-accessible or final class, we can not extend it for bean
        // enhancement. So we must throw some exception.
        if (shouldEnhance && ((Modifier.PUBLIC | Modifier.PROTECTED) & modifier) == 0) {
            throw new IllegalArgumentException(providerClass + " is not declared as public or protected.");
        }

        if (shouldEnhance && (Modifier.FINAL & modifier) != 0) {
            throw new IllegalArgumentException(providerClass + " is declared as final.");
        }

        // Decide the lifestyle of this model provider class. If this provider doesn't provide its
        // lifestyle explicitly, we use Prototype lifestyle which is default lifestyle in Ezbean.
        Class<? extends Lifestyle> lifestyleClass;
        Manageable manageable = model.type.getAnnotation(Manageable.class);

        if (manageable == null) {
            lifestyleClass = Prototype.class;
        } else {
            lifestyleClass = manageable.lifestyle();
        }

        // Don't use 'contains method' check here to resolve singleton based circular reference. So
        // we must judge it from the size of context. If the context contains too many classes, it
        // has a circular reference independencies.
        if (graph.size() > 64) {
            // Deque will be contain repeated Classes so we must shrink it with
            // maintaining its class order.
            throw new ClassCircularityError(new LinkedHashSet(graph).toString());
        }

        // Enhance this model class if needed.
        if (shouldEnhance) {
            providerClass = ModuleLoader.getModuleLoader(providerClass).loadClass(model, 0);
        }

        // Add this model from the circularity dependency graph.
        graph.add(providerClass);

        try {
            // Create new lifestyle for this model class
            lifestyle = make(lifestyleClass);

            // At first, we must try to resolve instantiation.
            M m = lifestyle.resolve();

            // If the specified model class is instantiated in static initializer for some reasons
            // (e.g. ClassLoasdListener may automatically instantiate class), we shuold avoid to
            // duplicate instance.
            if (lifestyles.containsKey(modelClass)) {
                return make(modelClass);
            }

            // Then, if the instantiation was success(any exception won't be thrown), we can
            // recognize that the lifestyle is valid and cache it.
            lifestyles.put(modelClass, lifestyle);

            // API definition
            return m;
        } finally {
            // Remove this model from the circularity dependency graph.
            graph.pollLast();
        }
    }

    /**
     * <p>
     * Create a mock object which can trace the property paths with calling accessor methods.
     * </p>
     * 
     * @param <M> A model object to trace property paths.
     * @param type A model class to trace.
     * @return A tracing object.
     * @throws NullPointerException If the specified tracer is <code>null</code>.
     */
    public static <M> M mock(M model) {
        if (model instanceof String) {
            // Enhancer which is an internal class of Ezbean uses this process.
            // The specified model object indicates the property path.
            tracers.resolve().peek().add(model);

            return null; // the returned value will not be used by Ezbean
        }

        // compute model class
        Class modelClass;

        if (model instanceof Class) {
            // Enhancer which is an internal class of Ezbean uses this process.
            // The specified model object indicates the model class itself.
            modelClass = (Class) model;
        } else {
            // Ezbean user uses this process with calling mock method from external.
            // The specified model object indicates the actual bean, so we must create new trace
            // context.
            modelClass = model.getClass();

            // retrieve the tracer context for current thread
            Deque<List> tracer = tracers.resolve();

            // for tracing, maximum necessary capacity is 2 (bind method needs it)
            if (1 < tracer.size()) {
                tracer.removeLast();
            }

            // create new tracer context for property path tracing
            tracer.addFirst(new ArrayList(4));

            // tracer context must have the source object at first element
            tracer.peek().add(model);
        }

        try {
            // ObjectStreamClass.lookup method will cache the instance of ObjectStreamClass (at
            // least Sun's implementation), so we don't.
            return (M) ReflectionFactory.getReflectionFactory()
                    .newConstructorForSerialization(ModuleLoader.getModuleLoader(modelClass)
                            .loadClass(Model.load(modelClass), 1), instantiator)
                    .newInstance();
        } catch (Exception e) {
            // If this exception will be thrown, it is bug of this program. So we must rethrow the
            // wrapped error in here.
            throw new Error(e);
        }
    }

    /**
     * <p>
     * Observe the property change event of the specified property path.
     * </p>
     * 
     * @param sourcePath A property path from the source object. Multiple path (e.g. parent.name) is
     *            acceptable.
     * @param listener A property change event listener. This value must not be <code>null</code>.
     * @return A {@link Disposable} object for this observation. You can stop observing to call the
     *         method {@link Disposable#dispose()} of the returned object.
     * @throws NullPointerException If the specified source's path or the specified listener is
     *             <code>null</code>.
     * @throws IndexOutOfBoundsException If the specified source's path is empty.
     */
    public static <M> Disposable observe(M sourcePath, PropertyListener<M> listener) {
        if (listener == null) {
            throw new NullPointerException();
        }

        // API definition
        return new Observer(tracers.resolve().poll(), listener);
    }

    /**
     * <p>
     * Parse the specified xml {@link InputSource} using the specified sequence of {@link XMLFilter}
     * . The application can use this method to instruct the XML reader to begin parsing an XML
     * document from any valid input source (a character stream, a byte stream, or a URI).
     * </p>
     * <p>
     * Ezbean use the {@link XMLReader} which has the following features.
     * </p>
     * <ul>
     * <li>Support XML namespaces.</li>
     * <li>Support <a href="http://www.w3.org/TR/xinclude/">XML Inclusions (XInclude) Version
     * 1.0</a>.</li>
     * <li><em>Not</em> support any validations (DTD or XML Schema).</li>
     * <li><em>Not</em> support external DTD completely (parser doesn't even access DTD, using
     * "http://apache.org/xml/features/nonvalidating/load-external-dtd" feature).</li>
     * </ul>
     * 
     * @param source A xml source.
     * @param filters A list of filters to parse a sax event. This may be <code>null</code>.
     * @throws NullPointerException If the specified source is <code>null</code>. If one of the
     *             specified filter is <code>null</code>.
     * @throws SAXException Any SAX exception, possibly wrapping another exception.
     * @throws IOException An IO exception from the parser, possibly from a byte stream or character
     *             stream supplied by the application.
     */
    public static void parse(InputSource source, XMLFilter... filters) {
        try {
            // create new xml reader
            XMLReader reader = sax.newSAXParser().getXMLReader();

            // chain filters if needed
            for (int i = 0; i < filters.length; i++) {
                // find the root filter of the current multilayer filter
                XMLFilter filter = filters[i];

                while (filter.getParent() instanceof XMLFilter) {
                    filter = (XMLFilter) filter.getParent();
                }

                // the root filter makes previous filter as parent xml reader
                filter.setParent(reader);

                if (filter instanceof LexicalHandler) {
                    reader.setProperty("http://xml.org/sax/properties/lexical-handler", filter);
                }

                // current filter is a xml reader in next step
                reader = filters[i];
            }

            // start parsing
            reader.parse(source);
        } catch (Exception e) {
            // We must throw the checked exception quietly and pass the original exception instead
            // of wrapped exception.
            throw quiet(e);
        }
    }

    /**
     * <p>
     * Throw the specified checked exception quietly.
     * </p>
     * <p>
     * This method <em>doesn't</em> wrap checked exception around unchecked exception (e.g. new
     * RuntimeException(e)) and <em>doesn't</em> shelve it. This method deceive the compiler that the
     * checked exception is unchecked one. So you can catch a raw checked exception in the caller of
     * the method which calls this method.
     * </p>
     * 
     * <pre>
     * private void callerWithoutErrorHandling() {
     *     methodQuietly();
     * }
     * 
     * private void callerWithErrorHandling() {
     *     try {
     *         methodQuietly();
     *     } catch (Exception e) {
     *         // you can catch the checked exception here
     *     }
     * }
     * 
     * private void methodQuietly() {
     *     try {
     *         // throw some cheched exception
     *     } catch (CheckedException e) {
     *         throw I.quiet(e); // rethrow checked exception quietly
     *     }
     * }
     * </pre>
     * 
     * @param throwable A exception to throw quietly.
     * @return A pseudo unchecked exception.
     * @throws NullPointerException If the specified exception is <code>null</code>.
     */
    public static RuntimeException quiet(Throwable throwable) {
        // retrieve original exception from the specified wrapped exception
        if (throwable instanceof InvocationTargetException) throwable = throwable.getCause();

        // throw quietly
        return I.<RuntimeException> quietly(throwable);
    }

    /**
     * <p>
     * Deceive complier that the specified checked exception is unchecked exception.
     * </p>
     * 
     * @param <T>
     * @param throwable
     * @return
     * @throws T
     */
    @SuppressWarnings("unchecked")
    private static <T extends Throwable> T quietly(Throwable throwable) throws T {
        throw (T) throwable;
    }

    /**
     * @param <S>
     * @param <T>
     * @param source
     * @param destinationClass
     * @return
     */
    public static <S, T> T transform(S source, Class<T> destinationClass) {
        // check null
        if (source == null) {
            return null;
        }

        Model<S> sourceModel = Model.load((Class) source.getClass());
        Model<T> destinationModel = Model.load(destinationClass);

        // no conversion
        if (sourceModel == destinationModel) {
            return (T) source;
        }

        Codec<S> sourceCodec = sourceModel.getCodec();
        Codec<T> destinationCodec = destinationModel.getCodec();

        // check whether each model are attribute model or not
        if (sourceCodec == null && destinationCodec == null) {
            // we should copy property values

            // create destination object
            T m = I.make(destinationClass);

            // copy actually
            sourceModel.walk(source, new ModelState(m, destinationModel));

            // API definition
            return m;
        } else {
            // type conversion
            if (destinationClass == String.class && sourceCodec != null) {
                return (T) sourceCodec.encode(source);
            }

            if (sourceModel.type == String.class && destinationCodec != null) {
                return destinationCodec.decode((String) source);
            }
            return (T) source;
        }
    }

    /**
     * <p>
     * Write out the given configuration object to the file.
     * </p>
     * 
     * @param model A configuration object. <code>null</code> is acceptable, but this method will do
     *            nothing (don't throw {@link java.lang.NullPointerException}).
     * @param output A target configuration file to write out. <code>null</code> is acceptable, but
     *            this method will do nothing (don't throw {@link java.lang.NullPointerException}).
     */
    public static void xml(Object model, File output) {
        if (model != null && output != null) {
            // We must confirm that the parent directory exists because FileOutputStream can't
            // create nested file.
            output.getAbsoluteFile().getParentFile().mkdirs();

            // lock
            lock.writeLock().lock();

            Writer writer = null;

            try {
                // prepare stream
                writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output), encoding));
                XMLFormatter xml = new XMLFormatter(writer);

                // xml start
                xml.startDocument();
                xml.startPrefixMapping("ez", URI);

                // traverse configuration
                new XMLWriter(xml).traverse(model);

                xml.endDocument();
                // xml end
            } catch (Exception e) {
                throw quiet(e);
            } finally {
                // unlock
                lock.writeLock().unlock();

                try {
                    if (writer != null) {
                        writer.close();
                    }
                } catch (IOException e) {
                    throw quiet(e);
                }
            }
        }
    }

    /**
     * <p>
     * Read the given configuration file and create the configuration object.
     * <p>
     * </p>
     * In view of the initial state (e.g. when the system starts up, the configuration file may been
     * not created yet), this method will do nothing when the file doesn't exist. </p>
     * 
     * @param input A configuration file to be red. <code>null</code> is acceptable, but this method
     *            will do nothing (don't throw {@link java.lang.NullPointerException}).
     * @param model A configuration object. <code>null</code> is acceptable, but this method will do
     *            nothing (don't throw {@link java.lang.NullPointerException}).
     */
    public static <M> M xml(File input, M model) {
        if (input != null && input.exists() && model != null) {
            // lock
            lock.readLock().lock();

            Reader reader = null;

            try {
                // prepare stream
                reader = new BufferedReader(new InputStreamReader(new FileInputStream(input), encoding));

                parse(new InputSource(reader), new ezbean.XMLReader(model));
            } catch (IOException e) {
                throw quiet(e);
            } finally {
                // unlock
                lock.readLock().unlock();

                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    throw quiet(e);
                }
            }
        }

        // API definition
        return model;
    }

    /**
     * <p>
     * Load the file as an additional classpath into JVM. If the file indicates the classpath which
     * is already loaded, that will be reloaded. The classpath can accept directory or archive (like
     * Jar). If it is <code>null</code> or a file, this method does nothing.
     * </p>
     * <p>
     * There are two advantages in the classpath loaded by this method. One is that you can add
     * classpath dynamically and the other is that you can listen to the specified class loading
     * event.
     * </p>
     * <p>
     * Generally, JVM collects classpath information from various sources (environment variable,
     * command line option and so on). However those means can't add or remove a classpath
     * dynamically. This method removes such limitations.
     * </p>
     * <p>
     * <em>NOTE</em> : System class loader in JVM can recognize the classpath which is specified by
     * usual means, but not by this method. Because Ezbean manages additional classpath for enabling
     * dynamic manipulation.
     * </p>
     * 
     * @param classPath A classpath to load. Directory or archive file (like Jar) can be accepted.
     * @see #unload(File)
     * @see ezbean.ClassLoadListener#load(Class)
     * @see java.lang.ClassLoader#getSystemClassLoader()
     */
    public static void load(File classPath) {
        make(Modules.class).load(classPath);
    }

    /**
     * <p>
     * Unload the file which is an additional classpath in JVM. If the file indicates the classpath
     * which is not loaded yet, that will be ignored. The classpath can accept directory or archive
     * (like Jar). If it is <code>null</code> or a file, this method does nothing.
     * </p>
     * <p>
     * There are two advantages in the classpath loaded by this method. One is that you can remove
     * classpath dynamically and the other is that you can listen to the specified class unloading
     * event.
     * </p>
     * <p>
     * Generally, JVM collects classpath information from various sources (environment variable,
     * command line option and so on). However those means can't add or remove a classpath
     * dynamically. This method removes such limitations.
     * </p>
     * <p>
     * <em>NOTE</em> : System class loader in JVM can recognize the classpath which is specified by
     * usual means, but not by this method. Because Ezbean manages additional classpath for enabling
     * dynamic manipulation.
     * </p>
     * 
     * @param classPath A classpath to unload. Directory or archive file (like Jar) can be accepted.
     * @see #load(File)
     * @see ezbean.ClassLoadListener#unload(Class)
     * @see java.lang.ClassLoader#getSystemClassLoader()
     */
    public static void unload(File classPath) {
        make(Modules.class).unload(classPath);
    }

    /**
     * @see ezbean.ClassLoadListener#load(java.lang.Class)
     */
    public final void load(Class extension) {
        // search and collect information for all extension points
        for (Class extensionPoint : ClassUtil.getTypes(extension)) {
            if (Arrays.asList(extensionPoint.getInterfaces()).contains(Extensible.class)) {
                // register new extension
                extensions.put(extensionPoint, extension);

                // register extension key
                Class[] params = ClassUtil.getParameter(extension, extensionPoint);

                if (params.length != 0 && params[0] != Object.class) {
                    keys.put(hash(extensionPoint, params[0]), extension);

                    // custom lifestyle
                    if (extensionPoint == Lifestyle.class) {
                        // add Class resolver
                        dependencies.resolve().add(params[0]);

                        // register lifestyle
                        lifestyles.put(params[0], (Lifestyle) make(extension));
                    }
                }
            }
        }
    }

    /**
     * @see ezbean.ClassLoadListener#unload(java.lang.Class)
     */
    public final void unload(Class extension) {
        // search and collect information for all extension points
        for (Class extensionPoint : ClassUtil.getTypes(extension)) {
            if (Arrays.asList(extensionPoint.getInterfaces()).contains(Extensible.class)) {
                // unregister extension
                extensions.remove(extensionPoint, extension);

                // unregister extension key
                Class[] params = ClassUtil.getParameter(extension, extensionPoint);

                if (params.length != 0 && params[0] != Object.class) {
                    keys.remove(hash(extensionPoint, params[0]), extension);

                    // custom lifestyle
                    if (extensionPoint == Lifestyle.class) {
                        for (Lifestyle lifestyle : lifestyles.get(params[0])) {
                            if (lifestyle.getClass() == extension) {
                                lifestyles.remove(params[0], lifestyle);
                                break;
                            }
                        }
                    }
                }
            }
        }
    }
}
