/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss;

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
import java.io.StringReader;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;
import java.util.zip.ZipFile;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

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
 * 
 * @version 2018/02/28 17:10:55
 */
public class I {

    // Candidates of Method Name
    //
    // annotate accept alert allow approve associate avoid attend affect agree acquire add afford
    // bind bundle
    // create class copy collect config convert
    // delete define deny describe detach dispatch drive drop dub depend die disrupt draw decline
    // edit error ensure examine exclude exist embed enhance enter evolve exchange expect extend
    // enregister
    // find fetch
    // get gain give go grant glance gaze grab garnish
    // hash have handle hold halt hang hasten
    // i18n include infer improve indicate inspire integrate introduce invite identify interpret
    // json join
    // kick know knock keep knot knit
    // locate load log launch lead leave live look list
    // make mock map
    // name note near notice narrow neglect
    // observe opt organize overcome offer order open obtain
    // parse pair plug
    // quiet
    // read refer reject recover retry run register
    // save staple schedule set signal scrape scan
    // transform type take tap task talk transport turn traverse transmit trigger think
    // unload use unite undo
    // vanish view value vouch vary vindicate
    // write warn walk watch wrap wise
    // xml
    // yield
    // zip zoom zone

    /** No Operation */
    public static final WiseRunnable NoOP = () -> {
    };

    /** The circularity dependency graph per thread. */
    static final ThreadSpecific<Deque<Class>> dependencies = new ThreadSpecific(ArrayDeque.class);

    /** The document builder. */
    static final DocumentBuilder dom;

    /** The xpath evaluator. */
    static final XPath xpath;

    /** The cache for {@link Lifestyle}. */
    private static final Map<Class, Lifestyle> lifestyles = new ConcurrentHashMap<>();

    /** The definitions of extensions. */
    private static final Map<Class, Ⅱ> extensions = new HashMap<>();

    /** The parallel task manager. */
    private static final ExecutorService parallel = Executors.newWorkStealingPool(16);

    /** The serial task manager. */
    private static final ScheduledExecutorService serial = Executors.newSingleThreadScheduledExecutor(run -> {
        Thread thread = new Thread(run);
        thread.setName("Sinobu Scheduler");
        thread.setDaemon(true);
        return thread;
    });

    /** The list of primitive classes. (except for void type) */
    private static final Class[] primitives = {boolean.class, int.class, long.class, float.class, double.class, byte.class, short.class,
            char.class, void.class};

    /** The list of wrapper classes. (except for void type) */
    private static final Class[] wrappers = {Boolean.class, Integer.class, Long.class, Float.class, Double.class, Byte.class, Short.class,
            Character.class, Void.class};

    /** XML literal pattern. */
    private static final Pattern xmlLiteral = Pattern.compile("^\\s*<.+>\\s*$", Pattern.DOTALL);

    /** The submarine {@link Encoder} / {@link Decoder} support for java.nio.file.Path. */
    private static Method path;

    /** The cached environment variables. */
    private static final Properties env = new Properties();

    // initialization
    static {
        // built-in lifestyles
        lifestyles.put(List.class, ArrayList::new);
        lifestyles.put(Map.class, HashMap::new);
        lifestyles.put(Set.class, HashSet::new);
        lifestyles.put(Lifestyle.class, new Prototype(Prototype.class));
        lifestyles.put(Prototype.class, new Prototype(Prototype.class));
        lifestyles.put(Locale.class, Locale::getDefault);

        try {
            // configure dom builder
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);

            dom = factory.newDocumentBuilder();
            dom.setErrorHandler(new DefaultHandler());
            xpath = XPathFactory.newInstance().newXPath();
        } catch (Exception e) {
            throw I.quiet(e);
        }

        try {
            path = Class.forName("java.nio.file.Paths").getMethod("get", String.class, String[].class);
        } catch (Exception e) {
            // ignore
        }

        // built-in encoders
        load(ExtensionFactory.class, Encoder.class, () -> (ExtensionFactory<Encoder>) type -> {
            if (type.isEnum()) {
                return value -> ((Enum) value).name();
            }
            switch (type.getName().hashCode()) {
            case -530663260: // java.lang.Class
                return value -> ((Class) value).getName();
            default:
                return String::valueOf;
            }
        });

        // built-in decoders
        load(ExtensionFactory.class, Decoder.class, () -> (ExtensionFactory<Decoder>) type -> {
            if (type.isEnum()) {
                return value -> Enum.valueOf((Class<Enum>) type, value);
            }
            switch (type.getName().hashCode()) {
            case 64711720: // boolean
            case 344809556: // java.lang.Boolean
                return Boolean::parseBoolean;
            case 104431: // int
            case -2056817302: // java.lang.Integer
                return Integer::parseInt;
            case 3327612: // long
            case 398795216: // java.lang.Long
                return Long::parseLong;
            case 97526364: // float
            case -527879800: // java.lang.Float
                return Float::parseFloat;
            case -1325958191: // double
            case 761287205: // java.lang.Double
                return Double::parseDouble;
            case 3039496: // byte
            case 398507100: // java.lang.Byte
                return Byte::parseByte;
            case 109413500: // short
            case -515992664: // java.lang.Short
                return Short::parseShort;
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
            case -1165211622: // java.util.Locale
                return Locale::forLanguageTag;
            case 1464606545: // java.nio.file.Path
            case -2015077501: // sun.nio.fs.WindowsPath
                return value -> {
                    try {
                        return path.invoke(null, value, new String[0]);
                    } catch (Exception e) {
                        throw I.quiet(e);
                    }
                };
            case -1023498007: // java.time.Duration
            case 1296075756: // java.time.Instant
            case -1246518012: // java.time.LocalDate
            case -1179039247: // java.time.LocalDateTime
            case -1246033885: // java.time.LocalTime
            case 649475153: // java.time.MonthDay
            case -682591005: // java.time.OffsetDateTime
            case -1917484011: // java.time.OffsetTime
            case 649503318: // java.time.Period
            case -1062742510: // java.time.Year
            case -537503858: // java.time.YearMonth
            case 1505337278: // java.time.ZonedDateTime
                return value -> {
                    try {
                        return type.getMethod("parse", CharSequence.class).invoke(null, value);
                    } catch (Exception e) {
                        throw I.quiet(e);
                    }
                };
            // case -89228377: // java.nio.file.attribute.FileTime
            // decoder = value -> FileTime.fromMillis(Long.valueOf(value));
            // encoder = (Encoder<FileTime>) value -> String.valueOf(value.toMillis());
            // break;
            default:
                return null;
            }
        });

        // build twelve-factor configuration
        try {
            env.load(new InputStreamReader(I.class.getClassLoader().getResourceAsStream(".env"), StandardCharsets.UTF_8));
        } catch (Exception e) {
            // ignore
        }
        try {
            env.load(Files.newBufferedReader(Paths.get(".env")));
        } catch (Exception e) {
            // ignore
        }
        env.putAll(System.getenv());
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
    public static <P> Predicate<P> accept() {
        return p -> true;
    }

    /**
     * <p>
     * Create {@link BiPredicate} which accepts any item.
     * </p>
     * 
     * @return An acceptable {@link BiPredicate}.
     */
    public static <P, Q> BiPredicate<P, Q> accepţ() {
        return (p, q) -> true;
    }

    /**
     * Merge two arrays.
     * 
     * @param one An array to merge.
     * @param other An array to merge.
     * @return A merged array.
     */
    public static <T> T[] array(T[] one, T... other) {
        if (one == null) {
            return other == null ? null : other;
        } else if (other == null) {
            return one;
        }

        T[] all = Arrays.copyOf(one, one.length + other.length);
        System.arraycopy(other, 0, all, one.length, other.length);
        return all;
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
    public static <F> F bundle(Collection<? extends F> functions) {
        Set<Class> types = null;
        Iterator<? extends F> iterator = functions.iterator();

        if (iterator.hasNext()) {
            types = Model.collectTypes(iterator.next().getClass());
            types.removeIf(v -> !v.isInterface());

            while (iterator.hasNext()) {
                types.retainAll(Model.collectTypes(iterator.next().getClass()));
            }
        }
        return bundle((Class<F>) (types == null || types.isEmpty() ? null : types.iterator().next()), functions);
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
    public static <F> F bundle(Class<F> type, Collection<? extends F> functions) {
        return make(type, (proxy, method, args) -> {
            Object result = null;

            if (functions != null) {
                for (Object fun : functions) {
                    if (fun != null) {
                        try {
                            result = method.invoke(fun, args);
                        } catch (InvocationTargetException e) {
                            throw e.getCause();
                        }
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
            collection.addAll(Arrays.asList(items));
        }
        return collection;
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
        try {
            input.transferTo(output);
        } catch (IOException e) {
            throw I.quiet(e);
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
     * ENV resolution order (sources higher in the list take precedence over those located lower):
     * <ol>
     * <li>System.getenv()</li>
     * <li>.env file in current working directory (might not exist)</li>
     * <li>.env file on the classpath (might not exist)</li>
     * </ol>
     * 
     * @param key A environment variable name.
     * @return
     */
    public static String env(String key) {
        return env.getProperty(key);
    }

    /**
     * ENV resolution order (sources higher in the list take precedence over those located lower):
     * <ol>
     * <li>System.getenv()</li>
     * <li>.env file in current working directory (might not exist)</li>
     * <li>.env file on the classpath (might not exist)</li>
     * </ol>
     * 
     * @param key A environment variable name.
     * @return
     */
    public static String env(String key, String defaults) {
        return env.getProperty(key, defaults);
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
        return I.signal(findBy(extensionPoint)).flatIterable(Ⅱ::ⅰ).skip(e -> Modifier.isAbstract(e.getModifiers())).map(I::make).toList();
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
        if (extensionPoint != null && key != null) {
            Ⅱ<List<Class<E>>, Map<Class, Supplier<E>>> extensions = findBy(extensionPoint);

            for (Class type : Model.collectTypes(key)) {
                Supplier<E> supplier = extensions.ⅱ.get(type);

                if (supplier != null) {
                    return supplier.get();
                }
            }

            if (extensionPoint != ExtensionFactory.class) {
                ExtensionFactory<E> factory = find(ExtensionFactory.class, extensionPoint);

                if (factory != null) {
                    return factory.create(key);
                }
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
        return new ArrayList(findBy(extensionPoint).ⅰ);
    }

    /**
     * <p>
     * Find the extension definition for the specified extension point.
     * </p>
     * 
     * @param extensionPoint A target extension point.
     * @return A extension definition.
     */
    private static <E extends Extensible> Ⅱ<List<Class<E>>, Map<Class, Supplier<E>>> findBy(Class<E> extensionPoint) {
        return extensions.computeIfAbsent(extensionPoint, p -> pair(new ArrayList(), new HashMap()));
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

        for (Class clazz : findAs(bundleClass)) {
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
     * 
     * @param input A json format text. <code>null</code> will throw {@link NullPointerException}.
     *            The empty or invalid format data will throw {@link IllegalStateException}.
     * @return A parsed {@link JSON}.
     * @throws NullPointerException If the input data or the root Java object is <code>null</code>.
     * @throws IllegalStateException If the input data is empty or invalid format.
     */
    public static JSON json(CharSequence input) {
        return json((Object) input);
    }

    /**
     * <p>
     * Parse the specified JSON format text.
     * </p>
     * 
     * @param input A json format text. <code>null</code> will throw {@link NullPointerException}.
     *            The empty or invalid format data will throw {@link IllegalStateException}.
     * @return A parsed {@link JSON}.
     * @throws NullPointerException If the input data or the root Java object is <code>null</code>.
     * @throws IllegalStateException If the input data is empty or invalid format.
     */
    public static JSON json(File input) {
        return json((Object) input);
    }

    /**
     * <p>
     * Parse the specified JSON format text.
     * </p>
     * 
     * @param input A json format text. <code>null</code> will throw {@link NullPointerException}.
     *            The empty or invalid format data will throw {@link IllegalStateException}.
     * @return A parsed {@link JSON}.
     * @throws NullPointerException If the input data or the root Java object is <code>null</code>.
     * @throws IllegalStateException If the input data is empty or invalid format.
     */
    public static JSON json(InputStream input) {
        return json((Object) input);
    }

    /**
     * <p>
     * Parse the specified JSON format text.
     * </p>
     * 
     * @param input A json format text. <code>null</code> will throw {@link NullPointerException}.
     *            The empty or invalid format data will throw {@link IllegalStateException}.
     * @return A parsed {@link JSON}.
     * @throws NullPointerException If the input data or the root Java object is <code>null</code>.
     * @throws IllegalStateException If the input data is empty or invalid format.
     */
    public static JSON json(URL input) {
        return json((Object) input);
    }

    /**
     * <p>
     * Parse the specified JSON format text.
     * </p>
     * 
     * @param input A json format text. <code>null</code> will throw {@link NullPointerException}.
     *            The empty or invalid format data will throw {@link IllegalStateException}.
     * @return A parsed {@link JSON}.
     * @throws NullPointerException If the input data or the root Java object is <code>null</code>.
     * @throws IllegalStateException If the input data is empty or invalid format.
     */
    public static JSON json(URI input) {
        return json((Object) input);
    }

    /**
     * <p>
     * Parse the specified JSON format text.
     * </p>
     * 
     * @param input A json format text. <code>null</code> will throw {@link NullPointerException}.
     *            The empty or invalid format data will throw {@link IllegalStateException}.
     * @return A parsed {@link JSON}.
     * @throws NullPointerException If the input data or the root Java object is <code>null</code>.
     * @throws IllegalStateException If the input data is empty or invalid format.
     */
    public static JSON json(Readable input) {
        return json((Object) input);
    }

    /**
     * <p>
     * Parse the specified JSON format text.
     * </p>
     * <ul>
     * <li>{@link JSON}</li>
     * <li>{@link File}</li>
     * <li>{@link InputStream}</li>
     * <li>{@link Readable}</li>
     * <li>{@link URL}</li>
     * <li>{@link URI}</li>
     * <li>{@link CharSequence}</li>
     * </ul>
     * 
     * @param input A json format text. <code>null</code> will throw {@link NullPointerException}.
     *            The empty or invalid format data will throw {@link IllegalStateException}.
     * @return A parsed {@link JSON}.
     * @throws NullPointerException If the input data or the root Java object is <code>null</code>.
     * @throws IllegalStateException If the input data is empty or invalid format.
     */
    private static JSON json(Object input) {
        InputStreamReader stream = null;

        try {
            // Parse as JSON
            return new JSON(stream = new InputStreamReader(new ByteArrayInputStream(read(input)), StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw quiet(e);
        } finally {
            I.quiet(stream);
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

    /**
     * <p>
     * Load all {@link Extensible} typs from the specified source.
     * </p>
     * <p>
     * You can define the special class "kiss.Index" which defines pre-scanned class names.
     * "kiss.Index" must implement List<Set<String>>.
     * </p>
     *
     * @param source A source class to indicate the class set which are loaded.
     * @return The unloader.
     * @see {@link Extensible}
     * @see #find(Class)
     * @see #find(Class, Class)
     * @see #findAs(Class)
     */
    public static <E extends Extensible> Disposable load(Class<E> source, boolean filter) {
        // =======================================
        // List up extension class names
        // =======================================
        Set<String> candidates = Collections.EMPTY_SET;

        try {
            // Scan at runtime
            Signal<String> names;
            File file = new File(source.getProtectionDomain().getCodeSource().getLocation().toURI());

            if (file.isFile()) {
                // from jar file
                names = I.signal(new ZipFile(file).entries()).map(entry -> entry.getName().replace('/', '.'));
            } else {
                // from class directory
                int prefix = file.getPath().length() + 1;
                names = I.signal(true, file, entry -> entry.flatArray(File::listFiles))
                        .take(File::isFile)
                        .map(entry -> entry.getPath().substring(prefix).replace(File.separatorChar, '.'));
            }
            candidates = names.take(name -> name.endsWith(".class")).map(name -> name.substring(0, name.length() - 6)).toSet();
        } catch (Throwable e) {
            // Fallback for Android or Booton.
            // Try to read from pre-scanned index class.
            // If there is no "kiss.Index" class, Sinobu will throw ClassNotFoundException.
            List<Set<String>> list = (List) I.make(type("kiss.Index"));
            String name = source.getName();

            for (Set<String> names : list) {
                if (names.contains(name)) {
                    candidates = names;
                    break;
                }
            }
        }

        // =======================================
        // Register class as extension
        // =======================================
        Disposable disposer = Disposable.empty();
        String pattern = filter ? source.getPackage().getName() : "";

        root: for (String name : candidates) {
            // exclude out of the specified package
            if (!name.startsWith(pattern)) {
                continue;
            }

            Class extension = I.type(name);

            // fast check : exclude non-initializable class
            if (extension.isEnum() || extension.isAnonymousClass()) {
                continue;
            }

            // slow check : exclude non-extensible class
            if (!Extensible.class.isAssignableFrom(extension)) {
                continue;
            }

            // search and collect information for all extension points
            for (Class<E> extensionPoint : Model.collectTypes(extension)) {
                if (Arrays.asList(extensionPoint.getInterfaces()).contains(Extensible.class)) {
                    // register as new extension
                    Ⅱ<List<Class<E>>, Map<Class, Supplier<E>>> extensions = findBy(extensionPoint);

                    // exclude duplication
                    if (extensions.ⅰ.contains(extension)) {
                        continue root;
                    }

                    // register extension
                    extensions.ⅰ.add(extension);
                    disposer.add(() -> extensions.ⅰ.remove(extension));

                    // register extension key
                    Type[] params = Model.collectParameters(extension, extensionPoint);

                    if (params.length != 0 && params[0] != Object.class) {
                        Class clazz = (Class) params[0];

                        // register extension by key
                        disposer.add(load(extensionPoint, clazz, () -> (E) I.make(extension)));

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
                            disposer.add(() -> lifestyles.remove(clazz));
                        }
                    }
                }
            }
        }
        return disposer;
    }

    /**
     * <p>
     * Register extension with key.
     * </p>
     * 
     * @param extensionPoint A extension point.
     * @param extensionKey A extension key,
     * @param extension A extension to register.
     * @return A disposer to unregister.
     */
    public static <E extends Extensible> Disposable load(Class<E> extensionPoint, Class extensionKey, Supplier<E> extension) {
        findBy(extensionPoint).ⅱ.put(extensionKey, extension);
        return () -> findBy(extensionPoint).ⅱ.remove(extensionKey);
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

        // Construct dependency graph for the current thraed.
        Deque<Class> dependency = dependencies.get();
        dependency.add(modelClass);

        try {
            // At first, we should search the associated lifestyle from extension points.
            lifestyle = find(Lifestyle.class, modelClass);

            // Then, check its Manageable annotation.
            if (lifestyle == null) {
                // If the actual model class doesn't provide its lifestyle explicitly, we use
                // Prototype lifestyle which is default lifestyle in Sinobu.
                Manageable manageable = modelClass.getAnnotation(Manageable.class);

                // Create new lifestyle for the actual model class
                lifestyle = (Lifestyle) make((Class) (manageable == null ? Prototype.class : manageable.lifestyle()));
            }

            if (lifestyles.containsKey(modelClass)) {
                return lifestyles.get(modelClass);
            } else {
                lifestyles.put(modelClass, lifestyle);
                return lifestyle;
            }
        } finally {
            dependency.pollLast();
        }
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
     * Ease the checked exception on lambda.
     * </p>
     * 
     * @param lambda A checked lambda.
     * @return A unchecked lambda.
     */
    public static Runnable quiet(WiseRunnable lambda) {
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
    public static <P> Consumer<P> quiet(WiseConsumer<P> lambda) {
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
    public static <P1, P2> BiConsumer<P1, P2> quiet(WiseBiConsumer<P1, P2> lambda) {
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
    public static <R> Supplier<R> quiet(WiseSupplier<R> lambda) {
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
    public static <P, R> Function<P, R> quiet(WiseFunction<P, R> lambda) {
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
    public static <P1, P2, R> BiFunction<P1, P2, R> quiet(WiseBiFunction<P1, P2, R> lambda) {
        return lambda;
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
            return I.<RuntimeException> quiet(throwable);
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
    private static <T extends Throwable> T quiet(Throwable throwable) throws T {
        throw (T) throwable;
    }

    /**
     * <p>
     * Reads Java object from the JSON input.
     * </p>
     *
     * @param input A serialized JSON representation of Java object. If the input is incompatible
     *            with Java object, this method ignores the input. <code>null</code> will throw
     *            {@link NullPointerException}. The empty or invalid format data will throw
     *            {@link IllegalStateException}.
     * @param output A root Java object. All properties will be assigned from the given data deeply.
     *            If the input is incompatible with Java object, this method ignores the input.
     *            <code>null</code> will throw {@link java.lang.NullPointerException}.
     * @return A root Java object.
     * @throws NullPointerException If the input data or the root Java object is <code>null</code>.
     * @throws IllegalStateException If the input data is empty or invalid format.
     * @see #write(Object, Appendable)
     */
    public static <M> M read(CharSequence input, M output) {
        return json(input).to(output);
    }

    /**
     * <p>
     * Reads Java object from the JSON input.
     * </p>
     * <p>
     * If the input object implements {@link AutoCloseable}, {@link AutoCloseable#close()} method
     * will be invoked certainly.
     * </p>
     *
     * @param input A serialized JSON representation of Java object. If the input is incompatible
     *            with Java object, this method ignores the input. <code>null</code> will throw
     *            {@link NullPointerException}. The empty or invalid format data will throw
     *            {@link IllegalStateException}.
     * @param output A root Java object. All properties will be assigned from the given data deeply.
     *            If the input is incompatible with Java object, this method ignores the input.
     *            <code>null</code> will throw {@link java.lang.NullPointerException}.
     * @return A root Java object.
     * @throws NullPointerException If the input data or the root Java object is <code>null</code>.
     * @throws IOError If the input data is empty or invalid format.
     * @see #write(Object, Appendable)
     */
    public static <M> M read(Readable input, M output) {
        return json(input).to(output);
    }

    /**
     * <p>
     * Define recursive {@link BiConsumer}.
     * </p>
     * <pre>
     * I.recurBC(self -> (param1, param2) -> {
     *   // your function code
     * });
     * </pre>
     * 
     * @param function A recursive function.
     * @return A created function.
     */
    public static <Param1, Param2> BiConsumer<Param1, Param2> recurseBC(Function<BiConsumer<Param1, Param2>, BiConsumer<Param1, Param2>> function) {
        Recursive<BiConsumer<Param1, Param2>> recursive = recursiveFunction -> function.apply((param1, param2) -> {
            recursiveFunction.apply(recursiveFunction).accept(param1, param2);
        });
        return recursive.apply(recursive);
    }

    /**
     * <p>
     * Define recursive {@link BiFunction}.
     * </p>
     * <pre>
     * I.recurBF(self -> (param1, param2) -> {
     *   // your function code
     * });
     * </pre>
     * 
     * @param function A recursive function.
     * @return A created function.
     */
    public static <Param1, Param2, Return> BiFunction<Param1, Param2, Return> recurseBF(Function<BiFunction<Param1, Param2, Return>, BiFunction<Param1, Param2, Return>> function) {
        Recursive<BiFunction<Param1, Param2, Return>> recursive = recursiveFunction -> function.apply((param1, param2) -> {
            return recursiveFunction.apply(recursiveFunction).apply(param1, param2);
        });
        return recursive.apply(recursive);
    }

    /**
     * <p>
     * Define recursive {@link Consumer}.
     * </p>
     * <pre>
     * I.recurC(self -> param1 -> {
     *   // your function code
     * });
     * </pre>
     * 
     * @param function A recursive function.
     * @return A created function.
     * @param function A target function to convert.
     * @return A converted recursive function.
     */
    public static <Param> Consumer<Param> recurseC(Function<Consumer<Param>, Consumer<Param>> function) {
        Recursive<Consumer<Param>> recursive = recursiveFunction -> function.apply(param -> {
            recursiveFunction.apply(recursiveFunction).accept(param);
        });
        return recursive.apply(recursive);
    }

    /**
     * <p>
     * Define recursive {@link Function}.
     * </p>
     * <pre>
     * I.recurF(self -> param -> {
     *   // your function code
     * });
     * </pre>
     * 
     * @param function A recursive function.
     * @return A created function.
     */
    public static <Param, Return> Function<Param, Return> recurseF(Function<Function<Param, Return>, Function<Param, Return>> function) {
        Recursive<Function<Param, Return>> recursive = recursiveFunction -> function.apply(param -> {
            return recursiveFunction.apply(recursiveFunction).apply(param);
        });

        return recursive.apply(recursive);
    }

    /**
     * <p>
     * Define recursive {@link Runnable}.
     * </p>
     * <pre>
     * I.recurR(self -> () -> {
     *   // your function code
     * });
     * </pre>
     * 
     * @param function A recursive function.
     * @return A created function.
     */
    public static Runnable recurseR(Function<Runnable, Runnable> function) {
        Recursive<Runnable> recursive = recursiveFunction -> function.apply(() -> {
            recursiveFunction.apply(recursiveFunction).run();
        });
        return recursive.apply(recursive);
    }

    /**
     * <p>
     * Define recursive {@link Supplier}.
     * </p>
     * <pre>
     * I.recurS(self -> () -> {
     *   // your function code
     * });
     * </pre>
     * 
     * @param function A recursive function.
     * @return A created function.
     */
    public static <Result> Supplier<Result> recurseS(Function<Supplier<Result>, Supplier<Result>> function) {
        Recursive<Supplier<Result>> recursive = recursiveFunction -> function.apply(() -> {
            return recursiveFunction.apply(recursiveFunction).get();
        });
        return recursive.apply(recursive);
    }

    /**
     * <p>
     * Define recursive {@link WiseTriConsumer}.
     * </p>
     * <pre>
     * I.recurTC(self -> (param1, param2, param3) -> {
     *   // your function code
     * });
     * </pre>
     * 
     * @param function A recursive function.
     * @return A created function.
     */
    public static <Param1, Param2, Param3> WiseTriConsumer<Param1, Param2, Param3> recurseTC(Function<WiseTriConsumer<Param1, Param2, Param3>, WiseTriConsumer<Param1, Param2, Param3>> function) {
        Recursive<WiseTriConsumer<Param1, Param2, Param3>> recursive = recursiveFunction -> function.apply((param1, param2, param3) -> {
            recursiveFunction.apply(recursiveFunction).accept(param1, param2, param3);
        });
        return recursive.apply(recursive);
    }

    /**
     * <p>
     * Define recursive {@link WiseTriFunction}.
     * </p>
     * <pre>
     * I.recurTF(self -> (param1, param2, param3) -> {
     *   // your function code
     * });
     * </pre>
     * 
     * @param function A recursive function.
     * @return A created function.
     */
    public static <Param1, Param2, Param3, Return> WiseTriFunction<Param1, Param2, Param3, Return> recurseTF(Function<WiseTriFunction<Param1, Param2, Param3, Return>, WiseTriFunction<Param1, Param2, Param3, Return>> function) {
        Recursive<WiseTriFunction<Param1, Param2, Param3, Return>> recursive = recursiveFunction -> function
                .apply((param1, param2, param3) -> {
                    return recursiveFunction.apply(recursiveFunction).apply(param1, param2, param3);
                });
        return recursive.apply(recursive);
    }

    /**
     * <p>
     * Create {@link Predicate} which rejects any item.
     * </p>
     * 
     * @return An rejectable {@link Predicate}.
     */
    public static <P> Predicate<P> reject() {
        return p -> false;
    }

    /**
     * <p>
     * Create {@link BiPredicate} which rejects any item.
     * </p>
     * 
     * @return An rejectable {@link BiPredicate}.
     */
    public static <P, Q> BiPredicate<P, Q> rejecţ() {
        return (p, q) -> false;
    }

    /**
     * Perform recoverable operation. If some recoverable error will occur, this method perform
     * recovery operation automatically.
     * 
     * @param operation A original user operation.
     * @param recoveries A list of recovery operations.
     */
    public static void run(WiseRunnable opereation, WiseFunction<Signal<? extends Throwable>, Signal<?>> notifier) {
        I.signal("").effect(opereation).retryWhen(notifier).to();
    }

    /**
     * Perform recoverable operation. If some recoverable error will occur, this method perform
     * recovery operation automatically.
     * 
     * @param operation A original user operation.
     * @param recoveries A list of recovery operations.
     * @return A operation result.
     */
    public static <R> R run(WiseSupplier<R> operation, WiseFunction<Signal<? extends Throwable>, Signal<?>> notifier) {
        return I.signal("").map(operation.asFunction()).retryWhen(notifier).to().v;
    }

    /**
     * <p>
     * Execute the specified task in background {@link Thread}.
     * </p>
     *
     * @param task A task to execute.
     */
    public static Future<?> schedule(Runnable task) {
        return CompletableFuture.runAsync(error(task), parallel);
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
        task = error(task);

        if (delay <= 0) {
            task.run();
            return CompletableFuture.completedFuture(null);
        }

        if (parallelExecution) {
            return CompletableFuture.runAsync(task, CompletableFuture.delayedExecutor(delay, unit, parallel));
        } else {
            return serial.schedule(task, delay, unit);
        }
    }

    /**
     * Decorate error handler.
     * 
     * @param task A target task to decorate.
     * @return A decorated task.
     */
    private static Runnable error(Runnable task) {
        return () -> {
            try {
                task.run();
            } catch (Throwable e) {
                Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
            }
        };
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
     * Signal the specified values.
     * </p>
     *
     * @param values A list of values to emit.
     * @return The {@link Signal} to emit sequencial values.
     */
    @SafeVarargs
    public static <V> Signal<V> signal(V... values) {
        return Signal.<V> empty().startWith(values);
    }

    /**
     * Converts a {@link Future} into a {@link Signal}.
     *
     * @param value The source {@link Future}.
     * @param <V> The type of object that the {@link Future} returns, and also the type of item to
     *            be emitted by the resulting {@link Signal}.
     * @return {@link Signal} that emits the item from the source {@link Future}.
     */
    public static <V> Signal<V> signal(Future<V> value) {
        return new Signal<>((observer, disposer) -> {
            I.schedule(() -> {
                try {
                    observer.accept(value.get());
                    observer.complete();
                } catch (Throwable e) {
                    observer.error(e);
                }
            });
            return disposer.add(() -> value.cancel(true));
        });
    }

    /**
     * Converts a {@link CompletableFuture} into a {@link Signal}.
     *
     * @param value The source {@link CompletableFuture}.
     * @param <V> The type of object that the {@link CompletableFuture} returns, and also the type
     *            of item to be emitted by the resulting {@link Signal}.
     * @return {@link Signal} that emits the item from the source {@link CompletableFuture}.
     */
    public static <V> Signal<V> signal(CompletableFuture<V> value) {
        return new Signal<>((observer, disposer) -> {
            value.whenComplete((v, e) -> {
                if (e == null) {
                    observer.accept(v);
                    observer.complete();
                } else {
                    observer.error(e);
                }
            });
            return disposer.add(() -> value.cancel(true));
        });
    }

    /**
     * <p>
     * Signal the specified values.
     * </p>
     *
     * @param values A list of values to emit.
     * @return The {@link Signal} to emit sequencial values.
     */
    public static <V> Signal<V> signal(Iterable<V> values) {
        return Signal.<V> empty().startWith(values);
    }

    /**
     * <p>
     * Signal the specified values.
     * </p>
     *
     * @param values A list of values to emit.
     * @return The {@link Signal} to emit sequencial values.
     */
    public static <V> Signal<V> signal(Enumeration<V> values) {
        return Signal.<V> empty().startWith(values);
    }

    /**
     * <p>
     * {@link Signal} the specified values.
     * </p>
     *
     * @param values A list of values to emit.
     * @return The {@link Signal} to emit sequencial values.
     */
    public static <V> Signal<V> signal(Supplier<V> value) {
        return Signal.<V> empty().startWith(value);
    }

    /**
     * Returns an {@link Signal} that emits {@code 0L} after a specified delay, and then completes.
     *
     * @param delayTime The initial delay before emitting a single {@code 0L}.
     * @param timeUnit Time units to use for {@code delay}.
     * @return {@link Signal} that {@code 0L} after a specified delay, and then completes.
     */
    public static Signal<Long> signal(long delayTime, TimeUnit timeUnit) {
        return new Signal<>((observer, disposer) -> {
            Future future = I.schedule(delayTime, timeUnit, true, () -> {
                observer.accept(0L);
                observer.complete();
            });

            return disposer.add(() -> future.cancel(true));
        });
    }

    /**
     * Returns an {@link Signal} that emits a {@code 0L} after the {@code delayTime} and ever
     * increasing numbers after each {@code intervalTime} of time thereafter.
     * 
     * @param delayTime The initial delay time to wait before emitting the first value of 0L
     * @param intervalTime The period of time between emissions of the subsequent numbers
     * @param timeUnit the time unit for both {@code initialDelay} and {@code period}
     * @return {@link Signal} that emits a 0L after the {@code delayTime} and ever increasing
     *         numbers after each {@code intervalTime} of time thereafter
     */
    public static Signal<Long> signal(long delayTime, long intervalTime, TimeUnit timeUnit) {
        return new Signal<>((observer, disposer) -> {
            Future[] result = new Future[1];
            AtomicLong count = new AtomicLong();

            result[0] = I.schedule(delayTime, timeUnit, true, I.recurseR(self -> () -> {
                observer.accept(count.getAndIncrement());

                result[0] = I.schedule(intervalTime, timeUnit, true, self);
            }));

            return disposer.add(() -> result[0].cancel(true));
        });
    }

    /**
     * <p>
     * Traverse from initial value to followings.
     * </p>
     * 
     * @param init An initial value to traverse.
     * @param iterator A function to navigate from a current to next.
     * @return {@link Signal} that emits values from initial to followings.
     */
    public static <T> Signal<T> signal(T init, WiseFunction<T, T> iterator) {
        return signal(true, init, e -> e.map(iterator));
    }

    /**
     * <p>
     * Traverse from initial value to followings.
     * </p>
     * 
     * @param sync Compute iteration synchronusly or not.
     * @param init An initial value to traverse.
     * @param iterator A function to navigate from a current to next.
     * @return {@link Signal} that emits values from initial to followings.
     */
    public static <T> Signal<T> signal(boolean sync, T init, UnaryOperator<Signal<T>> iterator) {
        // DON'T use the recursive call, it will throw StackOverflowError.
        return new Signal<T>((observer, disposer) -> {
            Runnable r = () -> {
                try {
                    LinkedList<T> values = new LinkedList(); // LinkedList accepts null
                    LinkedTransferQueue<Signal<T>> signal = new LinkedTransferQueue();
                    signal.put(I.signal(init));

                    while (disposer.isNotDisposed()) {
                        signal.take().to(v -> {
                            values.addLast(v);
                            observer.accept(v);
                        }, observer::error, () -> {
                            if (values.isEmpty()) {
                                observer.complete();
                            } else {
                                signal.put(iterator.apply(I.signal(values.pollFirst())));
                            }
                        });
                    }
                } catch (Throwable e) {
                    observer.error(e);
                }
            };

            if (sync) {
                r.run();
            } else {
                I.schedule(r);
            }
            return disposer;
        });
    }

    /**
     * <p>
     * Returns an {@link Signal} that invokes an {@link Observer#error(Throwable)} method when the
     * {@link Observer} subscribes to it.
     * </p>
     *
     * @param error An error to emit.
     * @return The {@link Signal} to emit error.
     */
    public static <V> Signal<V> signalError(Throwable error) {
        return new Signal<V>((observer, disposer) -> {
            observer.error(error);
            return disposer;
        });
    }

    /**
     * Signal a sequence of logns within a specified range.
     * 
     * @param start A value of the first long in the sequence.
     * @param count A number of sequential longs to generate.
     * @return A {@link Signal} that emits a range of sequential longs
     */
    public static Signal<Long> signalRange(long start, long count) {
        return signalRange(start, count, 1L);
    }

    /**
     * Signal a sequence of logns within a specified range.
     * 
     * @param start A value of the first long in the sequence.
     * @param count A number of sequential longs to generate.
     * @param step A step value for each sequential longs to generate.
     * @return A {@link Signal} that emits a range of sequential longs
     */
    public static Signal<Long> signalRange(long start, long count, long step) {
        return signal(start, v -> v + step).take(count);
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
            return Class.forName(fqcn, false, ClassLoader.getSystemClassLoader());
        } catch (ClassNotFoundException e) {
            throw quiet(e);
        }
    }

    /**
     * <p>
     * Down cast from {@link Runnable} to {@link WiseRunnable}.
     * </p>
     * 
     * @param lambda A target function.
     * @return A casted function.
     * @see #quiet(WiseRunnable)
     */
    public static WiseRunnable wise(Runnable lambda) {
        return lambda == null ? NoOP : lambda instanceof WiseRunnable ? (WiseRunnable) lambda : lambda::run;
    }

    /**
     * <p>
     * Down cast from {@link Consumer} to {@link WiseConsumer}.
     * </p>
     * 
     * @param lambda A target function.
     * @return A casted function.
     * @see #quiet(WiseConsumer)
     */
    public static <P> WiseConsumer<P> wise(Consumer<P> lambda) {
        return lambda == null || lambda instanceof WiseConsumer ? (WiseConsumer) lambda : lambda::accept;
    }

    /**
     * <p>
     * Down cast from {@link BiConsumer} to {@link WiseBiConsumer}.
     * </p>
     * 
     * @param lambda A target function.
     * @return A casted function.
     * @see #quiet(WiseBiConsumer)
     */
    public static <P1, P2> WiseBiConsumer<P1, P2> wise(BiConsumer<P1, P2> lambda) {
        return lambda == null || lambda instanceof WiseBiConsumer ? (WiseBiConsumer) lambda : lambda::accept;
    }

    /**
     * <p>
     * Down cast from {@link Supplier} to {@link WiseSupplier}.
     * </p>
     * 
     * @param lambda A target function.
     * @return A casted function.
     * @see #quiet(WiseSupplier)
     */
    public static <R> WiseSupplier<R> wise(Supplier<R> lambda) {
        return lambda == null || lambda instanceof WiseSupplier ? (WiseSupplier) lambda : lambda::get;
    }

    /**
     * <p>
     * Down cast from {@link Function} to {@link WiseFunction}.
     * </p>
     * 
     * @param lambda A target function.
     * @return A casted function.
     * @see #quiet(WiseFunction)
     */
    public static <P, R> WiseFunction<P, R> wise(Function<P, R> lambda) {
        return lambda == null || lambda instanceof WiseFunction ? (WiseFunction) lambda : lambda::apply;
    }

    /**
     * <p>
     * Down cast from {@link BiFunction} to {@link WiseBiFunction}.
     * </p>
     * 
     * @param lambda A target function.
     * @return A casted function.
     * @see #quiet(WiseBiFunction)
     */
    public static <P1, P2, R> WiseBiFunction<P1, P2, R> wise(BiFunction<P1, P2, R> lambda) {
        return lambda == null || lambda instanceof WiseBiFunction ? (WiseBiFunction) lambda : lambda::apply;
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
        if (type == null) {
            return Object.class;
        }

        if (type.isPrimitive()) {
            // check primitive classes
            for (int i = 0; i < primitives.length; i++) {
                if (primitives[i] == type) {
                    return wrappers[i];
                }
            }
        }

        // the specified class is not primitive
        return type;
    }

    /**
     * <p>
     * Write JSON representation of Java object.
     * </p>
     *
     * @param input A Java object. All properties will be serialized deeply. <code>null</code> will
     *            throw {@link java.lang.NullPointerException}.
     * @return A JSON representation of Java object.
     * @throws NullPointerException If the input Java object or the output is <code>null</code> .
     * @see #read(Readable, Object)
     * @see #read(CharSequence, Object)
     */
    public static String write(Object input) {
        StringBuilder output = new StringBuilder();
        I.write(input, output);
        return output.toString();
    }

    /**
     * <p>
     * Write JSON representation of Java object to the specified output.
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
     * @throws NullPointerException If the input Java object or the output is <code>null</code> .
     * @see #read(Readable, Object)
     * @see #read(CharSequence, Object)
     */
    public static void write(Object input, Appendable out) {
        Objects.requireNonNull(out);

        try {
            // traverse object as json
            Model model = Model.of(input);
            new JSON(out).write(model, new Property(model, ""), input);
        } finally {
            // close carefuly
            quiet(out);
        }
    }

    /**
     * <p>
     * Parse as xml fragment.
     * </p>
     *
     * @param source A xml expression.
     * @return A constructed {@link XML}.
     */
    public static XML xml(File source) {
        return I.xml(null, source);
    }

    /**
     * <p>
     * Parse as xml fragment.
     * </p>
     *
     * @param source A xml expression.
     * @return A constructed {@link XML}.
     */
    public static XML xml(InputStream source) {
        return I.xml(null, source);
    }

    /**
     * <p>
     * Parse as xml fragment.
     * </p>
     *
     * @param source A xml expression.
     * @return A constructed {@link XML}.
     */
    public static XML xml(Readable source) {
        return I.xml(null, source);
    }

    /**
     * <p>
     * Parse as xml fragment.
     * </p>
     *
     * @param source A xml expression.
     * @return A constructed {@link XML}.
     */
    public static XML xml(URL source) {
        return I.xml(null, source);
    }

    /**
     * <p>
     * Parse as xml fragment.
     * </p>
     *
     * @param source A xml expression.
     * @return A constructed {@link XML}.
     */
    public static XML xml(URI source) {
        return I.xml(null, source);
    }

    /**
     * <p>
     * Parse as xml fragment.
     * </p>
     *
     * @param source A xml expression.
     * @return A constructed {@link XML}.
     */
    public static XML xml(Node source) {
        return I.xml(null, source);
    }

    /**
     * <p>
     * Parse as xml fragment.
     * </p>
     *
     * @param source A xml expression.
     * @return A constructed {@link XML}.
     */
    public static XML xml(CharSequence source) {
        return I.xml(null, source);
    }

    /**
     * <p>
     * Parse as xml fragment.
     * </p>
     * <ul>
     * <li>{@link XML}</li>
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
                    return new XML(null, null).parse(bytes, StandardCharsets.UTF_8);
                }
            }

            String value = new String(bytes, StandardCharsets.UTF_8);

            if (xmlLiteral.matcher(value).matches()) {
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
                URL url = (URL) input;
                URLConnection connection = url.openConnection();
                connection.setRequestProperty("User-Agent", "");
                input = connection.getInputStream();
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
                copy((Readable) input, new OutputStreamWriter(out, StandardCharsets.UTF_8), true);
                input = out.toByteArray();
            }
        }
        return input instanceof byte[] ? (byte[]) input : input.toString().getBytes(StandardCharsets.UTF_8);
    }
}
