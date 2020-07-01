/*
 * Copyright (C) 2020 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss;

import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import antibug.CleanRoom;

@Execution(ExecutionMode.SAME_THREAD)
class TranscriptTest {

    static String originalLanguage;

    @RegisterExtension
    final CleanRoom room = new CleanRoom();

    @BeforeEach
    private void initialize() {
        I.Lang.set("en");
        I.env("LangDirectory", room.locateDirectory("transcript").toAbsolutePath().toString());

        I.bundles.clear();
    }

    @BeforeAll
    static void startup() {
        originalLanguage = I.Lang.v;
    }

    @AfterAll
    static void cleanup() {
        I.Lang.set(originalLanguage);
    }

    /**
     * Create bundle dynamically.
     * 
     * @param lang
     * @param base
     * @param translated
     */
    private void createBundle(String lang, String base, String translated) {
        Bundle bundle = new Bundle(lang);
        bundle.put(base, translated);
        bundle.store();
    }

    /**
     * Wait for online translation result.
     * 
     * @param text
     */
    private void waitForTranslation(Variable<String> text) {
        try {
            CompletableFuture future = new CompletableFuture();
            text.observe().to(future::complete);
            future.get();
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }

    /**
     * Wait for online translation result.
     * 
     * @param text
     */
    private void waitForTranslationTo(String lang, Variable<String> text) {
        try {
            CompletableFuture future = new CompletableFuture();
            text.observe().to(future::complete);

            I.Lang.set(lang);
            future.get();
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }

    @Test
    void base() {
        Variable<String> text = I.translate("test");
        assert text.is("test");
    }

    @Test
    void nullInput() {
        Assertions.assertThrows(NullPointerException.class, () -> I.translate((String) null));
    }

    @Test
    void context() {
        Variable<String> text = I.translate("You can use {0}.", "context");
        assert text.is("You can use context.");
    }

    @Test
    void contexts() {
        Variable<String> text = I.translate("You can {1} {0}.", "context", "use");
        assert text.is("You can use context.");
    }

    @Test
    void translateByBundle() {
        createBundle("fr", "base", "nombre d'unités");
        createBundle("ja", "base", "基数");

        Variable<String> text = I.translate("base");
        assert text.is("base");

        I.Lang.set("fr");
        assert text.is("nombre d'unités");

        I.Lang.set("ja");
        assert text.is("基数");
    }

    @Test
    void translateByOnline() {
        Variable<String> text = I.translate("Water");
        assert text.is("Water");

        // Immediately after the language change,
        // it has not yet been translated due to network usage.
        I.Lang.set("de");
        assert text.is("Water");

        // It will be reflected when the translation results are available.
        assert text.acquire().equals("Wasser");

        // Immediately after the language change,
        // it has not yet been translated due to network usage.
        I.Lang.set("ja");
        assert text.is("Wasser");

        // It will be reflected when the translation results are available.
        waitForTranslation(text);
        assert text.is("水");
    }

    @Test
    void translateLineFeed() {
        Variable<String> text = I.translate("one\ntwo");

        waitForTranslationTo("fr", text);
        assert text.is("Un deux");
    }

    @Test
    void translateCarrigeReturn() {
        Variable<String> text = I.translate("three\rfour");

        waitForTranslationTo("zh", text);
        assert text.is("三四");
    }

    @Test
    void translateBreak() {
        Variable<String> text = I.translate("five\r\nsix");

        waitForTranslationTo("ru", text);
        assert text.is("пять шести");
    }

    @Test
    void translateContextParameter() {
        Variable<String> text = I.translate("The correct answer is {0}.", "101");

        waitForTranslationTo("es", text);
        assert text.is("La respuesta correcta es 101.");
    }
}