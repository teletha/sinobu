/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss;

import static org.objectweb.asm.Opcodes.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PushbackReader;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.io.StringReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.CharBuffer;
import java.nio.channels.FileLock;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkPermission;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.CodeSource;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Period;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

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

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

import sun.misc.Unsafe;

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
 * there is no possibility that Operation Injection, Field Injection and Interface Injection will be
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
 * @version 2017/03/31 19:10:01
 */
public class I {

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

    /** No Operation */
    public static final Runnable NoOP = () -> {
        // no operation
    };

    /**
     * <p>
     * The configuration of charcter encoding in Sinobu, default value is <em>UTF-8</em>. It is
     * encouraged to use this encoding instead of platform default encoding when file I/O under the
     * Sinobu environment.
     * </p>
     */
    public static Charset $encoding = StandardCharsets.UTF_8;

    /** The configuration of root logger in Sinobu. */
    public static Logger $logger = Logger.getLogger("");

    /**
     * <p>
     * The configuration of working directory in Sinobu, default value is <em>current directory</em>
     * of JVM .
     * </p>
     */
    public static Path $working = Paths.get(""); // Poplar Taneshima

    /** The circularity dependency graph per thread. */
    static final ThreadSpecific<Deque<Class>> dependencies = new ThreadSpecific(ArrayDeque.class);

    /** The cache for {@link Lifestyle}. */
    private static final ClassVariable<Lifestyle> lifestyles = new ClassVariable<>();

    /** The extension file cache to check duplication. */
    private static final Set<String> extensionArea = new HashSet<>();

    /** The definitions for extensions. */
    private static final Map extensionDefinitions = new HashMap();

    /** The lock for configurations. */
    private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    /** The javascript engine for reuse. */
    private static final ScriptEngine script;

    // /** The locale name resolver. */
    // private static final Control control = Control.getControl(Control.FORMAT_CLASS);

    /** The root temporary directory for Sinobu. */
    private static final Path temporaries;

    /** The temporary directory for the current processing JVM. */
    private static final Path temporary;

    /** The accessible internal API. */
    private static final Unsafe unsafe;

    /** The modified class definitions. */
    private static final Map<String, Class> classes = new ConcurrentHashMap();

    /** The daemon thread factory. */
    private static final ThreadFactory factory = run -> {
        Thread thread = new Thread(run);
        thread.setDaemon(true);
        return thread;
    };

    /** The parallel task manager. */
    private static final ScheduledExecutorService parallel = Executors.newScheduledThreadPool(4, factory);

    /** The serial task manager. */
    private static final ScheduledExecutorService serial = Executors.newSingleThreadScheduledExecutor(factory);

    /** The associatable object holder. */
    private static final WeakHashMap<Object, WeakHashMap> associatables = new WeakHashMap();

    /** The document builder. */
    static final DocumentBuilder dom;

    /** The xpath evaluator. */
    static final XPath xpath;

    /** The list of primitive classes. (except for void type) */
    private static final Class[] primitives = {boolean.class, int.class, long.class, float.class, double.class, byte.class, short.class,
            char.class, void.class};

    /** The list of wrapper classes. (except for void type) */
    private static final Class[] wrappers = {Boolean.class, Integer.class, Long.class, Float.class, Double.class, Byte.class, Short.class,
            Character.class, Void.class};

    /**
     * The date format for W3CDTF. Date formats are not synchronized. It is recommended to create
     * separate format instances for each thread. If multiple threads access a format concurrently,
     * it must be synchronized externally.
     */
    private final static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    // initialization
    static {
        // remove all built-in log handlers
        for (Handler h : $logger.getHandlers()) {
            $logger.removeHandler(h);
        }

        // switch err temporaly to create console handler with System.out stream
        PrintStream error = System.err;
        System.setErr(System.out);
        config(new ConsoleHandler());
        System.setErr(error);

        // built-in lifestyles
        lifestyles.set(List.class, ArrayList::new);
        lifestyles.set(Map.class, HashMap::new);
        lifestyles.set(Set.class, HashSet::new);
        lifestyles.set(Lifestyle.class, new Prototype(Prototype.class));
        lifestyles.set(Prototype.class, new Prototype(Prototype.class));
        lifestyles.set(ListProperty.class, () -> new SimpleListProperty(FXCollections.observableArrayList()));
        lifestyles.set(ObservableList.class, FXCollections::observableArrayList);
        lifestyles.set(MapProperty.class, () -> new SimpleMapProperty(FXCollections.observableHashMap()));
        lifestyles.set(ObservableMap.class, FXCollections::observableHashMap);
        lifestyles.set(SetProperty.class, () -> new SimpleSetProperty(FXCollections.observableSet()));
        lifestyles.set(ObservableSet.class, FXCollections::observableSet);

        try {
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
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            unsafe = (Unsafe) field.get(null);

            // configure dom builder
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

            dom = factory.newDocumentBuilder();
            dom.setErrorHandler(new DefaultHandler());
            xpath = XPathFactory.newInstance().newXPath();
        } catch (Exception e) {
            throw I.quiet(e);
        }

        // configure javascript engine
        script = new ScriptEngineManager().getEngineByName("js");

        // Load myself as module. All built-in classload listeners and extension points will be
        // loaded and activated.
        load(I.class, true);

        // built-in encoders
        registerExtension(Encoder.class, type -> {
            if (type.isEnum()) {
                return value -> ((Enum) value).name();
            }

            switch (type.getName().hashCode()) {
            case -530663260: // java.lang.Class
                return value -> ((Class) value).getName();
            case 65575278: // java.util.Date
                // return LocalDateTime.ofInstant(value.toInstant(), ZoneOffset.UTC).toString();
                return format::format;

            default:
                return String::valueOf;
            }
        });

        // built-in decoders
        registerExtension(Decoder.class, type -> {
            if (type.isEnum()) {
                return value -> Enum.valueOf((Class<Enum>) type, value);
            }
            switch (type.getName().hashCode()) {
            case 64711720: // boolean
            case 344809556: // java.lang.Boolean
                return Boolean::new;
            case 104431: // int
            case -2056817302: // java.lang.Integer
                return Integer::new;
            case 3327612: // long
            case 398795216: // java.lang.Long
                return Long::new;
            case 97526364: // float
            case -527879800: // java.lang.Float
                return Float::new;
            case -1325958191: // double
            case 761287205: // java.lang.Double
                return Double::new;
            case 3039496: // byte
            case 398507100: // java.lang.Byte
                return Byte::new;
            case 109413500: // short
            case -515992664: // java.lang.Short
                return Short::new;
            case 3052374: // char
            case 155276373: // java.lang.Character
                return value -> value.charAt(0);
            case -530663260: // java.lang.Class
                return I::type;
            case 1195259493: // java.lang.String
                return String::new;
            case -1555282570: // java.lang.StringBuilder
                return StringBuilder::new;
            case 1196660485: // java.lang.StringBuffer
                return StringBuffer::new;
            case 2130072984: // java.io.File
                return File::new;
            case 2050244018: // java.net.URL
                return value -> {
                    try {
                        return new URL(value);
                    } catch (Exception e) {
                        throw I.quiet(e);
                    }
                };
            case 2050244015: // java.net.URI
                return URI::create;
            case -989675752: // java.math.BigInteger
                return BigInteger::new;
            case -1405464277: // java.math.BigDecimal
                return BigDecimal::new;
            case 65575278: // java.util.Date
                // return Date.from(LocalDateTime.parse(value).toInstant(ZoneOffset.UTC));
                return value -> {
                    try {
                        return format.parse(value);
                    } catch (Exception e) {
                        throw I.quiet(e);
                    }
                };
            case -1246033885: // java.time.LocalTime
                return LocalTime::parse;
            case -1246518012: // java.time.LocalDate
                return LocalDate::parse;
            case -1179039247: // java.time.LocalDateTime
                return LocalDateTime::parse;
            case -682591005: // java.time.OffsetDateTime
                return OffsetDateTime::parse;
            case -1917484011: // java.time.OffsetTime
                return OffsetTime::parse;
            case 1505337278: // java.time.ZonedDateTime
                return ZonedDateTime::parse;
            case 649475153: // java.time.MonthDay
                return MonthDay::parse;
            case -537503858: // java.time.YearMonth
                return YearMonth::parse;
            case -1062742510: // java.time.Year
                return Year::parse;
            case -1023498007: // java.time.Duration
                return Duration::parse;
            case 649503318: // java.time.Period
                return Period::parse;
            case 1296075756: // java.time.Instant
                return Instant::parse;
            case -1165211622: // java.util.Locale
                return Locale::forLanguageTag;
            case 1464606545: // java.nio.file.Path
            case -2015077501: // sun.nio.fs.WindowsPath
                return I::locate;

            // case -89228377: // java.nio.file.attribute.FileTime
            // decoder = value -> FileTime.fromMillis(Long.valueOf(value));
            // encoder = (Encoder<FileTime>) value -> String.valueOf(value.toMillis());
            // break;
            default:
                return null;
            }
        });
    }

    /**
     * <p>
     * Initialize environment.
     * </p>
     */
    private I() {
    }

    /**
     * <p>
     * Create {@link Predicate} which accepts any item.
     * </p>
     * 
     * @return An acceptable {@link Predicate}.
     */
    public static <V> Predicate<V> accept() {
        return e -> true;
    }

    /**
     * <p>
     * Write {@link Level#SEVERE} log.
     * </p>
     * 
     * @param message A message log.
     * @param params A list of parameters to format.
     */
    public static void alert(String message) {
        $logger.logp(Level.SEVERE, "", "", message);
    }

    /**
     * <p>
     * Write {@link Level#SEVERE} log.
     * </p>
     * 
     * @param message A message log.
     * @param params A list of parameters to format.
     */
    public static void alert(String message, Object... params) {
        $logger.logp(Level.SEVERE, "", "", message, params);
    }

    /**
     * <p>
     * Retrieve the associated value with the specified object by the specified type.
     * </p>
     *
     * @param host A host object.
     * @param type An association type.
     * @return An associated value.
     */
    public static <V> V associate(Object host, Class<V> type) {
        WeakHashMap<Class<V>, V> association = associatables.computeIfAbsent(host, key -> new WeakHashMap());
        return association.computeIfAbsent(type, I::make);
    }

    /**
     * <p>
     * Create the partial applied {@link Consumer}.
     * </p>
     *
     * @param function A target function.
     * @param param A parameter to apply.
     * @return A partial applied function.
     */
    public static <Param> Runnable bind(Consumer<Param> function, Param param) {
        return function == null ? NoOP : () -> function.accept(param);
    }

    /**
     * <p>
     * Create the partial applied {@link Consumer}.
     * </p>
     *
     * @param function A target function.
     * @param param A parameter to apply.
     * @return A partial applied function.
     */
    public static <Param1, Param2> Runnable bind(BiConsumer<Param1, Param2> function, Param1 param1, Param2 param2) {
        return function == null ? NoOP : () -> function.accept(param1, param2);
    }

    /**
     * <p>
     * Create the partial applied {@link Function}.
     * </p>
     *
     * @param function A target function.
     * @param param A parameter to apply.
     * @return A partial applied function.
     */
    public static <Param, Return> Supplier<Return> bind(Function<Param, Return> function, Param param) {
        Objects.requireNonNull(function);
        return () -> function.apply(param);
    }

    /**
     * <p>
     * Create the partial applied {@link Function}.
     * </p>
     *
     * @param function A target function.
     * @param param A parameter to apply.
     * @return A partial applied function.
     */
    public static <Param1, Param2, Return> Supplier<Return> bind(BiFunction<Param1, Param2, Return> function, Param1 param1, Param2 param2) {
        Objects.requireNonNull(function);
        return () -> function.apply(param1, param2);
    }

    /**
     * <p>
     * Bundle all given funcitons into single function.
     * </p>
     * 
     * @param functions A list of functions to bundle.
     * @return A bundled function.
     */
    public static <F> F bundle(F... functions) {
        return bundle((Class<F>) functions.getClass().getComponentType(), functions);
    }

    /**
     * <p>
     * Bundle all given funcitons into single function.
     * </p>
     * 
     * @param functions A list of functions to bundle.
     * @return A bundled function.
     */
    public static <F> F bundle(Collection<F> functions) {
        return bundle(findNCA(functions, Class::isInterface), functions);
    }

    // /**
    // * <p>
    // * Find the nearest common ancestor class of the given classes.
    // * </p>
    // *
    // * @param <X> A type.
    // * @param classes A list of classes.
    // * @return A nearest common ancestor class.
    // */
    // private static <X> Class findNCA(X... classes) {
    // return classes.getClass().getComponentType();
    // }

    /**
     * <p>
     * Find the nearest common ancestor class of the given classes.
     * </p>
     * 
     * @param <X> A type.
     * @param items A list of items.
     * @return A nearest common ancestor class.
     */
    private static <X> Class<X> findNCA(Collection<X> items, Predicate<Class> filter) {
        if (filter == null) {
            filter = accept();
        }

        Set<Class> types = null;
        Iterator<X> iterator = items.iterator();

        if (iterator.hasNext()) {
            types = Model.collectTypes(iterator.next().getClass());
            types.removeIf(filter.negate());

            while (iterator.hasNext()) {
                types.retainAll(Model.collectTypes(iterator.next().getClass()));
            }
        }
        return types == null || types.isEmpty() ? null : types.iterator().next();
    }

    /**
     * <p>
     * Bundle all given typed funcitons into single typed function.
     * </p>
     * 
     * @param type A function type.
     * @param functions A list of functions to bundle.
     * @return A bundled function.
     */
    public static <F> F bundle(Class<F> type, F... functions) {
        return bundle(type, Arrays.asList(functions));
    }

    /**
     * <p>
     * Bundle all given typed funcitons into single typed function.
     * </p>
     * 
     * @param type A function type.
     * @param functions A list of functions to bundle.
     * @return A bundled function.
     */
    public static <F> F bundle(Class<F> type, Collection<F> functions) {
        return make(type, (proxy, method, args) -> {
            Object result = null;

            if (functions != null) {
                for (Object fun : functions) {
                    if (fun != null) {
                        result = method.invoke(fun, args);
                    }
                }
            }
            return result;
        });
    }

    /**
     * <p>
     * Create the specified {@link Collection} with the specified items.
     * </p>
     * 
     * @param type A {@link Collection} type.
     * @param items A list of itmes.
     * @return The new created {@link Collection}.
     */
    public static <T extends Collection<V>, V> T collect(Class<T> type, V... items) {
        T collection = I.make(type);

        if (items != null) {
            for (V item : items) {
                collection.add(item);
            }
        }
        return collection;
    }

    /**
     * <p>
     * Rgister the specified log handler.
     * </p>
     * 
     * @param handler
     */
    public static void config(Handler handler) {
        handler.setFormatter(new Log());
        $logger.addHandler(handler);
    }

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
     * Copy data from a {@link Readable} to an {@link Appendable}. This method buffers the input
     * internally, so there is no need to use a buffer.
     * </p>
     *
     * @param input A {@link Readable} to read from.
     * @param output An {@link Appendable} to write to.
     * @param close Whether input and output steream will be closed automatically or not.
     * @throws IOException If an I/O error occurs.
     * @throws NullPointerException If the input or output is null.
     */
    public static void copy(Readable input, Appendable output, boolean close) {
        int size;
        CharBuffer buffer = CharBuffer.allocate(8192);

        try {
            while ((size = input.read(buffer)) != -1) {
                buffer.flip();
                output.append(buffer, 0, size);
                buffer.clear();
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
     *     // Copy input file to output directory.
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
        new Visitor(input, output, 0, patterns).walk();
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
     *     // Copy input file to output directory.
     *   }
     * } else {
     *   if (output.isFile) {
     *     // NoSuchFileException will be thrown.
     *   } else {
     *     // Copy input directory under output directory deeply.
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
     * @param filter A file filter to copy.
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
    public static void copy(Path input, Path output, BiPredicate<Path, BasicFileAttributes> filter) {
        new Visitor(input, output, 0, filter).walk();
    }

    /**
     * <p>
     * Write {@link Level#FINEST} log.
     * </p>
     * 
     * @param message A message log.
     * @param params A list of parameters to format.
     */
    public static void debug(String message) {
        $logger.logp(Level.FINEST, "", "", message);
    }

    /**
     * <p>
     * Write {@link Level#FINEST} log.
     * </p>
     * 
     * @param message A message log.
     * @param params A list of parameters to format.
     */
    public static void debug(String message, Object... params) {
        $logger.logp(Level.FINEST, "", "", message, params);
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
            new Visitor(input, null, 2, patterns).walk();
        }
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
     * @param filter A file filter.
     * @throws IOException If an I/O error occurs.
     * @throws NullPointerException If the specified input file is <code>null</code>.
     * @throws SecurityException In the case of the default provider, and a security manager is
     *             installed, the {@link SecurityManager#checkRead(String)} method is invoked to
     *             check read access to the source file, the
     *             {@link SecurityManager#checkWrite(String)} is invoked to check write access to
     *             the target file. If a symbolic link is copied the security manager is invoked to
     *             check {@link LinkPermission}("symbolic").
     */
    public static void delete(Path input, BiPredicate<Path, BasicFileAttributes> filter) {
        if (input != null) {
            new Visitor(input, null, 2, filter).walk();
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
     */
    public static <E extends Extensible> List<E> find(Class<E> extensionPoint) {
        return Signal.from(ex(extensionPoint)).flatIterable(Ⅱ::ⅰ).map(I::make).toList();
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

        Ⅱ<List<Class<E>>, List<Ⅱ<Class, E>>> extensions = ex(extensionPoint);

        for (Ⅱ<Class, E> extension : extensions.ⅱ) {
            if (extension.ⅰ.isAssignableFrom(key)) {
                return extension.ⅱ;
            }
        }

        if (extensionPoint != ExtensionFactory.class) {
            ExtensionFactory<E> factory = find(ExtensionFactory.class, extensionPoint);

            if (factory != null) {
                return factory.create(key);
            }
        }
        return null;
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
        return new ArrayList(ex(extensionPoint).ⅰ);
    }

    public static <P> Consumer<P> imitateConsumer(Runnable lambda) {
        return p -> {
            if (lambda != null) lambda.run();
        };
    }

    public static <P, R> Function<P, R> imitateFunction(Consumer<P> function) {
        return p -> {
            function.accept(p);
            return null;
        };
    }

    public static <P, R> Function<P, R> imitateFunction(Supplier<R> function) {
        return p -> function.get();
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

        if (delimiter == null) {
            delimiter = "";
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
     * Parse the specified JSON format text.
     * </p>
     * <ul>
     * <li>{@link JSON}</li>
     * <li>{@link Path}</li>
     * <li>{@link File}</li>
     * <li>{@link InputStream}</li>
     * <li>{@link Readable}</li>
     * <li>{@link URL}</li>
     * <li>{@link URI}</li>
     * <li>{@link CharSequence}</li>
     * </ul>
     * 
     * @param input A json format text. <code>null</code> will throw {@link NullPointerException}.
     *            The empty or invalid format data will throw {@link ScriptException}.
     * @return A parsed {@link JSON}.
     * @throws NullPointerException If the input data or the root Java object is <code>null</code>.
     * @throws ScriptException If the input data is empty or invalid format.
     */
    public static JSON json(Object input) {
        PushbackReader reader = null;

        try {
            // aquire lock
            lock.readLock().lock();

            // Parse as JSON
            reader = new PushbackReader(new InputStreamReader(new ByteArrayInputStream(read(input)), $encoding), 2);
            reader.unread(new char[] {'a', '='});
            return new JSON(script.eval(reader));
        } catch (Exception e) {
            throw quiet(e);
        } finally {
            // relese lock
            lock.readLock().unlock();

            // close carefuly
            quiet(reader);
        }
    }

    /**
     * <p>
     * Create {@link ArrayList} with the specified items.
     * </p>
     * 
     * @param items A list of itmes.
     * @return The new created {@link ArrayList}.
     */
    public static <V> List<V> list(V... items) {
        return collect(ArrayList.class, items);
    }

    private static <E extends Extensible> Ⅱ<List<Class<E>>, List<Ⅱ<Class, E>>> ex(Class<E> key) {
        return (Ⅱ<List<Class<E>>, List<Ⅱ<Class, E>>>) extensionDefinitions
                .computeIfAbsent(key, k -> pair(new ArrayList(), new ArrayList()));
    }

    private static <E extends Extensible> Disposable registerExtension(Class<E> extensionPoint, Class extensionKey, E builder) {
        Ⅱ<Class, E> pair = pair(extensionKey, builder);
        ex(extensionPoint).ⅱ.add(0, pair);

        return () -> ex(extensionPoint).ⅱ.remove(pair);
    }

    public static <F extends ExtensionFactory<E>, E extends Extensible> Disposable registerExtension(Class<E> extensionKey, F factory) {
        return registerExtension(ExtensionFactory.class, extensionKey, factory);
    }

    /**
     * <p>
     * Load {@link Extensible} typs from the path that the specified class indicates to. If same
     * path and patten is already called , that will do nothing at all.
     * </p>
     *
     * @param path A path to load.
     * @param filter <code>true</code> makes filter classes by package of the specified class.
     * @return The unloader.
     * @see {@link Extensible}
     * @see #find(Class)
     * @see #find(Class, Class)
     * @see #findAs(Class)
     */
    public static <E extends Extensible> Disposable load(Class path, boolean filter) {
        Disposable disposer = Disposable.empty();
        String pattern = filter ? path.getPackage().getName() : "";

        try {
            File file = locate(path).toFile().getAbsoluteFile();

            // exclude registered file or directory
            if (!extensionArea.add(file + pattern)) {
                return disposer;
            }
            disposer.and(() -> extensionArea.remove(file + pattern));

            int prefix = file.getPath().length() + 1;

            // At first, we must scan the specified directory or archive. If the module file is
            // archive, Sinobu automatically try to switch to other file system (e.g.
            // ZipFileSystem).
            Signal<String> names = file.isFile() ? Signal.from(new ZipFile(file).entries()).map(ZipEntry::getName)
                    : Signal.from(scan(file, new ArrayList<>())).map(File::getPath).map(name -> name.substring(prefix));

            names.to(name -> {
                // exclude non-class files
                if (!name.endsWith(".class")) {
                    return;
                }

                name = name.substring(0, name.length() - 6).replace('/', '.').replace(File.separatorChar, '.');

                // exclude out of the specified package
                if (!name.startsWith(pattern)) {
                    return;
                }

                Class extension = I.type(name);

                // fast check : exclude non-initializable class
                if (extension.isEnum() || extension.isAnonymousClass() || Modifier.isAbstract(extension.getModifiers())) {
                    return;
                }

                // slow check : exclude non-extensible class
                if (!Extensible.class.isAssignableFrom(extension)) {
                    return;
                }

                // search and collect information for all extension points
                for (Class<E> extensionPoint : Model.collectTypes(extension)) {
                    if (Arrays.asList(extensionPoint.getInterfaces()).contains(Extensible.class)) {
                        // register as new extension
                        ex(extensionPoint).ⅰ.add(extension);
                        disposer.and(() -> ex(extensionPoint).ⅰ.remove(extension));

                        // register extension key
                        java.lang.reflect.Type[] params = Model.collectParameters(extension, extensionPoint);

                        if (params.length != 0 && params[0] != Object.class) {
                            Class clazz = (Class) params[0];

                            // register extension by key
                            disposer.and(registerExtension(extensionPoint, clazz, (E) I.make(extension)));

                            // The user has registered a newly custom lifestyle, so we
                            // should update lifestyle for this extension key class.
                            // Normally, when we update some data, it is desirable to store
                            // the previous data to be able to restore it later.
                            // But, in this case, the contextual sensitive instance that
                            // the lifestyle emits changes twice on "load" and "unload"
                            // event from the point of view of the user.
                            // So the previous data becomes all but meaningless for a
                            // cacheable lifestyles (e.g. Singleton and ThreadSpecifiec).
                            // Therefore we we completely refresh lifestyles associated with
                            // this extension key class.
                            if (extensionPoint == Lifestyle.class) {
                                lifestyles.remove(clazz);
                                disposer.and(() -> lifestyles.remove(clazz));
                            }
                        }
                    }
                }
            });

            return disposer;
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }

    /**
     * <p>
     * Dig class files in directory.
     * </p>
     *
     * @param file A current location.
     * @param files A list of collected files.
     * @return A list of collected files.
     */
    private static List<File> scan(File file, List<File> files) {
        if (file.isDirectory()) {
            for (File sub : file.listFiles()) {
                scan(sub, files);
            }
        } else {
            files.add(file);
        }
        return files;
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
     * Locate the class archive (e.g. jar file, classes directory) by the specified sample class. If
     * the sample class belongs to system classloader (e.g. {@link String}), <code>null</code> will
     * be returned.
     * </p>
     *
     * @param clazz A sample class.
     * @return A class archive (e.g. jar file, classes directory) or <code>null</code>.
     */
    public static Path locate(Class clazz) {
        // retrieve code source of this sample class
        CodeSource source = clazz.getProtectionDomain().getCodeSource();

        // API definition
        return (source == null) ? null : locate(source.getLocation());
    }

    /**
     * <p>
     * Locate the class resource (e.g. in jar file, in classes directory) by the specified sample
     * class. If the sample class belongs to system classloader (e.g. {@link String}),
     * <code>null</code> will be returned.
     * </p>
     *
     * @param clazz A sample class.
     * @param filePath A location path.
     * @return A class resource (e.g. in jar file, in classes directory) or <code>null</code>.
     */
    public static Path locate(Class clazz, String filePath) {
        try {
            Path root = locate(clazz);

            if (Files.isRegularFile(root)) {
                root = FileSystems.newFileSystem(root, null).getPath("/");
            }
            return root.resolve(clazz.getName().replaceAll("\\.", "/")).resolveSibling(filePath);
        } catch (Exception e) {
            throw I.quiet(e);
        }
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
     * Write {@link Level#INFO} log.
     * </p>
     * 
     * @param message A message log.
     * @param params A list of parameters to format.
     */
    public static void log(String message) {
        $logger.logp(Level.INFO, "", "", message);
    }

    /**
     * <p>
     * Write {@link Level#INFO} log.
     * </p>
     * 
     * @param message A message log.
     * @param params A list of parameters to format.
     */
    public static void log(String message, Object... params) {
        $logger.logp(Level.INFO, "", "", message, params);
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
    public static <M> M make(Class<? extends M> modelClass) {
        return makeLifestyle(modelClass).get();
    }

    /**
     * <p>
     * Create proxy instance.
     * </p>
     * 
     * @param type A model type.
     * @param handler A proxy handler.
     * @return
     */
    public static <T> T make(Class<T> type, InvocationHandler handler) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(handler);

        if (type.isInterface() == false) {
            throw new IllegalArgumentException("Type must be interface.");
        }
        return (T) Proxy.newProxyInstance(I.class.getClassLoader(), new Class[] {type}, handler);
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
        Lifestyle<M> lifestyle = lifestyles.get(modelClass);

        if (lifestyle != null) return lifestyle; // use cache

        // Skip null check because this method can throw NullPointerException.
        // if (modelClass == null) throw new NullPointerException("NPE");

        // The model class have some preconditions to have to meet.
        if (modelClass.isLocalClass()) {
            throw new UnsupportedOperationException(modelClass + " is  inner class.");
        }

        // In the second place, we must find the actual model class which is associated with
        // this model class. If the actual model class is a concreate, we can use it directly.
        Class<M> actualClass = modelClass;

        // If this model is non-private or final class, we can extend it for interceptor
        // mechanism.
        if (((Modifier.PRIVATE | Modifier.FINAL) & modelClass.getModifiers()) == 0) {
            Map<Method, List<Annotation>> interceptables = Model.collectAnnotatedMethods(actualClass);

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
            return lifestyles.let(modelClass, lifestyle);
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
    private static synchronized Class define(Class model, Map<Method, List<Annotation>> interceptables) {
        // Compute fully qualified class name for the generated class.
        // The coder class name is prefix to distinguish enhancer type by a name and make core
        // package classes (e.g. swing components) enhance.
        // The statement "String name = coder.getName() + model.type.getName();" produces larger
        // byte code and more objects. To reduce them, we should use the method "concat".
        String name = model.getName().concat("+");

        if (name.startsWith("java.")) {
            name = "$".concat(name);
        }

        // API definition
        return classes.computeIfAbsent(name, key -> {
            // start writing byte code
            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);

            // write code actually
            write(writer, model, key.replace('.', '/'), interceptables);

            // retrieve byte code
            byte[] bytes = writer.toByteArray();

            // define class
            return unsafe.defineClass(key, bytes, 0, bytes.length, I.class.getClassLoader(), model.getProtectionDomain());
        });
    }

    /**
     * <p>
     * Generate a byte code which represents the specified class name and implements the specified
     * model.
     * </p>
     */
    private static void write(ClassVisitor cv, Class model, String className, Map<Method, List<Annotation>> interceptables) {
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
        Constructor constructor = Model.collectConstructors(model)[0];
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
            if (((Modifier.STATIC | Modifier.PRIVATE | Modifier.NATIVE | Modifier.FINAL) & method.getModifiers()) != 0) {
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
            Handle handle = new Handle(H_INVOKESPECIAL, className.substring(0, className.length() - 1), method.getName(), methodType
                    .getDescriptor(), false);
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
                            annotate((Annotation) Array.get(value, i), array.visitAnnotation(null, Type.getDescriptor(clazz)));
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
                Type wrapper = Type.getType(wrap(clazz));
                mv.visitTypeInsn(CHECKCAST, wrapper.getInternalName());
                mv.visitMethodInsn(INVOKEVIRTUAL, wrapper.getInternalName(), clazz.getName() + "Value", "()" + type.getDescriptor(), false);
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
            Type wrapper = Type.getType(wrap(clazz));
            mv.visitMethodInsn(INVOKESTATIC, wrapper
                    .getInternalName(), "valueOf", "(" + Type.getType(clazz).getDescriptor() + ")" + wrapper.getDescriptor(), false);
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
        new Visitor(input, output, 1, patterns).walk();
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
     * @param filter A file filter to move.
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
    public static void move(Path input, Path output, BiPredicate<Path, BasicFileAttributes> filter) {
        new Visitor(input, output, 1, filter).walk();
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
    public static <E extends Observable> Signal<E> observe(E observable) {
        if (observable == null) {
            return Signal.NEVER;
        }

        return new Signal<>((observer, disposer) -> {
            // create actual listener
            InvalidationListener listener = value -> observer.accept((E) value);

            // INITIALIZING STAGE : register listener and notify the current value
            observable.addListener(listener);
            observer.accept(observable);

            // DISPOSING STAGE : unregister listener
            return () -> observable.removeListener(listener);
        });
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
    public static <E> Signal<E> observe(ObservableValue<E> observable) {
        if (observable == null) {
            return Signal.NEVER;
        }

        return new Signal<>((observer, disposer) -> {
            // create actual listener
            ChangeListener<E> listener = (o, oldValue, newValue) -> observer.accept(newValue);

            // INITIALIZING STAGE : register listener and notify the current value
            observable.addListener(listener);
            observer.accept(observable.getValue());

            // DISPOSING STAGE : unregister listener
            return () -> observable.removeListener(listener);
        });
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
    public static Signal<WatchEvent<Path>> observe(Path path) {
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
    public static Signal<WatchEvent<Path>> observe(Path path, String... patterns) {
        if (!Files.isDirectory(path)) {
            return observe(path.getParent(), path.getFileName().toString());
        }

        return new Signal<>((observer, disposer) -> {
            // Create logical file system watch service.
            Visitor watcher = new Visitor(path, observer, patterns);

            // Run in anothor thread.
            schedule(watcher);

            // API definition
            return watcher;
        });
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
    public static <Param1, Param2> Ⅱ<Param1, Param2> pair(Param1 param1, Param2 param2) {
        return new Ⅱ(param1, param2);
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
    public static <Param1, Param2, Param3> Ⅲ<Param1, Param2, Param3> pair(Param1 param1, Param2 param2, Param3 param3) {
        return new Ⅲ(param1, param2, param3);
    }

    /**
     * <p>
     * Create paired value {@link Consumer}.
     * </p>
     *
     * @param consumer A {@link BiConsumer} to make parameters paired.
     * @return A paired value {@link Consumer}.
     */
    public static <Param1, Param2> Consumer<Ⅱ<Param1, Param2>> pair(BiConsumer<Param1, Param2> consumer) {
        return params -> consumer.accept(params.ⅰ, params.ⅱ);
    }

    /**
     * <p>
     * Create paired value {@link Function}.
     * </p>
     *
     * @param funtion A {@link BiFunction} to make parameters paired.
     * @return A paired value {@link Function}.
     */
    public static <Param1, Param2, Return> Function<Ⅱ<Param1, Param2>, Return> pair(BiFunction<Param1, Param2, Return> funtion) {
        return params -> funtion.apply(params.ⅰ, params.ⅱ);
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
            return I.<RuntimeException>quietly(throwable);
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
     * Ease the checked exception on lambda.
     * </p>
     * 
     * @param lambda A checked lambda.
     * @return A unchecked lambda.
     */
    public static Runnable quiet(UsefulRunnable lambda) {
        return lambda;
    }

    /**
     * <p>
     * Ease the checked exception on lambda.
     * </p>
     * 
     * @param lambda A checked lambda.
     * @return A unchecked lambda.
     */
    public static <P> Consumer<P> quiet(UsefulConsumer<P> lambda) {
        return lambda;
    }

    /**
     * <p>
     * Ease the checked exception on lambda.
     * </p>
     * 
     * @param lambda A checked lambda.
     * @return A unchecked lambda.
     */
    public static <P1, P2> BiConsumer<P1, P2> quiet(UsefulBiConsumer<P1, P2> lambda) {
        return lambda;
    }

    /**
     * <p>
     * Ease the checked exception on lambda.
     * </p>
     * 
     * @param lambda A checked lambda.
     * @return A unchecked lambda.
     */
    public static <R> Supplier<R> quiet(UsefulSupplier<R> lambda) {
        return lambda;
    }

    /**
     * <p>
     * Ease the checked exception on lambda.
     * </p>
     * 
     * @param lambda A checked lambda.
     * @return A unchecked lambda.
     */
    public static <P, R> Function<P, R> quiet(UsefulFunction<P, R> lambda) {
        return lambda;
    }

    /**
     * <p>
     * Ease the checked exception on lambda.
     * </p>
     * 
     * @param lambda A checked lambda.
     * @return A unchecked lambda.
     */
    public static <P1, P2, R> BiFunction<P1, P2, R> quiet(UsefulBiFunction<P1, P2, R> lambda) {
        return lambda;
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
        return json(input).to(output);
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
        return json(input).to(output);
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
        return json(input).to(output);
    }

    /**
     * <p>
     * Create {@link Predicate} which rejects any item.
     * </p>
     * 
     * @return An rejectable {@link Predicate}.
     */
    public static <V> Predicate<V> reject() {
        return e -> false;
    }

    /**
     * <p>
     * Convenient method to describe the recovery operation.
     * </p>
     * <p>
     * If the specified error will occur, retry the original action.
     * </p>
     * 
     * @param type A target error type.
     * @param recovery A recovery operation builder.
     * @return A recovery operation.
     * @see #run(UsefulRunnable, UsefulTriFunction...)
     * @see #run(UsefulSupplier, UsefulTriFunction...)
     */
    public static <O> UsefulTriFunction<O, Throwable, Integer, O> recoverWhen(Class<? extends Throwable> type, UnaryOperator<O> recovery) {
        return recoverWhen(type, Integer.MAX_VALUE, recovery);
    }

    /**
     * <p>
     * Convenient method to describe the recovery operation.
     * </p>
     * <p>
     * If the specified error will occur, retry the original action.
     * </p>
     * 
     * @param type A target error type.
     * @param limit A limit number of trials.
     * @param recovery A recovery operation builder.
     * @return A recovery operation.
     * @see #run(UsefulRunnable, UsefulTriFunction...)
     * @see #run(UsefulSupplier, UsefulTriFunction...)
     */
    public static <O> UsefulTriFunction<O, Throwable, Integer, O> recoverWhen(Class<? extends Throwable> type, int limit, UnaryOperator<O> recovery) {
        return (o, e, i) -> type.isInstance(e) && i < limit ? recovery.apply(o) : null;
    }

    /**
     * <p>
     * Convenient method to describe the recovery operation.
     * </p>
     * <p>
     * If the specified error will occur, retry the original action.
     * </p>
     * 
     * @param type A target error type.
     * @return A recovery operation.
     * @see #run(UsefulRunnable, UsefulTriFunction...)
     * @see #run(UsefulSupplier, UsefulTriFunction...)
     */
    public static <O> UsefulTriFunction<O, Throwable, Integer, O> retryWhen(Class<? extends Throwable> type) {
        return retryWhen(type, Integer.MAX_VALUE);
    }

    /**
     * <p>
     * Convenient method to describe the recovery operation.
     * </p>
     * <p>
     * If the specified error will occur, retry the original action.
     * </p>
     * 
     * @param type A target error type.
     * @param limit A limit number of trials.
     * @return A recovery operation.
     * @see #run(UsefulRunnable, UsefulTriFunction...)
     * @see #run(UsefulSupplier, UsefulTriFunction...)
     */
    public static <O> UsefulTriFunction<O, Throwable, Integer, O> retryWhen(Class<? extends Throwable> type, int limit) {
        return (o, e, i) -> type.isInstance(e) && i < limit ? o : null;
    }

    /**
     * <p>
     * Perform recoverable operation. If some recoverable error will occur, this method perform
     * recovery operation automatically.
     * </p>
     * 
     * @param operation A original user operation.
     * @param recoveries A list of recovery operations.
     * @see #retryWhen(Class)
     * @see #retryWhen(Class, int)
     * @see #recoverWhen(Class, UnaryOperator)
     * @see #recoverWhen(Class, int, UnaryOperator)
     */
    public static void run(UsefulRunnable operation, UsefulTriFunction<Runnable, Throwable, Integer, Runnable>... recoveries) {
        run(operation, operation, o -> {
            o.run();
            return null;
        }, recoveries, new int[recoveries.length]);
    }

    /**
     * <p>
     * Perform recoverable operation. If some recoverable error will occur, this method perform
     * recovery operation automatically.
     * </p>
     * 
     * @param operation A original user operation.
     * @param recoveries A list of recovery operations.
     * @return A operation result.
     * @see #retryWhen(Class)
     * @see #retryWhen(Class, int)
     * @see #recoverWhen(Class, UnaryOperator)
     * @see #recoverWhen(Class, int, UnaryOperator)
     */
    public static <R> R run(UsefulSupplier<R> operation, UsefulTriFunction<Supplier<R>, Throwable, Integer, Supplier<R>>... recoveries) {
        return run(operation, operation, Supplier<R>::get, recoveries, new int[recoveries.length]);
    }

    /**
     * <p>
     * Perform recoverable operation. If some recoverable error will occur, this method perform
     * recovery operation automatically.
     * </p>
     * 
     * @param original A original user operation.
     * @param recover A current (original or recovery) operation.
     * @param invoker A operation invoker.
     * @param recoveries A list of recovery operations.
     * @param counts A current number of trials.
     * @return A operation result.
     */
    private static <O, R> R run(O original, O recover, Function<O, R> invoker, UsefulTriFunction<O, Throwable, Integer, O>[] recoveries, int[] counts) {
        try {
            return invoker.apply(recover);
        } catch (Throwable e) {
            for (int i = 0; i < recoveries.length; i++) {
                O next = recoveries[i].apply(original, e, counts[i]);

                if (next != null) {
                    counts[i]++;
                    return run(original, next, invoker, recoveries, counts);
                }
            }
            throw e;
        }
    }

    /**
     * <p>
     * Execute the specified task in background {@link Thread}.
     * </p>
     *
     * @param task A task to execute.
     */
    public static Future<?> schedule(Runnable task) {
        return parallel.submit(task);
    }

    /**
     * <p>
     * Execute the specified task in background {@link Thread} with the specified delay.
     * </p>
     *
     * @param delay A initial delay time.
     * @param unit A delay time unit.
     * @param parallelExecution The <code>true</code> will execute task in parallel,
     *            <code>false</code> will execute task in serial.
     * @param task A task to execute.
     */
    public static Future<?> schedule(long delay, TimeUnit unit, boolean parallelExecution, Runnable task) {
        return (parallelExecution ? parallel : serial).schedule(task, delay, unit);

    }

    /**
     * <p>
     * Execute the specified task in background {@link Thread} with the specified delay.
     * </p>
     *
     * @param delay A initial delay time.
     * @param unit A delay time unit.
     * @param parallelExecution The <code>true</code> will execute task in parallel,
     *            <code>false</code> will execute task in serial.
     * @param task A task to execute.
     */
    public static Future<?> schedule(Runnable task, long interval, TimeUnit unit) {
        return parallel.scheduleWithFixedDelay(task, 0, interval, unit);
    }

    /**
     * <p>
     * Create {@link HashSet} with the specified items.
     * </p>
     * 
     * @param items A list of itmes.
     * @return The new created {@link HashSet}.
     */
    public static <V> Set<V> set(V... items) {
        return collect(HashSet.class, items);
    }

    /**
     * <p>
     * Transform any type object into the specified type if possible.
     * </p>
     *
     * @param <In> A input type you want to transform from.
     * @param <Out> An output type you want to transform into.
     * @param input A target object.
     * @param output A target type.
     * @return A transformed object.
     * @throws NullPointerException If the output type is <code>null</code>.
     */
    public static <In, Out> Out transform(In input, Class<Out> output) {
        if (input == null) {
            return null;
        }

        String encoded = input instanceof String ? (String) input : find(Encoder.class, input.getClass()).encode(input);

        if (output == String.class) {
            return (Out) encoded;
        }

        return ((Decoder<Out>) find(Decoder.class, output)).decode(encoded);
    }

    /**
     * <p>
     * Find the class by the specified fully qualified class name.
     * </p>
     *
     * @param fqcn A fully qualified class name to want.
     * @return The specified class.
     */
    public static Class type(String fqcn) {
        if (fqcn.indexOf('.') == -1) {
            for (Class clazz : primitives) {
                if (clazz.getName().equals(fqcn)) {
                    return clazz;
                }
            }
        }

        try {
            return Class.forName(fqcn);
        } catch (ClassNotFoundException e) {
            throw quiet(e);
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
        return new Visitor(start, null, 3, patterns).walk();
    }

    /**
     * <p>
     * Walk a file tree and collect files you want to filter by pattern matching.
     * </p>
     *
     * @param start A depature point. The result list doesn't include this starting path.
     * @param filter A file filter to visit.
     * @return All matched files. (<em>not</em> including directory)
     */
    public static List<Path> walk(Path start, BiPredicate<Path, BasicFileAttributes> filter) {
        return new Visitor(start, null, 3, filter).walk();
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
        return new Visitor(start, null, 4, patterns).walk();
    }

    /**
     * <p>
     * Walk a file tree and collect directories you want to filter by various conditions.
     * </p>
     *
     * @param start A departure point. The result list include this starting path.
     * @param filter A directory filter.
     * @return All matched directories. (<em>not</em> including file)
     */
    public static List<Path> walkDirectory(Path start, BiPredicate<Path, BasicFileAttributes> filter) {
        return new Visitor(start, null, 4, filter).walk();
    }

    /**
     * <p>
     * Write {@link Level#WARNING} log.
     * </p>
     * 
     * @param message A message log.
     * @param params A list of parameters to format.
     */
    public static void warn(String message) {
        $logger.logp(Level.WARNING, "", "", message);
    }

    /**
     * <p>
     * Write {@link Level#WARNING} log.
     * </p>
     * 
     * @param message A message log.
     * @param params A list of parameters to format.
     */
    public static void warn(String message, Object... params) {
        $logger.logp(Level.WARNING, "", "", message, params);
    }

    /**
     * <p>
     * Return a non-primitive {@link Class} of the specified {@link Class} object. <code>null</code>
     * will be return <code>null</code>.
     * </p>
     *
     * @param type A {@link Class} object to convert to non-primitive class.
     * @return A non-primitive {@link Class} object.
     */
    public static Class wrap(Class type) {
        // check primitive classes
        for (int i = 0; i < primitives.length; i++) {
            if (primitives[i] == type) {
                return wrappers[i];
            }
        }

        // the specified class is not primitive
        return type;
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
    public static void write(Object input, Path output) {
        try {
            if (Files.notExists(output)) {
                Files.createDirectories(output.getParent());
            }

            I.write(input, Files.newBufferedWriter(output, $encoding));
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
     * @param out A serialized data output. <code>null</code> will throw
     *            {@link NullPointerException}.
     * @param format <code>true</code> will produce JSON expression, <code>false</code> will produce
     *            XML expression.
     * @throws NullPointerException If the input Java object or the output is <code>null</code> .
     */
    public static void write(Object input, Appendable out) {
        Objects.nonNull(out);

        try {
            // aquire lock
            lock.writeLock().lock();

            // traverse object as json
            Model model = Model.of(input);
            Format format = new Format();
            format.out = out;
            format.accept(pair(model, new Property(model, ""), input));
        } finally {
            // relese lock
            lock.writeLock().unlock();

            // close carefuly
            quiet(out);
        }
    }

    /**
     * <p>
     * Parse as xml fragment.
     * </p>
     * <ul>
     * <li>{@link XML}</li>
     * <li>{@link Path}</li>
     * <li>{@link File}</li>
     * <li>{@link InputStream}</li>
     * <li>{@link Readable}</li>
     * <li>{@link URL}</li>
     * <li>{@link URI}</li>
     * <li>{@link Node}</li>
     * <li>{@link CharSequence}</li>
     * </ul>
     * <ul>
     * <li>XML Literal</li>
     * <li>HTML Literal</li>
     * </ul>
     *
     * @param xml A xml expression.
     * @return A constructed {@link XML}.
     */
    public static XML xml(Object xml) {
        return xml(null, xml);
    }

    /** XML literal pattern. */
    private static final Pattern XMLiteral = Pattern.compile("^\\s*<.+>\\s*$", Pattern.DOTALL);

    /**
     * <p>
     * Parse as xml fragment.
     * </p>
     * <ul>
     * <li>{@link XML}</li>
     * <li>{@link Path}</li>
     * <li>{@link File}</li>
     * <li>{@link InputStream}</li>
     * <li>{@link Readable}</li>
     * <li>{@link URL}</li>
     * <li>{@link URI}</li>
     * <li>{@link Node}</li>
     * <li>{@link CharSequence}</li>
     * </ul>
     * <ul>
     * <li>XML Literal</li>
     * <li>HTML Literal</li>
     * </ul>
     *
     * @param xml A xml expression.
     * @return A constructed {@link XML}.
     */
    static XML xml(Document doc, Object xml) {
        try {
            // XML related types
            if (xml instanceof XML) {
                return (XML) xml;
            } else if (xml instanceof Node) {
                return new XML(((Node) xml).getOwnerDocument(), list(xml));
            }

            // byte data types
            byte[] bytes = read(xml);

            if (6 < bytes.length && bytes[0] == '<') {
                // doctype declaration (starts with <! )
                // root element is html (starts with <html> )
                if (bytes[1] == '!' || (bytes[1] == 'h' && bytes[2] == 't' && bytes[3] == 'm' && bytes[4] == 'l' && bytes[5] == '>')) {
                    return new XML(null, null).parse(bytes, $encoding);
                }
            }

            String value = new String(bytes, $encoding);

            if (XMLiteral.matcher(value).matches()) {
                doc = dom.parse(new InputSource(new StringReader("<m>".concat(value.replaceAll("<\\?.+\\?>", "")).concat("</m>"))));
                return new XML(doc, XML.convert(doc.getFirstChild().getChildNodes()));
            } else {
                return xml(doc != null ? doc.createTextNode(value) : dom.newDocument().createElement(value));
            }
        } catch (Exception e) {
            throw quiet(e);
        }
    }

    /**
     * <p>
     * Read byte data from various sources.
     * </p>
     * 
     * @param input A data source.
     * @return A data.
     */
    private static byte[] read(Object input) throws Exception {
        // skip character data
        if (input instanceof CharSequence == false) {
            // object to stream
            if (input instanceof URI) {
                input = ((URI) input).toURL();
            }
            if (input instanceof URL) {
                input = ((URL) input).openStream();
            } else if (input instanceof Path) {
                input = Files.newInputStream((Path) input);
            } else if (input instanceof File) {
                input = new FileInputStream((File) input);
            }

            // stream to byte
            if (input instanceof InputStream) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                copy((InputStream) input, out, true);
                input = out.toByteArray();
            } else if (input instanceof Readable) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                copy((Readable) input, new OutputStreamWriter(out, $encoding), true);
                input = out.toByteArray();
            }
        }
        return input instanceof byte[] ? (byte[]) input : input.toString().getBytes();
    }
}
