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

import static java.util.concurrent.TimeUnit.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.System.Logger.Level;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.api.LoggerComponentBuilder;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import org.slf4j.LoggerFactory;
import org.tinylog.TaggedLogger;
import org.tinylog.configuration.Configuration;

import antibug.profiler.Benchmark;
import antibug.profiler.Inspection;
import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.FileAppender;

public class LogBenchmark {

    private enum OutputType {
        File, Console;
    }

    private enum ExecutionType {
        Sync, Async, Both;
    }

    private enum CallerType {
        Caller, NoCaller, Both;
    }

    private static final boolean flush = false;

    private static final OutputType output = OutputType.Console;

    private static final ExecutionType execution = ExecutionType.Both;

    private static final CallerType caller = CallerType.Both;

    private static final String message = "Write your message! This is comparison of  logging libraries. これは漢字カタカナﾊﾝｶｸｶﾅ①②♡♧㍉🚀😀👩‍❤️‍👩👍🏽👨🏻‍🦱🏠🈵♌🕦📒👿";

    public static void main(String[] args) throws Exception {
        Benchmark benchmark = new Benchmark().memory("128m").visualize(Inspection.TimePerCall, Inspection.GC, Inspection.PeakMemory);

        if (output == OutputType.Console) {
            benchmark.discardSystemOutput();
        }

        performTinyLog(benchmark);
        performJUL(benchmark);
        performSinobu(benchmark);
        performLog4j(benchmark);
        performLogback(benchmark);

        benchmark.perform();
    }

    private static void perform(WiseBiConsumer<ExecutionType, CallerType> process) {
        EnumSet<ExecutionType> executions = execution == ExecutionType.Both ? EnumSet.of(ExecutionType.Sync, ExecutionType.Async)
                : EnumSet.of(execution);
        EnumSet<CallerType> callers = caller == CallerType.Both ? EnumSet.of(CallerType.Caller, CallerType.NoCaller) : EnumSet.of(caller);

        for (ExecutionType executionType : executions) {
            for (CallerType callerType : callers) {
                process.accept(executionType, callerType);
            }
        }
    }

    static void performJUL(Benchmark benchmark) throws Exception {
        perform((execution, caller) -> {
            if (execution == ExecutionType.Async) {
                return; // JUL has no async-implementation, ignore it!
            }

            Handler handler = output == OutputType.File ? new FileHandler(".log/logging-jul-" + caller + ".log") : new ConsoleHandler();
            handler.setFormatter(new ModifiableFormatter(caller));

            java.util.logging.Logger logger = java.util.logging.Logger.getLogger(execution + "-" + caller);
            logger.setUseParentHandlers(false);
            logger.addHandler(handler);

            benchmark.measure("JUL " + logger.getName(), () -> {
                logger.severe(message);
                return -1;
            });
        });
    }

    /**
     * Copy from JDK.
     */
    static class ModifiableFormatter extends SimpleFormatter {
        private String format;

        private ModifiableFormatter(CallerType caller) {
            format = switch (caller) {
            case Caller -> "%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS.%1$tL %4$s %2$s %5$s%6$s%n";
            case NoCaller -> "%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS.%1$tL %4$s %5$s%6$s%n";
            default -> "";
            };
        }

        @Override
        public String format(LogRecord record) {
            ZonedDateTime zdt = ZonedDateTime.ofInstant(record.getInstant(), ZoneId.systemDefault());
            String source;
            if (record.getSourceClassName() != null) {
                source = record.getSourceClassName();
                if (record.getSourceMethodName() != null) {
                    source += " " + record.getSourceMethodName();
                }
            } else {
                source = record.getLoggerName();
            }
            String message = formatMessage(record);
            String throwable = "";
            if (record.getThrown() != null) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                pw.println();
                record.getThrown().printStackTrace(pw);
                pw.close();
                throwable = sw.toString();
            }
            return String.format(format, zdt, source, record.getLoggerName(), record.getLevel().getLocalizedName(), message, throwable);
        }
    }

    static void performLog4j(Benchmark benchmark) throws Exception {
        System.setProperty("log4j2.DiscardThreshold", "OFF");
        ConfigurationBuilder<BuiltConfiguration> builder = ConfigurationBuilderFactory.newConfigurationBuilder();

        perform((execution, caller) -> {
            if (execution == ExecutionType.Async && caller == CallerType.Caller) {
                return; // log4j2 ignore location pattern in async logger
            }

            String name = execution + "-" + caller;

            AppenderComponentBuilder appender = builder.newAppender(name, output == OutputType.File ? "File" : "Console");
            appender.addAttribute("fileName", ".log/logging-log4j2-" + name + ".log");
            appender.addAttribute("append", false);
            appender.addAttribute("immediateFlush", flush);
            appender.add(builder.newLayout("PatternLayout")
                    .addAttribute("pattern", caller == CallerType.Caller ? "%date{yyyy-MM-dd HH:mm:ss.SSS} %level %location %msg%n"
                            : "%date{yyyy-MM-dd HH:mm:ss.SSS} %level %msg%n"));
            builder.add(appender);

            LoggerComponentBuilder logger = execution == ExecutionType.Sync ? builder.newLogger(name, org.apache.logging.log4j.Level.ALL)
                    : builder.newAsyncLogger(name, org.apache.logging.log4j.Level.ALL);
            logger.addAttribute("additivity", false);
            logger.addAttribute("includeLocation", caller == CallerType.Caller);
            logger.add(builder.newAppenderRef(name));
            builder.add(logger);
        });

        LoggerContext context = Configurator.initialize(builder.build());

        perform((execution, caller) -> {
            if (execution == ExecutionType.Async && caller == CallerType.Caller) {
                return; // log4j2 ignore location pattern in async logger
            }

            String name = execution + "-" + caller;

            org.apache.logging.log4j.Logger log = context.getLogger(name);
            benchmark.measure("Log4j " + name, () -> {
                log.error(message);
                return -1;
            });
        });
    }

    static void performTinyLog(Benchmark benchmark) throws Exception {
        AtomicInteger id = new AtomicInteger(1);

        perform((execution, caller) -> {
            String name = execution + "-" + caller;
            String writer = "writer" + id.getAndIncrement();

            Configuration.set("writingthread", execution == ExecutionType.Sync ? "false" : "true");
            Configuration.set(writer, output == OutputType.File ? "file" : "console");
            Configuration.set(writer + ".file", ".log/logging-tinylog" + name + ".log");
            Configuration.set(writer + ".tag", name);
            Configuration.set(writer + ".format", caller == CallerType.Caller
                    ? "{date:yyyy-MM-dd HH:mm:ss.SSS} {level} {class}.{method}({file}:{line}) {message}"
                    : "{date:yyyy-MM-dd HH:mm:ss.SSS} {level} {message}");
            Configuration.set(writer + ".append", "false");
        });

        perform((execution, caller) -> {
            String name = execution + "-" + caller;

            TaggedLogger logger = org.tinylog.Logger.tag(name);
            benchmark.measure("TinyLog " + name, env -> env.when(caller == CallerType.NoCaller)
                    .duration(200, MILLISECONDS)
                    .memory("512m"), () -> {
                        logger.error(message);
                        return -1;
                    });
        });
    }

    static void performLogback(Benchmark benchmark) throws Exception {
        perform((execution, caller) -> {
            String name = execution + "-" + caller;
            ch.qos.logback.classic.LoggerContext context = (ch.qos.logback.classic.LoggerContext) LoggerFactory.getILoggerFactory();

            PatternLayoutEncoder layout = new PatternLayoutEncoder();
            layout.setContext(context);
            layout.setPattern(caller == CallerType.Caller ? "%date{YYYY-MM-dd HH:mm:ss.SSS} %level %class.%method\\(%file:%line\\) %msg%n"
                    : "%date{YYYY-MM-dd HH:mm:ss.SSS} %level %msg%n");
            layout.start();

            Appender appender;

            if (output == OutputType.Console) {
                ConsoleAppender console = new ConsoleAppender();
                console.setContext(context);
                console.setEncoder(layout);
                console.setImmediateFlush(flush);
                console.start();
                appender = console;
            } else {
                FileAppender file = new FileAppender();
                file.setContext(context);
                file.setEncoder(layout);
                file.setAppend(false);
                file.setImmediateFlush(flush);
                file.setFile(".log/logging-logback-" + name + ".log");
                file.start();
                appender = file;
            }

            if (execution == ExecutionType.Async) {
                AsyncAppender async = new AsyncAppender();
                async.setContext(context);
                async.addAppender(appender);
                async.setIncludeCallerData(caller == CallerType.Caller);
                async.setDiscardingThreshold(0);
                async.start();

                appender = async;
            }

            ch.qos.logback.classic.Logger logger = context.getLogger(name);
            logger.setAdditive(false);
            logger.addAppender(appender);
            logger.setLevel(ch.qos.logback.classic.Level.TRACE);

            benchmark.measure("Logback " + name, () -> {
                logger.error(message);
                return -1;
            });
        });
    }

    static void performSinobu(Benchmark benchmark) throws Exception {

        perform((execution, caller) -> {
            if (execution == ExecutionType.Async) {
                return; // async logging is not supported
            }

            String type = execution + "-" + caller;
            String name = "logging-sinobu-" + type;
            I.env(name + ".append", false);
            I.env(name + ".caller", caller == CallerType.Caller ? Level.ALL : Level.OFF);
            I.env(name + ".file", output == OutputType.File ? Level.ALL : Level.OFF);
            I.env(name + ".console", output == OutputType.Console ? Level.ALL : Level.OFF);

            benchmark.measure("Sinobu " + type, () -> {
                I.error(name, message);
                return -1;
            });
        });
    }
}