/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.signal;

import org.junit.Test;

/**
 * @version 2017/04/15 10:24:35
 */
public class MergeTest extends SignalTestBase {

    @Test
    public void completeFromOther() throws Exception {
        monitor(signal -> signal.merge(other.signal()));

        emit("Main");
        assert result.value("Main");
        other.emit("Other");
        assert result.value("Other");

        // test complete
        assert result.isNotCompleted();

        // complete other
        other.complete();
        other.emit("Other is completed so this value will be ignored.");
        assert result.value();
        assert result.isNotCompleted();

        emit("Main is not completed.");
        assert result.value("Main is not completed.");

        // complete main
        emit(Complete, "Main is completed so this value will be ignored.");
        assert result.value();
        assert result.isCompleted();
    }

    @Test
    public void completeFromMain() throws Exception {
        monitor(signal -> signal.merge(other.signal()));

        emit("Main");
        assert result.value("Main");
        other.emit("Other");
        assert result.value("Other");

        // test complete
        assert result.isNotCompleted();

        // complete main
        emit(Complete, "Main is completed so this value will be ignored.");
        assert result.value();
        assert result.isNotCompleted();

        other.emit("Other is not completed.");
        assert result.value("Other is not completed.");

        // complete other
        other.complete();
        other.emit("Other is completed so this value will be ignored.");
        assert result.value();
        assert result.isCompleted();
    }

    @Test
    public void disposeByTake() throws Exception {
        monitor(() -> signal(1).merge(signal(10, 20)).effect(log1).take(2));

        assert log1.value(1, 10);
        assert result.value(1, 10);
        assert result.isCompleted();
    }
}
