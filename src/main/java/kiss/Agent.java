/*
 * Copyright (C) 2016 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss;

import java.io.IOException;
import java.nio.file.WatchEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

import kiss.model.Model;
import kiss.model.Property;
import kiss.model.PropertyWalker;

/**
 * <p>
 * Versatile wrapper or delegator.
 * </p>
 * 
 * @version 2015/06/24 13:07:48
 */
class Agent<T> implements Observer<T>, WatchEvent, Decoder<Date>, Encoder<Date>, Disposable, PropertyWalker {

    /** For reuse. */
    T object;

    /**
     * {@link Agent} must have this constructor only. Dont use instance field initialization to
     * reduce creation cost.
     */
    Agent() {
    }

    List<Disposable> disposables;

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        if (disposables != null) {
            for (Disposable disposable : disposables) {
                disposable.dispose();
            }
            disposables = null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Disposable and(Disposable next) {
        if (next != null) {
            if (disposables == null) {
                disposables = new ArrayList();
            }

            if (next != this) {
                disposables.add(next);
            }
        }
        return this;
    }

    // ============================================================
    // For Observer
    // ============================================================

    /** The delegation. */
    Observer observer;

    /** The delegation. */
    Consumer<T> next;

    /** The delegation. */
    Consumer<Throwable> error;

    /** The delegation. */
    Runnable complete;

    /**
     * {@inheritDoc}
     */
    @Override
    public void complete() {
        if (complete != null) {
            complete.run();
        } else if (observer != null) {
            observer.complete();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void error(Throwable e) {
        if (error != null) {
            error.accept(e);
        } else if (observer != null) {
            observer.error(e);
        } else {
            Thread.currentThread().getThreadGroup().uncaughtException(Thread.currentThread(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void accept(T value) {
        if (next != null) {
            next.accept(value);
        } else if (observer != null) {
            observer.accept(value);
        }
    }

    // ============================================================
    // For WatchEvent
    // ============================================================

    /** The event holder. */
    WatchEvent watch;

    /**
     * {@inheritDoc}
     */
    @Override
    public Kind kind() {
        return watch.kind();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int count() {
        return watch.count();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T context() {
        return object;
    }

    // ============================================================
    // For XML Desirialization and Bean Conversion
    // ============================================================
    /** The current model. */
    Model model;

    /** The property for xml deserialization process. */
    Property property;

    /** The current location for deserialization process. */
    int index;

    // ============================================================
    // For JSON Serialization
    // ============================================================

    // ============================================================
    // For XML Desirialization and Bean Conversion
    // ============================================================
    /** The charcter sequence for output as JSON. */
    Appendable out;

    /** The format depth. */
    int depth;

    /**
     * {@inheritDoc}
     */
    @Override
    public void walk(Model model, Property property, Object object) {
        if (!property.isTransient) {
            try {
                // check whether this is first property in current context or not.
                if (index++ != 0) {
                    out.append(',');
                }
                indent();

                // write property key (List node doesn't need key)
                if (model.type != List.class) {
                    write(property.name);
                    out.append(": ");
                }

                // write property value
                Class type = property.model.type;

                if (property.isAttribute()) {
                    write(I.transform(object, String.class));
                } else {
                    Agent walker = new Agent();
                    walker.out = out;
                    walker.depth = depth + 1;

                    out.append(type == List.class ? '[' : '{');
                    property.model.walk(object, walker);
                    if (walker.index != 0) indent();
                    out.append(type == List.class ? ']' : '}');
                }
            } catch (IOException e) {
                throw I.quiet(e);
            }
        }
    }

    /**
     * <p>
     * Write JSON literal with quote.
     * </p>
     * 
     * @param value A character sequence.
     * @throws IOException
     */
    private void write(String value) throws IOException {
        out.append('"');

        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);

            switch (c) {
            case '"':
                out.append("\\\"");
                break;

            case '\\':
                out.append("\\\\");
                break;

            case '\b':
                out.append("\\b");
                break;

            case '\f':
                out.append("\\f");
                break;

            case '\n':
                out.append("\\n");
                break;

            case '\r':
                out.append("\\r");
                break;

            case '\t':
                out.append("\\t");
                break;

            default:
                out.append(c);
            }
        }
        out.append('"');
    }

    private void indent() throws IOException {
        out.append("\r\n");

        for (int i = 0; i < depth; i++) {
            out.append('\t');
        }
    }

    /**
     * The date format for W3CDTF. Date formats are not synchronized. It is recommended to create
     * separate format instances for each thread. If multiple threads access a format concurrently,
     * it must be synchronized externally.
     */
    private final static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    /**
     * {@inheritDoc}
     */
    @Override
    public Date decode(String value) {
        try {
            return format.parse(value);
        } catch (Exception e) {
            throw I.quiet(e);
        }
        // return Date.from(LocalDateTime.parse(value).toInstant(ZoneOffset.UTC));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String encode(Date value) {
        return format.format(value);
        // return LocalDateTime.ofInstant(value.toInstant(), ZoneOffset.UTC).toString();
    }
}
