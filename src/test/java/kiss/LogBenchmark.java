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

import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;

import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.api.LoggerComponentBuilder;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
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
        performTinyLog(benchmark);
        // performLogback(benchmark);
        performCustomJUL(benchmark);

        benchmark.perform();
    }

    private static void performJUL(Benchmark benchmark) throws Exception {
        System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS.%1$tL %4$s %2$s %5$s%6$s%n");

        FileHandler handler = new FileHandler("logging-jul.log");
        handler.setFormatter(new SimpleFormatter());

        java.util.logging.Logger log = java.util.logging.Logger.getLogger(LogBenchmark.class.getName());
        log.setUseParentHandlers(false);
        log.addHandler(handler);

        benchmark.measure("JUL", () -> {
            log.info("Message");
            return -1;
        });
    }

    private static void performLog4j(Benchmark benchmark) throws Exception {
        ConfigurationBuilder<BuiltConfiguration> builder = ConfigurationBuilderFactory.newConfigurationBuilder();

        // ================================
        // Sync Logger
        // ================================
        AppenderComponentBuilder syncFile = builder.newAppender("syncFile", "File");
        syncFile.addAttribute("fileName", "logging-lg4j2.log");
        syncFile.addAttribute("append", false);
        syncFile.add(builder.newLayout("PatternLayout")
                .addAttribute("pattern", "%date{yyyy-MM-dd HH:mm:ss.SSS} %level %class %method %msg%n"));
        builder.add(syncFile);

        LoggerComponentBuilder syncLog = builder.newLogger("sync", org.apache.logging.log4j.Level.ALL);
        syncLog.addAttribute("additivity", false);
        syncLog.add(builder.newAppenderRef("syncFile"));
        builder.add(syncLog);

        // ================================
        // Async Logger
        // ================================
        // AppenderComponentBuilder asyncFile = builder.newAppender("asyncFile", "File");
        // asyncFile.addAttribute("fileName", "logging-lg4j2-async.log");
        // asyncFile.addAttribute("append", false);
        // asyncFile.add(builder.newLayout("PatternLayout")
        // .addAttribute("pattern", "%date{yyyy-MM-dd HH:mm:ss.SSS} %level %class %method %msg%n"));
        // builder.add(asyncFile);
        //
        // AppenderComponentBuilder asyncWrapper = builder.newAppender("wrapper", "Async");
        // asyncWrapper.addComponent(builder.newAppenderRef("asyncFile"));
        // asyncWrapper.addAttribute("includeLocation", true);
        // builder.add(asyncWrapper);
        //
        // LoggerComponentBuilder asyncLog = builder.newLogger("async",
        // org.apache.logging.log4j.Level.ALL);
        // asyncLog.addAttribute("additivity", false);
        // asyncLog.add(builder.newAppenderRef("wrapper"));
        // builder.add(asyncLog);

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

        // org.apache.logging.log4j.Logger async = context.getLogger("async");
        // benchmark.measure("Log4j Async", () -> {
        // async.info("Message");
        // return -1;
        // });
    }

    private static void performTinyLog(Benchmark benchmark) throws Exception {
        Configuration.set("writer", "file");
        Configuration.set("writer.file", "logging-tinylog.log");
        Configuration.set("writer.format", "{date:yyyy-MM-dd HH:mm:ss.SSS} {level} {class} {method} {message}");
        Configuration.set("writer.append", "false");
        Configuration.set("writingthread", "true");

        benchmark.measure("TinyLog Async", () -> {
            org.tinylog.Logger.info("Message");
            return -1;
        });
    }

    private static void performLogback(Benchmark benchmark) throws Exception {
        ch.qos.logback.classic.LoggerContext context = new ch.qos.logback.classic.LoggerContext();

        // ================================
        // Sync Logger
        // ================================
        PatternLayoutEncoder layout = new PatternLayoutEncoder();
        layout.setContext(context);
        layout.setPattern("%date{YYYY-MM-dd HH:mm:ss.SSS} %level %class %method %msg%n");
        layout.start();

        FileAppender file = new FileAppender();
        file.setContext(context);
        file.setName("file");
        file.setEncoder(layout);
        file.setAppend(false);
        file.setFile("logging-logback.log");
        file.start();

        ch.qos.logback.classic.Logger log = context.getLogger(LogBenchmark.class);
        log.setAdditive(false);
        log.addAppender(file);
        log.setLevel(ch.qos.logback.classic.Level.ALL);

        benchmark.measure("Logback", () -> {
            log.info("Message");
            return -1;
        });

        // ================================
        // Async Logger
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
    }

    private static void performCustomJUL(Benchmark benchmark) throws Exception {
        benchmark.measure("Custom JUL", () -> {
            I.info("Message");
            return -1;
        });
    }
}