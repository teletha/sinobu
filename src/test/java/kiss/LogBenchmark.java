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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.System.Logger.Level;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.logging.FileHandler;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.api.LoggerComponentBuilder;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import org.tinylog.TaggedLogger;
import org.tinylog.configuration.Configuration;

import antibug.profiler.Benchmark;
import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.FileAppender;

public class LogBenchmark {

    public static void main(String[] args) throws Exception {
        Benchmark benchmark = new Benchmark();

        // performJUL(benchmark);
        // performLog4j(benchmark);
        // performTinyLog(benchmark);
        // performLogback(benchmark);
        performCustomJUL(benchmark);

        benchmark.perform();
    }

    private static void performJUL(Benchmark benchmark) throws Exception {
        System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS.%1$tL %4$s %2$s %5$s%6$s%n");

        // ================================
        // Sync Logger with Caller
        // ================================
        FileHandler handler = new FileHandler("logging-jul.log");
        handler.setFormatter(new SimpleFormatter());

        java.util.logging.Logger with = java.util.logging.Logger.getLogger(LogBenchmark.class.getName());
        with.setUseParentHandlers(false);
        with.addHandler(handler);

        benchmark.measure("JUL", () -> {
            with.info("Message");
            return -1;
        });

        // ================================
        // Sync Logger without Caller
        // ================================
        FileHandler withoudHandler = new FileHandler("logging-jul-noCaller.log");
        withoudHandler.setFormatter(new WithoutCaller());

        java.util.logging.Logger without = java.util.logging.Logger.getLogger(WithoutCaller.class.getName());
        without.setUseParentHandlers(false);
        without.addHandler(withoudHandler);

        benchmark.measure("JUL NoCaller", () -> {
            without.info("Message");
            return -1;
        });
    }

    /**
     * Copy from JDK.
     */
    private static class WithoutCaller extends SimpleFormatter {
        private String format = "%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS.%1$tL %4$s %5$s%6$s%n";

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

    private static void performLog4j(Benchmark benchmark) throws Exception {
        ConfigurationBuilder<BuiltConfiguration> builder = ConfigurationBuilderFactory.newConfigurationBuilder();

        // ================================
        // Sync Logger with Caller
        // ================================
        AppenderComponentBuilder syncWithFile = builder.newAppender("syncFile", "File");
        syncWithFile.addAttribute("fileName", "logging-log4j2.log");
        syncWithFile.addAttribute("append", false);
        syncWithFile.add(builder.newLayout("PatternLayout")
                .addAttribute("pattern", "%date{yyyy-MM-dd HH:mm:ss.SSS} %level %class %method %msg%n"));
        builder.add(syncWithFile);

        LoggerComponentBuilder syncLog = builder.newLogger("sync", org.apache.logging.log4j.Level.ALL);
        syncLog.addAttribute("additivity", false);
        syncLog.add(builder.newAppenderRef("syncFile"));
        builder.add(syncLog);

        // ================================
        // Sync Logger without Caller
        // ================================
        AppenderComponentBuilder syncNoCallerFile = builder.newAppender("syncNoCallerFile", "File");
        syncNoCallerFile.addAttribute("fileName", "logging-log4j2-noCaller.log");
        syncNoCallerFile.addAttribute("append", false);
        syncNoCallerFile.add(builder.newLayout("PatternLayout").addAttribute("pattern", "%date{yyyy-MM-dd HH:mm:ss.SSS} %level %msg%n"));
        builder.add(syncNoCallerFile);

        LoggerComponentBuilder syncNoCallerLog = builder.newLogger("syncNoCaller", org.apache.logging.log4j.Level.ALL);
        syncNoCallerLog.addAttribute("additivity", false);
        syncNoCallerLog.add(builder.newAppenderRef("syncNoCallerFile"));
        builder.add(syncNoCallerLog);

        // ================================
        // Async Logger with Caller
        // ================================
        AppenderComponentBuilder asyncFile = builder.newAppender("asyncFile", "File");
        asyncFile.addAttribute("fileName", "logging-log4j2-async.log");
        asyncFile.addAttribute("append", false);
        asyncFile.add(builder.newLayout("PatternLayout")
                .addAttribute("pattern", "%date{yyyy-MM-dd HH:mm:ss.SSS} %level %class %method %msg%n"));
        builder.add(asyncFile);

        LoggerComponentBuilder asyncLog = builder.newAsyncLogger("async", org.apache.logging.log4j.Level.ALL);
        asyncLog.addAttribute("additivity", false);
        asyncLog.addAttribute("includeLocation", true);
        asyncLog.add(builder.newAppenderRef("asyncFile"));
        builder.add(asyncLog);

        // ================================
        // Async Logger without Caller
        // ================================
        AppenderComponentBuilder asyncNoCallerFile = builder.newAppender("asyncNoCallerFile", "File");
        asyncNoCallerFile.addAttribute("fileName", "logging-log4j2-async-noCaller.log");
        asyncNoCallerFile.addAttribute("append", false);
        asyncNoCallerFile.add(builder.newLayout("PatternLayout").addAttribute("pattern", "%date{yyyy-MM-dd HH:mm:ss.SSS} %level %msg%n"));
        builder.add(asyncNoCallerFile);

        LoggerComponentBuilder asyncNoCallerLog = builder.newAsyncLogger("asyncNoCaller", org.apache.logging.log4j.Level.ALL);
        asyncNoCallerLog.addAttribute("additivity", false);
        asyncNoCallerLog.add(builder.newAppenderRef("asyncNoCallerFile"));
        builder.add(asyncNoCallerLog);

        // ================================
        // Initialize
        // ================================
        LoggerContext context = Configurator.initialize(builder.build());

        // ================================
        // Run Benchmark
        // ================================
        org.apache.logging.log4j.Logger sync = context.getLogger("sync");
        benchmark.measure("Log4j", () -> {
            sync.info("Message");
            return -1;
        });

        org.apache.logging.log4j.Logger syncNoCaller = context.getLogger("syncNoCaller");
        benchmark.measure("Log4j NoCaller", () -> {
            syncNoCaller.info("Message");
            return -1;
        });

        org.apache.logging.log4j.Logger async = context.getLogger("async");
        benchmark.measure("Log4j Async", () -> {
            async.info("Message");
            return -1;
        });

        org.apache.logging.log4j.Logger asyncNoCaller = context.getLogger("asyncNoCaller");
        benchmark.measure("Log4j Async NoCaller", () -> {
            asyncNoCaller.info("Message");
            return -1;
        });
    }

    private static void performTinyLog(Benchmark benchmark) throws Exception {
        boolean async = true;
        Configuration.set("writingthread", String.valueOf(async));

        Configuration.set("writer", "file");
        Configuration.set("writer.file", "logging-tinylog.log");
        Configuration.set("writer.tag", "-");
        Configuration.set("writer.format", "{date:yyyy-MM-dd HH:mm:ss.SSS} {level} {class} {method} {message}");
        Configuration.set("writer.append", "false");

        Configuration.set("writer2", "file");
        Configuration.set("writer2.file", "logging-tinylog-noCaller.log");
        Configuration.set("writer2.tag", "NoCaller");
        Configuration.set("writer2.format", "{date:yyyy-MM-dd HH:mm:ss.SSS} {level} {message}");
        Configuration.set("writer2.append", "false");

        benchmark.measure("TinyLog" + (async ? " Async" : ""), () -> {
            org.tinylog.Logger.info("Message");
            return -1;
        });

        TaggedLogger noCaller = org.tinylog.Logger.tag("NoCaller");
        benchmark.measure("TinyLog " + (async ? "Async" : "") + " NoCaller", () -> {
            noCaller.info("Message");
            return -1;
        });
    }

    private static void performLogback(Benchmark benchmark) throws Exception {
        ch.qos.logback.classic.LoggerContext context = new ch.qos.logback.classic.LoggerContext();

        // ================================
        // Sync Logger with Caller
        // ================================
        PatternLayoutEncoder layout = new PatternLayoutEncoder();
        layout.setContext(context);
        layout.setPattern("%date{YYYY-MM-dd HH:mm:ss.SSS} %level %class %method %msg%n");
        layout.start();

        FileAppender file = new FileAppender();
        file.setContext(context);
        file.setEncoder(layout);
        file.setAppend(false);
        file.setFile("logging-logback.log");
        file.start();

        ch.qos.logback.classic.Logger log = context.getLogger("Sync");
        log.setAdditive(false);
        log.addAppender(file);
        log.setLevel(ch.qos.logback.classic.Level.ALL);

        benchmark.measure("Logback", () -> {
            log.info("Message");
            return -1;
        });

        // ================================
        // Sync Logger without Caller
        // ================================
        PatternLayoutEncoder noCallerLayout = new PatternLayoutEncoder();
        noCallerLayout.setContext(context);
        noCallerLayout.setPattern("%date{YYYY-MM-dd HH:mm:ss.SSS} %level %msg%n");
        noCallerLayout.start();

        FileAppender noCallerFile = new FileAppender();
        noCallerFile.setContext(context);
        noCallerFile.setEncoder(noCallerLayout);
        noCallerFile.setAppend(false);
        noCallerFile.setFile("logging-logback-noCaller.log");
        noCallerFile.start();

        ch.qos.logback.classic.Logger noCaller = context.getLogger("SyncNoCaller");
        noCaller.setAdditive(false);
        noCaller.addAppender(noCallerFile);
        noCaller.setLevel(ch.qos.logback.classic.Level.ALL);

        benchmark.measure("Logback NoCaller", () -> {
            noCaller.info("Message");
            return -1;
        });

        // ================================
        // Async Logger with Caller
        // ================================
        FileAppender asyncFile = new FileAppender();
        asyncFile.setContext(context);
        asyncFile.setName("asyncFile");
        asyncFile.setEncoder(layout);
        asyncFile.setAppend(false);
        asyncFile.setFile("logging-logback-async.log");
        asyncFile.start();

        AsyncAppender wrapper = new AsyncAppender();
        wrapper.setContext(context);
        wrapper.addAppender(asyncFile);
        wrapper.setIncludeCallerData(true);
        wrapper.start();

        ch.qos.logback.classic.Logger async = context.getLogger("async");
        async.setAdditive(false);
        async.addAppender(wrapper);
        async.setLevel(ch.qos.logback.classic.Level.ALL);

        benchmark.measure("Logback Async", () -> {
            async.info("Message");
            return -1;
        });

        // ================================
        // Async Logger without Caller
        // ================================
        FileAppender asyncNoCallerFile = new FileAppender();
        asyncNoCallerFile.setContext(context);
        asyncNoCallerFile.setEncoder(noCallerLayout);
        asyncNoCallerFile.setAppend(false);
        asyncNoCallerFile.setFile("logging-logback-async-noCaller.log");
        asyncNoCallerFile.start();

        AsyncAppender wrapperNoCaller = new AsyncAppender();
        wrapperNoCaller.setContext(context);
        wrapperNoCaller.addAppender(asyncNoCallerFile);
        wrapperNoCaller.setIncludeCallerData(false);
        wrapperNoCaller.start();

        ch.qos.logback.classic.Logger asyncNoCaller = context.getLogger("asyncNoCaller");
        asyncNoCaller.setAdditive(false);
        asyncNoCaller.addAppender(wrapperNoCaller);
        asyncNoCaller.setLevel(ch.qos.logback.classic.Level.ALL);

        benchmark.measure("Logback Async NoCaller", () -> {
            asyncNoCaller.info("Message");
            return -1;
        });
    }

    private static void performCustomJUL(Benchmark benchmark) throws Exception {
        I.LogAppend = false;
        I.LogConsole = Level.OFF;
        I.LogFile = Level.ALL;

        // ================================
        // Sync Logger with Caller
        // ================================
        benchmark.measure("Custom JUL", () -> {
            I.LogAsync = false;
            I.LogCaller = Level.ALL;
        }, () -> {
            I.info(Sync.class, "Message");
            return -1;
        });

        // ================================
        // Sync Logger without Caller
        // ================================
        benchmark.measure("Custom JUL NoCaller", () -> {
            I.LogAsync = false;
            I.LogCaller = Level.OFF;
        }, () -> {
            I.info(SyncNoCaller.class, "Message");
            return -1;
        });

        // ================================
        // Async Logger with Caller
        // ================================
        benchmark.measure("Custom JUL Async", () -> {
            I.LogAsync = true;
            I.LogCaller = Level.ALL;
        }, () -> {
            I.info(Async.class, "Message");
            return -1;
        });

        // ================================
        // Async Logger without Caller
        // ================================
        benchmark.measure("Custom JUL Async NoCaller", () -> {
            I.LogAsync = true;
            I.LogCaller = Level.OFF;
        }, () -> {
            I.info(AsyncNoCaller.class, "Message");
            return -1;
        });
    }

    private static class Sync {
    }

    private static class Async {
    }

    private static class SyncNoCaller {
    }

    private static class AsyncNoCaller {
    }
}