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
    static final CleanRoom room = new CleanRoom();

    @BeforeEach
    void initialize() {
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
        Subscriber bundle = new Subscriber(lang);
        bundle.messages.put(base, translated);
        bundle.store();
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
    void disposable() {
        createBundle("fr", "base", "nombre d'unités");
        createBundle("ja", "base", "基数");

        Disposable disposer = Disposable.empty();
        Variable<String> text = I.translate(disposer, "base");
        assert text.is("base");

        I.Lang.set("fr");
        assert text.is("nombre d'unités");

        disposer.dispose();

        I.Lang.set("ja");
        assert text.is("nombre d'unités");
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
        assert text.next().equals("Wasser");

        // Immediately after the language change,
        // it has not yet been translated due to network usage.
        I.Lang.set("ja");
        assert text.is("Wasser");

        // It will be reflected when the translation results are available.
        assert text.next().equals("水");
    }

    @Test
    void translateLineFeed() {
        Variable<String> text = I.translate("one\ntwo");

        I.Lang.set("fr");
        assert text.next().equals("un deux");
    }

    @Test
    void translateCarrigeReturn() {
        Variable<String> text = I.translate("three\rfour");

        I.Lang.set("zh");
        assert text.next().equals("三四");
    }

    @Test
    void translateBreak() {
        Variable<String> text = I.translate("five\r\nsix");

        I.Lang.set("ru");
        assert text.next().equals("пять шесть");
    }

    @Test
    void translateContextParameter() {
        Variable<String> text = I.translate("The correct answer is {0}.", "101");

        I.Lang.set("es");
        assert text.next().equals("La respuesta correcta es 101.");
    }
}