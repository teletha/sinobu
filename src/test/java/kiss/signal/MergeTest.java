/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.signal;

import org.junit.Test;

/**
 * @version 2017/04/15 10:24:35
 */
public class MergeTest extends SignalTester {

    @Test
    public void completeFromOther() throws Exception {
        monitor(signal -> signal.merge(other.signal()));

        main.emit("Main");
        assert main.value("Main");
        other.emit("Other");
        assert main.value("Other");

        // test complete
        assert main.isNotCompleted();

        // complete other
        other.emit(Complete, "Other is completed so this value will be ignored.");
        assert main.value();
        assert main.isNotCompleted();

        main.emit("Main is not completed.");
        assert main.value("Main is not completed.");

        // complete main
        main.emit(Complete, "Main is completed so this value will be ignored.");
        assert main.value();
        assert main.isCompleted();
    }

    @Test
    public void completeFromMain() throws Exception {
        monitor(signal -> signal.merge(other.signal()));

        main.emit("Main");
        assert main.value("Main");
        other.emit("Other");
        assert main.value("Other");

        // test complete
        assert main.isNotCompleted();

        // complete main
        main.emit(Complete, "Main is completed so this value will be ignored.");
        assert main.value();
        assert main.isNotCompleted();

        other.emit("Other is not completed.");
        assert main.value("Other is not completed.");

        // complete other
        other.emit(Complete, "Other is completed so this value will be ignored.");
        assert main.value();
        assert main.isCompleted();
    }

    @Test
    public void disposeByTake() throws Exception {
        monitor(() -> signal(1).merge(signal(10, 20)).effect(log1).take(2));

        assert log1.value(1, 10);
        assert main.value(1, 10);
        assert main.isCompleted();
    }
}
