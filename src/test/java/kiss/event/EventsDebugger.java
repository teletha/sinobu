/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.event;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

import kiss.Disposable;
import kiss.Events;
import kiss.I;

/**
 * @version 2017/03/18 10:55:14
 */
public class EventsDebugger {

    private static final Class Agent;

    private static final Field AgentDisposableList;

    private static final String EVENTS = Events.class.getName();

    /** Ignorable methods. */
    private static final Set<String> ignores = I.set("<init>");

    private static final Map<Events, Integer> eventManager = new ConcurrentHashMap();

    private static int eventId = 0;

    private static final Map<Disposable, Integer> disposerManager = new ConcurrentHashMap();

    private static int disposerId = 0;

    private static final Map<BiFunction, Integer> subscriberManager = new ConcurrentHashMap();

    private static int subscriberId = 0;

    static {
        try {
            Agent = Class.forName("kiss.Agent");
            AgentDisposableList = Agent.getDeclaredField("disposables");
            AgentDisposableList.setAccessible(true);
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }

    /**
     * <p>
     * Dump infomation.
     * </p>
     * 
     * @return
     */
    public static void dump(Object... texts) {
        StringBuilder builder = new StringBuilder();

        for (Object text : texts) {
            if (text instanceof Events) {
                builder.append(info((Events) text));
            } else if (text instanceof Disposable) {
                builder.append(info((Disposable) text));
            } else {
                builder.append(text);
            }
        }
        System.out.println(builder.toString());
    }

    /**
     * <p>
     * Dump infomation.
     * </p>
     * 
     * @return
     */
    private static String info(Events events) {
        return computeName() + " ID : " + id(events) + "  Subscriber : " + id(subscriber(events));
    }

    private static String info(Disposable disposable) {
        return disposer(disposable);
    }

    /**
     * <p>
     * Assign human-readable id.
     * </p>
     * 
     * @param events
     * @return
     */
    private static Integer id(Events events) {
        return eventManager.computeIfAbsent(events, k -> eventId++);
    }

    /**
     * <p>
     * Assign human-readable id.
     * </p>
     * 
     * @param events
     * @return
     */
    private static Integer id(Disposable key) {
        return disposerManager.computeIfAbsent(key, k -> disposerId++);
    }

    /**
     * <p>
     * Assign human-readable id.
     * </p>
     * 
     * @param events
     * @return
     */
    private static Integer id(BiFunction key) {
        return subscriberManager.computeIfAbsent(key, k -> subscriberId++);
    }

    private static String disposer(Disposable disposable) {
        try {
            if (!disposable.getClass().getSimpleName().equals("Agent")) {
                return "Disposer@" + id(disposable);
            } else {
                List list = (List) AgentDisposableList.get(disposable);

                return "Agent@" + id(disposable) + disposer(list);
            }
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }

    private static String disposer(List<Disposable> disposables) {
        StringJoiner joiner = new StringJoiner(", ", "[", "]");
        if (disposables != null) {
            for (Disposable disposable : disposables) {
                joiner.add(disposer(disposable));
            }
        }
        return joiner.toString();
    }

    private static BiFunction subscriber(Events events) {
        try {
            Field field = Events.class.getDeclaredField("subscriber");
            field.setAccessible(true);
            return (BiFunction) field.get(events);
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }

    /**
     * <p>
     * Compute creater method.
     * </p>
     * 
     * @return
     */
    private static String computeName() {
        Error error = new Error();

        for (StackTraceElement e : error.getStackTrace()) {
            if (e.getClassName().equals(EVENTS)) {
                String name = e.getMethodName();
                if (!ignores.contains(name)) {
                    return EVENTS + "." + name + "(" + Events.class.getSimpleName() + ".java:" + e.getLineNumber() + ")";
                }
            }
        }

        // If this exception will be thrown, it is bug of this program. So we must rethrow the
        // wrapped error in here.
        throw error;
    }
}
