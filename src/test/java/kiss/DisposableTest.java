/*
 * Copyright (C) 2021 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss;

import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import org.junit.jupiter.api.Test;

class DisposableTest {

    @Test
    void disposeAnd() {
        Task task1 = new Task();
        Task task2 = new Task();
        Disposable composed = task1.add(task2);
        assert task1 == composed;

        assert task1.executed == false;
        assert task2.executed == false;

        composed.dispose();
        assert task1.executed == true;
        assert task2.executed == true;
    }

    @Test
    void disposeAndNullDisposable() {
        Task task1 = new Task();
        Disposable composed = task1.add((Disposable) null);
        assert task1 == composed;

        assert task1.executed == false;

        composed.dispose();
        assert task1.executed == true;
    }

    @Test
    void disposeAndFuture() {
        Task task1 = new Task();
        FuturedTask task2 = new FuturedTask();
        Disposable composed = task1.add(task2);
        assert task1 == composed;

        assert task1.executed == false;
        assert task2.canceled == false;

        composed.dispose();
        assert task1.executed == true;
        assert task2.canceled == true;
    }

    @Test
    void disposeAndNullFuture() {
        Task task1 = new Task();
        Disposable composed = task1.add((Future) null);
        assert task1 == composed;

        assert task1.executed == false;

        composed.dispose();
        assert task1.executed == true;
    }

    @Test
    void disposeSub() {
        Task task1 = new Task();
        Task task2 = new Task();
        Disposable child = task1.sub().add(task2);
        assert task1 != child;

        assert task1.executed == false;
        assert task2.executed == false;

        task1.dispose();
        assert task1.executed == true;
        assert task2.executed == true;
    }

    @Test
    void disposeSubOnly() {
        Task task1 = new Task();
        Task task2 = new Task();
        Disposable child = task1.sub().add(task2);
        assert task1 != child;

        assert task1.executed == false;
        assert task2.executed == false;

        child.dispose();
        assert task1.executed == false;
        assert task2.executed == true;
    }

    /**
     * 
     */
    private static class Task implements Disposable {

        private boolean executed;

        /**
         * {@inheritDoc}
         */
        @Override
        public void vandalize() {
            executed = true;
        }
    }

    /**
     * 
     */
    private static class FuturedTask extends FutureTask {

        private boolean canceled;

        /**
         * 
         */
        public FuturedTask() {
            super(() -> "don't used");
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            canceled = true;
            return true;
        }
    }
}