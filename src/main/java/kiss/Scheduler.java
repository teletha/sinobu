/*
 * Copyright (C) 2024 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss;

import static java.util.concurrent.Executors.*;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongUnaryOperator;

/**
 * A custom scheduler implementation based on the {@link ScheduledExecutorService} interface,
 * using virtual threads to schedule tasks with specified delays or intervals.
 * 
 * <p>
 * This class extends {@link AbstractExecutorService} and implements
 * {@link ScheduledExecutorService} to provide scheduling capabilities with a task queue and delay
 * mechanisms. It leverages {@link DelayQueue} to manage task execution times, and uses virtual
 * threads to run tasks in a lightweight and efficient manner.
 * </p>
 * 
 * <h2>Core Functionality</h2>
 * <ul>
 * <li>Allows scheduling of tasks with delays or fixed intervals.</li>
 * <li>Supports scheduling based on cron expressions for periodic execution.</li>
 * <li>Uses virtual threads to minimize memory consumption and resource overhead.</li>
 * </ul>
 * 
 * <h2>Thread Management</h2>
 * <p>
 * Virtual threads are created in an "unstarted" state when tasks are registered. Execution is
 * delayed until the scheduled time, reducing memory usage. Once the scheduled time arrives, the
 * virtual threads are started and the tasks are executed. If the task is periodic, it is
 * rescheduled after completion.
 * </p>
 * 
 * <h2>Usage</h2>
 * <p>
 * You can use the following methods to schedule tasks:
 * <ul>
 * <li>{@link #schedule(Runnable, long, TimeUnit)}: Schedule a task with a delay.</li>
 * <li>{@link #scheduleAtFixedRate(Runnable, long, long, TimeUnit)}: Schedule a task at fixed
 * intervals.</li>
 * <li>{@link #scheduleWithFixedDelay(Runnable, long, long, TimeUnit)}: Schedule a task with a fixed
 * delay between executions.</li>
 * <li>{@link #scheduleAt(Runnable, String)}: Schedule a task based on a cron expression.</li>
 * </ul>
 * </p>
 * 
 * <h2>Task Lifecycle</h2>
 * <p>
 * The scheduler maintains internal counters to track running tasks and completed tasks using
 * {@link AtomicLong}. The task queue is managed through {@link DelayQueue}, which ensures tasks
 * are executed at the correct time. Each task is wrapped in a custom {@link Task} class that
 * handles execution, cancellation, and rescheduling (for periodic tasks).
 * </p>
 * 
 * <h2>Shutdown and Termination</h2>
 * <p>
 * The scheduler can be shut down using the {@link #shutdown()} or {@link #shutdownNow()} methods,
 * which stops the execution of any further tasks. The {@link #awaitTermination(long, TimeUnit)}
 * method can be used to block until all tasks are finished executing after a shutdown request.
 * </p>
 * 
 * @see ScheduledExecutorService
 */
public class Scheduler extends AbstractExecutorService implements ScheduledExecutorService {

    /** The the running task manager. */
    protected final Set<Task> runs = ConcurrentHashMap.newKeySet();

    /** The task queue. */
    protected DelayQueue<Task> queue = new DelayQueue();

    /** The running state of task queue. */
    private volatile boolean run = true;

    /** Controls the number of tasks that can be executed concurrently. */
    private final Semaphore max;

    /**
     * Create {@link Scheduler} without the specified concurrency limit.
     */
    public Scheduler() {
        this(Integer.MAX_VALUE);
    }

    /**
     * Create {@link Scheduler} with the specified concurrency limit.
     * 
     * @param limit Controls the number of tasks that can be executed concurrently.
     */
    public Scheduler(int limit) {
        max = new Semaphore(limit);

        Thread.ofVirtual().start(() -> {
            try {
                while (run || !queue.isEmpty()) {
                    max.acquire();
                    Task task = queue.take();
                    // Task execution state management is performed before thread execution because
                    // it is too slow if the task execution state management is performed within the
                    // task's execution thread.
                    runs.add(task);

                    // execute task actually
                    task.thread.start();
                }
            } catch (InterruptedException e) {
                // stop
            }
        });
    }

    /**
     * Execute the task.
     * 
     * @param task
     */
    protected Task executeTask(Task<?> task) {
        if (!run) {
            throw new RejectedExecutionException();
        }

        if (!task.isCancelled()) {
            // Threads are created when a task is registered, but execution is delayed until the
            // scheduled time. Although it would be simpler to immediately schedule the task using
            // Thread#sleep after execution, this implementation method is used to reduce memory
            // usage as much as possible. Note that only the creation of the thread is done first,
            // since the information is not inherited by InheritableThreadLocal if the thread is
            // simply placed in the task queue.
            task.thread = Thread.ofVirtual().unstarted(() -> {
                try {
                    if (!task.isCancelled()) {
                        task.run();

                        if (task.interval == null || !run) {
                            // one shot or scheduler is already stopped
                        } else {
                            // reschedule task
                            task.next = task.interval.applyAsLong(task.next);
                            executeTask(task);
                        }
                    }
                } finally {
                    runs.remove(task);
                    max.release();
                }
            });
            queue.add(task);
        }

        return task;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        return schedule(Executors.callable(command), delay, unit);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <V> ScheduledFuture<V> schedule(Callable<V> command, long delay, TimeUnit unit) {
        return executeTask(new Task(command, next(delay, unit), null));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long delay, long interval, TimeUnit unit) {
        return executeTask(new Task(callable(command), next(delay, unit), old -> old + unit.toMillis(interval)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long delay, long interval, TimeUnit unit) {
        return executeTask(new Task(callable(command), next(delay, unit), old -> System.currentTimeMillis() + unit.toMillis(interval)));
    }

    /**
     * Schedules a task to be executed periodically based on a cron expression.
     * <p>
     * This method uses a cron expression to determine the execution intervals for the given
     * {@code Runnable} command. The task calculates the next execution time dynamically after each
     * run
     * using the provided cron format.
     * </p>
     * <p>
     * The method supports a general cron format with five fields (seconds, minutes, hours, day of
     * month, month, and day of week) or six fields including seconds. Additionally, it includes
     * special keywords such as {@code L, W, R, ?, /} to enhance scheduling flexibility.
     * </p>
     * 
     * <h3>Cron Expression Syntax</h3>
     * <p>
     * A cron expression is a string that represents a schedule using six fields (with seconds) or
     * five fields (without seconds).
     * Each field allows specifying ranges, lists, and special keywords to determine the precise
     * schedule of the task.
     * The following are the six fields:
     * <ul>
     * <li><b>Seconds</b>: 0-59</li>
     * <li><b>Minutes</b>: 0-59</li>
     * <li><b>Hours</b>: 0-23</li>
     * <li><b>Day of Month</b>: 1-31</li>
     * <li><b>Month</b>: 1-12 or JAN-DEC (month names)</li>
     * <li><b>Day of Week</b>: 0-7 (Sunday is both 0 and 7) or SUN-SAT (day names)</li>
     * </ul>
     * </p>
     * 
     * <h4>Supported Cron Special Characters</h4>
     * <p>
     * The following special characters are supported in cron expressions:
     * <ul>
     * <li><b>,</b> (Comma): Used to specify multiple values. For example, {@code 0,15,30 * * * * *}
     * means the task will run at 0, 15, and 30 seconds.</li>
     * <li><b>-</b> (Dash): Specifies a range of values. For example, {@code 10-20 * * * * *} will
     * run the task every second between 10 and 20 seconds.</li>
     * <li><b>/</b> (Slash): Specifies intervals. For example, {@code 0/10 * * * * *} means the task
     * will run every 10 seconds starting from 0 seconds.</li>
     * <li><b>L</b> (Last): Indicates the last day of the month or the last weekday of the month,
     * depending on its context.
     * <ul>
     * <li>For the day-of-month field, {@code L} means the last day of the month (e.g.,
     * {@code L * * * *} runs on the last day of every month).</li>
     * <li>For the day-of-week field, {@code L} means the last occurrence of a specific weekday
     * (e.g., {@code 5L} means the last Friday of the month).</li>
     * </ul>
     * </li>
     * <li><b>W</b> (Weekday): Runs the task on the closest weekday (Monday to Friday) to the
     * specified day.
     * For example, {@code 1W} means the task will run on the closest weekday to the 1st of the
     * month. If the 1st is a Saturday, the task runs on the following Monday (the 3rd), but it does
     * not cross into the next month. Similarly, {@code 31W} in a month with fewer than 31 days will
     * run on the last weekday of that month.</li>
     * <li><b>? (Any)</b>: Used in the day-of-month or day-of-week field to specify that no specific
     * value is set. It is typically used when you want to define one of these fields but not the
     * other.</li>
     * <li><b># (Nth Weekday)</b>: Specifies the Nth occurrence of a weekday in a given month. For
     * example, {@code 3#1} means the first Tuesday of the month.</li>
     * <li><b>R (Random)</b>: This is a custom extension similar to Jenkins' H keyword. {@code R} is
     * used to schedule tasks at random times based on the hash of the task.
     * For example, {@code 5-30R * * * * *} will select a random second between 5 and 30 for each
     * execution.
     * This helps distribute task executions over time to avoid simultaneous task execution peaks.
     * By scattering task execution times randomly, the risk of heavy load caused by multiple tasks
     * running at the same moment is reduced, effectively distributing the load across time.</li>
     * </ul>
     * </p>
     * 
     * <h4>Examples</h4>
     * <p>
     * Some cron expression examples:
     * <ul>
     * <li><b>0 0 12 * * ?</b>: Executes at 12:00 PM every day.</li>
     * <li><b>0 15 10 * * ?</b>: Executes at 10:15 AM every day.</li>
     * <li><b>0 0/5 14 * * ?</b>: Executes every 5 minutes starting at 2:00 PM.</li>
     * <li><b>0 0 10 ? * 2#1</b>: Executes at 10:00 AM on the first Monday of every month.</li>
     * <li><b>0 0 1W * * ?</b>: Executes on the nearest weekday to the 1st day of every month.</li>
     * <li><b>0 0 0 L * ?</b>: Executes at midnight on the last day of every month.</li>
     * <li><b>0 0 0 LW * ?</b>: Executes at midnight on the last weekday of every month.</li>
     * <li><b>0 10,20,30 * * * ?</b>: Executes at 10, 20, and 30 minutes of every hour.</li>
     * <li><b>0 0 9-17 * * ?</b>: Executes every hour between 9:00 AM and 5:00 PM.</li>
     * <li><b>0 0 9-17/2 * * ?</b>: Executes every 2 hours between 9:00 AM and 5:00 PM.</li>
     * <li><b>0 0 9-17,20 * * ?</b>: Executes every hour between 9:00 AM and 5:00 PM, and at 8:00
     * PM.</li>
     * <li><b>0 15 10 15 * ?</b>: Executes at 10:15 AM on the 15th of every month.</li>
     * <li><b>0 15 10 ? * 6L</b>: Executes at 10:15 AM on the last Friday of every month.</li>
     * <li><b>0 0 12 15 JAN * </b>: Executes at noon on January 15th every year.</li>
     * <li><b>0 0 12 ? * MON-FRI</b>: Executes at noon on weekdays (Monday to Friday).</li>
     * <li><b>0 15 10 15 AUG-DEC * *</b>: Executes at 10:15 AM on the 15th of every month from
     * August to December.</li>
     * <li><b>0 0 8 1-7/2 * *</b>: Executes at 8:00 AM on every second day between the 1st and 7th
     * of each month.</li>
     * <li><b>0 30 9 1,15,30 * *</b>: Executes at 9:30 AM on the 1st, 15th, and 30th of every
     * month.</li>
     * <li><b>0 0 10 5-10 * MON-WED</b>: Executes at 10:00 AM between the 5th and 10th of every
     * month, but only if the day is a Monday, Tuesday, or Wednesday.</li>
     * <li><b>0 0/15 9-17 * * MON-FRI</b>: Executes every 15 minutes between 9:00 AM and 5:00 PM on
     * weekdays (Monday to Friday).</li>
     * <li><b>5-10R * * * * *</b>: Executes at a random second between 5 and 10 every minute.</li>
     * </ul>
     * </p>
     * 
     * @param command The {@code Runnable} task to be scheduled for periodic execution.
     * @param format A valid cron expression that defines the schedule for task execution.
     *            The cron format is parsed to calculate the next execution time.
     * 
     * @return A {@code ScheduledFuture<?>} representing the pending completion of the task.
     *         The {@code ScheduledFuture} can be used to cancel or check the status of the task.
     * 
     * @throws IllegalArgumentException If the cron format is invalid or cannot be parsed correctly.
     */
    public ScheduledFuture<?> scheduleAt(Runnable command, String format) {
        Cron[] fields = parse(format, command.toString().hashCode());
        LongUnaryOperator next = old -> next(fields, ZonedDateTime.now()).toInstant().toEpochMilli();

        return executeTask(new Task(callable(command), next.applyAsLong(0L), next));
    }

    /**
     * Parses a cron expression into an array of {@link Cron} objects.
     * The cron expression is expected to have 5 or 6 parts:
     * - For a standard cron expression with 5 parts (minute, hour, day of month, month, day of
     * week), the seconds field will be assumed to be "0".
     * - For a cron expression with 6 parts (second, minute, hour, day of month, month, day of
     * week), all fields are used directly from the cron expression.
     *
     * @param cron the cron expression to parse
     * @return an array of {@link Cron} objects representing the parsed cron fields.
     * @throws IllegalArgumentException if the cron expression does not have 5 or 6 parts
     */
    static Cron[] parse(String cron, int hash) {
        String[] parts = cron.strip().split("\\s+");
        int i = parts.length - 5;
        if (i != 0 && i != 1) {
            throw new IllegalArgumentException(cron);
        }

        return new Cron[] {new Cron(ChronoField.SECOND_OF_MINUTE, 0, 59, "", "R", "/R", i == 1 ? parts[0] : "0", hash),
                new Cron(ChronoField.MINUTE_OF_HOUR, 0, 59, "", "R", "/R", parts[i++], hash),
                new Cron(ChronoField.HOUR_OF_DAY, 0, 23, "", "R", "/R", parts[i++], hash),
                new Cron(ChronoField.DAY_OF_MONTH, 1, 31, "", "?LRW", "/R", parts[i++], hash),
                new Cron(ChronoField.MONTH_OF_YEAR, 1, 12, "JAN FEB MAR APR MAY JUN JUL AUG SEP OCT NOV DEC ", "R", "/R", parts[i++], hash),
                new Cron(ChronoField.DAY_OF_WEEK, 1, 7, "MON TUE WED THU FRI SAT SUN ", "?LR", "#/R", parts[i++], hash)};
    }

    /**
     * Calculates the next execution time based on the provided cron fields and a base time.
     * 
     * The search for the next execution time will start from the base time and continue until
     * a matching time is found. The search will stop if no matching time is found within four
     * years.
     * 
     * @param cron an array of {@link Cron} objects representing the parsed cron fields
     * @param base the {@link ZonedDateTime} representing the base time to start the search from
     * @return the next execution time as a {@link ZonedDateTime}
     * @throws IllegalArgumentException if no matching execution time is found within four years
     */
    static ZonedDateTime next(Cron[] cron, ZonedDateTime base) {
        // The range is four years, taking into account leap years.
        ZonedDateTime limit = base.plusYears(4);

        ZonedDateTime[] next = {base.plusSeconds(1).truncatedTo(ChronoUnit.SECONDS)};
        root: while (true) {
            if (next[0].isAfter(limit)) throw new IllegalArgumentException("Next time is not found before " + limit);
            if (!cron[4].matches(next)) continue;

            int month = next[0].getMonthValue();
            while (!(cron[3].matches(next[0]) && cron[5].matches(next[0]))) {
                next[0] = next[0].plusDays(1).truncatedTo(ChronoUnit.DAYS);
                if (next[0].getMonthValue() != month) continue root;
            }

            if (!cron[2].matches(next)) continue;
            if (!cron[1].matches(next)) continue;
            if (!cron[0].matches(next)) continue;
            return next[0];
        }
    }

    /**
     * Calculates the next time point by adding the specified delay to the current system time.
     * 
     * This method takes the current system time (in milliseconds) and adds the provided delay,
     * which is converted to milliseconds based on the provided {@link TimeUnit}. The result is the
     * time point (in milliseconds since the Unix epoch) that corresponds to the current time plus
     * the delay.
     * 
     * @param delay the delay to add to the current time
     * @param unit the {@link TimeUnit} representing the unit of the delay (e.g., seconds, minutes)
     * @return the next time point in milliseconds since the Unix epoch
     */
    static long next(long delay, TimeUnit unit) {
        return System.currentTimeMillis() + unit.toMillis(delay);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void shutdown() {
        run = false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Runnable> shutdownNow() {
        run = false;
        for (Task run : runs) {
            run.thread.interrupt();
        }

        DelayQueue temp = queue;
        queue = new DelayQueue();
        return new ArrayList(temp);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean awaitTermination(long time, TimeUnit unit) throws InterruptedException {
        long end = next(time, unit);
        while (!isTerminated()) {
            long rem = end - System.currentTimeMillis();
            if (rem < 0) {
                return false;
            }
            Thread.sleep(Math.min(rem + 1, 100));
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isShutdown() {
        return !run;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isTerminated() {
        return !run && queue.isEmpty() && runs.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(Runnable command) {
        schedule(command, 0, TimeUnit.MILLISECONDS);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
        return new Task(callable, 0, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
        return newTaskFor(Executors.callable(runnable, value));
    }
}