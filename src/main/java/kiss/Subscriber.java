/*
 * Copyright (C) 2020 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Consumer;
import java.util.zip.GZIPInputStream;

/**
 * In order to reduce code size, a variety of less relevant interfaces are implemented in a single
 * class. Fields should only be initialized if they are needed in the constructor. If you initialize
 * a field at the time of its declaration, even unnecessary fields will be initialized.
 */
class Subscriber<T> implements Observer<T>, Disposable, WebSocket.Listener, Storable<Subscriber> {

    /** Generic counter. */
    volatile long index;

    /** Generic list. */
    List<T> list;

    /**
     * {@link Subscriber} must have this constructor only. Dont use instance field initialization to
     * reduce creation cost.
     */
    Subscriber() {
    }

    /** The delegation. */
    Observer observer;

    /** The delegation. */
    Consumer<? super T> next;

    /** The delegation. */
    Consumer<Throwable> error;

    /** The delegation. */
    Runnable complete;

    /** The delegation. */
    Disposable disposer;

    /**
     * {@inheritDoc}
     */
    @Override
    public void complete() {
        if (disposer == null || disposer.isDisposed() == false) {
            if (complete != null) {
                complete.run();
            } else if (observer != null) {
                observer.complete();
            }
        }
        if (disposer != null && index == 1) disposer.dispose();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void error(Throwable e) {
        if (disposer == null || disposer.isDisposed() == false) {
            if (error != null) {
                error.accept(e);
            } else if (observer != null) {
                observer.error(e);
            } else {
                Observer.super.error(e);
            }
        }
        if (disposer != null && index == 1) disposer.dispose();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void accept(T value) {
        // To reduce CPU computation, the termination of the event stream must be confirmed
        // by the caller of the accept method, not by the callee.
        //
        // When the callee confirms the termination, the action is to enumerate all the values and
        // ignore the ones after the termination, but if the caller confirms the termination, the
        // enumeration of the values can be interrupted immediately upon termination.
        //
        // if (disposer == null || disposer.isDisposed() == false) {
        try {
            if (next != null) {
                next.accept(value);
            } else if (observer != null) {
                observer.accept(value);
            }
        } catch (Throwable e) {
            error(e);
        }
        // }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void vandalize() {
    }

    /**
     * Utility to create the specific {@link Signal} for this {@link Subscriber}.
     * 
     * @return
     */
    Signal<T> signal() {
        Signaling<T> signal = new Signaling();
        observer = signal;
        return signal.expose;
    }

    // ======================================================================
    // Anonymous Disposable Instance Manager
    // ======================================================================
    private static Map<Disposable, Subscriber> cache = new WeakHashMap();

    static synchronized Subscriber of(Disposable disposable) {
        if (disposable instanceof Subscriber) {
            return (Subscriber) disposable;
        } else {
            return cache.computeIfAbsent(disposable, k -> new Subscriber());
        }
    }

    // ======================================================================
    // Websocket Listener
    // ======================================================================
    StringBuilder text;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onOpen(WebSocket web) {
        web.request(8);
        next.accept((T) web);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletionStage<?> onText(WebSocket web, CharSequence data, boolean last) {
        web.request(1);
        text.append(data);

        if (last) {
            observer.accept(text.toString());
            text = new StringBuilder();
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletionStage<?> onBinary(WebSocket web, ByteBuffer data, boolean last) {
        try {
            byte[] b = new byte[data.remaining()];
            data.get(b);

            StringBuilder out = new StringBuilder();
            I.copy(new InputStreamReader(new GZIPInputStream(new ByteArrayInputStream(b)), StandardCharsets.UTF_8), out, true);
            return onText(web, out, last);
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletionStage<?> onClose(WebSocket web, int status, String reason) {
        if (status != 1000) {
            observer.error(new Error(reason));
        } else {
            observer.complete();
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onError(WebSocket web, Throwable e) {
        observer.error(e);
    }

    // ======================================================================
    // Resource Bundle
    // ======================================================================
    public Map<String, String> messages;

    /**
     * @param lang An associated language.
     */
    Subscriber(String lang) {
        text = new StringBuilder(lang);
        messages = new ConcurrentSkipListMap();

        restore();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String locate() {
        return I.env("LangDirectory", "lang") + "/" + text + ".json";
    }
}