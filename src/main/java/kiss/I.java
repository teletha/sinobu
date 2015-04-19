/*
 * Copyright (C) 2014 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss;

import static jdk.internal.org.objectweb.asm.Opcodes.*;

import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackReader;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.io.StringReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.FileLock;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.LinkPermission;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.ProtectionDomain;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.ListProperty;
import javafx.beans.property.MapProperty;
import javafx.beans.property.SetProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleMapProperty;
import javafx.beans.property.SimpleSetProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.ObservableSet;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import jdk.internal.org.objectweb.asm.AnnotationVisitor;
import jdk.internal.org.objectweb.asm.ClassVisitor;
import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.Handle;
import jdk.internal.org.objectweb.asm.Label;
import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.internal.org.objectweb.asm.Type;
import kiss.model.ClassUtil;
import kiss.model.Model;
import kiss.model.Property;

/**
 * <p>
 * Sinobu is not obsolete framework but utility, which can manipulate objects as a
 * extremely-condensed facade.
 * </p>
 * <dl>
 * <dt>Instantiation and Management</dt>
 * <dd>
 * <p>
 * Usually, the container which manages the object uses “get” method to provide such functionality.
 * However, Sinobu uses {@link #make(Class)}. This difference of method name indicates the
 * difference of the way for the management of objects, which also greatly affects the default
 * lifestyle (called Scope in other containers).
 * </p>
 * <p>
 * We do not provides the functionalities related to object lifecycle through Sinobu, because we
 * believe that it is better to use functionalities equipped in Java as much as possible. For
 * example, if you want to receive initialization callbacks, it is better to use constructor. If you
 * want to receive the destruction callbacks, it is better to use {@link #finalize()} method.
 * </p>
 * </dd>
 * <dt>Dependency Injection</dt>
 * <dd>
 * <p>
 * Sinobu supports Constructor Injection <em>only</em>. The Constructor Injection is a dependency
 * injection variant where an object gets all its dependencies via the constructor. We can say that
 * there is no possibility that Setter Injection, Field Injection and Interface Injection will be
 * supported in the days to come. This is one of the most important policy in Sinobu. The following
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
 * <h1 id="ConfigurableEnvironment">Configurable Environment</h1>
 * <p>
 * Sinobu provides some enviroment variables that you can configure.
 * </p>
 * <ul>
 * <li><a href="#encoding">Character Encoding</a></li>
 * <li><a href="#loader">Parent Class Loader</a></li>
 * <li><a href="#working">Working Directory</a></li>
 * <li><a href="#scheduler">Task Scheduler</a></li>
 * </ul>
 * <p>
 * When you want to initialize these enviroment variables and your application environment related
 * to Sinobu, you have to manipulate these variables at static initialization phase of your
 * application class.
 * </p>
 * <h2 id="Patterns">Include/Exclude Patterns</h2>
 * <p>
 * Sinobu adopts "glob" pattern matching instead of "regex". * The case-insensitivity is platform
 * dependent and therefore not specified. Example is the following that:
 * </p>
 * <dl>
 * <dt>*</dt>
 * <dd>Matches zero or more characters of a name component without crossing directory boundaries.
 * </dd>
 * <dt>**</dt>
 * <dd>Matches zero or more characters of a name component with crossing directory boundaries.</dd>
 * <dt>?</dt>
 * <dd>Matches exactly one character of a name component.</dd>
 * <dt>*.java</dt>
 * <dd>Matches a path that represents a file name ending with ".java" in the current directory.</dd>
 * <dt>**.java</dt>
 * <dd>Matches a path that represents a file name ending with ".java" in all directories.</dd>
 * <dt>!**.java</dt>
 * <dd>Matches file names <em>not</em> ending with ".java" in all directories.</dd>
 * <dt>**.*</dt>
 * <dd>Matches file names containing a dot.</dd>
 * <dt>**.{java,class}</dt>
 * <dd>Matches file names ending with ".java" or ".class".</dd>
 * <dt>**&#47;foo.?</dt>
 * <dd>Matches file names starting with "foo." and a single character extension.</dd>
 * </dl>
 * <p>
 * The backslash character (\) is used to escape characters that would otherwise be interpreted as
 * special characters. The expression \\ matches a single backslash and "\{" matches a left brace
 * for example.
 * </p>
 * <p>
 * The frequently used patterns are the followings:
 * </p>
 * <dl>
 * <dt>*</dt>
 * <dd>All children paths which are under the user specified path are matched. (descendant paths
 * will not match, root path will not match)</dd>
 * <dt>**</dt>
 * <dd>All descendant paths which are under the user specified path are matched. (root path will not
 * match)</dd>
 * <dt>*.txt</dt>
 * <dd>All children paths which are under the user specified path and have ".txt" suffix are
 * matched. (descendant paths will not match, root path will not match)</dd>
 * <dt>image*</dt>
 * <dd>All children paths which are under the user specified path and have "image" prefix are
 * matched. (descendant paths will not match, root path will not match)</dd>
 * <dt>**.html</dt>
 * <dd>All descendant paths which are under the user specified path and have ".html" suffix are
 * matched. (root path will not match)</dd>
 * </dl>
 *
 * @version 2014/01/24 16:01:56
 */
@SuppressWarnings({"resource", "unchecked"})
public class I implements ThreadFactory, ClassListener<Extensible> {

    // Candidates of Method Name
    //
    // annotate
    // bind
    // create class copy
    // delete define
    // edit error
    // find
    // get
    // hash have
    // i18n include
    // json join
    // kick
    // locate load log
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
    // write weave warn walk watch
    // xml xerox
    // yield
    // zip

    /**
     * <p>
     * The configuration of charcter encoding in Sinobu, default value is <em>UTF-8</em>. It is
     * encouraged to use this encoding instead of platform default encoding when file I/O under the
     * Sinobu environment.
     * </p>
     */
    public static Charset $encoding = StandardCharsets.UTF_8;

    /**
     * <p>
     * The configuration of parent class loader in Sinobu, default value is
     * <code><em>I.class.getClassLoader()</em></code>.
     * </p>
     */
    public static ClassLoader $loader = I.class.getClassLoader();

    /**
     * <p>
     * The configuration of working directory in Sinobu, default value is <em>current directory</em>
     * of JVM .
     * </p>
     */
    public static Path $working = Paths.get(""); // Poplar Taneshima

    /** The namespace uri of Sinobu. */
    static final String URI = "sinobu";

    /** The circularity dependency graph per thread. */
    static final ThreadSpecific<Deque<Class>> dependencies = new ThreadSpecific(ArrayDeque.class);

    /** The cache for Module, Model and Lifestyle. */
    static final Modules modules = new Modules();

    /** The mapping from extension point to extensions. */
    private static final Table<Class, Class> extensions = new Table();

    /** The mapping from extension point to assosiated extension mapping. */
    private static final Table<String, Class> keys = new Table();

    /** The lock for configurations. */
    private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    /** The stax parser factory for reuse. */
    private static final XMLInputFactory stax = XMLInputFactory.newFactory();

    /** The document builder. */
    private static final DocumentBuilder dom;

    /** The javascript engine for reuse. */
    private static final ScriptEngine script;

    // /** The locale name resolver. */
    // private static final Control control = Control.getControl(Control.FORMAT_CLASS);

    /** The root temporary directory for Sinobu. */
    private static final Path temporaries;

    /** The temporary directory for the current processing JVM. */
    private static final Path temporary;

    /** The accessible internal method for class loading. */
    private static final Method find;

    /** The accessible internal method for class loading. */
    private static final Method define;

    private static final WeakHashMap<Object, Map> weak = new WeakHashMap();

    /** The parallel task manager. */
    // private static final ExecutorService parallel = Executors.newWorkStealingPool(4);
    private static final ExecutorService parallel = Executors.newCachedThreadPool(new I());

    /** The serial task manager. */
    private static final ExecutorService serial = Executors.newSingleThreadExecutor(new I());

    // initialization
    static {
        // built-in lifestyles
        modules.set(List.class, new Prototype(ArrayList.class));
        modules.set(Map.class, new Prototype(HashMap.class));
        modules.set(Prototype.class, new Prototype(Prototype.class));
        modules.set(ListProperty.class, () -> {
            return new SimpleListProperty(FXCollections.observableArrayList());
        } );
        modules.set(ObservableList.class, FXCollections::observableArrayList);
        modules.set(MapProperty.class, () -> {
            return new SimpleMapProperty(FXCollections.observableHashMap());
        } );
        modules.set(ObservableMap.class, FXCollections::observableHashMap);
        modules.set(SetProperty.class, () -> {
            return new SimpleSetProperty(FXCollections.observableSet());
        } );
        modules.set(ObservableSet.class, FXCollections::observableSet);

        try {
            // configure dom builder
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

            dom = factory.newDocumentBuilder();

            // Create the root temporary directory for Sinobu.
            temporaries = Files.createDirectories(Paths.get(System.getProperty("java.io.tmpdir"), "Sinobu"));

            // Clean up any old temporary directories by listing all of the files, using a prefix
            // filter and that don't have a lock file.
            for (Path path : walkDirectory(temporaries, "temporary*")) {
                // create a file to represent the lock
                RandomAccessFile file = new RandomAccessFile(path.resolve("lock").toFile(), "rw");

                // test whether we can acquire lock or not
                FileLock lock = file.getChannel().tryLock();

                // release lock immediately
                file.close();

                // delete the all contents in the temporary directory since we could acquire a
                // exclusive lock
                if (lock != null) {
                    I.delete(path);
                }
            }

            // Create the temporary directory for the current processing JVM.
            temporary = Files.createTempDirectory(temporaries, "temporary");

            // Create a lock after creating the temporary directory so there is no race condition
            // with another application trying to clean our temporary directory.
            new RandomAccessFile(temporary.resolve("lock").toFile(), "rw").getChannel().tryLock();

            // reflect class loading related methods
            find = ClassLoader.class.getDeclaredMethod("findLoadedClass", String.class);
            find.setAccessible(true);
            define = ClassLoader.class
                    .getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class, ProtectionDomain.class);
            define.setAccessible(true);
        } catch (Exception e) {
            throw I.quiet(e);
        }

        // configure javascript engine
        script = new ScriptEngineManager($loader).getEngineByName("js");

        // Load myself as module. All built-in classload listeners and extension points will be
        // loaded and activated.
        $loader = load(I.class, true);
    }

    /**
     * <p>
     * Initialize environment.
     * </p>
     */
    private I() {
    }

    public static <T> T associate(Object host, Class<T> type) {
        Map map = weak.get(host);

        if (map == null) {
            map = new ConcurrentHashMap();
            weak.put(host, map);
        }

        Object value = map.get(type);

        if (value == null) {
            value = I.make(type);
            map.put(type, value);
        }
        return (T) value;
    }

    // /**
    // * <p>
    // * Retrieve file name and extension from the specified path.
    // * </p>
    // *
    // * @param path A target path.
    // * @return A file name array like the following [name, extension].
    // * @throws NullPointerException A path is <code>null</code>.
    // */
    // public static String[] call(Path path) {
    // String[] names = {"", ""};
    // String name = path.getFileName().toString();
    // int index = name.lastIndexOf('.');
    //
    // if (index == -1) {
    // names[0] = name;
    // } else {
    // names[0] = name.substring(0, index);
    // names[1] = name.substring(index + 1);
    // }
    // return names;
    // }

    /**
     * <p>
     * Note : This method closes both input and output stream carefully.
     * </p>
     * <p>
     * Copy bytes from a {@link InputStream} to an {@link OutputStream}. This method buffers the
     * input internally, so there is no need to use a buffered stream.
     * </p>
     *
     * @param input A {@link InputStream} to read from.
     * @param output An {@link OutputStream} to write to.
     * @param close Whether input and output steream will be closed automatically or not.
     * @throws IOException If an I/O error occurs.
     * @throws NullPointerException If the input or output is null.
     * @throws SecurityException If a security manager exists and its
     *             {@link SecurityManager#checkWrite(String)} method does not allow a file to be
     *             created.
     */
    public static void copy(InputStream input, OutputStream output, boolean close) {
        int size;
        byte[] buffer = new byte[8192];

        try {
            while ((size = input.read(buffer)) != -1) {
                output.write(buffer, 0, size);
            }
        } catch (IOException e) {
            throw quiet(e);
        } finally {
            if (close) {
                quiet(input);
                quiet(output);
            }
        }
    }

    /**
     * <p>
     * Copy a input {@link Path} to the output {@link Path} with its attributes. Simplified strategy
     * is the following:
     * </p>
     * <p>
     * <pre>
     * if (input.isFile) {
     *   if (output.isFile) {
     *     // Copy input file to output file.
     *   } else {
     *     // Copy input file under output directory.
     *   }
     * } else {
     *   if (output.isFile) {
     *     // NoSuchFileException will be thrown.
     *   } else {
     *     // Copy input directory under output directory deeply.
     *     // You can also specify <a href="#Patterns">include/exclude patterns</a>.
     *   }
     * }
     * </pre>
     * <p>
     * If the output file already exists, it will be replaced by input file unconditionaly. The
     * exact file attributes that are copied is platform and file system dependent and therefore
     * unspecified. Minimally, the last-modified-time is copied to the output file if supported by
     * both the input and output file store. Copying of file timestamps may result in precision
     * loss.
     * </p>
     * <p>
     * Copying a file is not an atomic operation. If an {@link IOException} is thrown then it
     * possible that the output file is incomplete or some of its file attributes have not been
     * copied from the input file.
     * </p>
     *
     * @param input A input {@link Path} object which can be file or directory.
     * @param output An output {@link Path} object which can be file or directory.
     * @param patterns <a href="#Patterns">include/exclude patterns</a> you want to sort out.
     * @throws IOException If an I/O error occurs.
     * @throws NullPointerException If the specified input or output file is <code>null</code>.
     * @throws NoSuchFileException If the input file is directory and the output file is
     *             <em>not</em> directory.
     * @throws SecurityException In the case of the default provider, and a security manager is
     *             installed, the {@link SecurityManager#checkRead(String)} method is invoked to
     *             check read access to the source file, the
     *             {@link SecurityManager#checkWrite(String)} is invoked to check write access to
     *             the target file. If a symbolic link is copied the security manager is invoked to
     *             check {@link LinkPermission}("symbolic").
     */
    public static void copy(Path input, Path output, String... patterns) {
        new Visitor(input, output, 0, null, patterns);
    }

    /**
     * <p>
     * Delete a input {@link Path}. Simplified strategy is the following:
     * </p>
     * <p>
     * <pre>
     * if (input.isFile) {
     *   // Delete input file unconditionaly.
     * } else {
     *   // Delete input directory deeply.
     *   // You can also specify <a href="#Patterns">include/exclude patterns</a>.
     * }
     * </pre>
     * <p>
     * On some operating systems it may not be possible to remove a file when it is open and in use
     * by this Java virtual machine or other programs.
     * </p>
     *
     * @param input A input {@link Path} object which can be file or directory.
     * @param patterns <a href="#Patterns">include/exclude patterns</a> you want to sort out.
     * @throws IOException If an I/O error occurs.
     * @throws NullPointerException If the specified input file is <code>null</code>.
     * @throws SecurityException In the case of the default provider, and a security manager is
     *             installed, the {@link SecurityManager#checkRead(String)} method is invoked to
     *             check read access to the source file, the
     *             {@link SecurityManager#checkWrite(String)} is invoked to check write access to
     *             the target file. If a symbolic link is copied the security manager is invoked to
     *             check {@link LinkPermission}("symbolic").
     */
    public static void delete(Path input, String... patterns) {
        if (input != null) {
            new Visitor(input, null, 2, null, patterns);
        }
    }

    /**
     * <p>
     * Find all <a href="Extensible.html#Extension">Extensions</a> which are specified by the given
     * <a href="Extensible#ExtensionPoint">Extension Point</a>.
     * </p>
     * <p>
     * The returned list will be "safe" in that no references to it are maintained by Sinobu. (In
     * other words, this method must allocate a new list). The caller is thus free to modify the
     * returned list.
     * </p>
     *
     * @param <E> An Extension Point.
     * @param extensionPoint An extension point class. The
     *            <a href="Extensible#ExtensionPoint">Extension Point</a> class is only accepted,
     *            otherwise this method will return empty list.
     * @return All Extensions of the given Extension Point or empty list.
     * @throws NullPointerException If the Extension Point is <code>null</code>.
     */
    public static <E extends Extensible> List<E> find(Class<E> extensionPoint) {
        // Skip null check because this method can throw NullPointerException.
        List<Class> classes = extensions.get(extensionPoint);

        // instantiate all found extesions
        List list = new ArrayList(classes.size());

        for (Class extension : classes) {
            list.add(make(extension));
        }
        return list;
    }

    /**
     * <p>
     * Find the <a href="Extensible.html#Extension">Extension</a> which are specified by the given
     * <a href="Extensible#ExtensionPoint">Extension Point</a> and the given key.
     * </p>
     *
     * @param <E> An Extension Point.
     * @param extensionPoint An Extension Point class. The
     *            <a href="Extensible#ExtensionPoint">Extension Point</a> class is only accepted,
     *            otherwise this method will return <code>null</code>.
     * @param key An <a href="Extensible.html#ExtensionKey">Extension Key</a> class.
     * @return A associated Extension of the given Extension Point and the given Extension Key or
     *         <code>null</code>.
     */
    public static <E extends Extensible> E find(Class<E> extensionPoint, Class key) {
        if (extensionPoint == null || key == null) {
            return null;
        }

        Class<E> supplier = keys.find(extensionPoint.getName().concat(key.getName()));

        return supplier == null ? null : make(supplier);
    }

    /**
     * <p>
     * Find all <a href="Extensible.html#Extension">Extensions</a> classes which are specified by
     * the given <a href="Extensible#ExtensionPoint">Extension Point</a>.
     * </p>
     * <p>
     * The returned list will be "safe" in that no references to it are maintained by Sinobu. (In
     * other words, this method must allocate a new list). The caller is thus free to modify the
     * returned list.
     * </p>
     *
     * @param <E> An Extension Point.
     * @param extensionPoint An extension point class. The
     *            <a href="Extensible#ExtensionPoint">Extension Point</a> class is only accepted,
     *            otherwise this method will return empty list.
     * @return All Extension classes of the given Extension Point or empty list.
     * @throws NullPointerException If the Extension Point is <code>null</code>.
     */
    public static <E extends Extensible> List<Class<E>> findAs(Class<E> extensionPoint) {
        return new ArrayList(extensions.get(extensionPoint));
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
        String lang = "_".concat(make(Locale.class).getLanguage());

        for (Class clazz : extensions.get(bundleClass)) {
            if (clazz.getName().endsWith(lang)) {
                bundleClass = clazz;
                break;
            }
        }
        return make(bundleClass);
    }

    // GAE doesn't allow ResourceBundle.Control.
    //
    // public static <B extends Extensible> B i18n(Class<B> bundleClass) {
    // root: for (Locale locale : control.getCandidateLocales("", I.make(Locale.class))) {
    // List<Class> list = extensions.get(bundleClass);
    //
    // if (list != null) {
    // for (Class clazz : list) {
    // if (clazz.getName().endsWith(locale.getLanguage())) {
    // bundleClass = clazz;
    // break root;
    // }
    // }
    // }
    // }
    // return make(bundleClass);
    // }

    /**
     * <p>
     * Returns a string containing the string representation of each of items, using the specified
     * separator between each.
     * </p>
     *
     * @param delimiter A sequence of characters that is used to separate each of the elements in
     *            the resulting String.
     * @param items A {@link Iterable} items.
     * @return A concat expression.
     * @throws NullPointerException If items is <code>null</code>.
     */
    public static String join(CharSequence delimiter, CharSequence... items) {
        return join(delimiter, Arrays.asList(items));
    }

    /**
     * <p>
     * Returns a string containing the string representation of each of items, using the specified
     * separator between each.
     * </p>
     *
     * @param delimiter A sequence of characters that is used to separate each of the elements in
     *            the resulting String.
     * @param items A {@link Iterable} items.
     * @return A concat expression.
     */
    public static String join(CharSequence delimiter, Iterable items) {
        if (items == null) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        Iterator iterator = items.iterator();

        if (iterator.hasNext()) {
            builder.append(iterator.next());

            while (iterator.hasNext()) {
                builder.append(delimiter).append(iterator.next());
            }
        }
        return builder.toString();
    }

    /**
     * <p>
     * Locate the specified file URL and return the plain {@link Path} object.
     * </p>
     *
     * @param filePath A location path.
     * @return A located {@link Path}.
     * @throws NullPointerException If the given file path is null.
     * @throws SecurityException If a security manager exists and its
     *             {@link SecurityManager#checkWrite(String)} method does not allow a file to be
     *             created.
     */
    public static Path locate(URL filePath) {
        try {
            // Use File constructor with URI to resolve escaped character.
            return new File(filePath.toURI()).toPath();
        } catch (URISyntaxException e) {
            return new File(filePath.getPath()).toPath();
        }
    }

    /**
     * <p>
     * Locate the specified file path and return the plain {@link Path} object.
     * </p>
     *
     * @param filePath A location path.
     * @return A located {@link Path}.
     * @throws NullPointerException If the given file path is null.
     * @throws SecurityException If a security manager exists and its
     *             {@link SecurityManager#checkWrite(String)} method does not allow a file to be
     *             created.
     */
    public static Path locate(String filePath) {
        return Paths.get(filePath);
    }

    /**
     * <p>
     * Creates a new abstract file somewhere beneath the system's temporary directory (as defined by
     * the <code>java.io.tmpdir</code> system property).
     * </p>
     *
     * @return A newly created temporary file which is not exist yet.
     * @throws SecurityException If a security manager exists and its
     *             {@link SecurityManager#checkWrite(String)} method does not allow a file to be
     *             created.
     */
    public static Path locateTemporary() {
        try {
            Path path = Files.createTempDirectory(temporary, "temporary");

            // Delete entity file.
            Files.delete(path);

            // API definition
            return path;
        } catch (IOException e) {
            throw quiet(e);
        }
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
     * @param <M> A model type.
     * @param modelClass A target class to create instance.
     * @return A instance of the specified model class. This instance is managed by Sinobu.
     * @throws NullPointerException If the model class is <code>null</code>.
     * @throws IllegalArgumentException If the model class is non-accessible or final class.
     * @throws UnsupportedOperationException If the model class is inner-class.
     * @throws ClassCircularityError If the model has circular dependency.
     * @throws InstantiationException If Sinobu can't instantiate(resolve) the model class.
     */
    public static <M> M make(Class<M> modelClass) {
        return makeLifestyle(modelClass).get();
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
     * @param <M> A model class.
     * @return A instance of the specified model class. This instance is managed by Sinobu.
     * @throws NullPointerException If the model class is <code>null</code>.
     * @throws IllegalArgumentException If the model class is non-accessible or final class.
     * @throws UnsupportedOperationException If the model class is inner-class.
     * @throws ClassCircularityError If the model has circular dependency.
     * @throws InstantiationException If Sinobu can't instantiate(resolve) the model class.
     */
    static <M> Lifestyle<M> makeLifestyle(Class<M> modelClass) {
        // Skip null check because this method can throw NullPointerException.
        // if (modelClass == null) throw new NullPointerException("NPE");
        if (modelClass.isSynthetic()) {
            modelClass = (Class<M>) modelClass.getSuperclass();
        }

        // At first, we must confirm the cached lifestyle associated with the model class. If
        // there is no such cache, we will try to create newly lifestyle.
        Lifestyle<M> lifestyle = modules.get(modelClass);

        if (lifestyle != null) return lifestyle; // use cache

        // Skip null check because this method can throw NullPointerException.
        // if (modelClass == null) throw new NullPointerException("NPE");

        // The model class have some preconditions to have to meet.
        if (modelClass.isLocalClass()) {
            throw new UnsupportedOperationException(modelClass + " is  inner class.");
        }

        int modifier = modelClass.getModifiers();

        // In the second place, we must find the actual model class which is associated with
        // this model class. If the actual model class is a concreate, we can use it directly.
        Class<M> actualClass = modelClass;

        if (((Modifier.ABSTRACT | Modifier.INTERFACE) & modifier) != 0) {
            // TODO model provider finding strategy
            // This strategy is decided at execution phase.
            actualClass = modules.find(modelClass);

            // updata to the actual model class's modifier
            modifier = actualClass.getModifiers();
        }

        // If this model is non-private or final class, we can extend it for interceptor
        // mechanism.
        if (((Modifier.PRIVATE | Modifier.FINAL) & modifier) == 0) {
            Table<Method, Annotation> interceptables = ClassUtil.getAnnotations(actualClass);

            // Enhance the actual model class if needed.
            if (!interceptables.isEmpty()) {
                actualClass = define(actualClass, interceptables);
            }
        }

        // Construct dependency graph for the current thred.
        Deque<Class> dependency = dependencies.get();
        dependency.add(actualClass);

        // Don't use 'contains' method check here to resolve singleton based
        // circular reference. So we must judge it from the size of context. If the
        // context contains too many classes, it has a circular reference
        // independencies.
        if (16 < dependency.size()) {
            // Deque will be contain repeated Classes so we must shrink it with
            // maintaining its class order.
            throw new ClassCircularityError(new LinkedHashSet(dependency).toString());
        }

        try {
            // At first, we should search the associated lifestyle from extension points.
            lifestyle = find(Lifestyle.class, modelClass);

            // Then, check its Manageable annotation.
            if (lifestyle == null) {
                // If the actual model class doesn't provide its lifestyle explicitly, we use
                // Prototype lifestyle which is default lifestyle in Sinobu.
                Manageable manageable = actualClass.getAnnotation(Manageable.class);

                // Create new lifestyle for the actual model class
                lifestyle = (Lifestyle) make((Class) (manageable == null ? Prototype.class : manageable.lifestyle()));
            }

            // Trace dependency graph to detect circular dependencies.
            if (lifestyle instanceof Prototype) {
                for (Class param : ((Prototype) lifestyle).instantiator.getParameterTypes()) {
                    if (param != Class.class) {
                        makeLifestyle(param);
                    }
                }
            }

            // This lifestyle is safe and has no circular dependencies.
            return modules.let(modelClass, lifestyle);
        } finally {
            dependency.pollLast();
        }
    }

    /**
     * Returns the automatic generated class which implements or extends the given model.
     *
     * @param model A class information of the model.
     * @param interceptables Information of interceptable methods.
     * @return A generated {@link Class} object.
     */
    private static synchronized Class define(Class model, Table interceptables) {
        model = Model.load(model).type;

        // Compute fully qualified class name for the generated class.
        // The coder class name is prefix to distinguish enhancer type by a name and make core
        // package classes (e.g. swing components) enhance.
        // The statement "String name = coder.getName() + model.type.getName();" produces larger
        // byte code and more objects. To reduce them, we should use the method "concat".
        String name = model.getName().concat(interceptables == null ? "-" : "+");

        if (name.startsWith("java.")) {
            name = "$".concat(name);
        }

        ClassLoader loader = model.getClassLoader();

        if (loader == null) {
            loader = $loader;
        }

        // find class from cache of class loader
        try {
            Class clazz = (Class) find.invoke(loader, name);

            if (clazz == null) {
                // start writing byte code
                ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);

                // write code actually
                write(writer, model, name.replace('.', '/'), interceptables);

                // retrieve byte code
                byte[] bytes = writer.toByteArray();

                // define class
                clazz = (Class) define.invoke(loader, name, bytes, 0, bytes.length, model.getProtectionDomain());
            }

            // API definition
            return clazz;
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }

    /**
     * <p>
     * Generate a byte code which represents the specified class name and implements the specified
     * model.
     * </p>
     */
    private static void write(ClassVisitor cv, Class model, String className, Table<Method, Annotation> interceptables) {
        Type type = Type.getType(model);

        // ================================================
        // START CODING
        // ================================================
        // The following steps is an outline flow.
        // 1. define class
        //
        // 2. define default constructor
        // The generated class must have only the default constructor which has no parameters. To
        // resolve dependencies, we provide the solution named as 'builtin construction'. The
        // constructor must call the parent constructor, so we prepare arguments in that step by
        // using bytecode enhancement.
        //
        // 3. define properties
        //
        // 4. implement accessible interfaces
        //
        // 5 fiinish
        //
        // ================================================

        // -----------------------------------------------------------------------------------
        // Define Class
        // -----------------------------------------------------------------------------------
        // public class GeneratedClass extends SuperClass implements Inteface1, Interface2....
        cv.visit(V1_8, ACC_PUBLIC | ACC_SUPER | ACC_SYNTHETIC, className, null, type.getInternalName(), null);

        // don't use visitSource method because this generated source is unknown
        // visitSource(className, null);

        // -----------------------------------------------------------------------------------
        // Define Constructor
        // -----------------------------------------------------------------------------------
        // decide constructor
        Constructor constructor = ClassUtil.getMiniConstructor(model);
        String descriptor = Type.getConstructorDescriptor(constructor);

        // public GeneratedClass( param1, param2 ) { super(param1, param2); ... }
        MethodVisitor mv = cv.visitMethod(ACC_PUBLIC, "<init>", descriptor, null, null);
        mv.visitCode();
        for (int i = 0; i < constructor.getParameterTypes().length + 1; i++) {
            mv.visitVarInsn(ALOAD, i); // allocate 'this' and parameter variables
        }
        mv.visitMethodInsn(INVOKESPECIAL, type.getInternalName(), "<init>", descriptor, false);
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0); // compute by ASM
        mv.visitEnd();

        // -----------------------------------------------------------------------------------
        // Define Annotation Pool
        // -----------------------------------------------------------------------------------
        cv.visitField(ACC_PRIVATE + ACC_STATIC, "pool", "Ljava/util/Map;", null, null).visitEnd();

        Label end = new Label();
        Label loop = new Label();

        mv = cv.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
        mv.visitCode();
        mv.visitTypeInsn(NEW, "java/util/HashMap");
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, "java/util/HashMap", "<init>", "()V", false);
        mv.visitFieldInsn(PUTSTATIC, className, "pool", "Ljava/util/Map;");
        mv.visitLdcInsn(Type.getType("L" + className + ";"));
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getDeclaredMethods", "()[Ljava/lang/reflect/Method;", false);
        mv.visitInsn(DUP);
        mv.visitVarInsn(ASTORE, 3);
        mv.visitInsn(ARRAYLENGTH);
        mv.visitVarInsn(ISTORE, 2);
        mv.visitInsn(ICONST_0);
        mv.visitVarInsn(ISTORE, 1);
        mv.visitJumpInsn(GOTO, end);
        mv.visitLabel(loop);
        mv.visitVarInsn(ALOAD, 3);
        mv.visitVarInsn(ILOAD, 1);
        mv.visitInsn(AALOAD);
        mv.visitVarInsn(ASTORE, 0);
        mv.visitFieldInsn(GETSTATIC, className, "pool", "Ljava/util/Map;");
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/reflect/Method", "getName", "()Ljava/lang/String;", false);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/reflect/Method", "getParameterTypes", "()[Ljava/lang/Class;", false);
        mv.visitMethodInsn(INVOKESTATIC, "java/util/Arrays", "toString", "([Ljava/lang/Object;)Ljava/lang/String;", false);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "concat", "(Ljava/lang/String;)Ljava/lang/String;", false);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/reflect/Method", "getAnnotations", "()[Ljava/lang/annotation/Annotation;", false);
        mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", true);
        mv.visitInsn(POP);
        mv.visitIincInsn(1, 1); // increment counter
        mv.visitLabel(end);
        mv.visitVarInsn(ILOAD, 1);
        mv.visitVarInsn(ILOAD, 2);
        mv.visitJumpInsn(IF_ICMPLT, loop);
        mv.visitInsn(RETURN);
        mv.visitMaxs(5, 4);
        mv.visitEnd();

        // -----------------------------------------------------------------------------------
        // Define Interceptable Methods
        // -----------------------------------------------------------------------------------
        Type context = Type.getType(Table.class);

        for (Entry<Method, List<Annotation>> entry : interceptables.entrySet()) {
            Method method = entry.getKey();

            // exclude the method which modifier is final, static, private or native
            if (((Modifier.STATIC | Modifier.PRIVATE | Modifier.NATIVE | Modifier.FINAL) & method
                    .getModifiers()) != 0) {
                continue;
            }

            Type methodType = Type.getType(method);

            mv = cv.visitMethod(ACC_PUBLIC, method.getName(), methodType.getDescriptor(), null, null);

            // Write annotations
            for (Annotation annotation : entry.getValue()) {
                annotate(annotation, mv.visitAnnotation(Type.getDescriptor(annotation.annotationType()), true));
            }

            // Write code
            mv.visitCode();

            // Zero parameter : Method name
            mv.visitLdcInsn(method.getName());

            // First parameter : Method delegation
            Handle handle = new Handle(H_INVOKESPECIAL, className.substring(0, className.length() - 1), method
                    .getName(), methodType.getDescriptor());
            mv.visitLdcInsn(handle);

            // Second parameter : Callee instance
            mv.visitVarInsn(ALOAD, 0);

            // Third parameter : Method parameter delegation
            Class[] params = method.getParameterTypes();

            mv.visitIntInsn(BIPUSH, params.length);
            mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");

            // objects[0] ~ [n] are method parameter
            for (int i = 0; i < params.length; i++) {
                mv.visitInsn(DUP);
                mv.visitIntInsn(BIPUSH, i);
                mv.visitVarInsn(Type.getType(params[i]).getOpcode(ILOAD), i + 1);
                wrap(params[i], mv);
                mv.visitInsn(AASTORE);
            }

            // Fourth parameter : Pass annotation information
            mv.visitFieldInsn(GETSTATIC, className, "pool", "Ljava/util/Map;");
            mv.visitLdcInsn(method.getName().concat(Arrays.toString(method.getParameterTypes())));
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "get", "(Ljava/lang/Object;)Ljava/lang/Object;", true);
            mv.visitTypeInsn(CHECKCAST, "[Ljava/lang/annotation/Annotation;");

            // Invoke interceptor method
            mv.visitMethodInsn(INVOKESTATIC, "kiss/Interceptor", "invoke", "(Ljava/lang/String;Ljava/lang/invoke/MethodHandle;Ljava/lang/Object;[Ljava/lang/Object;[Ljava/lang/annotation/Annotation;)Ljava/lang/Object;", false);
            cast(method.getReturnType(), mv);
            mv.visitInsn(methodType.getReturnType().getOpcode(IRETURN));
            mv.visitMaxs(0, 0); // compute by ASM
            mv.visitEnd();
        }

        /**
         * <p>
         * Make context field.
         * </p>
         * <pre>
         * private transient Table context;
         * </pre>
         */
        cv.visitField(ACC_PUBLIC | ACC_TRANSIENT, "context", context.getDescriptor(), null, null).visitEnd();

        // -----------------------------------------------------------------------------------
        // Finish Writing Source Code
        // -----------------------------------------------------------------------------------
        cv.visitEnd();
    }

    /**
     * <p>
     * Helper method to write annotation code.
     * </p>
     *
     * @param annotation An annotation you want to write.
     * @param visitor An annotation target.
     */
    private static void annotate(Annotation annotation, AnnotationVisitor visitor) {
        // For access non-public annotation class, use "getDeclaredMethods" instead of "getMethods".
        for (Method method : annotation.annotationType().getDeclaredMethods()) {
            method.setAccessible(true);

            try {
                Class clazz = method.getReturnType();
                Object value = method.invoke(annotation);

                if (clazz == Class.class) {
                    // Class
                    visitor.visit(method.getName(), Type.getType((Class) value));
                } else if (clazz.isEnum()) {
                    // Enum
                    visitor.visitEnum(method.getName(), Type.getDescriptor(clazz), ((Enum) value).name());
                } else if (clazz.isAnnotation()) {
                    // Annotation
                    annotate((Annotation) value, visitor.visitAnnotation(method.getName(), Type.getDescriptor(clazz)));
                } else if (clazz.isArray()) {
                    // Array
                    clazz = clazz.getComponentType();
                    AnnotationVisitor array = visitor.visitArray(method.getName());

                    for (int i = 0; i < Array.getLength(value); i++) {
                        if (clazz.isAnnotation()) {
                            // Annotation Array
                            annotate((Annotation) Array.get(value, i), array
                                    .visitAnnotation(null, Type.getDescriptor(clazz)));
                        } else if (clazz == Class.class) {
                            // Class Array
                            array.visit(null, Type.getType((Class) Array.get(value, i)));
                        } else if (clazz.isEnum()) {
                            // Enum Array
                            array.visitEnum(null, Type.getDescriptor(clazz), ((Enum) Array.get(value, i)).name());
                        } else {
                            // Other Type Array
                            array.visit(null, Array.get(value, i));
                        }
                    }
                    array.visitEnd();
                } else {
                    // Other Type
                    visitor.visit(method.getName(), value);
                }
            } catch (Exception e) {
                throw I.quiet(e);
            }
        }
        visitor.visitEnd();
    }

    /**
     * Helper method to write cast code. This cast mostly means down cast. (e.g. Object -> String,
     * Object -> int)
     *
     * @param clazz A class to cast.
     * @return A class type to be casted.
     */
    private static Type cast(Class clazz, MethodVisitor mv) {
        Type type = Type.getType(clazz);

        if (clazz.isPrimitive()) {
            if (clazz != Void.TYPE) {
                Type wrapper = Type.getType(ClassUtil.wrap(clazz));
                mv.visitTypeInsn(CHECKCAST, wrapper.getInternalName());
                mv.visitMethodInsn(INVOKEVIRTUAL, wrapper
                        .getInternalName(), clazz.getName() + "val", "()" + type.getDescriptor(), false);
            }
        } else {
            mv.visitTypeInsn(CHECKCAST, type.getInternalName());
        }

        // API definition
        return type;
    }

    /**
     * Helper method to write cast code. This cast mostly means up cast. (e.g. String -> Object, int
     * -> Integer)
     *
     * @param clazz A primitive class type to wrap.
     */
    private static void wrap(Class clazz, MethodVisitor mv) {
        if (clazz.isPrimitive() && clazz != Void.TYPE) {
            Type wrapper = Type.getType(ClassUtil.wrap(clazz));
            mv.visitMethodInsn(INVOKESTATIC, wrapper.getInternalName(), "valueOf", "(" + Type.getType(clazz)
                    .getDescriptor() + ")" + wrapper.getDescriptor(), false);
        }
    }

    /**
     * <p>
     * Move a input {@link Path} to an output {@link Path} with its attributes. Simplified strategy
     * is the following:
     * </p>
     * <p>
     * <pre>
     * if (input.isFile) {
     *   if (output.isFile) {
     *     // Move input file to output file.
     *   } else {
     *     // Move input file under output directory.
     *   }
     * } else {
     *   if (output.isFile) {
     *     // NoSuchFileException will be thrown.
     *   } else {
     *     // Move input directory under output directory deeply.
     *     // You can also specify <a href="#Patterns">include/exclude patterns</a>.
     *   }
     * }
     * </pre>
     * <p>
     * If the output file already exists, it will be replaced by input file unconditionaly. The
     * exact file attributes that are copied is platform and file system dependent and therefore
     * unspecified. Minimally, the last-modified-time is copied to the output file if supported by
     * both the input and output file store. Copying of file timestamps may result in precision
     * loss.
     * </p>
     * <p>
     * Moving a file is an atomic operation.
     * </p>
     *
     * @param input A input {@link Path} object which can be file or directory.
     * @param output An output {@link Path} object which can be file or directory.
     * @param patterns <a href="#Patterns">include/exclude patterns</a> you want to sort out.
     * @throws IOException If an I/O error occurs.
     * @throws NullPointerException If the specified input or output file is <code>null</code>.
     * @throws NoSuchFileException If the input file is directory and the output file is
     *             <em>not</em> directory.
     * @throws SecurityException In the case of the default provider, and a security manager is
     *             installed, the {@link SecurityManager#checkRead(String)} method is invoked to
     *             check read access to the source file, the
     *             {@link SecurityManager#checkWrite(String)} is invoked to check write access to
     *             the target file. If a symbolic link is copied the security manager is invoked to
     *             check {@link LinkPermission}("symbolic").
     */
    public static void move(Path input, Path output, String... patterns) {
        new Visitor(input, output, 1, null, patterns);
    }

    //
    // /**
    // * <p>
    // * Parse the specified xml {@link Path} using the specified sequence of {@link
    // XMLFilter} .
    // The
    // * application can use this method to instruct the XML reader to begin parsing an
    // XML document
    // * from the specified path.
    // * </p>
    // * <p>
    // * Sinobu use the {@link XMLReader} which has the following features.
    // * </p>
    // * <ul>
    // * <li>Support XML namespaces.</li>
    // * <li>Support <a href="http://www.w3.org/TR/xinclude/">XML Inclusions (XInclude)
    // Version
    // * 1.0</a>.</li>
    // * <li><em>Not</em> support any validations (DTD or XML Schema).</li>
    // * <li><em>Not</em> support external DTD completely (parser doesn't even access
    // DTD, using
    // * "http://apache.org/xml/features/nonvalidating/load-external-dtd"
    // feature).</li>
    // * </ul>
    // *
    // * @param source A path to xml source.
    // * @param filters A list of filters to parse a sax event. This may be
    // <code>null</code>.
    // * @throws NullPointerException If the specified source is <code>null</code>. If
    // one of the
    // * specified filter is <code>null</code>.
    // * @throws SAXException Any SAX exception, possibly wrapping another exception.
    // * @throws IOException An IO exception from the parser, possibly from a byte
    // stream or
    // character
    // * stream supplied by the application.
    // */
    // public static void parse(Path source, XMLFilter... filters) {
    // try {
    // InputSource input = new InputSource(Files.newBufferedReader(source, $encoding));
    // input.setPublicId(source.toString());
    //
    // parse(input, filters);
    // } catch (Exception e) {
    // throw quiet(e);
    // }
    // }
    //
    // /**
    // * <p>
    // * Parse the specified xml {@link InputSource} using the specified sequence of
    // {@link
    // XMLFilter}
    // * . The application can use this method to instruct the XML reader to begin
    // parsing an XML
    // * document from any valid input source (a character stream, a byte stream, or a
    // URI).
    // * </p>
    // * <p>
    // * Sinobu use the {@link XMLReader} which has the following features.
    // * </p>
    // * <ul>
    // * <li>Support XML namespaces.</li>
    // * <li>Support <a href="http://www.w3.org/TR/xinclude/">XML Inclusions (XInclude)
    // Version
    // * 1.0</a>.</li>
    // * <li><em>Not</em> support any validations (DTD or XML Schema).</li>
    // * <li><em>Not</em> support external DTD completely (parser doesn't even access
    // DTD, using
    // * "http://apache.org/xml/features/nonvalidating/load-external-dtd"
    // feature).</li>
    // * </ul>
    // *
    // * @param source A xml source.
    // * @param filters A list of filters to parse a sax event. This may be
    // <code>null</code>.
    // * @throws NullPointerException If the specified source is <code>null</code>. If
    // one of the
    // * specified filter is <code>null</code>.
    // * @throws SAXException Any SAX exception, possibly wrapping another exception.
    // * @throws IOException An IO exception from the parser, possibly from a byte
    // stream or
    // character
    // * stream supplied by the application.
    // */
    // public static void parse(InputSource source, XMLFilter... filters) {
    // try {
    // // create new xml reader
    // XMLReader reader = sax.newSAXParser().getXMLReader();
    //
    // // chain filters if needed
    // for (int i = 0; i < filters.length; i++) {
    // // find the root filter of the current multilayer filter
    // XMLFilter filter = filters[i];
    //
    // while (filter.getParent() instanceof XMLFilter) {
    // filter = (XMLFilter) filter.getParent();
    // }
    //
    // // the root filter makes previous filter as parent xml reader
    // filter.setParent(reader);
    //
    // if (filter instanceof LexicalHandler) {
    // reader.setProperty("http://xml.org/sax/properties/lexical-handler", filter);
    // }
    //
    // // current filter is a xml reader in next step
    // reader = filters[i];
    // }
    //
    // // start parsing
    // reader.parse(source);
    // } catch (Exception e) {
    // // We must throw the checked exception quietly and pass the original exception
    // instead
    // // of wrapped exception.
    // throw quiet(e);
    // }
    // }

    /**
     * <p>
     * Observe the specified {@link ObservableValue}.
     * </p>
     * <p>
     * An implementation of {@link ObservableValue} may support lazy evaluation, which means that
     * the value is not immediately recomputed after changes, but lazily the next time the value is
     * requested.
     * </p>
     * 
     * @param observable A target to observe.
     * @return A observable event stream.
     */
    public static <E extends Observable> Events<E> observe(E observable) {
        if (observable == null) {
            return Events.NEVER;
        }

        return new Events<>(observer -> {
            // create actual listener
            InvalidationListener listener = value -> {
                observer.onNext((E) value);
            };

            observable.addListener(listener); // register listener

            return () -> {
                observable.removeListener(listener); // unregister listener
            };
        } );
    }

    /**
     * <p>
     * Observe the specified {@link ObservableValue}.
     * </p>
     * <p>
     * An implementation of {@link ObservableValue} may support lazy evaluation, which means that
     * the value is not immediately recomputed after changes, but lazily the next time the value is
     * requested.
     * </p>
     * 
     * @param observable A target to observe.
     * @return A observable event stream.
     */
    public static <E> Events<E> observe(ObservableValue<E> observable) {
        if (observable == null) {
            return Events.NEVER;
        }

        return new Events<>(observer -> {
            // create actual listener
            ChangeListener<E> listener = (o, oldValue, newValue) -> {
                observer.onNext(newValue);
            };

            observable.addListener(listener); // register listener

            // notify the current value
            E value = observable.getValue();

            if (value != null) {
                listener.changed(observable, null, value);
            }

            return () -> {
                observable.removeListener(listener); // unregister listener
            };
        } );
    }

    /**
     * <p>
     * Observe the file system change and raises events when a file, directory, or file in a
     * directory, changes.
     * </p>
     * <p>
     * You can watch for changes in files and subdirectories of the specified directory.
     * </p>
     * <p>
     * The operating system interpret a cut-and-paste action or a move action as a rename action for
     * a directory and its contents. If you cut and paste a folder with files into a directory being
     * watched, the {@link Observer} object reports only the directory as new, but not its contents
     * because they are essentially only renamed.
     * </p>
     * <p>
     * Common file system operations might raise more than one event. For example, when a file is
     * moved from one directory to another, several Modify and some Create and Delete events might
     * be raised. Moving a file is a complex operation that consists of multiple simple operations,
     * therefore raising multiple events. Likewise, some applications might cause additional file
     * system events that are detected by the {@link Observer}.
     * </p>
     *
     * @param path A target path you want to observe. (file and directory are acceptable)
     * @return A observable event stream.
     * @throws NullPointerException If the specified path or listener is <code>null</code>.
     * @throws SecurityException In the case of the default provider, and a security manager is
     *             installed, the {@link SecurityManager#checkRead(String)} method is invoked to
     *             check read access to the source file, the
     *             {@link SecurityManager#checkWrite(String)} is invoked to check write access to
     *             the target file. If a symbolic link is copied the security manager is invoked to
     *             check {@link LinkPermission}("symbolic").
     */
    public static Events<WatchEvent<Path>> observe(Path path) {
        return observe(path, new String[0]);
    }

    /**
     * <p>
     * Observe the file system change and raises events when a file, directory, or file in a
     * directory, changes.
     * </p>
     * <p>
     * You can watch for changes in files and subdirectories of the specified directory.
     * </p>
     * <p>
     * The operating system interpret a cut-and-paste action or a move action as a rename action for
     * a directory and its contents. If you cut and paste a folder with files into a directory being
     * watched, the {@link Observer} object reports only the directory as new, but not its contents
     * because they are essentially only renamed.
     * </p>
     * <p>
     * Common file system operations might raise more than one event. For example, when a file is
     * moved from one directory to another, several Modify and some Create and Delete events might
     * be raised. Moving a file is a complex operation that consists of multiple simple operations,
     * therefore raising multiple events. Likewise, some applications might cause additional file
     * system events that are detected by the {@link Observer}.
     * </p>
     *
     * @param path A target path you want to observe. (file and directory are acceptable)
     * @param patterns <a href="#Patterns">include/exclude patterns</a> you want to sort out. Ignore
     *            patterns if you want to observe a file.
     * @return A observable event stream.
     * @throws NullPointerException If the specified path or listener is <code>null</code>.
     * @throws SecurityException In the case of the default provider, and a security manager is
     *             installed, the {@link SecurityManager#checkRead(String)} method is invoked to
     *             check read access to the source file, the
     *             {@link SecurityManager#checkWrite(String)} is invoked to check write access to
     *             the target file. If a symbolic link is copied the security manager is invoked to
     *             check {@link LinkPermission}("symbolic").
     */
    public static Events<WatchEvent<Path>> observe(Path path, String... patterns) {
        if (!Files.isDirectory(path)) {
            return observe(path.getParent(), path.getFileName().toString());
        }

        return new Events<>(observer -> {
            // Create logical file system watch service.
            Visitor watcher = new Visitor(path, observer, patterns);

            // Run in anothor thread.
            schedule(watcher);

            // API definition
            return watcher;
        } );
    }

    //
    // /**
    // * <p>
    // * Parse the specified xml {@link Path} using the specified sequence of {@link
    // XMLFilter} .
    // The
    // * application can use this method to instruct the XML reader to begin parsing an
    // XML document
    // * from the specified path.
    // * </p>
    // * <p>
    // * Sinobu use the {@link XMLReader} which has the following features.
    // * </p>
    // * <ul>
    // * <li>Support XML namespaces.</li>
    // * <li>Support <a href="http://www.w3.org/TR/xinclude/">XML Inclusions (XInclude)
    // Version
    // * 1.0</a>.</li>
    // * <li><em>Not</em> support any validations (DTD or XML Schema).</li>
    // * <li><em>Not</em> support external DTD completely (parser doesn't even access
    // DTD, using
    // * "http://apache.org/xml/features/nonvalidating/load-external-dtd"
    // feature).</li>
    // * </ul>
    // *
    // * @param source A path to xml source.
    // * @param filters A list of filters to parse a sax event. This may be
    // <code>null</code>.
    // * @throws NullPointerException If the specified source is <code>null</code>. If
    // one of the
    // * specified filter is <code>null</code>.
    // * @throws SAXException Any SAX exception, possibly wrapping another exception.
    // * @throws IOException An IO exception from the parser, possibly from a byte
    // stream or
    // character
    // * stream supplied by the application.
    // */
    // public static void parse(Path source, XMLFilter... filters) {
    // try {
    // InputSource input = new InputSource(Files.newBufferedReader(source, $encoding));
    // input.setPublicId(source.toString());
    //
    // parse(input, filters);
    // } catch (Exception e) {
    // throw quiet(e);
    // }
    // }
    //
    // /**
    // * <p>
    // * Parse the specified xml {@link InputSource} using the specified sequence of
    // {@link
    // XMLFilter}
    // * . The application can use this method to instruct the XML reader to begin
    // parsing an XML
    // * document from any valid input source (a character stream, a byte stream, or a
    // URI).
    // * </p>
    // * <p>
    // * Sinobu use the {@link XMLReader} which has the following features.
    // * </p>
    // * <ul>
    // * <li>Support XML namespaces.</li>
    // * <li>Support <a href="http://www.w3.org/TR/xinclude/">XML Inclusions (XInclude)
    // Version
    // * 1.0</a>.</li>
    // * <li><em>Not</em> support any validations (DTD or XML Schema).</li>
    // * <li><em>Not</em> support external DTD completely (parser doesn't even access
    // DTD, using
    // * "http://apache.org/xml/features/nonvalidating/load-external-dtd"
    // feature).</li>
    // * </ul>
    // *
    // * @param source A xml source.
    // * @param filters A list of filters to parse a sax event. This may be
    // <code>null</code>.
    // * @throws NullPointerException If the specified source is <code>null</code>. If
    // one of the
    // * specified filter is <code>null</code>.
    // * @throws SAXException Any SAX exception, possibly wrapping another exception.
    // * @throws IOException An IO exception from the parser, possibly from a byte
    // stream or
    // character
    // * stream supplied by the application.
    // */
    // public static void parse(InputSource source, XMLFilter... filters) {
    // try {
    // // create new xml reader
    // XMLReader reader = sax.newSAXParser().getXMLReader();
    //
    // // chain filters if needed
    // for (int i = 0; i < filters.length; i++) {
    // // find the root filter of the current multilayer filter
    // XMLFilter filter = filters[i];
    //
    // while (filter.getParent() instanceof XMLFilter) {
    // filter = (XMLFilter) filter.getParent();
    // }
    //
    // // the root filter makes previous filter as parent xml reader
    // filter.setParent(reader);
    //
    // if (filter instanceof LexicalHandler) {
    // reader.setProperty("http://xml.org/sax/properties/lexical-handler", filter);
    // }
    //
    // // current filter is a xml reader in next step
    // reader = filters[i];
    // }
    //
    // // start parsing
    // reader.parse(source);
    // } catch (Exception e) {
    // // We must throw the checked exception quietly and pass the original exception
    // instead
    // // of wrapped exception.
    // throw quiet(e);
    // }
    // }

    /**
     * <p>
     * Create value set.
     * </p>
     * 
     * @param param1 A first parameter.
     * @return
     */
    public static <Param1> Unary<Param1> pair(Param1 param1) {
        return new Unary(param1);
    }

    /**
     * <p>
     * Create value set.
     * </p>
     * 
     * @param param1 A first parameter.
     * @param param2 A second parameter.
     * @return
     */
    public static <Param1, Param2> Binary<Param1, Param2> pair(Param1 param1, Param2 param2) {
        return new Binary(param1, param2);
    }

    /**
     * <p>
     * Create value set.
     * </p>
     * 
     * @param param1 A first parameter.
     * @param param2 A second parameter.
     * @param param3 A third parameter.
     * @return
     */
    public static <Param1, Param2, Param3> Ternary<Param1, Param2, Param3> pair(Param1 param1, Param2 param2, Param3 param3) {
        return new Ternary(param1, param2, param3);
    }

    /**
     * <p>
     * Close the specified object quietly if it is {@link AutoCloseable}. Equivalent to
     * {@link AutoCloseable#close()}, except any exceptions will be ignored. This is typically used
     * in finally block like the following.
     * </p>
     * <p>
     * <pre>
     * AutoCloseable input = null;
     *
     * try {
     *     // some IO action
     * } catch (Exception e) {
     *     throw e;
     * } finally {
     *     I.quiet(input);
     * }
     * </pre>
     * <p>
     * Throw the specified checked exception quietly or close the specified {@link AutoCloseable}
     * object quietly.
     * </p>
     * <p>
     * This method <em>doesn't</em> wrap checked exception around unchecked exception (e.g. new
     * RuntimeException(e)) and <em>doesn't</em> shelve it. This method deceive the compiler that
     * the checked exception is unchecked one. So you can catch a raw checked exception in the
     * caller of the method which calls this method.
     * </p>
     * <p>
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
     * @param object A exception to throw quietly or a object to close quietly.
     * @return A pseudo unchecked exception.
     * @throws NullPointerException If the specified exception is <code>null</code>.
     */
    public static RuntimeException quiet(Object object) {
        if (object instanceof Throwable) {
            Throwable throwable = (Throwable) object;

            // retrieve original exception from the specified wrapped exception
            if (throwable instanceof InvocationTargetException) throwable = throwable.getCause();

            // throw quietly
            return I.<RuntimeException> quietly(throwable);
        }

        if (object instanceof AutoCloseable) {
            try {
                ((AutoCloseable) object).close();
            } catch (Exception e) {
                throw quiet(e);
            }
        }

        // API definition
        return null;
    }

    /**
     * <p>
     * Deceive complier that the specified checked exception is unchecked exception.
     * </p>
     *
     * @param <T> A dummy type for {@link RuntimeException}.
     * @param throwable Any error.
     * @return A runtime error.
     * @throws T Dummy error to deceive compiler.
     */
    @SuppressWarnings("unchecked")
    private static <T extends Throwable> T quietly(Throwable throwable) throws T {
        throw (T) throwable;
    }

    /**
     * <p>
     * Reads Java object tree from the given XML or JSON input.
     * </p>
     *
     * @param input A serialized Java object tree data as XML or JSON. If the input is incompatible
     *            with Java object, this method ignores the input. <code>null</code> will throw
     *            {@link NullPointerException}. The empty or invalid format data will throw
     *            {@link ScriptException}.
     * @param output A root Java object. All properties will be assigned from the given data deeply.
     *            If the input is incompatible with Java object, this method ignores the input.
     *            <code>null</code> will throw {@link java.lang.NullPointerException}.
     * @return A root Java object.
     * @throws NullPointerException If the input data or the root Java object is <code>null</code>.
     * @throws ScriptException If the input data is empty or invalid format.
     * @throws NoSuchFileException If the input path doesn't exist.
     * @throws AccessDeniedException If the input is not regular file but directory.
     */
    public static <M> M read(Path input, M output) {
        try {
            return read(Files.newBufferedReader(input, $encoding), output);
        } catch (Exception e) {
            throw quiet(e);
        }
    }

    /**
     * <p>
     * Reads Java object tree from the given XML or JSON input.
     * </p>
     *
     * @param input A serialized Java object tree data as XML or JSON. If the input is incompatible
     *            with Java object, this method ignores the input. <code>null</code> will throw
     *            {@link NullPointerException}. The empty or invalid format data will throw
     *            {@link ScriptException}.
     * @param output A root Java object. All properties will be assigned from the given data deeply.
     *            If the input is incompatible with Java object, this method ignores the input.
     *            <code>null</code> will throw {@link java.lang.NullPointerException}.
     * @return A root Java object.
     * @throws NullPointerException If the input data or the root Java object is <code>null</code>.
     * @throws ScriptException If the input data is empty or invalid format.
     */
    public static <M> M read(CharSequence input, M output) {
        Objects.nonNull(input);

        return read(new StringReader(input.toString()), output);
    }

    /**
     * <p>
     * Reads Java object tree from the given XML or JSON input.
     * </p>
     * <p>
     * If the input object implements {@link AutoCloseable}, {@link AutoCloseable#close()} method
     * will be invoked certainly.
     * </p>
     *
     * @param input A serialized Java object tree data as XML or JSON. If the input is incompatible
     *            with Java object, this method ignores the input. <code>null</code> will throw
     *            {@link NullPointerException}. The empty or invalid format data will throw
     *            {@link ScriptException}.
     * @param output A root Java object. All properties will be assigned from the given data deeply.
     *            If the input is incompatible with Java object, this method ignores the input.
     *            <code>null</code> will throw {@link java.lang.NullPointerException}.
     * @return A root Java object.
     * @throws NullPointerException If the input data or the root Java object is <code>null</code>.
     * @throws IOError If the input data is empty or invalid format.
     */
    public static <M> M read(Reader input, M output) {
        PushbackReader reader = new PushbackReader(input, 4);

        Model model = Model.load(output.getClass());

        try {
            // aquire lock
            lock.readLock().lock();

            // read first character
            int c = reader.read();

            // revert stream
            reader.unread(c);

            if (c == '{') {
                // Parse as JSON
                reader.unread(new char[] {'a', '='});

                return read(model, output, script.eval(reader));
            } else {
                // Parse as XML
                return read(model, output, reader);
            }
        } catch (Exception e) {
            throw new IOError(e);
        } finally {
            // relese lock
            lock.readLock().unlock();

            // close carefuly
            quiet(reader);
        }
    }

    /**
     * <p>
     * XML reader for Object Graph deserialization.
     * </p>
     *
     * @param <M> A current model type.
     * @param model A java object model.
     * @param root A root java value.
     * @param input A data input.
     * @return A restored java object.
     */
    private static <M> M read(Model model, M root, Reader input) throws Exception {
        HashMap objects = new HashMap();
        ArrayDeque<Agent> states = new ArrayDeque();

        XMLStreamReader node = stax.createXMLStreamReader(input);

        while (node.hasNext()) {
            switch (node.next()) {
            case XMLStreamReader.START_ELEMENT:
                Agent state;
                String name = node.getLocalName();

                // create next state
                if (states.size() == 0) {
                    // this is root object
                    state = new Agent();
                    state.model = model;
                    state.object = root;
                } else {
                    Agent parent = states.peekLast();

                    // Compute property.
                    //
                    // A name of the current element indicates the hint of the property name.
                    // So you can get a valid property from the parent state.
                    if (parent.model.isCollection()) {
                        name = node.getAttributeValue(URI, "key");

                        if (name == null) {
                            name = String.valueOf(parent.index++);
                        }
                    }

                    Property property = parent.model.getProperty(name);

                    // Compute object
                    //
                    // Property indicates a object, so you should create a suitable object.
                    Object object;

                    // check attribute model
                    Codec codec = property.model.getCodec();

                    if (codec != null) {
                        String value = node.getAttributeValue("", "value");

                        if (value == null) {
                            object = null;
                        } else {
                            object = codec.decode(value);
                        }
                    } else {
                        // collection model and normal model
                        object = make(property.model.type);
                    }

                    // create next state
                    state = new Agent();
                    state.model = property.model;
                    state.object = object;
                    state.property = property;
                }

                // assign properties which are represented by attributes
                for (int i = 0; i < node.getAttributeCount(); i++) {
                    // check namespace
                    if (URI.equals(node.getAttributeNamespace(i))) {
                        if (node.getAttributeLocalName(i).equals("id")) {
                            // retrieve identifier for the current object
                            name = node.getAttributeValue(i);

                            // retrieve object for the identifier
                            Object object = objects.get(name);

                            if (object == null) {
                                // Object is not registered for the identifier, so this is first
                                // encounter of thie object.
                                objects.put(name, state.object);
                            } else {
                                // Object is registered for the identifier, so this is
                                // referenced object.
                                state.object = object;
                            }
                        }
                    } else {
                        Property property = state.model.getProperty(node.getAttributeLocalName(i));

                        // ignore deprecated property
                        if (property != null) {
                            // restore a property value form an attribute value
                            Codec codec = property.model.getCodec();

                            if (codec != null) {
                                state.model.set(state.object, property, codec.decode(node.getAttributeValue(i)));
                            }
                        }
                    }
                }

                // stack current state for reference
                states.offer(state);
                break;

            case XMLStreamReader.END_ELEMENT:
                Agent current = states.pollLast();
                Agent parent = states.peekLast();

                if (parent != null) {
                    parent.model.set(parent.object, current.property, current.object);
                }
                break;
            }
        }
        return root;
    }

    /**
     * <p>
     * Helper method to traverse json structure using Java Object {@link Model}.
     * </p>
     *
     * @param <M> A current model type.
     * @param model A java object model.
     * @param java A java value.
     * @param js A javascript value.
     * @return A restored java object.
     */
    private static <M> M read(Model model, M java, Object js) {
        if (js instanceof Map) {
            Map<String, Object> map = (Map) js;

            for (String id : map.keySet()) {
                // compute property
                Property property = model.getProperty(id);

                if (property != null) {
                    // calculate value

                    // The JDK 7 release is co-bundled with the Mozilla Rhino JavaScript engine
                    // based on version 1.7R3 pre-release sources with Oracle modifications. From
                    // Rhino 1.7R3, JavaScript Objects now implement the java.util.Map interface
                    // while Arrays implement java.util.List. This means that JavaScript objects can
                    // be passed seamlessly to Java methods expecting a Map while arrays can be
                    // passed to methods expecting a List or java.util.Collection.
                    Object value = map.get(id);
                    Class type = property.model.type;

                    // convert value
                    if (property.isAttribute()) {
                        // Rhino recognizes all numeric value as Double. If we need int or long
                        // value, we must convert it by hand.
                        if (value instanceof Double && (type == int.class || type == long.class)) {
                            value = ((Double) value).longValue();
                        }
                        value = transform(transform(value, String.class), type);
                    } else {
                        value = read(property.model, make(type), value);
                    }

                    // assign value
                    model.set(java, property, value);
                }
            }
        }

        // API definition
        return java;
    }

    /**
     * <p>
     * Execute the specified task in background {@link Thread}.
     * </p>
     *
     * @param task A task to execute.
     */
    public static Future<?> schedule(Runnable task) {
        return schedule(0, null, true, task);
    }

    /**
     * <p>
     * Execute the specified task in background {@link Thread} with the specified delay.
     * </p>
     *
     * @param time A delay time.
     * @param unit A delay time unit.
     * @param parallelExecution The <code>true</code> will execute task in parallel,
     *            <code>false</code> will execute task in serial.
     * @param task A task to execute.
     */
    public static Future<?> schedule(long time, TimeUnit unit, boolean parallelExecution, Runnable task) {
        Runnable runnable = task;

        if (time != 0 && unit != null) {
            runnable = () -> {
                try {
                    Thread.sleep(unit.toMillis(time));
                    task.run();
                } catch (Throwable e) {
                    throw quiet(e);
                }
            };
        }
        return (parallelExecution ? parallel : serial).submit(runnable);
    }

    /**
     * <p>
     * Transform any type object into the specified type possible.
     * </p>
     *
     * @param <M> A output type you want to transform into.
     * @param input A target object.
     * @param output A target type.
     * @return A transformed object.
     * @throws NullPointerException If the output type is <code>null</code>.
     */
    public static <M> M transform(Object input, Class<M> output) {
        // check null
        if (input == null) {
            return null;
        }

        Model inputModel = Model.load(input.getClass());
        Model outputModel = Model.load(output);

        // no conversion
        if (inputModel == outputModel) {
            return (M) input;
        }

        Codec inputCodec = inputModel.getCodec();
        Codec<M> outputCodec = outputModel.getCodec();

        // check whether each model are attribute model or not
        if (inputCodec == null && outputCodec == null) {
            // we should copy property values

            // create destination object
            M m = make(output);

            // copy actually
            inputModel.walk(input, (model, property, value) -> {
                Property dest = outputModel.getProperty(property.name);

                // never check null because PropertyWalker traverses existing properties
                outputModel.set(m, dest, I.transform(value, dest.model.type));
            } );

            // API definition
            return m;
        } else {
            // type conversion
            if (output == String.class) {
                return (M) ((inputCodec != null) ? inputCodec.encode(input) : input.toString());
            }

            if (inputModel.type == String.class && outputCodec != null) {
                return outputCodec.decode((String) input);
            }
            return (M) input;
        }
    }

    /**
     * <p>
     * Walk a file tree and collect files you want to filter by pattern matching.
     * </p>
     *
     * @param start A depature point. The result list doesn't include this starting path.
     * @param patterns <a href="#Patterns">include/exclude patterns</a> you want to visit.
     * @return All matched files. (<em>not</em> including directory)
     */
    public static List<Path> walk(Path start, String... patterns) {
        return new Visitor(start, null, 3, null, patterns);
    }

    /**
     * <p>
     * Walk a file tree and collect directories you want to filter by various conditions.
     * </p>
     *
     * @param start A depature point. The result list include this starting path.
     * @param patterns <a href="#Patterns">include/exclude patterns</a> you want to visit.
     * @return All matched directories. (<em>not</em> including file)
     */
    public static List<Path> walkDirectory(Path start, String... patterns) {
        return new Visitor(start, null, 5, null, patterns);
    }

    /**
     * <p>
     * Walk a file tree. The starting path is not notified to {@link FileVisitor} .
     * </p>
     * <p>
     * This method walks a file tree rooted at a given starting file. The file tree traversal is
     * depth-first with the given {@link FileVisitor} invoked for each file encountered. File tree
     * traversal completes when all accessible files in the tree have been visited, or a visit
     * method returns a result of {@link FileVisitResult#TERMINATE}. Where a visit method terminates
     * due an {@link IOException}, an uncaught error, or runtime exception, then the traversal is
     * terminated and the error or exception is propagated to the caller of this method.
     * </p>
     * <p>
     * For each file encountered this method attempts to read its {@link BasicFileAttributes}. If
     * the file is not a directory then the
     * {@link FileVisitor#visitFile(Object, BasicFileAttributes)} method is invoked with the file
     * attributes. If the file attributes cannot be read, due to an I/O exception, then the
     * {@link FileVisitor#visitFileFailed(Object, IOException)} method is invoked with the I/O
     * exception.
     * </p>
     * <p>
     * Where the file is a directory, and the directory could not be opened, then the
     * {@link FileVisitor#visitFileFailed(Object, IOException)} method is invoked with the I/O
     * exception, after which, the file tree walk continues, by default, at the next sibling of the
     * directory.
     * </p>
     * <p>
     * Where the directory is opened successfully, then the entries in the directory, and their
     * descendants are visited. When all entries have been visited, or an I/O error occurs during
     * iteration of the directory, then the directory is closed and the visitor's
     * {@link FileVisitor#postVisitDirectory(Object, IOException)} method is invoked. The file tree
     * walk then continues, by default, at the next sibling of the directory.
     * </p>
     * <p>
     * If a visitor returns a result of <code>null</code> then {@link NullPointerException} is
     * thrown.
     * </p>
     * <p>
     * When a security manager is installed and it denies access to a file (or directory), then it
     * is ignored and the visitor is not invoked for that file (or directory).
     * </p>
     *
     * @param start A depature point. This starting path is not notified to your visitor.
     * @param visitor A file tree visitor to invoke for each file and directory.
     * @param patterns <a href="#Patterns">include/exclude patterns</a> you want to visit.
     */
    public static void walk(Path start, FileVisitor visitor, String... patterns) {
        new Visitor(start, null, 4, visitor, patterns);
    }

    /**
     * <p>
     * Writes Java object tree as preference XML.
     * </p>
     * <p>
     * The encoding of output data is equivalent to {@link #$encoding}.
     * </p>
     *
     * @param input A Java object. All properties will be serialized deeply. <code>null</code> will
     *            throw {@link java.lang.NullPointerException}.
     * @throws NullPointerException If the input Java object is <code>null</code> .
     */
    public static void write(Object input) {
        Lifestyle lifestyle = modules.get(Model.load(input.getClass()).type);

        if (lifestyle instanceof Preference) {
            write(input, ((Preference) lifestyle).path, false);
        }
    }

    /**
     * <p>
     * Writes Java object tree to the given output as XML or JSON.
     * </p>
     * <p>
     * The encoding of output data is equivalent to {@link #$encoding}.
     * </p>
     * <p>
     * If the output object implements {@link AutoCloseable}, {@link AutoCloseable#close()} method
     * will be invoked certainly.
     * </p>
     *
     * @param input A Java object. All properties will be serialized deeply. <code>null</code> will
     *            throw {@link java.lang.NullPointerException}.
     * @param output A serialized data output. <code>null</code> will throw
     *            {@link NullPointerException}.
     * @param json <code>true</code> will produce JSON expression, <code>false</code> will produce
     *            XML expression.
     * @throws NullPointerException If the input Java object or the output is <code>null</code> .
     * @throws AccessDeniedException If the output is not regular file but directory.
     */
    public static void write(Object input, Path output, boolean json) {
        try {
            if (Files.notExists(output)) {
                Files.createDirectories(output.getParent());
            }

            I.write(input, Files.newBufferedWriter(output, $encoding), json);
        } catch (Exception e) {
            throw quiet(e);
        }
    }

    /**
     * <p>
     * Writes Java object tree to the given output as XML or JSON.
     * </p>
     * <p>
     * If the output object implements {@link AutoCloseable}, {@link AutoCloseable#close()} method
     * will be invoked certainly.
     * </p>
     *
     * @param input A Java object. All properties will be serialized deeply. <code>null</code> will
     *            throw {@link java.lang.NullPointerException}.
     * @param output A serialized data output. <code>null</code> will throw
     *            {@link NullPointerException}.
     * @param json <code>true</code> will produce JSON expression, <code>false</code> will produce
     *            XML expression.
     * @throws NullPointerException If the input Java object or the output is <code>null</code> .
     */
    public static void write(Object input, Appendable output, boolean json) {
        Objects.nonNull(output);

        try {
            // aquire lock
            lock.writeLock().lock();

            Model model = Model.load(input.getClass());
            Property property = new Property(model, model.name);

            if (json) {
                // traverse configuration as json
                new JSON(output).walk(model, property, input);
            } else {
                // traverse configuration as xml
                XMLUtil writer = new XMLUtil(output);
                writer.xml = xml(null);
                writer.walk(model, property, input);
                writer.xml.attr("xmlns:ss", URI).to(output);
            }
        } finally {
            // relese lock
            lock.writeLock().unlock();

            // close carefuly
            quiet(output);
        }
    }

    /**
     * <p>
     * Parse as xml fragment.
     * </p>
     * <ul>
     * <li>{@link XML}</li>
     * <li>{@link Path}</li>
     * <li>{@link InputSource}</li>
     * <li>{@link URL}</li>
     * <li>{@link Node}</li>
     * <li>{@link String}</li>
     * </ul>
     * <ul>
     * <li>URL Expression (http and https)</li>
     * <li>XML Literal</li>
     * <li>Element Name</li>
     * </ul>
     *
     * @param xml A xml expression.
     * @return A constructed {@link XML}.
     */
    public static XML xml(Object xml) {
        return xml(xml, false);
    }

    /**
     * <p>
     * Parse as xml fragment.
     * </p>
     * <ul>
     * <li>{@link XML}</li>
     * <li>{@link Path}</li>
     * <li>{@link InputSource}</li>
     * <li>{@link URL}</li>
     * <li>{@link Node}</li>
     * <li>{@link String}</li>
     * </ul>
     * <ul>
     * <li>URL Expression (http and https)</li>
     * <li>XML Literal</li>
     * <li>Element Name</li>
     * </ul>
     *
     * @param xml A xml expression.
     * @param text Allow text contents.
     * @return A constructed {@link XML}.
     */
    static XML xml(Object xml, boolean text) {
        Document doc;

        try {
            if (xml == null) {
                doc = dom.newDocument();

                return new XML(doc, new ArrayList(Collections.singleton(doc)));
            } else if (xml instanceof XML) {
                return (XML) xml;
            } else if (xml instanceof Path) {
                doc = dom.parse(((Path) xml).toFile());
            } else if (xml instanceof InputSource) {
                doc = dom.parse((InputSource) xml);
            } else if (xml instanceof URL) {
                return new XMLUtil(((URL) xml).openStream()).parse($encoding);
            } else if (xml instanceof Document) {
                doc = (Document) xml;
            } else if (xml instanceof Node) {
                return new XML(((Node) xml).getOwnerDocument(), new ArrayList(Collections.singleton(xml)));
            } else {
                // ================================
                // Parse as String
                // ================================
                String value = xml.toString();

                if (value.charAt(0) == '<' && 3 < value.length()) {
                    // ========================
                    // XML Literal
                    // ========================
                    doc = dom.parse(new InputSource(new StringReader("<m>".concat(value).concat("</m>"))));

                    return new XML(doc, XML.convert(doc.getFirstChild().getChildNodes()));
                }

                if (value.startsWith("http://") || value.startsWith("https://")) {
                    // ========================
                    // HTML from URL
                    // ========================
                    return new XMLUtil(new URL(value).openStream()).parse($encoding);
                }

                // ========================
                // Element Name or Text
                // ========================
                doc = dom.newDocument();
                return xml(text ? doc.createTextNode(value) : doc.createElement(value), text);
            }
        } catch (Exception e) {
            throw quiet(e);
        }
        return new XML(doc, XML.convert(doc.getChildNodes()));
    }

    /**
     * <p>
     * Load the file as an additional classpath into JVM. If the file indicates the classpath which
     * is already loaded, that will do nothing at all. The classpath can accept directory or archive
     * (like Jar). If it is <code>null</code> or a file, this method does nothing.
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
     * usual means, but not by this method. Because Sinobu manages additional classpath for enabling
     * dynamic manipulation.
     * </p>
     *
     * @param classPath A classpath to load.
     * @param filter Filter classes by package of the specified class.
     * @return A managed {@link ClassLoader}.
     * @see #unload(Path)
     * @see kiss.ClassListener#load(Class)
     * @see java.lang.ClassLoader#getSystemClassLoader()
     */
    public static ClassLoader load(Class classPath, boolean filter) {
        Path path = ClassUtil.getArchive(classPath);

        return filter ? modules.load(path, classPath.getPackage().getName().replace('.', '/')) : load(path);
    }

    /**
     * <p>
     * Load the file as an additional classpath into JVM. If the file indicates the classpath which
     * is already loaded, that will do nothing at all. The classpath can accept directory or archive
     * (like Jar). If it is <code>null</code> or a file, this method does nothing.
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
     * usual means, but not by this method. Because Sinobu manages additional classpath for enabling
     * dynamic manipulation.
     * </p>
     *
     * @param classPath A classpath to load. Directory or archive file (like Jar) can be accepted.
     * @return A managed {@link ClassLoader}.
     * @see #unload(Path)
     * @see kiss.ClassListener#load(Class)
     * @see java.lang.ClassLoader#getSystemClassLoader()
     */
    public static ClassLoader load(Path classPath) {
        return modules.load(classPath, "");
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
     * usual means, but not by this method. Because Sinobu manages additional classpath for enabling
     * dynamic manipulation.
     * </p>
     *
     * @param classPath A classpath to unload. Directory or archive file (like Jar) can be accepted.
     * @see #load(Path)
     * @see kiss.ClassListener#unload(Class)
     * @see java.lang.ClassLoader#getSystemClassLoader()
     */
    public static void unload(Path classPath) {
        modules.unload(classPath);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void load(Class<Extensible> extension) {
        // search and collect information for all extension points
        for (Class extensionPoint : ClassUtil.getTypes(extension)) {
            if (Arrays.asList(extensionPoint.getInterfaces()).contains(Extensible.class)) {
                // register new extension
                extensions.push(extensionPoint, extension);

                // register extension key
                Class[] params = ClassUtil.getParameter(extension, extensionPoint);

                if (params.length != 0 && params[0] != Object.class) {
                    // register extension by key
                    keys.push(extensionPoint.getName().concat(params[0].getName()), extension);

                    // Task : unregister extension by key

                    // The user has registered a newly custom lifestyle, so we should update
                    // lifestyle for this extension key class. Normally, when we update some data,
                    // it is desirable to store the previous data to be able to restore it later.
                    // But, in this case, the contextual sensitive instance that the lifestyle emits
                    // changes twice on "load" and "unload" event from the point of view of the
                    // user. So the previous data becomes all but meaningless for a cacheable
                    // lifestyles (e.g. Singleton and ThreadSpecifiec). Therefore we we completely
                    // refresh lifestyles associated with this extension key class.
                    if (extensionPoint == Lifestyle.class) {
                        modules.remove(params[0]);
                    }
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unload(Class<Extensible> extension) {
        // search and collect information for all extension points
        for (Class extensionPoint : ClassUtil.getTypes(extension)) {
            if (Arrays.asList(extensionPoint.getInterfaces()).contains(Extensible.class)) {
                // register new extension
                extensions.pull(extensionPoint, extension);

                // register extension key
                Class[] params = ClassUtil.getParameter(extension, extensionPoint);

                if (params.length != 0 && params[0] != Object.class) {
                    // register extension by key
                    keys.pull(extensionPoint.getName().concat(params[0].getName()), extension);

                    // Task : unregister extension by key

                    // The user has registered a newly custom lifestyle, so we should update
                    // lifestyle for this extension key class. Normally, when we update some data,
                    // it is desirable to store the previous data to be able to restore it later.
                    // But, in this case, the contextual sensitive instance that the lifestyle emits
                    // changes twice on "load" and "unload" event from the point of view of the
                    // user. So the previous data becomes all but meaningless for a cacheable
                    // lifestyles (e.g. Singleton and ThreadSpecifiec). Therefore we we completely
                    // refresh lifestyles associated with this extension key class.
                    if (extensionPoint == Lifestyle.class) {
                        modules.remove(params[0]);
                    }
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Thread newThread(Runnable r) {
        Thread thread = new Thread(r);
        thread.setDaemon(true);

        return thread;
    }
}
