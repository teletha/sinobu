/*
 * Copyright (C) 2016 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.core;

import org.junit.Test;

import kiss.I;
import kiss.Manageable;
import kiss.Singleton;

/**
 * @version 2016/10/13 9:41:51
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
    @Manageable(lifestyle = Singleton.class)
    private static class SingletonSelfCircularReference {

        static SingletonSelfCircularReference instance = I.make(SingletonSelfCircularReference.class);
    }

    @Test(expected = ClassCircularityError.class)
    public void selfCircularReferenceInConstructor() {
        I.make(SelfCircularReferenceInConstructor.class);
    }

    /**
     * @version 2016/10/13 9:43:24
     */
    private static class SelfCircularReferenceInConstructor {

        private SelfCircularReferenceInConstructor(SelfCircularReferenceInConstructor self) {
        }
    }

    @Test(expected = ClassCircularityError.class)
    public void singletonSelfCircularReferenceInConstructor() {
        I.make(SingletonSelfCircularReferenceInConstructor.class);
    }

    /**
     * @version 2016/10/13 9:43:47
     */
    @Manageable(lifestyle = Singleton.class)
    private static class SingletonSelfCircularReferenceInConstructor {

        private SingletonSelfCircularReferenceInConstructor(SingletonSelfCircularReferenceInConstructor self) {
        }
    }
}
