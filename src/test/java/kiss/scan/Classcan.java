/*
 * Copyright (C) 2020 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.scan;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import kiss.I;
import kiss.Signal;

public class Classcan {

    /**
     * 
     */
    private Classcan() {
    }

    /**
     * Retrieves a list of subclasses of the given class.
     * <p/>
     * <p>
     * The class must be annotated with {@link Indexable} for it's subclasses to be indexed at
     * compile-time by {@link ClassIndexProcessor}.
     * </p>
     *
     * @param superClass class to find subclasses for
     * @return list of subclasses
     */
    public static <T> Signal<Class<T>> find(Class<T> superClass) {
        return find(superClass, Thread.currentThread().getContextClassLoader());
    }

    /**
     * Retrieves a list of subclasses of the given class.
     * <p/>
     * <p>
     * The class must be annotated with {@link Indexable} for it's subclasses to be indexed at
     * compile-time by {@link ClassIndexProcessor}.
     * </p>
     *
     * @param type class to find subclasses for
     * @param loader classloader for loading classes
     * @return list of subclasses
     */
    public static <T> Signal<Class<T>> find(Class<T> type, ClassLoader loader) {
        return I.signal("META-INF/services/" + type.getName()).flatEnum(loader::getResources).flatIterable(url -> {
            List<String> lines = new ArrayList();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    lines.add(line);
                }
            } catch (IOException e) {
                // ignore
            }
            return lines;
        }).map(name -> (Class<T>) Class.forName(name, false, loader));
    }
}