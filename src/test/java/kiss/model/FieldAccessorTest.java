/*
 * Copyright (C) 2022 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.model;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import kiss.I;
import kiss.json.JSONMappingBenchmark.Person;

public class FieldAccessorTest {

    @Test
    void testName() {
        BiConsumer<Person, String> setter = wise(Person.class, "name", String.class);
        Function<Person, String> getter = (Function) setter;

        System.out.println(setter + "  @@ " + getter);

        Person p = new Person();
        assert p.name == null;
        assert getter.apply(p) == null;

        setter.accept(p, "ok");
        assert p.name.equals("ok");
        assert getter.apply(p).equals("ok");
    }

    @Test
    void primitiveInt() {
        BiConsumer<Person, Integer> setter = wise(Person.class, "age", int.class);
        Function<Person, Integer> getter = (Function) setter;

        System.out.println(setter + "  " + getter);

        Person p = new Person();
        assert p.age == 0;
        assert getter.apply(p) == 0;

        setter.accept(p, 10);
        assert p.age == 10;
        assert getter.apply(p) == 10;
    }

    private static final Map<Class, String[]> primitives = Map
            .of(Integer.class, new String[] {"I", ",8,105,110,116,86,97,108,117,101,1,0,3,40,41,D,12,0,19,0,20,10,0,18,0,21,1",
                    ",7,118,97,108,117,101,79,102,1,0,22,40,D,41,76,P,59,12,0,29,0,30,10,0,18,0,31,1,0"});

    /**
     * Create mixined class.
     * 
     * @return
     */
    public static <A extends Function & BiConsumer> A wise(Class modelClass, String propertyName, Class propertyClass) {
        propertyClass = I.wrap(propertyClass);

        String[] codes = primitives.get(propertyClass);
        if (codes == null) {
            codes = new String[] {"L" + propertyClass.getName() + ";", "", ""};
        }

        String[] raw = "-54,-2,-70,-66,0,0,0,61,0,34,1,0,16,107,105,115,115,47,65,49,53,48,52,53,49,53,52,55,54,7,0,1,1,0,16,106,97,118,97,47,108,97,110,103,47,79,98,106,101,99,116,7,0,3,1,0,27,106,97,118,97,47,117,116,105,108,47,102,117,110,99,116,105,111,110,47,70,117,110,99,116,105,111,110,7,0,5,1,0,29,106,97,118,97,47,117,116,105,108,47,102,117,110,99,116,105,111,110,47,66,105,67,111,110,115,117,109,101,114,7,0,7,1,0,6,60,105,110,105,116,62,1,0,3,40,41,86,12,0,9,0,10,10,0,4,0,11,1,0,6,97,99,99,101,112,116,1,0,39,40,76,106,97,118,97,47,108,97,110,103,47,79,98,106,101,99,116,59,76,106,97,118,97,47,108,97,110,103,47,79,98,106,101,99,116,59,41,86,1,0,37,M,7,0,15,1,0,17,P,7,0,17,1,0U,0,3,N,1,0,1,D,12,0,23,0,24,9,0,16,0,25,1,0,5,97,112,112,108,121,1,0,38,40,76,106,97,118,97,47,108,97,110,103,47,79,98,106,101,99,116,59,41,76,106,97,118,97,47,108,97,110,103,47,79,98,106,101,99,116,59,1,0W,4,67,111,100,101,0,33,0,2,0,4,0,2,0,6,0,8,0,0,0,3,0,1,0,9,0,10,0,1,0,33,0,0,0,17,0,1,0,1,0,0,0,5,42,-D,0,12,-79,0,0,0,0,0,1,0,13,0,14,0,1,0,33,0,0,0,27,0,2,0,3,0,0,0,15,43,-64,0,16,44,-64,0,18,-74,0,22,-75,0,26,-79,0,0,0,0,0,1,0,27,0,28,0,1,0,33,0,0,0,23,0,1,0,2,0,0,0,11,43,-64,0,16,-76,0,26,-72,0,32,-80,0,0,0,0,0,0"

                .replaceAll("M", b(modelClass.getName()))
                .replaceAll("U", codes[1])
                .replaceAll("W", codes[2])
                .replaceAll("P", b(propertyClass.getName()))
                .replaceAll("N", b(propertyName))
                .replaceAll("D", b(codes[0]))
                .split(",");

        byte[] bytes = new byte[raw.length];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = Byte.valueOf(raw[i]);
        }

        try {
            return I.make((Class<A>) MethodHandles.privateLookupIn(I.class, MethodHandles.lookup()).defineClass(bytes));
        } catch (Throwable e) {
            throw I.quiet(e);
        }
    }

    /**
     * Build binary code.
     * 
     * @param name
     * @return
     */
    private static String b(String name) {
        String v = Arrays.toString(name.replace('.', '/').getBytes());
        return v.substring(1, v.length() - 1).replaceAll(" ", "");
    }
}
