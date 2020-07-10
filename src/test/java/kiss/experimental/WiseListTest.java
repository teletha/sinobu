/*
 * Copyright (C) 2020 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.experimental;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.jupiter.api.Test;

import kiss.I;

public class WiseListTest {

    /** The mixined class holder. */
    private static final Map<String, Class> wised = new ConcurrentHashMap();

    /**
     * Create mixined class.
     * 
     * @param <MIX>
     * @param base A base implementation.
     * @param mixin A mixin interface.
     * @return
     */
    public static <MIX> MIX wise(Class base, Class<MIX> mixin) {
        return (MIX) I.make(wised.computeIfAbsent(base.getName() + mixin.getName(), k -> {
            try {
                String[] raw = "-54,-2,-70,-66,0,0,0,58,0,12,1,0,U,7,0,1,1,0,S,7,0,3,1,0,I,7,0,5,1,0,6,60,105,110,105,116,62,1,0,3,40,41,86,12,0,7,0,8,10,0,4,0,9,1,0,4,67,111,100,101,0,1,0,2,0,4,0,1,0,6,0,0,0,1,0,1,0,7,0,8,0,1,0,11,0,0,0,17,0,2,0,1,0,0,0,5,42,-73,0,10,-79,0,0,0,0,0,0"
                        .replace("U", b("kiss/experimental/".concat(base.getName().replace('.', '_'))))
                        .replace("S", b(base.getName()))
                        .replace("I", b(mixin.getName()))
                        .split(",");
                byte[] bytes = new byte[raw.length];
                for (int i = 0; i < bytes.length; i++) {
                    bytes[i] = Byte.valueOf(raw[i]);
                }
                return (Class<MIX>) MethodHandles.lookup().defineClass(bytes);
            } catch (Exception e) {
                throw I.quiet(e);
            }
        }));
    }

    /**
     * Build binary code.
     * 
     * @param name
     * @return
     */
    private static String b(String name) {
        String v = Arrays.toString(name.replace('.', '/').getBytes());
        return name.length() + "," + v.substring(1, v.length() - 1).replaceAll(" ", "");
    }

    @Test
    void peekFirst() {
        WiseList<Integer> list = wise(ArrayList.class, WiseList.class);
        assert list.first().isAbsent();

        list.add(1);
        assert list.first().isPresent();
    }

    @Test
    void select() {
        WiseList<Integer> list = wise(ArrayList.class, WiseList.class);
        list.addAll(Arrays.asList(1, 2, 3, 4, 5));

        WiseList<Integer> selected = list.take(v -> v % 2 == 0);
        assert selected.size() == 2;
        assert selected instanceof ArrayList;
    }
}