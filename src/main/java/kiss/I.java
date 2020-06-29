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
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.ServiceLoader.Provider;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Matcher;
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

    /** The automatic saver references. */
    static final WeakHashMap<Object, Disposable> autosaver = new WeakHashMap();

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

    private static final ThreadFactory factory = run -> {
        Thread thread = new Thread(run);
        thread.setName("Sinobu Scheduler");
        thread.setDaemon(true);
        return thread;
    };

    /** The parallel task manager. */
    static final ExecutorService parallel = Executors.newCachedThreadPool(factory);

    /** The parallel task scheduler. */
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(8, factory);

    /** The list of primitive classes. (except for void type) */
    private static final Class[] primitives = {boolean.class, int.class, long.class, float.class, double.class, byte.class, short.class,
            char.class, void.class};

    /** The list of wrapper classes. (except for void type) */
    private static final Class[] wrappers = {Boolean.class, Integer.class, Long.class, Float.class, Double.class, Byte.class, Short.class,
            Character.class, Void.class};

    /** XML literal pattern. */
    private static final Pattern xmlLiteral = Pattern.compile("^\\s*<.+>\\s*$", Pattern.DOTALL);

    /** The cached environment variables. */
    private static final Properties env = new Properties();

    /** The expression placeholder syntax. */
    private static final Pattern express = Pattern.compile("\\{([^}]+)\\}");

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

        // built-in encoders
        load(ExtensionFactory.class, Encoder.class, () -> (ExtensionFactory<Encoder>) type -> {
            if (type.isEnum()) {
                return (Encoder<Enum>) Enum::name;
            }
            switch (type.getName().hashCode()) {
            case -530663260: // java.lang.Class
                return (Encoder<Class>) Class::getName;
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
                return Path::of;
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
     * @param defaults If the specified key is not found, return this default value.
     * @return
     */
    public static String env(String key, String defaults) {
        return env.getProperty(key, defaults);
    }

    /**
     * If a value is not set for the specified key, register it as an environment variable on
     * runtime.
     * 
     * @param key A environment variable name.
     * @param value A value to set.
     */
    public static void envy(String key, String defaults) {
        env.putIfAbsent(key, defaults);
    }

    /**
     * Calculate expression language in the specified text by using the given contexts.
     * 
     * @param text A text with {some} placefolder.
     * @param contexts A list of value contexts.
     * @return A calculated text.
     */
    public static String express(String text, Object... contexts) {
        return express(text, contexts, (WiseTriFunction[]) null);
    }

    /**
     * Calculate expression language in the specified text by using the given contexts.
     * 
     * @param text A text with {some} placefolder.
     * @param contexts A list of value contexts.
     * @return A calculated text.
     */
    public static String express(String text, Object[] contexts, WiseTriFunction<Model, Object, String, Object>... resolvers) {
        // skip when context is empty
        if (contexts == null || contexts.length == 0) {
            return text;
        }

        resolvers = I.array(new WiseTriFunction[] {(WiseTriFunction<Model, Object, String, Object>) Model::get}, resolvers);

        StringBuilder str = new StringBuilder();

        // find all expression placeholder
        Matcher matcher = express.matcher(text);

        nextPlaceholder: while (matcher.find()) {
            // normalize expression (remove all white space) and split it
            String[] e = matcher.group(1).replaceAll("[\\s　]", "").split("\\.");

            // evaluate each model (first model has high priority)
            nextContext: for (int i = 0; i < contexts.length; i++) {
                Object c = contexts[i];

                // evaluate expression from head
                nextExpression: for (int j = 0; j < e.length; j++) {
                    Model m = Model.of(c);

                    // evaluate expression by each resolvers
                    for (int k = 0; k < resolvers.length; k++) {
                        Object o = resolvers[k].apply(m, c, e[j]);

                        if (o != null) {
                            // suitable value was found, step into next expression
                            c = o;
                            continue nextExpression;
                        }
                    }

                    // any resolver can't find suitable value, try to next context
                    continue nextContext;
                }

                // full expression was evaluated correctly, convert it to string
                matcher.appendReplacement(str, I.transform(c, String.class));

                continue nextPlaceholder;
            }

            // any context can't find suitable value, so use empty text
            matcher.appendReplacement(str, "");
        }
        matcher.appendTail(str);

        return str.toString();
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
    public static synchronized <E extends Extensible> List<E> find(Class<E> extensionPoint) {
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
    public static synchronized <E extends Extensible> E find(Class<E> extensionPoint, Class key) {
        if (extensionPoint != null && key != null) {
            Ⅱ<List<Class<E>>, Map<Class, Lifestyle<E>>> extensions = findBy(extensionPoint);

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
    public static synchronized <E extends Extensible> List<Class<E>> findAs(Class<E> extensionPoint) {
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
    private static synchronized <E extends Extensible> Ⅱ<List<Class<E>>, Map<Class, Lifestyle<E>>> findBy(Class<E> extensionPoint) {
        return extensions.computeIfAbsent(extensionPoint, p -> pair(new CopyOnWriteArrayList(), new ConcurrentHashMap()));
    }

    /**
     * <p>
     * Returns a string containing the string representation of each of items, using the specified
     * separator between each.
     * </p>
     *
     * @param delimiter A sequence of characters that is used to separate each of the elements in
     *            the resulting String.
     * @param items A list of items.
     * @return A concat expression.
     */
    public static String join(CharSequence delimiter, Object... items) {
        return join(delimiter, list(items));
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
        return String.join(delimiter == null ? "" : delimiter, I.signal(items).map(String::valueOf).toList());
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
    public static JSON json(HttpRequest input) {
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
     * Load all {@link Extensible} types from the specified source.
     * </p>
     * <p>
     * You can create the special service loader file "META-INF/services/kiss.Extensible" which
     * enumerates pre-scanned class names.
     * </p>
     *
     * @param source A source class to indicate the class set which are loaded.
     * @return Call {@link Disposable#dispose()} to unload the registered extension.
     * @see Extensible
     * @see ExtensionFactory
     * @see #find(Class)
     * @see #find(Class, Class)
     * @see #findAs(Class)
     */
    public static Disposable load(URL source) {
        URLClassLoader loader = URLClassLoader.newInstance(new URL[] {source});
        return load(source, "", loader).add(((WiseRunnable) loader::close)::run);
    }

    /**
     * <p>
     * Load all {@link Extensible} types from the specified source.
     * </p>
     * <p>
     * You can create the special service loader file "META-INF/services/kiss.Extensible" which
     * enumerates pre-scanned class names.
     * </p>
     *
     * @param source A source class to indicate the class set which are loaded.
     * @return Call {@link Disposable#dispose()} to unload the registered extension.
     * @see Extensible
     * @see ExtensionFactory
     * @see #find(Class)
     * @see #find(Class, Class)
     * @see #findAs(Class)
     */
    public static Disposable load(Class source) {
        return load(source.getProtectionDomain().getCodeSource().getLocation(), source.getPackage().getName(), source.getClassLoader());
    }

    /**
     * <p>
     * Load all {@link Extensible} typs from the specified source.
     * </p>
     * <p>
     * You can create the special service loader file "META-INF/services/kiss.Extensible" which
     * enumerates pre-scanned class names.
     * </p>
     *
     * @param source A source class to indicate the class set which are loaded.
     * @return Call {@link Disposable#dispose()} to unload the registered extension.
     * @see Extensible
     * @see ExtensionFactory
     * @see #find(Class)
     * @see #find(Class, Class)
     * @see #findAs(Class)
     */
    private static Disposable load(URL source, String pattern, ClassLoader loader) {
        // =======================================
        // List up extension class names
        // =======================================
        Signal<String> names;

        try {
            // Scan at runtime
            File file = new File(source.toURI());

            if (file.isFile()) {
                // from jar file
                names = I.signal(new ZipFile(file).entries()).map(entry -> entry.getName().replace('/', '.'));
            } else {
                // from class directory
                int prefix = file.getPath().length() + 1;
                names = I.signal(file)
                        .recurseMap(entry -> entry.flatArray(File::listFiles))
                        .take(File::isFile)
                        .map(entry -> entry.getPath().substring(prefix).replace(File.separatorChar, '.'));
            }
            names = names.take(name -> name.endsWith(".class")).map(name -> name.substring(0, name.length() - 6));
        } catch (Throwable e) {
            // FALLBACK
            // Read from pre-scanned file "kiss.Extensible" as service provider interface.
            names = I.signal(ServiceLoader.load(Extensible.class).stream()::iterator).map(Provider::type).map(Class::getName);
        }

        // =======================================
        // Register class as extension
        // =======================================
        Disposable disposer = Disposable.empty();

        for (String name : names.toSet()) {
            // exclude out of the specified package
            if (name.startsWith(pattern)) {
                try {
                    disposer.add(loadE((Class) loader.loadClass(name)));
                } catch (Throwable e) {
                    throw I.quiet(e);
                }
            }
        }
        return disposer;
    }

    /**
     * Load the specified extension, all implemented extension points are recognized automatically.
     * 
     * @param extension A target extension.
     * @return Call {@link Disposable#dispose()} to unload the registered extension.
     */
    static <E extends Extensible> Disposable loadE(Class<E> extension) {
        // fast check : exclude non-initializable class
        if (extension.isEnum() || extension.isAnonymousClass()) {
            return null;
        }

        // slow check : exclude non-extensible class
        if (!Extensible.class.isAssignableFrom(extension)) {
            return null;
        }

        Disposable disposer = Disposable.empty();

        // search and collect information for all extension points
        for (Class<E> extensionPoint : Model.collectTypes(extension)) {
            if (Arrays.asList(extensionPoint.getInterfaces()).contains(Extensible.class)) {
                // register as new extension
                Ⅱ<List<Class<E>>, Map<Class, Lifestyle<E>>> extensions = findBy(extensionPoint);

                // exclude duplication
                if (extensions.ⅰ.contains(extension)) {
                    return null;
                }

                // register extension
                extensions.ⅰ.add(extension);
                disposer.add(() -> extensions.ⅰ.remove(extension));

                // register extension key
                Type[] params = Model.collectParameters(extension, extensionPoint);

                if (params.length != 0 && params[0] != Object.class) {
                    Class clazz = (Class) params[0];

                    // register extension by key
                    disposer.add(load(extensionPoint, clazz, I.makeLifestyle(extension)));

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
    private static <E extends Extensible> Disposable load(Class<E> extensionPoint, Class extensionKey, Lifestyle<E> extension) {
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
     * Create wise function interface.
     * 
     * @param o
     * @param target
     * @param handler
     * @return
     */
    static <F> F make(Object o, Class target, Wise handler) {
        Type type = o == null ? target : Model.collectParameters(o.getClass().getInterfaces()[0], target)[0];

        if (type instanceof ParameterizedType) {
            type = ((ParameterizedType) type).getRawType();
        }

        if (type == WiseRunnable.class) {
            return (F) (WiseRunnable) handler::invoke;
        } else if (type == WiseSupplier.class) {
            return (F) (WiseSupplier) handler::invoke;
        } else if (type == WiseConsumer.class) {
            return (F) (WiseConsumer) handler::invoke;
        } else if (type == WiseFunction.class) {
            return (F) (WiseFunction) handler::invoke;
        } else if (type == WiseBiConsumer.class) {
            return (F) (WiseBiConsumer) handler::invoke;
        } else if (type == WiseBiFunction.class) {
            return (F) (WiseBiFunction) handler::invoke;
        } else if (type == WiseTriConsumer.class) {
            return (F) (WiseTriConsumer) handler::invoke;
        } else {
            return (F) (WiseTriFunction) handler::invoke;
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
                Managed managed = modelClass.getAnnotation(Managed.class);

                // Create new lifestyle for the actual model class
                lifestyle = (Lifestyle) make((Class) (managed == null ? Prototype.class : managed.value()));
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
     * I.recurse((self, param1, param2) -> {
     *   // your function code
     * });
     * </pre>
     * 
     * @param function A recursive function.
     * @return A created function.
     */
    public static <Param1, Param2> WiseBiConsumer<Param1, Param2> recurse(WiseTriConsumer<WiseBiConsumer<Param1, Param2>, Param1, Param2> function) {
        Variable<WiseBiConsumer<Param1, Param2>> ref = Variable.empty();
        ref.set(function.bindLazily(ref));
        return ref.v;
    }

    /**
     * <p>
     * Define recursive {@link BiFunction}.
     * </p>
     * <pre>
     * I.recurse((self, param1, param2) -> {
     *   // your function code
     * });
     * </pre>
     * 
     * @param function A recursive function.
     * @return A created function.
     */
    public static <Param1, Param2, Return> WiseBiFunction<Param1, Param2, Return> recurse(WiseTriFunction<WiseBiFunction<Param1, Param2, Return>, Param1, Param2, Return> function) {
        Variable<WiseBiFunction<Param1, Param2, Return>> ref = Variable.empty();
        ref.set(function.bindLazily(ref));
        return ref.v;
    }

    /**
     * <p>
     * Define recursive {@link Consumer}.
     * </p>
     * <pre>
     * I.recurse((self, param) -> {
     *   // your function code
     * });
     * </pre>
     * 
     * @param function A recursive function.
     * @return A created function.
     * @param function A target function to convert.
     * @return A converted recursive function.
     */
    public static <Param> WiseConsumer<Param> recurse(WiseBiConsumer<WiseConsumer<Param>, Param> function) {
        Variable<WiseConsumer<Param>> ref = Variable.empty();
        ref.set(function.bindLazily(ref));
        return ref.v;
    }

    /**
     * <p>
     * Define recursive {@link Function}.
     * </p>
     * <pre>
     * I.recurse((self, param) -> {
     *   // your function code
     * });
     * </pre>
     * 
     * @param function A recursive function.
     * @return A created function.
     */
    public static <Param, Return> WiseFunction<Param, Return> recurse(WiseBiFunction<WiseFunction<Param, Return>, Param, Return> function) {
        Variable<WiseFunction<Param, Return>> ref = Variable.empty();
        ref.set(function.bindLazily(ref));
        return ref.v;
    }

    /**
     * <p>
     * Define recursive {@link Runnable}.
     * </p>
     * <pre>
     * I.recurse(self -> {
     *   // your function code
     * });
     * </pre>
     * 
     * @param function A recursive function.
     * @return A created function.
     */
    public static WiseRunnable recurse(WiseConsumer<WiseRunnable> function) {
        Variable<WiseRunnable> ref = Variable.empty();
        ref.set(function.bindLazily(ref));
        return ref.v;
    }

    /**
     * <p>
     * Define recursive {@link Supplier}.
     * </p>
     * <pre>
     * I.recurse(self -> {
     *   // your function code
     * });
     * </pre>
     * 
     * @param function A recursive function.
     * @return A created function.
     */
    public static <Result> WiseSupplier<Result> recurse(WiseFunction<WiseSupplier<Result>, Result> function) {
        Variable<WiseSupplier<Result>> ref = Variable.empty();
        ref.set(function.bindLazily(ref));
        return ref.v;
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
     * Execute the specified task in the sinobu managed background thread pool.
     *
     * @param task A task to execute.
     * @return A result of the executing task.
     */
    public static CompletableFuture schedule(Runnable task) {
        return schedule(null, task);
    }

    /**
     * Internal API : Execute the task on the specified {@link Executor}.
     * 
     * @param executor A task executor.
     * @param task A task to execute.
     * @return A result of the executing task.
     */
    static CompletableFuture schedule(Executor executor, Runnable task) {
        if (task == null) {
            return CompletableFuture.completedFuture(null);
        }

        return CompletableFuture.runAsync(() -> {
            try {
                task.run();
            } catch (Throwable e) {
                Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
            }
        }, executor == null ? parallel : executor);
    }

    /**
     * Execute the specified task lazily in the sinobu managed background thread pool.
     *
     * @param time A delay time.
     * @param unit A delay time unit.
     * @param task A task to execute.
     * @return A result of the executing task.
     */
    public static CompletableFuture schedule(long time, TimeUnit unit, Runnable task) {
        return schedule(time, unit, null, task);
    }

    /**
     * Internal API : Execute the specified task lazily in the sinobu managed background thread
     * pool.
     *
     * @param time A delay time.
     * @param unit A delay time unit.
     * @param executor A task executor.
     * @param task A task to execute.
     * @return A result of the executing task.
     */
    public static CompletableFuture schedule(long time, TimeUnit unit, ScheduledExecutorService executor, Runnable task) {
        return schedule(time <= 0 ? Runnable::run : t -> (executor == null ? I.scheduler : executor).schedule(t, time, unit), task);
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
    public static <V> Signal<V> signal(V... values) {
        return new Signal<V>((observer, disposer) -> {
            if (disposer.isNotDisposed()) observer.complete();
            return disposer;
        }).startWith(values);
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
            try {
                observer.accept(value.get());
                observer.complete();
            } catch (Throwable e) {
                observer.error(e);
            }
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
        return I.<V> signal().startWith(values);
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
        return I.<V> signal().startWith(values);
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
        return I.<V> signal().startWith(value);
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
        return signal(delayTime, intervalTime, timeUnit, scheduler);
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
    public static Signal<Long> signal(long delayTime, long intervalTime, TimeUnit timeUnit, ScheduledExecutorService scheduler) {
        return I.signal(0L)
                .delay(delayTime, timeUnit, scheduler)
                .recurseMap(s -> s.map(v -> v + 1).delay(intervalTime, timeUnit, scheduler), scheduler);
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
     * Cast from {@link Runnable} to {@link WiseRunnable}.
     * 
     * @param lambda A target function.
     * @return A casted function.
     */
    public static WiseRunnable wiseR(Runnable lambda) {
        return lambda instanceof WiseRunnable ? (WiseRunnable) lambda : lambda::run;
    }

    /**
     * Cast from {@link Consumer} to {@link WiseConsumer}.
     * 
     * @param lambda A target function.
     * @return A casted function.
     */
    public static <P> WiseConsumer<P> wiseC(Consumer<P> lambda) {
        return lambda instanceof WiseConsumer ? (WiseConsumer) lambda : lambda::accept;
    }

    /**
     * Cast from {@link Runnable} to {@link WiseConsumer}. All missing parameters will be added on
     * the right side. All additional caller arguments are ignored.
     * 
     * @param lambda A target function.
     * @return A casted function.
     */
    public static <P> WiseConsumer<P> wiseC(Runnable lambda) {
        return make(null, WiseConsumer.class, I.wiseR(lambda));
    }

    /**
     * Cast from {@link BiConsumer} to {@link WiseBiConsumer}.
     * 
     * @param lambda A target function.
     * @return A casted function.
     */
    public static <P1, P2> WiseBiConsumer<P1, P2> wiseBC(BiConsumer<P1, P2> lambda) {
        return lambda instanceof WiseBiConsumer ? (WiseBiConsumer) lambda : lambda::accept;
    }

    /**
     * Cast from {@link Consumer} to {@link WiseBiConsumer}. All missing parameters will be added on
     * the right side. All additional caller arguments are ignored.
     * 
     * @param lambda A target function.
     * @return A casted function.
     */
    public static <P1, P2> WiseBiConsumer<P1, P2> wiseBC(Consumer<P1> lambda) {
        return make(null, WiseBiConsumer.class, I.wiseC(lambda));
    }

    /**
     * Cast from {@link Runnable} to {@link WiseBiConsumer}. All missing parameters will be added on
     * the right side. All additional caller arguments are ignored.
     * 
     * @param lambda A target function.
     * @return A casted function.
     */
    public static <P1, P2> WiseBiConsumer<P1, P2> wiseBC(Runnable lambda) {
        return make(null, WiseBiConsumer.class, I.wiseR(lambda));
    }

    /**
     * Cast from {@link BiConsumer} to {@link WiseTriConsumer}. All missing parameters will be added
     * on the right side. All additional caller arguments are ignored.
     * 
     * @param lambda A target function.
     * @return A casted function.
     */
    public static <P1, P2, P3> WiseTriConsumer<P1, P2, P3> wiseTC(BiConsumer<P1, P2> lambda) {
        return make(null, WiseTriConsumer.class, I.wiseBC(lambda));
    }

    /**
     * Cast from {@link Consumer} to {@link WiseTriConsumer}. All missing parameters will be added
     * on the right side. All additional caller arguments are ignored.
     * 
     * @param lambda A target function.
     * @return A casted function.
     */
    public static <P1, P2, P3> WiseTriConsumer<P1, P2, P3> wiseTC(Consumer<P1> lambda) {
        return make(null, WiseTriConsumer.class, I.wiseC(lambda));
    }

    /**
     * Cast from {@link Runnable} to {@link WiseTriConsumer}. All missing parameters will be added
     * on the right side. All additional caller arguments are ignored.
     * 
     * @param lambda A target function.
     * @return A casted function.
     */
    public static <P1, P2, P3> WiseTriConsumer<P1, P2, P3> wiseTC(Runnable lambda) {
        return make(null, WiseTriConsumer.class, I.wiseR(lambda));
    }

    /**
     * Cast from {@link Supplier} to {@link WiseSupplier}.
     * 
     * @param lambda A target function.
     * @return A casted function.
     */
    public static <R> WiseSupplier<R> wiseS(Supplier<R> lambda) {
        return lambda instanceof WiseSupplier ? (WiseSupplier) lambda : lambda::get;
    }

    /**
     * Cast from {@link Function} to {@link WiseFunction}.
     * 
     * @param lambda A target function.
     * @return A casted function.
     */
    public static <P, R> WiseFunction<P, R> wiseF(Function<P, R> lambda) {
        return lambda instanceof WiseFunction ? (WiseFunction) lambda : lambda::apply;
    }

    /**
     * Cast from {@link Supplier} to {@link WiseFunction}. All missing parameters will be added on
     * the right side. All additional caller arguments are ignored.
     * 
     * @param lambda A target function.
     * @return A casted function.
     */
    public static <P, R> WiseFunction<P, R> wiseF(Supplier<R> lambda) {
        return make(null, WiseFunction.class, I.wiseS(lambda));
    }

    /**
     * Cast from {@link BiFunction} to {@link WiseBiFunction}.
     * 
     * @param lambda A target function.
     * @return A casted function.
     */
    public static <P1, P2, R> WiseBiFunction<P1, P2, R> wiseBF(BiFunction<P1, P2, R> lambda) {
        return lambda instanceof WiseBiFunction ? (WiseBiFunction) lambda : lambda::apply;
    }

    /**
     * Cast from {@link Function} to {@link WiseBiFunction}. All missing parameters will be added on
     * the right side. All additional caller arguments are ignored.
     * 
     * @param lambda A target function.
     * @return A casted function.
     */
    public static <P1, P2, R> WiseBiFunction<P1, P2, R> wiseBF(Function<P1, R> lambda) {
        return make(null, WiseBiFunction.class, I.wiseF(lambda));
    }

    /**
     * Cast from {@link Supplier} to {@link WiseBiFunction}. All missing parameters will be added on
     * the right side. All additional caller arguments are ignored.
     * 
     * @param lambda A target function.
     * @return A casted function.
     */
    public static <P1, P2, R> WiseBiFunction<P1, P2, R> wiseBF(Supplier<R> lambda) {
        return make(null, WiseBiFunction.class, I.wiseS(lambda));
    }

    /**
     * Cast from {@link BiFunction} to {@link WiseTriFunction}. All missing parameters will be added
     * on the right side. All additional caller arguments are ignored.
     * 
     * @param lambda A target function.
     * @return A casted function.
     */
    public static <P1, P2, P3, R> WiseTriFunction<P1, P2, P3, R> wiseTF(BiFunction<P1, P2, R> lambda) {
        return make(null, WiseTriFunction.class, I.wiseBF(lambda));
    }

    /**
     * Cast from {@link Function} to {@link WiseTriFunction}. All missing parameters will be added
     * on the right side. All additional caller arguments are ignored.
     * 
     * @param lambda A target function.
     * @return A casted function.
     */
    public static <P1, P2, P3, R> WiseTriFunction<P1, P2, P3, R> wiseTF(Function<P1, R> lambda) {
        return make(null, WiseTriFunction.class, I.wiseF(lambda));
    }

    /**
     * Cast from {@link Supplier} to {@link WiseTriFunction}. All missing parameters will be added
     * on the right side. All additional caller arguments are ignored.
     * 
     * @param lambda A target function.
     * @return A casted function.
     */
    public static <P1, P2, P3, R> WiseTriFunction<P1, P2, P3, R> wiseTF(Supplier<R> lambda) {
        return make(null, WiseTriFunction.class, I.wiseS(lambda));
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
    public static XML xml(HttpRequest source) {
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
    static synchronized XML xml(Document doc, Object xml) {
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
            if (input instanceof URL) {
                input = ((URL) input).toURI();
            }

            if (input instanceof URI) {
                URI uri = (URI) input;
                input = uri.getScheme().equals("file") ? new File(uri) : HttpRequest.newBuilder(uri).build();
            }

            if (input instanceof File) {
                input = new FileInputStream((File) input);
            } else if (input instanceof HttpRequest) {
                input = HttpClient.newHttpClient().send((HttpRequest) input, BodyHandlers.ofByteArray()).body();
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
