/*
 * Copyright (C) 2021 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.lang.System.LoggerFinder;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.HttpRetryException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.net.http.WebSocket;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;
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
 * example, if you want to receive initialization callbacks, it is better to use constructor.
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
    public static final WiseRunnable NoOP = new Subscriber()::vandalize;

    /** The default language in this vm environment. */
    public static final Variable<String> Lang = Variable.of(Locale.getDefault().getLanguage());

    /** The automatic saver references. */
    static final WeakHashMap<Object, Disposable> autosaver = new WeakHashMap();

    /** The circularity dependency graph per thread. */
    static final ThreadLocal<Deque<Class>> dependencies = ThreadLocal.withInitial(ArrayDeque::new);

    /** The document builder. */
    static final DocumentBuilder dom;

    /** The xpath evaluator. */
    static final XPath xpath;

    /** The logger manager. */
    private static final Map<String, Subscriber> logs = new ConcurrentHashMap<>();

    /** The cache for {@link Lifestyle}. */
    private static final Map<Class, Lifestyle> lifestyles = new ConcurrentHashMap<>();

    /** The definitions of extensions. */
    private static final Map<Class, Ⅱ> extensions = new ConcurrentHashMap<>();

    /** The sequential execution queue for IO-intensive processing. */
    private static final ArrayBlockingQueue<WiseRunnable> tasks = new ArrayBlockingQueue(256);

    /** The parallel task scheduler. */
    static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5, run -> {
        Thread t = new Thread(run);
        t.setName("Sinobu Scheduler");
        t.setDaemon(true);
        return t;
    });

    /** The list of built-in primitive and wrapper classes. */
    private static final Class[] types = {boolean.class, int.class, long.class, float.class, double.class, char.class, byte.class,
            short.class, void.class, Boolean.class, Integer.class, Long.class, Float.class, Double.class, Character.class, Byte.class,
            Short.class, Void.class};

    /** XML literal pattern. */
    private static final Pattern xmlLiteral = Pattern.compile("^\\s*<.+>\\s*$", Pattern.DOTALL);

    /** The cached environment variables. */
    private static final Properties env = new Properties();

    /** The expression placeholder syntax. */
    private static final Pattern express = Pattern.compile("([\\r\\n\s]*)\\{(.+?)\\}");

    /** The reusable http client. */
    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .followRedirects(Redirect.ALWAYS)
            .build();

    // initialization
    static {
        // built-in lifestyles
        lifestyles.put(List.class, ArrayList::new);
        lifestyles.put(Map.class, HashMap::new);
        lifestyles.put(Set.class, HashSet::new);
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
            if (type.isEnum()) return (Encoder<Enum>) Enum::name;
            switch (type.getName().hashCode()) {
            case -530663260: // java.lang.Class
                return (Encoder<Class>) Class::getName;
            default:
                return String::valueOf;
            }
        });

        // built-in decoders
        load(ExtensionFactory.class, Decoder.class, () -> (ExtensionFactory<Decoder>) type -> {
            if (type.isEnum()) return value -> Enum.valueOf((Class<Enum>) type, value);
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
            env.load(Files.newBufferedReader(Path.of(".env")));
        } catch (Exception e) {
            // ignore
        }
        env.putAll(System.getenv());

        // start sequential task executor
        I.schedule((WiseRunnable) () -> {
            while (true) {
                tasks.take().RUN();
            }
        });
    }

    /**
     * Initialize environment.
     */
    private I() {
    }

    /**
     * Create a new {@link Predicate} which accepts any item. It will conform to any type except
     * primitive types depending on the context of the caller.
     * 
     * @param <A> Don't worry about this type as it is automatically determined.
     * @return A new created {@link Predicate} function that always returns <code>true</code>.
     */
    public static <A> Predicate<A> accept() {
        return p -> true;
    }

    /**
     * Create a new {@link BiPredicate} which accepts any item. It will conform to any type except
     * primitive types depending on the context of the caller.
     * 
     * @param <A> Don't worry about this type as it is automatically determined.
     * @param <B> Don't worry about this type as it is automatically determined.
     * @return A new created {@link BiPredicate} function that always returns <code>true</code>.
     */
    public static <A, B> BiPredicate<A, B> accepţ() {
        return (p, q) -> true;
    }

    /**
     * Creates a new array whose elements are the concatenated elements of the two given arrays.
     * 
     * @param <T> Array element types.
     * @param one Array to be concatenated at the top.
     * @param other Array to be concatenated at the end.
     * @return Newly created array with concatenated elements.
     */
    public static <T> T[] array(T[] one, T... other) {
        if (one == null)
            return other == null ? null : other;
        else if (other == null) return one;

        T[] all = Arrays.copyOf(one, one.length + other.length);
        System.arraycopy(other, 0, all, one.length, other.length);
        return all;
    }

    /**
     * Create a new bundled implementation of the interface common to the given objects. Calling a
     * method of the retrieved implementation object will transparently call the same method on all
     * the given objects. In the case of a method with a return value, the result of the last
     * object's call will be used.
     * <p>
     * In situations where the compiler cannot estimate the type of the common interface, use
     * {@link I#bundle(Class, Object...)} instead, which can specify the type.
     * </p>
     * 
     * @param <T> Interface type.
     * @param items A set of objects that implement a common interface.
     * @return A bundled interface.
     * @throws IllegalArgumentException When the compiler cannot estimate the type of the common
     *             interface.
     */
    public static <T> T bundle(T... items) {
        return bundle((Class<T>) items.getClass().getComponentType(), items);
    }

    /**
     * Create a new bundled implementation of the interface common to the given objects. Calling a
     * method of the retrieved implementation object will transparently call the same method on all
     * the given objects. In the case of a method with a return value, the result of the last
     * object's call will be used.
     * <p>
     * In situations where the compiler cannot estimate the type of the common interface, use
     * {@link I#bundle(Class, Iterable)} instead, which can specify the type.
     * </p>
     * 
     * @param <T> Interface type.
     * @param items A set of objects that implement a common interface.
     * @return A bundled interface.
     * @throws IllegalArgumentException When the compiler cannot estimate the type of the common
     *             interface.
     */
    public static <T> T bundle(Iterable<? extends T> items) {
        Set<Class> types = null;
        Iterator<? extends T> iterator = items.iterator();

        if (iterator.hasNext()) {
            types = Model.collectTypes(iterator.next().getClass());
            types.removeIf(v -> !v.isInterface());

            while (iterator.hasNext())
                types.retainAll(Model.collectTypes(iterator.next().getClass()));
        }
        return bundle((Class<T>) (types == null || types.isEmpty() ? null : types.iterator().next()), items);
    }

    /**
     * Create a new bundled implementation of the interface common to the given objects. Calling a
     * method of the retrieved implementation object will transparently call the same method on all
     * the given objects. In the case of a method with a return value, the result of the last
     * object's call will be used.
     * 
     * @param <T> Interface type.
     * @param type Specify the common interfaces.
     * @param items A set of objects that implement a common interface.
     * @return A bundled interface.
     */
    public static <T> T bundle(Class<T> type, T... items) {
        return bundle(type, Arrays.asList(items));
    }

    /**
     * Create a new bundled implementation of the interface common to the given objects. Calling a
     * method of the retrieved implementation object will transparently call the same method on all
     * the given objects. In the case of a method with a return value, the result of the last
     * object's call will be used.
     * 
     * @param <T> Interface type.
     * @param type Specify the common interfaces.
     * @param items A set of objects that implement a common interface.
     * @return A bundled interface.
     */
    public static <T> T bundle(Class<T> type, Iterable<? extends T> items) {
        return make(type, (proxy, method, args) -> {
            Object result = null;

            if (items != null) for (Object fun : items)
                if (fun != null) try {
                    result = method.invoke(fun, args);
                } catch (InvocationTargetException e) {
                    throw e.getCause();
                }
            return result;
        });
    }

    /**
     * Create a new {@link Collection} to hold the specified items. Basically, the type of the
     * collection should be a real class, but if it is a {@link List} or {@link Set}, the default
     * implementation class ({@link ArrayList} , {@link HashSet}) will be used.
     * 
     * @param <T> Specify the concrete class which implements the {@link Collection}, but the
     *            {@link List} and {@link Set} interfaces may be specified as exceptions.
     * @param <V> The type of the {@link Collection}'s items.
     * @param type A {@link Collection} type.
     * @param items A list of itmes.
     * @return The new created {@link Collection}.
     * @see #list(Object...)
     * @see #set(Object...)
     */
    public static <T extends Collection<V>, V> T collect(Class<T> type, V... items) {
        T collection = I.make(type);

        if (items != null) collection.addAll(Arrays.asList(items));
        return collection;
    }

    /**
     * Copies data from {@link InputStream} to {@link OutputStream}. This method does the data
     * buffering internally, so you do not need to do the buffering explicitly.
     *
     * @param input {@link InputStream} to which data will be read from.
     * @param output {@link OutputStream} to which data will be write to.
     * @param close Whether input and output streams will be closed automatically or not.
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
     * Copies data from {@link Readable} to {@link Appendable}. This method does the data buffering
     * internally, so you do not need to do the buffering explicitly.
     *
     * @param input {@link Readable} to which data will be read from.
     * @param output {@link Appendable} to which data will be write to.
     * @param close Whether input and output streams will be closed automatically or not.
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
     * Write {@link java.lang.System.Logger.Level#DEBUG} log.
     * 
     * @param msg A message log.
     */
    public static void debug(Object msg) {
        log("system", Level.DEBUG, msg);
    }

    /**
     * Write {@link java.lang.System.Logger.Level#DEBUG} log.
     * 
     * @param name A logger name by {@link Class#getSimpleName()}.
     * @param msg A message log.
     */
    public static void debug(String name, Object msg) {
        log(name, Level.DEBUG, msg);
    }

    /**
     * Read environment variables based on the following priorities (sources higher in the list take
     * precedence over those located lower).
     * <ol>
     * <li>{@link System#getenv(String)}</li>
     * <li>.env property file in current working directory (optional)</li>
     * <li>.env property file on the classpath (optional)</li>
     * </ol>
     * 
     * @param name A environment variable name.
     * @return The value of the environment variable with the specified name, or <code>null</code>
     *         if it does not exist.
     */
    public static String env(String name) {
        return env.getProperty(name);
    }

    /**
     * Read environment variables based on the following priorities (sources higher in the list take
     * precedence over those located lower). If the environment variable with the specified name
     * does not exist, the value specified as the default value will be set as the new environment
     * variable and used.
     * <ol>
     * <li>{@link System#getenv(String)}</li>
     * <li>.env property file in current working directory (optional)</li>
     * <li>.env property file on the classpath (optional)</li>
     * </ol>
     * 
     * @param name A environment variable name.
     * @param defaults If the specified name is not found, set and return this default value.
     * @return The value of the environment variable with the specified name.
     */
    public static <T> T env(String name, T defaults) {
        T value = I.transform(env.getProperty(name), (Class<T>) defaults.getClass());
        if (value == null) env.setProperty(name, I.transform(value = defaults, String.class));
        return value;
    }

    /**
     * Write {@link java.lang.System.Logger.Level#ERROR} log.
     * 
     * @param msg A message log.
     */
    public static void error(Object msg) {
        log("system", Level.ERROR, msg);
    }

    /**
     * Write {@link java.lang.System.Logger.Level#ERROR} log.
     * 
     * @param name A logger name by {@link Class#getSimpleName()}.
     * @param msg A message log.
     */
    public static void error(String name, Object msg) {
        log(name, Level.ERROR, msg);
    }

    /**
     * It is a very simple template engine that can calculate a string that replaces the path of a
     * property names enclosed in "{}" with the actual value of the property. Support
     * <a href="https://mustache.github.io/mustache.5.html">Mustache Syntax</a> partially.
     * 
     * @param text A text with the path of the property names enclosed in "{}".
     * @param contexts A list of context values.
     * @return A calculated text.
     */
    public static String express(String text, Object... contexts) {
        return express(text, contexts, new WiseTriFunction[0]);
    }

    /**
     * It is a very simple template engine that can calculate a string that replaces the path of a
     * property names enclosed in "{}" with the actual value of the property. Support
     * <a href="https://mustache.github.io/mustache.5.html">Mustache Syntax</a> partially.
     * 
     * @param text A text with the path of the property names enclosed in "{}".
     * @param contexts A list of context values.
     * @return A calculated text.
     */
    public static String express(String text, Object[] contexts, WiseTriFunction<Model, Object, String, Object>... resolvers) {
        // skip when context is empty
        if (contexts == null || contexts.length == 0) return text;

        StringBuilder str = new StringBuilder();

        // find all expression placeholder
        Matcher matcher = express.matcher(text);

        nextPlaceholder: while (matcher.find()) {
            // normalize expression (remove all white space) and split it
            String spaces = matcher.group(1);
            String path = matcher.group(2).trim();
            char type = path.charAt(0);
            if (type == '!') {
                matcher.appendReplacement(str, "");
                continue;
            }
            if (type == '#' || type == '^') path = path.substring(1);
            String[] e = path.split("[\\.\\s　]+");

            // evaluate each model (first model has high priority)
            nextContext: for (int i = 0; i < contexts.length; i++) {
                Object c = contexts[i];
                Model m = Model.of(c);

                // evaluate expression from head
                for (int j = 0; j < e.length; j++) {
                    // special keyword for the current context
                    if (e[j].equals("this")) continue;

                    // evaluate expression by property named resolver
                    Object o = m.get(c, e[j]);
                    if (o == null) {
                        // evaluate expression by user defined resolvers
                        for (int k = 0; k < resolvers.length; k++) {
                            o = resolvers[k].apply(m, c, e[j]);
                            if (o != null) {
                                break;
                            }
                        }

                        // any resolver can't find suitable value, try to next context
                        if (o == null) continue nextContext;
                    }

                    // step into the next expression
                    m = Model.of(c = o);
                }

                // handle special sections
                if (type == '#' || type == '^') {
                    // skip the nested sections
                    int count = 1;
                    int end = 0;
                    Matcher tag = Pattern.compile("(\\{[#/^]" + path + "\\})").matcher(text.substring(matcher.end()));
                    while (tag.find()) {
                        count += tag.group().charAt(1) == '/' ? -1 : 1;
                        if (count == 0) {
                            end = matcher.end() + tag.start();
                            break;
                        }
                    }

                    // extract the target section
                    String sec = text.substring(matcher.end(), end).trim();

                    matcher.appendReplacement(str, "");
                    if ((c == Boolean.TRUE && type == '#') || (type == '^' && (c == Boolean.FALSE || (c instanceof List && ((List) c)
                            .isEmpty()) || (c instanceof Map && ((Map) c).isEmpty()))))
                        str.append(spaces).append(I.express(sec, c, resolvers));
                    else if (type == '#') m.walk(c, (x, p, o) -> str.append(spaces).append(I.express(sec, new Object[] {o}, resolvers)));
                    matcher.reset(text = text.substring(end + 3 + path.length()));
                } else {
                    // full expression was evaluated correctly, convert it to string
                    matcher.appendReplacement(str, spaces.concat(I.transform(c, String.class)));
                }

                continue nextPlaceholder;
            }

            // any context can't find suitable value, so use empty text
            matcher.appendReplacement(str, spaces);
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
     * @throws NullPointerException If the extension point is null.
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
            Ⅱ<List<Class<E>>, Map<Class, Lifestyle<E>>> extensions = findBy(extensionPoint);

            // In the majority of cases, a search query for an extension uses the extension key
            // itself, and rarely a subclass of the extension key is used. Since it is very costly
            // to obtain all types of extension key, we try to save computation resource by
            // performing a searc with the specified extension key at the beginning.
            Lifestyle<E> lifestyle = extensions.ⅱ.get(key);
            if (lifestyle != null) return lifestyle.get();

            // search from extension factory
            if (extensionPoint != ExtensionFactory.class) {
                ExtensionFactory<E> factory = find(ExtensionFactory.class, extensionPoint);
                if (factory != null) return factory.create(key);
            }

            // Since a search query using the extension key itself did not find any extensions, we
            // extend the search by using the ancestor classes and interfaces of the extension key.
            for (Class type : Model.collectTypes(key)) {
                lifestyle = extensions.ⅱ.get(type);

                if (lifestyle != null) return lifestyle.get();
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
     * @throws NullPointerException If the extension point is null.
     */
    private static <E extends Extensible> Ⅱ<List<Class<E>>, Map<Class, Lifestyle<E>>> findBy(Class<E> extensionPoint) {
        return extensions.computeIfAbsent(extensionPoint, p -> pair(new CopyOnWriteArrayList(), new ConcurrentHashMap()));
    }

    /**
     * Gets the response from the specified URL (including https), converting it to the specified
     * type. Supported types are as follows:
     * <ul>
     * <li>{@link String}</li>
     * <li>{@link InputStream}</li>
     * <li>{@link HttpResponse}</li>
     * <li>{@link XML}</li>
     * <li>{@link JSON}</li>
     * <li>Any types that can be mapped from JSON</li>
     * </ul>
     * <p>
     * It will check the Content-Encoding header and automatically decompress the body if it is
     * compressed with gzip or deflate. HTTP communication by this method is done asynchronously. It
     * is possible to process it synchronously by calling the return value's
     * {@link Signal#waitForTerminate()}.
     * </p>
     * 
     * @param <T> {@link String}, {@link InputStream}, {@link HttpResponse}, {@link XML}, or your
     *            bean class
     * @param request Request URI.
     * @param type Response handler. ({@link String}, {@link InputStream}, {@link HttpResponse},
     *            {@link XML}, or your bean class)
     * @return If the request is successful, the content will be sent. If the request is
     *         unsuccessful, an error will be sent.
     * @throws NullPointerException When one of the arguments is null.
     */
    public static <T> Signal<T> http(String request, Class<T> type, HttpClient... client) {
        return http(HttpRequest.newBuilder(URI.create(request)), type, client);
    }

    /**
     * Gets the response from the specified URL (including https), converting it to the specified
     * type. Supported types are as follows:
     * <ul>
     * <li>{@link String}</li>
     * <li>{@link InputStream}</li>
     * <li>{@link HttpResponse}</li>
     * <li>{@link XML}</li>
     * <li>{@link JSON}</li>
     * <li>Any types that can be mapped from JSON</li>
     * </ul>
     * <p>
     * It will check the Content-Encoding header and automatically decompress the body if it is
     * compressed with gzip or deflate. HTTP communication by this method is done asynchronously. It
     * is possible to process it synchronously by calling the return value's
     * {@link Signal#waitForTerminate()}.
     * </p>
     * 
     * @param <T> {@link String}, {@link InputStream}, {@link HttpResponse}, {@link XML}, or your
     *            bean class
     * @param request Request builder.
     * @param type Response handler. ({@link String}, {@link InputStream}, {@link HttpResponse},
     *            {@link XML}, or your bean class)
     * @return If the request is successful, the content will be sent. If the request is
     *         unsuccessful, an error will be sent.
     * @throws NullPointerException When one of the arguments is null.
     */
    public static <T> Signal<T> http(HttpRequest.Builder request, Class<T> type, HttpClient... client) {
        return new Signal<>((observer, disposer) -> {
            return disposer.add(I.signal(client)
                    .to()
                    .or(I.client)
                    .sendAsync(request.build(), BodyHandlers.ofInputStream())
                    .whenComplete((res, e) -> {
                        if (e == null) try {
                            if (res.statusCode() < 400) {
                                InputStream in = res.body();

                                // =============================================
                                // Decoding Phase
                                // =============================================
                                List<String> encodings = res.headers().allValues("Content-Encoding");
                                if (encodings.contains("gzip"))
                                    in = new GZIPInputStream(in);
                                else if (encodings.contains("deflate")) in = new InflaterInputStream(in);

                                // =============================================
                                // Materializing Phase
                                // =============================================
                                T v = (T) (type == String.class ? new String(in.readAllBytes(), StandardCharsets.UTF_8)
                                        : type == InputStream.class ? in
                                                : type == HttpResponse.class ? res : type == XML.class ? I.xml(in) : I.json(in).as(type));

                                // =============================================
                                // Signaling Phase
                                // =============================================
                                observer.accept(v);
                                observer.complete();
                                return;
                            } else
                                e = new HttpRetryException(new String(res.body().readAllBytes(), StandardCharsets.UTF_8), res
                                        .statusCode(), res.uri().toString());
                        } catch (Exception x) {
                            e = x; // fall-through to error handling
                        }
                        observer.error(e);
                    }));
        });
    }

    /**
     * Connect to the specified URI by Websocket. The status of the communication is transmitted to
     * {@link Signal}. Once the connection is established, it performs a 'open' callback.
     * 
     * @param uri URI to connect.
     * @param open Called only once, when a connection is established.
     * @return Communication status.
     * @throws NullPointerException if uri or client is null.
     */
    public static Signal<String> http(String uri, Consumer<WebSocket> open, HttpClient... client) {
        return new Signal<>((observer, disposer) -> {
            Subscriber sub = new Subscriber();
            sub.observer = observer;
            sub.disposer = disposer;
            sub.text = new StringBuilder();
            sub.next = open;

            return disposer.add(I.signal(client)
                    .to()
                    .or(I.client)
                    .newWebSocketBuilder()
                    .connectTimeout(Duration.ofSeconds(15))
                    .buildAsync(URI.create(uri), sub)
                    .whenComplete((ok, e) -> {
                        if (e != null) observer.error(e);
                    }));
        });
    }

    /**
     * Write {@link java.lang.System.Logger.Level#INFO} log.
     * 
     * @param msg A message log.
     */
    public static void info(Object msg) {
        log("system", Level.INFO, msg);
    }

    /**
     * Write {@link java.lang.System.Logger.Level#INFO} log.
     * 
     * @param name A logger name by {@link Class#getSimpleName()}.
     * @param msg A message log.
     */
    public static void info(String name, Object msg) {
        log(name, Level.INFO, msg);
    }

    /**
     * Returns a string containing the string representation of each of items, using the specified
     * separator between each.
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
     * Returns a string containing the string representation of each of items, using the specified
     * separator between each.
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
     * Parse the specified JSON format text.
     * 
     * @param input A json format text. <code>null</code> will throw {@link NullPointerException}.
     *            The empty or invalid format data will throw {@link IllegalStateException}.
     * @return A parsed {@link JSON}.
     * @throws NullPointerException If the input data or the root Java object is <code>null</code>.
     * @throws IllegalStateException If the input data is empty or invalid format.
     */
    public static JSON json(String input) {
        return json(new StringReader(input));
    }

    /**
     * Parse the specified JSON format text.
     * 
     * @param input A json format text. <code>null</code> will throw {@link NullPointerException}.
     *            The empty or invalid format data will throw {@link IllegalStateException}.
     * @return A parsed {@link JSON}.
     * @throws NullPointerException If the input data or the root Java object is <code>null</code>.
     * @throws IllegalStateException If the input data is empty or invalid format.
     */
    public static JSON json(Path input) {
        try {
            return json(Files.newBufferedReader(input));
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }

    /**
     * Parse the specified JSON format text.
     * 
     * @param input A json format text. <code>null</code> will throw {@link NullPointerException}.
     *            The empty or invalid format data will throw {@link IllegalStateException}.
     * @return A parsed {@link JSON}.
     * @throws NullPointerException If the input data or the root Java object is <code>null</code>.
     * @throws IllegalStateException If the input data is empty or invalid format.
     */
    public static JSON json(InputStream input) {
        return json(new InputStreamReader(input, StandardCharsets.UTF_8));
    }

    /**
     * Parse the specified JSON format text.
     * 
     * @param input A json format text. <code>null</code> will throw {@link NullPointerException}.
     *            The empty or invalid format data will throw {@link IllegalStateException}.
     * @return A parsed {@link JSON}.
     * @throws NullPointerException If the input data or the root Java object is <code>null</code>.
     * @throws IllegalStateException If the input data is empty or invalid format.
     */
    public static JSON json(Reader input) {
        try {
            return new JSON(input);
        } catch (Exception e) {
            throw I.quiet(e);
        } finally {
            I.quiet(input);
        }
    }

    /**
     * Create {@link ArrayList} with the specified items.
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
    @SuppressWarnings("resource")
    private static Disposable load(URL source, String pattern, ClassLoader loader) {
        // =======================================
        // List up extension class names
        // =======================================
        Signal<String> names;

        try {
            // Scan at runtime
            File file = new File(source.toURI());

            if (file.isFile())
                // from jar file
                names = I.signal(new ZipFile(file).entries()).map(entry -> entry.getName().replace('/', '.'));
            else {
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

        for (String name : names.toSet())
            // exclude out of the specified package
            if (name.startsWith(pattern)) try {
                disposer.add(loadE((Class) loader.loadClass(name)));
            } catch (Throwable e) {
                // ignore
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
        if (extension.isEnum() || extension.isAnonymousClass()) return null;

        // slow check : exclude non-extensible class
        if (!Extensible.class.isAssignableFrom(extension)) return null;

        Disposable disposer = Disposable.empty();

        // search and collect information for all extension points
        for (Class<E> extensionPoint : Model.collectTypes(extension))
            if (Arrays.asList(extensionPoint.getInterfaces()).contains(Extensible.class)) {
                // register as new extension
                Ⅱ<List<Class<E>>, Map<Class, Lifestyle<E>>> extensions = findBy(extensionPoint);

                // exclude duplication
                if (extensions.ⅰ.contains(extension)) return null;

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
        return disposer;
    }

    /**
     * Register extension with key.
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

    private static Path locate(String name, LocalDate day) throws Exception {
        Path p = Path.of(".log");
        Files.createDirectories(p);
        return p.resolve(Objects.requireNonNullElse(name, "system") + day.format(DateTimeFormatter.BASIC_ISO_DATE) + ".log");
    }

    /** The configuration for log level. */
    public static DateTimeFormatter LogFormat = DateTimeFormatter.ofPattern(I.env("LogFormat", "yyyy-MM-dd HH:mm:ss.SSS"));

    /**
     * Configure whether to include logging caller infomation in the log (default:
     * {@link Level#OFF}). It can also be set during application initialization through the .env
     * file with 'LogCaller' key. Note that turning on this setting will increase the logging
     * process time extremely.
     * 
     * @see I#env(String)
     * @see I#env(String, Object)
     */
    public static Level LogCaller = I.env("LogCaller", Level.OFF);

    /**
     * Configure whether to output the log to the system console (default: {@link Level#INFO}). It
     * can also be set during application initialization through the .env file with 'LogConsole'
     * key. If you turn off the both file and console output, all logs will be routed to the
     * platform logger.
     * 
     * @see I#LogFile
     * @see I#env(String)
     * @see I#env(String, Object)
     * @see Logger
     * @see LoggerFinder
     */
    public static Level LogConsole = I.env("LogConsole", Level.INFO);

    /**
     * Configure whether to output the log to the rotatable local file (default: {@link Level#ALL}).
     * It can also be set during application initialization through the .env file with 'LogFile'
     * key. If you turn off the both file and console output, all logs will be routed to the
     * platform logger.
     * 
     * @see I#LogConsole
     * @see I#env(String)
     * @see I#env(String, Object)
     * @see Logger
     * @see LoggerFinder
     */
    public static Level LogFile = I.env("LogFile", Level.ALL);

    /**
     * Configure whether to create a new file or append to an existing file when logging to a local
     * file (default: true). It can also be set during application initialization through the .env
     * file with 'LogAppend' key.
     * 
     * @see I#env(String)
     * @see I#env(String, Object)
     */
    public static boolean LogAppend = I.env("LogAppend", true);

    private static long lastTime;

    private static String lastTimeFormat = "";

    /**
     * Generic logging helper.
     * 
     * @param name
     * @param level
     * @param msg
     */
    private static void log(String name, Level level, Object msg) {
        int o = level.ordinal();

        if (LogFile.ordinal() <= o || LogConsole.ordinal() <= o) {
            long mills = System.currentTimeMillis();
            StackTraceElement e = LogCaller.ordinal() <= o
                    ? StackWalker.getInstance().walk(s -> s.skip(2).findFirst().get().toStackTraceElement())
                    : null;

            try {
                tasks.put(() -> {
                    // lookup logger by the simple class name
                    Subscriber log = logs.computeIfAbsent(name, key -> {
                        Subscriber v = new Subscriber();
                        v.index = Instant.now().truncatedTo(ChronoUnit.DAYS).toEpochMilli();
                        return v;
                    });

                    if (LogFile.ordinal() <= o && log.index <= mills) {
                        // stop old
                        I.quiet(log.writer);

                        // start new
                        LocalDate day = LocalDate.now();

                        log.writer = new BufferedWriter(new FileWriter(locate(name, day).toFile(), LogAppend));
                        log.index += 24 * 60 * 60 * 1000;

                        // delete oldest
                        day = day.minusDays(30);
                        while (Files.deleteIfExists(locate(name, day))) {
                            day = day.minusDays(1);
                        }
                    }

                    if (mills != lastTime) {
                        lastTime = mills;
                        lastTimeFormat = Instant.ofEpochMilli(mills).atZone(ZoneId.systemDefault()).format(LogFormat);
                    }

                    StringBuilder text = new StringBuilder(lastTimeFormat).append(' ').append(level).append('\t').append(msg);
                    if (e != null) {
                        text.append('\t')
                                .append(e.getClassName())
                                .append('#')
                                .append(e.getMethodName())
                                .append(':')
                                .append(e.getLineNumber());
                    }
                    text.append('\n');

                    if (msg instanceof Throwable) {
                        Stream.of(((Throwable) msg).getStackTrace()).map(StackTraceElement::toString).forEach(text::append);
                    }

                    if (LogFile.ordinal() <= o) log.writer.append(text);
                    if (LogConsole.ordinal() <= o) System.out.append(text);
                });
            } catch (Exception x) {
                throw I.quiet(x);
            }
        } else {
            System.getLogger(name).log(level, msg);
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
     * @param <T> A model type.
     * @param modelClass A target class to create instance.
     * @return A instance of the specified model class. This instance is managed by Sinobu.
     * @throws NullPointerException If the model class is <code>null</code>.
     * @throws IllegalArgumentException If the model class is non-accessible or final class.
     * @throws UnsupportedOperationException If the model class is inner-class.
     * @throws ClassCircularityError If the model has circular dependency.
     */
    public static <T> T make(Class<? extends T> modelClass) {
        return makeLifestyle(modelClass).get();
    }

    /**
     * Create proxy instance.
     * 
     * @param type A model type.
     * @param handler A proxy handler.
     * @return
     */
    public static <T> T make(Class<T> type, InvocationHandler handler) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(handler);

        if (type.isInterface() == false) throw new IllegalArgumentException("Type must be interface.");
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

        if (type instanceof ParameterizedType) type = ((ParameterizedType) type).getRawType();

        if (type == WiseRunnable.class)
            return (F) (WiseRunnable) handler::invoke;
        else if (type == WiseSupplier.class)
            return (F) (WiseSupplier) handler::invoke;
        else if (type == WiseConsumer.class)
            return (F) (WiseConsumer) handler::invoke;
        else if (type == WiseFunction.class)
            return (F) (WiseFunction) handler::invoke;
        else if (type == WiseBiConsumer.class)
            return (F) (WiseBiConsumer) handler::invoke;
        else if (type == WiseBiFunction.class)
            return (F) (WiseBiFunction) handler::invoke;
        else if (type == WiseTriConsumer.class)
            return (F) (WiseTriConsumer) handler::invoke;
        else
            return (F) (WiseTriFunction) handler::invoke;
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
     * @throws UnsupportedOperationException If the model class is anonymous-class.
     * @throws ClassCircularityError If the model has circular dependency.
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
        if (modelClass.isAnonymousClass()) throw new UnsupportedOperationException(modelClass + " is  inner class.");

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
                if (managed == null || managed.value() == Lifestyle.class)
                    lifestyle = I.prototype(modelClass);
                else
                    lifestyle = I.make(managed.value());
            }

            if (lifestyles.containsKey(modelClass))
                return lifestyles.get(modelClass);
            else {
                lifestyles.put(modelClass, lifestyle);
                return lifestyle;
            }
        } finally {
            dependency.pollLast();
        }
    }

    /**
     * Create value set.
     *
     * @param param1 A first parameter.
     * @param param2 A second parameter.
     * @return
     */
    public static <A, B> Ⅱ<A, B> pair(A param1, B param2) {
        return new Ⅱ(param1, param2);
    }

    /**
     * Create value set.
     *
     * @param param1 A first parameter.
     * @param param2 A second parameter.
     * @param param3 A third parameter.
     * @return
     */
    public static <A, B, C> Ⅲ<A, B, C> pair(A param1, B param2, C param3) {
        return new Ⅲ(param1, param2, param3);
    }

    /**
     * Build prototype-like {@link Lifestyle} that creates a new instance every time demanded. This
     * is default lifestyle in Sinobu.
     * <p>
     * The created {@link Lifestyle} has the functionality of Dependency Injection. The Lifestyle
     * attempts to create an instance using the first constructor declared with the fewest number of
     * arguments. If the argument contains a {@link Managed} type, an instance of that type will
     * also be created automatically. This dependency injection is done at the same time when the
     * model is instantiated. But if you want to delay the creation of the dependency until it is
     * needed, you can set the argument type to Lifestyle<DEPENDENCY_TYPE>.
     * <p>
     * You may also specify a {@link Class} type as an argument if you need the currently processing
     * model type. This feature is mainly available when implementing the special generic
     * {@link Lifestyle}.
     * 
     * @param <M> A {@link Managed} class.
     * @param model A model type.
     * @return A built {@link Lifestyle} that creates a new instance every time demanded.
     * @see Singleton
     */
    public static <M> Lifestyle<M> prototype(Class<M> model) {
        // find default constructor as instantiator
        Constructor constructor = Model.collectConstructors(model)[0];

        // We can safely call the method 'newInstance()' because the generated class has
        // only one public constructor without arguments. But we should make this
        // instantiator accessible because it makes the creation speed faster.
        constructor.setAccessible(true);

        return () -> {
            Class[] types = constructor.getParameterTypes();

            // constructor injection
            Object[] params = null;

            // We should use lazy initialization of parameter array to avoid that the constructor
            // without parameters doesn't create futile array instance.
            if (types.length != 0) {
                params = new Object[types.length];

                for (int i = 0; i < params.length; i++)
                    if (types[i] == Lifestyle.class)
                        params[i] = I.makeLifestyle((Class) Model
                                .collectParameters(constructor.getGenericParameterTypes()[i], Lifestyle.class)[0]);
                    else if (types[i] == Class.class)
                        params[i] = I.dependencies.get().peekLast();
                    else if (types[i].isPrimitive())
                        params[i] = Array.get(Array.newInstance(types[i], 1), 0);
                    else
                        params[i] = I.make(types[i]);
            }
            // create new instance
            return (M) constructor.newInstance(params);
        };
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

        if (object instanceof AutoCloseable) try {
            ((AutoCloseable) object).close();
        } catch (Exception e) {
            throw quiet(e);
        }

        // API definition
        return null;
    }

    /**
     * Deceive complier that the specified checked exception is unchecked exception.
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
     * Define recursive {@link BiConsumer}.
     * </p>
     * <pre>
     * I.recurse((self, param1, param2) -> {
     *     // your function code
     * });
     * </pre>
     * 
     * @param function A recursive function.
     * @return A created function.
     */
    public static <A, B> WiseBiConsumer<A, B> recurse(WiseTriConsumer<WiseBiConsumer<A, B>, A, B> function) {
        Variable<WiseBiConsumer<A, B>> ref = Variable.empty();
        ref.set(function.bindLazily(ref));
        return ref.v;
    }

    /**
     * <p>
     * Define recursive {@link BiFunction}.
     * </p>
     * <pre>
     * I.recurse((self, param1, param2) -> {
     *     // your function code
     * });
     * </pre>
     * 
     * @param function A recursive function.
     * @return A created function.
     */
    public static <A, B, R> WiseBiFunction<A, B, R> recurse(WiseTriFunction<WiseBiFunction<A, B, R>, A, B, R> function) {
        Variable<WiseBiFunction<A, B, R>> ref = Variable.empty();
        ref.set(function.bindLazily(ref));
        return ref.v;
    }

    /**
     * <p>
     * Define recursive {@link Consumer}.
     * </p>
     * <pre>
     * I.recurse((self, param) -> {
     *     // your function code
     * });
     * </pre>
     * 
     * @param function A target function to convert.
     * @return A converted recursive function.
     */
    public static <A> WiseConsumer<A> recurse(WiseBiConsumer<WiseConsumer<A>, A> function) {
        Variable<WiseConsumer<A>> ref = Variable.empty();
        ref.set(function.bindLazily(ref));
        return ref.v;
    }

    /**
     * <p>
     * Define recursive {@link Function}.
     * </p>
     * <pre>
     * I.recurse((self, param) -> {
     *     // your function code
     * });
     * </pre>
     * 
     * @param function A recursive function.
     * @return A created function.
     */
    public static <A, R> WiseFunction<A, R> recurse(WiseBiFunction<WiseFunction<A, R>, A, R> function) {
        Variable<WiseFunction<A, R>> ref = Variable.empty();
        ref.set(function.bindLazily(ref));
        return ref.v;
    }

    /**
     * <p>
     * Define recursive {@link Runnable}.
     * </p>
     * <pre>
     * I.recurse(self -> {
     *     // your function code
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
     *     // your function code
     * });
     * </pre>
     * 
     * @param function A recursive function.
     * @return A created function.
     */
    public static <R> WiseSupplier<R> recurse(WiseFunction<WiseSupplier<R>, R> function) {
        Variable<WiseSupplier<R>> ref = Variable.empty();
        ref.set(function.bindLazily(ref));
        return ref.v;
    }

    /**
     * Create a new {@link Predicate} which rejects any item. It will conform to any type except
     * primitive types depending on the context of the caller.
     * 
     * @param <A> Don't worry about this type as it is automatically determined.
     * @return A new created {@link Predicate} function that always returns <code>false</code>.
     */
    public static <A> Predicate<A> reject() {
        return p -> false;
    }

    /**
     * Create a new {@link BiPredicate} which rejects any item. It will conform to any type except
     * primitive types depending on the context of the caller.
     * 
     * @param <A> Don't worry about this type as it is automatically determined.
     * @param <B> Don't worry about this type as it is automatically determined.
     * @return A new created {@link BiPredicate} function that always returns <code>false</code>.
     */
    public static <A, B> BiPredicate<A, B> rejecţ() {
        return (p, q) -> false;
    }

    /**
     * Execute the specified task in the sinobu managed background thread pool.
     *
     * @param task A task to execute.
     * @return A result of the executing task.
     * @see #schedule(long, TimeUnit)
     * @see #schedule(long, TimeUnit, ScheduledExecutorService)
     * @see #schedule(long, long, TimeUnit, boolean)
     * @see #schedule(long, long, TimeUnit, boolean, ScheduledExecutorService)
     */
    public static CompletableFuture<?> schedule(Runnable task) {
        return CompletableFuture.runAsync(task, scheduler);
    }

    /**
     * Returns an {@link Signal} that emits a {@code 1L} after the {@code delayTime}.
     *
     * @param delayTime The delay time to wait before emitting the first value of 1L
     * @param unit the time unit for {@code delayTime}
     * @return {@link Signal} that emits a {@code 1L} after the {@code delayTime}
     */
    public static Signal<Long> schedule(long delayTime, TimeUnit unit) {
        return schedule(delayTime, unit, (ScheduledExecutorService) null);
    }

    /**
     * Returns an {@link Signal} that emits a {@code 1L} after the {@code delayTime}.
     *
     * @param delayTime The delay time to wait before emitting the first value of 1L
     * @param unit The time unit for {@code delayTime}
     * @param scheduler The task scheduler.
     * @return {@link Signal} that emits a {@code 1L} after the {@code delayTime}
     */
    public static Signal<Long> schedule(long delayTime, TimeUnit unit, ScheduledExecutorService scheduler) {
        return schedule(delayTime, -1, unit, false, scheduler).take(1);
    }

    /**
     * Returns an {@link Signal} that emits a {@code 1L} after the {@code delayTime} and ever
     * increasing numbers after each {@code intervalTime} of time thereafter.
     * 
     * @param delayTime The initial delay time to wait before emitting the first value of 1L
     * @param intervalTime The period of time between emissions of the subsequent numbers
     * @param unit the time unit for both {@code delayTime} and {@code intervalTime}
     * @return {@link Signal} that emits a 1L after the {@code delayTime} and ever increasing
     *         numbers after each {@code intervalTime} of time thereafter
     */
    public static Signal<Long> schedule(long delayTime, long intervalTime, TimeUnit unit, boolean fixedRate) {
        return schedule(delayTime, intervalTime, unit, fixedRate, null);
    }

    /**
     * Returns an {@link Signal} that emits a {@code 1L} after the {@code delayTime} and ever
     * increasing numbers after each {@code intervalTime} of time thereafter.
     * 
     * @param delayTime The initial delay time to wait before emitting the first value of 1L
     * @param intervalTime The period of time between emissions of the subsequent numbers
     * @param unit the time unit for both {@code delayTime} and {@code intervalTime}
     * @return {@link Signal} that emits a 1L after the {@code delayTime} and ever increasing
     *         numbers after each {@code intervalTime} of time thereafter
     */
    public static Signal<Long> schedule(long delayTime, long intervalTime, TimeUnit unit, boolean fixedRate, ScheduledExecutorService scheduler) {
        return schedule(() -> delayTime, intervalTime, unit, fixedRate, scheduler);
    }

    /**
     * Returns an {@link Signal} that emits a {@code 1L} after the {@code delayTime} and ever
     * increasing numbers after each {@code intervalTime} of time thereafter.
     * 
     * @param delayTime The initial delay time to wait before emitting the first value of 1L
     * @param intervalTime The period of time between emissions of the subsequent numbers
     * @param unit the time unit for both {@code delayTime} and {@code intervalTime}
     * @return {@link Signal} that emits a 1L after the {@code delayTime} and ever increasing
     *         numbers after each {@code intervalTime} of time thereafter
     */
    private static Signal<Long> schedule(LongSupplier delayTime, long intervalTime, TimeUnit unit, boolean fixedRate, ScheduledExecutorService scheduler) {
        Objects.requireNonNull(unit);

        return new Signal<>((observer, disposer) -> {
            long delay = delayTime.getAsLong();
            Runnable task = I.wiseC(observer).bindLast(null);
            Future future;
            ScheduledExecutorService exe = scheduler == null ? I.scheduler : scheduler;

            if (intervalTime <= 0) {
                if (delay <= 0)
                    future = CompletableFuture.runAsync(task, Runnable::run);
                else
                    future = exe.schedule(task, delay, unit);
            } else if (fixedRate)
                future = exe.scheduleAtFixedRate(task, delay, intervalTime, unit);
            else
                future = exe.scheduleWithFixedDelay(task, delay, intervalTime, unit);
            return disposer.add(future);
        }).count();
    }

    /**
     * Create a time-based periodic executable scheduler. It will be executed at regular intervals
     * starting from a specified base time. (For example, if the base time is 00:05 and the interval
     * is 30 minutes, the actual execution time will be 00:05, 00:30, 01:05, 01:35, and so on.
     * 
     * @param time The base time.
     * @param interval The period of time between emissions of the subsequent numbers
     * @param unit The interval time unit.
     * @return {@link Signal} that emits a 1L at the {@code time} and ever increasing numbers after
     *         each {@code interval} of time thereafter
     */
    public static Signal<Long> schedule(LocalTime time, long interval, TimeUnit unit) {
        return schedule(time, interval, unit, null);
    }

    /**
     * Create a time-based periodic executable scheduler. It will be executed at regular intervals
     * starting from a specified base time. (For example, if the base time is 00:05 and the interval
     * is 30 minutes, the actual execution time will be 00:05, 00:30, 01:05, 01:35, and so on.
     * 
     * @param time The base time.
     * @param interval The period of time between emissions of the subsequent numbers
     * @param unit The interval time unit.
     * @param scheduler The task scheduler.
     * @return {@link Signal} that emits a 1L at the {@code time} and ever increasing numbers after
     *         each {@code interval} of time thereafter
     */
    public static Signal<Long> schedule(LocalTime time, long interval, TimeUnit unit, ScheduledExecutorService scheduler) {
        return schedule(() -> {
            long now = System.currentTimeMillis();
            long start = now / 86400000 * 86400000 + time.toNanoOfDay() / 1000000;
            while (start < now)
                start += unit.toMillis(interval);
            return start - now;
        }, unit.toMillis(interval), TimeUnit.MILLISECONDS, true, scheduler);
    }

    /**
     * Create {@link HashSet} with the specified items.
     * 
     * @param items A list of itmes.
     * @return The new created {@link HashSet}.
     */
    public static <V> Set<V> set(V... items) {
        return collect(HashSet.class, items);
    }

    /**
     * Signal the specified values.
     *
     * @param values A list of values to emit.
     * @return The {@link Signal} to emit sequencial values.
     */
    public static <V> Signal<V> signal(V... values) {
        return new Signal<V>((observer, disposer) -> {
            if (!disposer.isDisposed()) observer.complete();
            return disposer;
        }).startWith(values);
    }

    /**
     * Signal the specified values.
     *
     * @param values A list of values to emit.
     * @return The {@link Signal} to emit sequencial values.
     */
    public static <V> Signal<V> signal(Iterable<V> values) {
        return I.<V> signal().startWith(values);
    }

    /**
     * Signal the specified values.
     *
     * @param values A list of values to emit.
     * @return The {@link Signal} to emit sequencial values.
     */
    public static <V> Signal<V> signal(Enumeration<V> values) {
        return I.<V> signal().startWith(values);
    }

    /**
     * {@link Signal} the specified values.
     *
     * @param value A value to emit.
     * @return The {@link Signal} to emit sequencial values.
     */
    public static <V> Signal<V> signal(Supplier<V> value) {
        return I.<V> signal().startWith(value);
    }

    /**
     * Returns an {@link Signal} that invokes an {@link Observer#error(Throwable)} method when the
     * {@link Observer} subscribes to it.
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
     * Transform any type object into the specified type if possible.
     *
     * @param <In> A input type you want to transform from.
     * @param <Out> An output type you want to transform into.
     * @param input A target object.
     * @param output A target type.
     * @return A transformed object.
     * @throws NullPointerException If the output type is <code>null</code>.
     */
    public static <In, Out> Out transform(In input, Class<Out> output) {
        if (input == null) return null;

        String encoded = input instanceof String ? (String) input : find(Encoder.class, input.getClass()).encode(input);

        if (output == String.class) return (Out) encoded;

        // support abstract enum
        if (output.isAnonymousClass()) {
            Class parent = output.getEnclosingClass();
            if (parent.isEnum()) output = parent;
        }

        Decoder<Out> out = I.find(Decoder.class, output);
        return out == null ? I.make(output) : out.decode(encoded);
    }

    /**
     * The text will be automatically translated. Basic sentences must be written in English. It
     * will be translated online automatically into the language specified in the global variable
     * {@link #Lang}. Once the text is translated, it is saved to the local disk and loaded from
     * there in the future.
     * 
     * @param text Basic English sentences.
     * @param context Parameters to be assigned to variables in a sentence. (Optional)
     */
    public static Variable<String> translate(String text, Object... context) {
        Variable<String> t = Variable.empty();
        I.Lang.observing().switchMap(lang -> {
            // First, check inline cache.
            if ("en".equals(lang)) return I.signal(text);

            // The next step is to check for already translated text from
            // the locally stored bundle files. Iit can help reduce translationresources.
            Subscriber<?> bundle = bundles.computeIfAbsent(lang, Subscriber::new);
            String cached = bundle.messages.get(text);
            if (cached != null) return I.signal(cached);

            // Perform the translation online.
            // TODO We do not want to make more than one request at the same time,
            // so we have certain intervals.
            return I.http(HttpRequest.newBuilder()
                    .uri(URI.create("https://www.ibm.com/demos/live/watson-language-translator/api/translate/text"))
                    .header("Content-Type", "application/json")
                    .POST(BodyPublishers.ofString("{\"text\":\"" + text
                            .replaceAll("[\\n|\\r]+", " ") + "\",\"source\":\"en\",\"target\":\"" + lang + "\"}")), JSON.class)
                    .flatIterable(v -> v.find(String.class, "payload", "translations", "0", "translation"))
                    .skipNull()
                    .map(v -> {
                        bundle.messages.put(text, v);
                        translate.accept(bundle);
                        return v;
                    });
        }).startWith(text).to(v -> {
            t.set(I.express(v, I.list(context)));
        });
        return t;
    }

    /** In-memory cache for dynamic bundles. */
    static final Map<String, Subscriber> bundles = new ConcurrentHashMap();

    /** Coordinator of bundle save timing */
    static final Signaling<Subscriber> translate = new Signaling();

    static {
        // Automatic translation is often done multiple times in a short period of time, and
        // it is not efficient to save the translation results every time you get them, so
        // it is necessary to process them in batches over a period of time.
        translate.expose.debounce(1, TimeUnit.MINUTES).to(Subscriber::store);
    }

    /**
     * Find the class by the specified fully qualified class name.
     *
     * @param fqcn A fully qualified class name to want.
     * @return The specified class.
     */
    public static Class type(String fqcn) {
        if (fqcn.indexOf('.') == -1) for (int i = 0; i < 9; i++)
            if (types[i].getName().equals(fqcn)) return types[i];

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
    public static <A> WiseConsumer<A> wiseC(Consumer<A> lambda) {
        return lambda instanceof WiseConsumer ? (WiseConsumer) lambda : lambda::accept;
    }

    /**
     * Cast from {@link Runnable} to {@link WiseConsumer}. All missing parameters will be added on
     * the right side. All additional caller arguments are ignored.
     * 
     * @param lambda A target function.
     * @return A casted function.
     */
    public static <A> WiseConsumer<A> wiseC(Runnable lambda) {
        return make(null, WiseConsumer.class, I.wiseR(lambda));
    }

    /**
     * Cast from {@link BiConsumer} to {@link WiseBiConsumer}.
     * 
     * @param <A> Any type for first parameter.
     * @param <B> Any type for second parameter.
     * @param lambda A target function.
     * @return A casted function.
     */
    public static <A, B> WiseBiConsumer<A, B> wiseBC(BiConsumer<A, B> lambda) {
        return lambda instanceof WiseBiConsumer ? (WiseBiConsumer) lambda : lambda::accept;
    }

    /**
     * Cast from {@link Consumer} to {@link WiseBiConsumer}. All missing parameters will be added on
     * the right side. All additional caller arguments are ignored.
     * 
     * @param lambda A target function.
     * @return A casted function.
     */
    public static <A, B> WiseBiConsumer<A, B> wiseBC(Consumer<A> lambda) {
        return make(null, WiseBiConsumer.class, I.wiseC(lambda));
    }

    /**
     * Cast from {@link Runnable} to {@link WiseBiConsumer}. All missing parameters will be added on
     * the right side. All additional caller arguments are ignored.
     * 
     * @param lambda A target function.
     * @return A casted function.
     */
    public static <A, B> WiseBiConsumer<A, B> wiseBC(Runnable lambda) {
        return make(null, WiseBiConsumer.class, I.wiseR(lambda));
    }

    /**
     * Cast from {@link BiConsumer} to {@link WiseTriConsumer}. All missing parameters will be added
     * on the right side. All additional caller arguments are ignored.
     * 
     * @param lambda A target function.
     * @return A casted function.
     */
    public static <A, B, C> WiseTriConsumer<A, B, C> wiseTC(BiConsumer<A, B> lambda) {
        return make(null, WiseTriConsumer.class, I.wiseBC(lambda));
    }

    /**
     * Cast from {@link Consumer} to {@link WiseTriConsumer}. All missing parameters will be added
     * on the right side. All additional caller arguments are ignored.
     * 
     * @param lambda A target function.
     * @return A casted function.
     */
    public static <A, B, C> WiseTriConsumer<A, B, C> wiseTC(Consumer<A> lambda) {
        return make(null, WiseTriConsumer.class, I.wiseC(lambda));
    }

    /**
     * Cast from {@link Runnable} to {@link WiseTriConsumer}. All missing parameters will be added
     * on the right side. All additional caller arguments are ignored.
     * 
     * @param lambda A target function.
     * @return A casted function.
     */
    public static <A, B, C> WiseTriConsumer<A, B, C> wiseTC(Runnable lambda) {
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
    public static <A, R> WiseFunction<A, R> wiseF(Function<A, R> lambda) {
        return lambda instanceof WiseFunction ? (WiseFunction) lambda : lambda::apply;
    }

    /**
     * Cast from {@link Supplier} to {@link WiseFunction}. All missing parameters will be added on
     * the right side. All additional caller arguments are ignored.
     * 
     * @param lambda A target function.
     * @return A casted function.
     */
    public static <A, R> WiseFunction<A, R> wiseF(Supplier<R> lambda) {
        return make(null, WiseFunction.class, I.wiseS(lambda));
    }

    /**
     * Cast from {@link BiFunction} to {@link WiseBiFunction}.
     * 
     * @param lambda A target function.
     * @return A casted function.
     */
    public static <A, B, R> WiseBiFunction<A, B, R> wiseBF(BiFunction<A, B, R> lambda) {
        return lambda instanceof WiseBiFunction ? (WiseBiFunction) lambda : lambda::apply;
    }

    /**
     * Cast from {@link Function} to {@link WiseBiFunction}. All missing parameters will be added on
     * the right side. All additional caller arguments are ignored.
     * 
     * @param lambda A target function.
     * @return A casted function.
     */
    public static <A, B, R> WiseBiFunction<A, B, R> wiseBF(Function<A, R> lambda) {
        return make(null, WiseBiFunction.class, I.wiseF(lambda));
    }

    /**
     * Cast from {@link Supplier} to {@link WiseBiFunction}. All missing parameters will be added on
     * the right side. All additional caller arguments are ignored.
     * 
     * @param lambda A target function.
     * @return A casted function.
     */
    public static <A, B, R> WiseBiFunction<A, B, R> wiseBF(Supplier<R> lambda) {
        return make(null, WiseBiFunction.class, I.wiseS(lambda));
    }

    /**
     * Cast from {@link BiFunction} to {@link WiseTriFunction}. All missing parameters will be added
     * on the right side. All additional caller arguments are ignored.
     * 
     * @param lambda A target function.
     * @return A casted function.
     */
    public static <A, B, C, R> WiseTriFunction<A, B, C, R> wiseTF(BiFunction<A, B, R> lambda) {
        return make(null, WiseTriFunction.class, I.wiseBF(lambda));
    }

    /**
     * Cast from {@link Function} to {@link WiseTriFunction}. All missing parameters will be added
     * on the right side. All additional caller arguments are ignored.
     * 
     * @param lambda A target function.
     * @return A casted function.
     */
    public static <A, B, C, R> WiseTriFunction<A, B, C, R> wiseTF(Function<A, R> lambda) {
        return make(null, WiseTriFunction.class, I.wiseF(lambda));
    }

    /**
     * Cast from {@link Supplier} to {@link WiseTriFunction}. All missing parameters will be added
     * on the right side. All additional caller arguments are ignored.
     * 
     * @param lambda A target function.
     * @return A casted function.
     */
    public static <A, B, C, R> WiseTriFunction<A, B, C, R> wiseTF(Supplier<R> lambda) {
        return make(null, WiseTriFunction.class, I.wiseS(lambda));
    }

    /**
     * Return a non-primitive {@link Class} of the specified {@link Class} object. <code>null</code>
     * will be return <code>null</code>.
     *
     * @param type A {@link Class} object to convert to non-primitive class.
     * @return A non-primitive {@link Class} object.
     */
    public static Class wrap(Class type) {
        if (type == null) return Object.class;

        if (type.isPrimitive()) // check primitive classes
            for (int i = 0; i < 9; i++)
            if (types[i] == type) return types[i + 9];

        // the specified class is not primitive
        return type;
    }

    /**
     * Write JSON representation of Java object.
     *
     * @param input A Java object. All properties will be serialized deeply. <code>null</code> will
     *            throw {@link java.lang.NullPointerException}.
     * @return A JSON representation of Java object.
     * @throws NullPointerException If the input Java object or the output is <code>null</code> .
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
     */
    public static void write(Object input, Appendable out) {
        Objects.requireNonNull(out);

        try {
            // traverse object as json
            Model model = Model.of(input);
            new JSON(out).write(model, new Property(model, "", null), input);
        } finally {
            // close carefuly
            I.quiet(out);
        }
    }

    /**
     * Parse the specified XML format text.
     *
     * @param input Text of xml representation.
     * @return A constructed {@link XML}.
     * @throws NullPointerException If the input data is <code>null</code>.
     * @throws IllegalStateException If the input data is empty or invalid format.
     */
    public static XML xml(String input) {
        return I.xml(null, input);
    }

    /**
     * Parse the specified XML format text.
     *
     * @param input Path to the XML file.
     * @return A constructed {@link XML}.
     * @throws NullPointerException If the input data is <code>null</code>.
     * @throws IllegalStateException If the input data is empty or invalid format.
     */
    public static XML xml(Path input) {
        try {
            return xml(null, Files.readAllBytes(input));
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }

    /**
     * Parse the specified XML format text.
     *
     * @param input Text stream of xml representation.
     * @return A constructed {@link XML}.
     * @throws NullPointerException If the input data is <code>null</code>.
     * @throws IllegalStateException If the input data is empty or invalid format.
     */
    public static XML xml(InputStream input) {
        try {
            return xml(null, input.readAllBytes());
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }

    /**
     * Parse the specified XML format text.
     *
     * @param input Text stream of xml representation.
     * @return A constructed {@link XML}.
     * @throws NullPointerException If the input data is <code>null</code>.
     * @throws IllegalStateException If the input data is empty or invalid format.
     */
    public static XML xml(Reader input) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        I.copy(input, new OutputStreamWriter(out, StandardCharsets.UTF_8), true);
        return I.xml(null, out.toByteArray());
    }

    /**
     * Parse the specified XML format text.
     *
     * @param input A xml expression.
     * @return A constructed {@link XML}.
     * @throws NullPointerException If the input data is <code>null</code>.
     */
    public static XML xml(Node input) {
        return I.xml(null, input);
    }

    /**
     * Parse the specified XML format text.
     *
     * @param xml A xml expression.
     * @return A constructed {@link XML}.
     */
    static synchronized XML xml(Document doc, Object xml) {
        try {
            // XML related types
            if (xml instanceof XML)
                return (XML) xml;
            else if (xml instanceof Node) return new XML(((Node) xml).getOwnerDocument(), list(xml));

            // byte data types
            byte[] bytes = xml instanceof String ? ((String) xml).getBytes(StandardCharsets.UTF_8) : (byte[]) xml;
            if (6 < bytes.length && bytes[0] == '<') // doctype declaration (starts with <! )
                // root element is html (starts with <html> )
                if (bytes[1] == '!' || (bytes[1] == 'h' && bytes[2] == 't' && bytes[3] == 'm' && bytes[4] == 'l' && bytes[5] == '>'))
                    return new XML(null, null).parse(bytes, StandardCharsets.UTF_8);

            String value = new String(bytes, StandardCharsets.UTF_8);

            if (xmlLiteral.matcher(value).matches()) {
                doc = dom.parse(new InputSource(new StringReader("<m>".concat(value.replaceAll("<\\?.+\\?>", "")).concat("</m>"))));
                return new XML(doc, XML.convert(doc.getFirstChild().getChildNodes()));
            } else
                return xml(doc != null ? doc.createTextNode(value) : dom.newDocument().createElement(value));
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }
}