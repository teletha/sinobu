/*
 * Copyright (C) 2024 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.core;

import org.junit.jupiter.api.Test;

import kiss.I;
import kiss.Managed;
import kiss.Singleton;

/**
 * @version 2017/04/21 21:09:35
 */
public class MakeCircularReferenceTest {

    @Test
    public void selfCircularReferenceInStaticInitializer() {
        SelfCircularReference instance = I.make(SelfCircularReference.class);
        assert instance != null;
        assert SelfCircularReference.instance != null;
        assert instance != SelfCircularReference.instance;
    }

    /**
     * @version 2016/10/13 9:42:29
     */
    private static class SelfCircularReference {

        static SelfCircularReference instance = I.make(SelfCircularReference.class);
    }

    @Test
    public void singletonSelfCircularReference() {
        SingletonSelfCircularReference instance = I.make(SingletonSelfCircularReference.class);
        assert instance != null;
        assert SingletonSelfCircularReference.instance != null;
        assert instance == SingletonSelfCircularReference.instance;
    }

    /**
     * @version 2016/10/13 9:42:50
     */
    @Managed(value = Singleton.class)
    private static class SingletonSelfCircularReference {

        static SingletonSelfCircularReference instance = I.make(SingletonSelfCircularReference.class);
    }
}