/*
 * Copyright (C) 2014 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

/**
 * @version 2011/12/19 11:07:07
 */
public class ModuleAwareMapTest {

    @Test
    public void assignable() throws Exception {
        Map<Class, Object> test1 = new HashMap();
        I.aware(test1);

        HashMap<Class, Object> test2 = new HashMap();
        I.aware(test2);

        Map test3 = new HashMap();
        I.aware(test3);

        @SuppressWarnings("unused")
        Map<Method, Object> wrong = new HashMap();
        // I.aware(wrong);
    }
}
