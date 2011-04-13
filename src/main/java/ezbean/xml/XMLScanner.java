/*
 * Copyright (C) 2011 Nameless Production Committee.
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
package ezbean.xml;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;

import ezbean.I;

/**
 * <p>
 * This class provides high usability XML manipulating API and high performance which is based on
 * SAX.
 * </p>
 * <p>
 * The following helper method release you from the melancholy which have roots in troublesome
 * arguments of SAX. All of these methods can resolve namespace prefix gracefully.
 * </p>
 * <ul>
 * <li>{@link #start(String, String...)}</li>
 * <li>{@link #start(String, Attributes)}</li>
 * <li>{@link #end()}</li>
 * <li>{@link #text(String)}</li>
 * </ul>
 * <p>
 * if you want to the root element matching pattern (e.g. "/" in XSLT). you should use methods
 * {@link #startDocument()} and {@link #endDocument()}.
 * </p>
 * <p>
 * The following methods ({@link #start(String, Attributes)}, {@link #start(String, String...)},
 * {@link #end()}, {@link #text(String)}) are shorthand of the same effect methods to omit namespace
 * uri and local name of element. You can declare namespaces by using
 * <code>public static final {@link String} field</code> which is prefixed by the value
 * '<dfn>XMLNS</dfn>'. If you want to declare two namespaces (e.g. xmlns="http://namespace/",
 * xmlns:prefix="http://namespace/with/prefix/"), you must define the following fields.
 * </p>
 * 
 * <pre>
 * public static final String XMLNS = &quot;http://namespace/&quot;;
 * 
 * public static final String XMLNS_PREFIX = &quot;http://namespace/with/prefix&quot;;
 * </pre>
 * <p>
 * This declaration automatically adds prefix mapping at the start of the document (using
 * {@link #startPrefixMapping(String, String)} internally). If you want to exclude the prefix
 * mapping from the result document, you can use &quot;transient&quot; modifier like the following.
 * </p>
 * 
 * <pre>
 * public static final transient String XMLNS_PREFIX = &quot;http://namespace/with/prefix&quot;;
 * </pre>
 * 
 * @see Rule
 * @version 2010/05/17 19:07:50
 */
public class XMLScanner extends XMLFilterImpl {

    /** The rule set cache. */
    private static final Map<Class, Object[]> caches = I.aware(new HashMap());

    /** The all compiled rule methods that are defined in the rule class. */
    private final RuleMethod[] methods;

    /** The flag whether we should use rule methods or not. */
    private boolean aware;

    /** The namespace mapping for prefix. */
    private ArrayList<String> prefixes = new ArrayList();

    /** The namespace mapping for uri. */
    private ArrayList<String> uris = new ArrayList();

    /** The set of prefixies to exclude. */
    private HashSet<String> excludes = new HashSet();

    /** The latest context. */
    private RuleContext latest = null;

    /** The context queue. */
    private final ArrayDeque<RuleContext> contexts = new ArrayDeque();

    /** The cursor for element location. */
    private final ArrayList<int[]> paths = new ArrayList();

    /** The element name stack. */
    private final ArrayDeque<String> names = new ArrayDeque();

    /**
     * Create XMLScanner instance.
     */
    public XMLScanner() {
        // find rules cache
        Object[] cache = caches.get(getClass());

        // build new cache for the current processing rule class
        if (cache == null) {
            // resolve namespace declarations and exclude-result-prefixes
            try {
                for (Field field : getClass().getFields()) {
                    // we want to access to fields of inner private class
                    field.setAccessible(true);

                    // cache a name to reduce method invocation
                    String name = field.getName();

                    // namespace declaration
                    if (name.startsWith("XMLNS") && field.getType() == String.class) {
                        prefixes.add(name.length() == 5 ? "" : name.substring(6).toLowerCase());
                        uris.add((String) field.get(null));

                        // transient modifier means that the specified uri is excluded from result
                        if ((Modifier.TRANSIENT & field.getModifiers()) != 0) {
                            excludes.add(uris.get(uris.size() - 1));
                        }
                    }
                }
            } catch (Exception e) {
                throw I.quiet(e);
            }

            // resolve rule methods
            ArrayList<RuleMethod> methods = new ArrayList();

            for (Method method : getClass().getMethods()) {
                Rule rule = method.getAnnotation(Rule.class);

                if (rule != null) {
                    methods.add(new RuleMethod(method, rule, this));
                }
            }

            // sort by priority
            Collections.sort(methods);

            // build cache
            cache = new Object[4];
            cache[0] = methods.toArray(new RuleMethod[methods.size()]);
            cache[1] = excludes;
            cache[2] = prefixes;
            cache[3] = uris;

            // store cache
            caches.put(getClass(), cache);

            // clean up cached prefixes and uris, don't use clear method
            prefixes = new ArrayList();
            uris = new ArrayList();
        }

        // retrieve information from cache
        this.methods = (RuleMethod[]) cache[0];
        this.excludes = (HashSet) cache[1];
        this.prefixes.addAll((ArrayList) cache[2]);
        this.uris.addAll((ArrayList) cache[3]);

        // optimization for making the execution speed faster
        this.aware = methods.length != 0;
    }

    /**
     * @see org.xml.sax.helpers.XMLFilterImpl#startDocument()
     */
    @Override
    public void startDocument() {
        try {
            // delegation
            super.startDocument();

            // Start initial namespace mapping.
            // This loop executes form tail to avoid infinite loop at startPrefixMapping method.
            for (int i = prefixes.size() - 1; -1 < i; i--) {
                startPrefixMapping(prefixes.remove(i), uris.remove(i));
            }
        } catch (SAXException e) {
            throw I.quiet(e);
        }
    }

    /**
     * @see org.xml.sax.helpers.XMLFilterImpl#endDocument()
     */
    @Override
    public void endDocument() {
        try {
            // End initial namespace mapping.
            for (int i = prefixes.size() - 1; -1 < i; i--) {
                endPrefixMapping(prefixes.get(i));
            }

            // delegation
            super.endDocument();
        } catch (SAXException e) {
            throw I.quiet(e);
        }
    }

    /**
     * @see org.xml.sax.helpers.XMLFilterImpl#startPrefixMapping(java.lang.String, java.lang.String)
     */
    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        // Before the current startPrefixMapping event will be recorded, check whether there is the
        // namespace declaration having same prefix and URI beforehand or not.
        int i = prefixes.lastIndexOf(prefix);

        // We must all events demanded by the lower layer event publisher (e.g. XMLFilter,
        // SAXParser) unconditionally to keep consistency with endPrefixMapping event. However, it
        // does not just send all events to the upper layer event consumer.
        prefixes.add(prefix);
        uris.add(uri);

        // If there is namespace declaration having same prefix and URI beforehand, we must not send
        // an event to avoid duplication. In addition, we must not send an event which URI is
        // designated as Exclude Result Prefix either.
        if ((i == -1 || !uris.get(i).equals(uri)) && !excludes.contains(uri)) super.startPrefixMapping(prefix, uri);
    }

    /**
     * @see org.xml.sax.helpers.XMLFilterImpl#endPrefixMapping(java.lang.String)
     */
    @Override
    public void endPrefixMapping(String prefix) throws SAXException {
        // We must all events demanded by the lower layer event publisher (e.g. XMLFilter,
        // SAXParser) unconditionally to keep consistency with startPrefixMapping event. However, it
        // does not just send all events to the upper layer event consumer.
        prefixes.remove(prefixes.size() - 1);
        String uri = uris.remove(uris.size() - 1);

        // After the current endPrefixMapping event was ejected, check whether there is the
        // namespace declaration having same prefix and URI beforehand or not.
        int i = prefixes.lastIndexOf(prefix);

        // If there is namespace declaration having same prefix and URI beforehand, we must not send
        // an event to avoid duplication. In addition, we must not send an event which URI is
        // designated as Exclude Result Prefix either.
        if ((i == -1 || !uris.get(i).equals(uri)) && !excludes.contains(uri)) super.endPrefixMapping(prefix);
    }

    /**
     * @see org.xml.sax.helpers.XMLFilterImpl#startElement(java.lang.String, java.lang.String,
     *      java.lang.String, org.xml.sax.Attributes)
     */
    @Override
    public void startElement(String uri, String local, String name, Attributes atts) throws SAXException {
        if (aware) {
            // move cursor and compile path
            paths.add(new int[] {uri.hashCode(), local.hashCode()});

            // if the context doesn't have bit buffer, it means that we must ignore current event
            if (latest != null && latest.bits == null) {
                return;
            }

            // test rule methods
            for (RuleMethod rule : methods) {
                if (rule.match(paths)) {
                    // store latest context
                    if (latest != null) {
                        contexts.offer(latest);
                    }

                    // start new context
                    latest = new RuleContext(this, rule, atts);
                    latest.start = paths.size() - 1;

                    // invoke context
                    aware = false; // direct call in rule method
                    latest.start();
                    aware = true; // restore

                    // finish
                    return;
                }
            }
        }

        // delegation
        if (latest == null) {
            super.startElement(uri, local, name, atts);
        } else {
            latest.startElement(uri, local, name, atts);
        }
    }

    /**
     * @see org.xml.sax.helpers.XMLFilterImpl#endElement(java.lang.String, java.lang.String,
     *      java.lang.String)
     */
    @Override
    public void endElement(String uri, String local, String name) throws SAXException {
        if (aware) {
            // move cursor
            paths.remove(paths.size() - 1);

            // check latest context
            if (latest != null) {
                if (latest.start == paths.size()) {
                    // dispose latest context
                    aware = false; // direct call in rule method
                    latest.end();
                    aware = true; // restore

                    latest = contexts.pollLast();
                    return;
                }

                if (latest.bits == null) {
                    return;
                }
            }
        }

        // delegation
        if (latest == null) {
            super.endElement(uri, local, name);
        } else {
            latest.endElement(uri, local, name);
        }
    }

    /**
     * @see org.xml.sax.helpers.XMLFilterImpl#characters(char[], int, int)
     */
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        // if the context doesn't have bit buffer, it means that we must ignore current event
        if (!aware || latest == null || latest.bits != null) {
            // delegation
            if (latest == null) {
                super.characters(ch, start, length);
            } else {
                latest.characters(ch, start, length);
            }
        }
    }

    /**
     * Proceeds SAX parser's process.
     * 
     * @throws SAXException
     */
    protected final void proceed() {
        if (!aware) {
            latest.proceed();
        }
    }

    /**
     * <p>
     * Send buffered sax events to the this {@link XMLScanner}.
     * </p>
     * 
     * @param bits An event fragment.
     */
    protected final void include(Bits bits) {
        try {
            for (Object[] bit : bits.bits) {
                switch (bit.length) {
                case 3:
                    endElement((String) bit[0], (String) bit[1], (String) bit[2]);

                    break;

                case 4:
                    startElement((String) bit[0], (String) bit[1], (String) bit[2], (Attributes) bit[3]);
                    break;

                default:
                    text((String) bit[0]);
                }
            }
        } catch (SAXException e) {
            throw I.quiet(e);
        }
    }

    /**
     * <p>
     * Helper method to send the SAX event (start element).
     * </p>
     * <p>
     * This is shorthand for the method {@link #startElement(String, String, String, Attributes)},
     * so you can't use this method in the overirde method of the sub class. If you do so, it will
     * cause {@link StackOverflowError}. You must equate calling this method with calling the method
     * {@link #startElement(String, String, String, Attributes)}.
     * </p>
     * 
     * @param name A qualified name (with prefix).
     * @param atts An attribute.
     * @throws SAXException Any SAX exception, possibly wrapping another exception.
     * @throws StackOverflowError If you use this method in the overirde method
     *             {@link #startElement(String, String, String, Attributes)} of sub class.
     */
    public final void start(String name, Attributes atts) {
        String[] resolved = resolve(name);
        names.add(name);

        try {
            if (latest == null) {
                super.startElement(resolved[0], resolved[1], name, atts);
            } else {
                latest.startElement(resolved[0], resolved[1], name, atts);
            }
        } catch (SAXException e) {
            throw I.quiet(e);
        }
    }

    /**
     * <p>
     * Helper method to send the SAX event (start element).
     * </p>
     * <p>
     * This is shorthand for the method {@link #startElement(String, String, String, Attributes)},
     * so you can't use this method in the overirde method of the sub class. If you do so, it will
     * cause {@link StackOverflowError}. You must equate calling this method with calling the method
     * {@link #startElement(String, String, String, Attributes)}.
     * </p>
     * 
     * @param name A qualified name (with prefix).
     * @param atts A list of name and value pairs.
     * @throws SAXException Any SAX exception, possibly wrapping another exception.
     * @throws StackOverflowError If you use this method in the overirde method
     *             {@link #startElement(String, String, String, Attributes)} of sub class.
     */
    public final void start(String name, String... atts) {
        // create attributes
        AttributesImpl impl = new AttributesImpl();

        // we should ignore the attribute which has no value
        for (int i = 0; i + 1 < atts.length; i += 2) {
            String[] resolved = resolve(atts[i]);
            impl.addAttribute(resolved[0], resolved[1], atts[i], "CDATA", atts[i + 1]);
        }

        // delegation
        start(name, impl);
    }

    /**
     * <p>
     * Helper method to send the SAX event (end element).
     * </p>
     * <p>
     * This is shorthand for the method {@link #endElement(String, String, String)}, so you can't
     * use this method in the overirde method of the sub class. If you do so, it will cause
     * {@link StackOverflowError}. You must equate calling this method with calling the method
     * {@link #endElement(String, String, String)}.
     * </p>
     * 
     * @throws SAXException Any SAX exception, possibly wrapping another exception.
     * @throws StackOverflowError If you use this method in the overirde method
     *             {@link #endElement(String, String, String)} of sub class.
     */
    public final void end() {
        String name = names.pollLast();
        String[] resolved = resolve(name);

        try {
            if (latest == null) {
                super.endElement(resolved[0], resolved[1], name);
            } else {
                latest.endElement(resolved[0], resolved[1], name);
            }
        } catch (SAXException e) {
            throw I.quiet(e);
        }
    }

    /**
     * <p>
     * Hlper method to send the SAX event (characters).
     * </p>
     * <p>
     * This is shorthand for the method {@link #characters(char[], int, int)}, so you can't use this
     * method in the overirde method of the sub class. If you do so, it will cause
     * {@link StackOverflowError}. You must equate calling this method with calling the method
     * {@link #characters(char[], int, int)}.
     * </p>
     * 
     * @param ch A characters.
     * @throws SAXException Any SAX exception, possibly wrapping another exception.
     * @throws StackOverflowError If you use this method in the overirde method
     *             {@link #characters(char[], int, int)} of sub class.
     */
    public final void text(String ch) {
        try {
            if (latest == null) {
                super.characters(ch.toCharArray(), 0, ch.length());
            } else {
                latest.characters(ch.toCharArray(), 0, ch.length());
            }
        } catch (SAXException e) {
            throw I.quiet(e);
        }
    }

    /**
     * <p>
     * Helper method to send the SAX event (start and end element with characters).
     * </p>
     * <p>
     * This is shorthand for the method {@link #startElement(String, String, String, Attributes)},
     * {@link #characters(char[], int, int)} and {@link #endElement(String, String, String)}, so you
     * can't use this method in the overirde method of the sub class. If you do so, it will cause
     * {@link StackOverflowError}. You must equate calling this method with methods
     * {@link #startElement(String, String, String, Attributes)},
     * {@link #characters(char[], int, int)} and {@link #endElement(String, String, String)}.
     * </p>
     * 
     * @param name A qualified name (with prefix).
     * @param atts A list of attributes (name and value pair) and text contents.
     * @throws SAXException Any SAX exception, possibly wrapping another exception.
     * @throws StackOverflowError If you use this method in the overirde method
     *             {@link #startElement(String, String, String, Attributes)},
     *             {@link #characters(char[], int, int)} and
     *             {@link #endElement(String, String, String)} of sub class.
     */
    public final void element(String name, String... atts) {
        int i = atts.length - 1;
        String[] dist = (i & 1) == 0 ? Arrays.copyOf(atts, i) : atts;

        // delegation
        start(name, dist);
        if ((i & 1) == 0) text(atts[i]);
        end();
    }

    /**
     * Resolve namespace and name.
     * 
     * @param name A element name with prefix.
     * @return A resolved names.
     */
    String[] resolve(String name) {
        int i = name.indexOf(':');

        String[] parts = new String[2];
        parts[0] = (i == -1) ? "" : name.substring(0, i);
        parts[1] = name.substring(i + 1);

        // resolve the specified prefix to the namespace uri actually
        i = prefixes.lastIndexOf(parts[0]);

        if (i != -1) {
            parts[0] = uris.get(i);
        }

        // API definition
        return parts;
    }
}
