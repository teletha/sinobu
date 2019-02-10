/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.scan.sample;

/**
 * @version 2017/04/25 8:24:52
 */
@SuppressWarnings("unused")
public class InNonTestClass {

    private static class InTest1 implements IndexableInterface {
    }

    private static class InTest2 implements IndexableInterface {
    }

    private static abstract class InTest3 implements Runnable {
    }

    private static class InTest4 {
    }
}
