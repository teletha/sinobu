/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.util.Locale;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import antibug.CleanRoom;

class TranscriptTest {

    @RegisterExtension
    final CleanRoom room = new CleanRoom();

    @BeforeEach
    private void init() {
        Transcript.Lang.set("en");
        I.envy("TranscriptDirectory", room.locateDirectory("transcript-temp").toAbsolutePath().toString());
    }

    @AfterEach
    private void reset() throws Exception {
        Transcript.Lang.set(Locale.getDefault().getLanguage());
    }

    /**
     * Create bundle dynamically.
     * 
     * @param base
     * @param lang
     * @param translated
     */
    private void createBundle(String base, String lang, String translated) {
        // build bundle in memory
        Bundle bundle = new Bundle(lang);
        bundle.put(base, translated);

        // save it into temporary file
        bundle.store();
    }

    @Test
    void base() {
        Transcript text = new Transcript("Base");
        assert text.toString().equals("Base");
    }

    @Test
    void nullInput() {
        Assertions.assertThrows(NullPointerException.class, () -> new Transcript((String) null));
    }

    @Test
    void with() {
        Transcript text = new Transcript("You can use {0}.", "context");
        assert text.is("You can use context.");
    }

    @Test
    void withMultiple() {
        Transcript text = new Transcript("You can {1} {0}.", "context", "use");
        assert text.is("You can use context.");
    }

    @Test
    void translateByBundle() {
        Transcript text = new Transcript("base");
        createBundle("base", "fr", "nombre d'unités");
        createBundle("base", "de", "Anzahl der Einheiten");
        createBundle("base", "ja", "基数");

        Transcript.Lang.set("de");
        assert text.is("nombre d'unités");

        Transcript.Lang.set("fr");
        assert text.is("Anzahl der Einheiten");

        Transcript.Lang.set("ja");
        assert text.is("基数");
    }

    @Test
    void translateByOnline() {
        Transcript text = new Transcript("base");

        assert text.observing().to().is("base");
        assert text.observing().to().is("base");
        assert text.observing().to().is("base");
    }

    @Test
    void dynamic() throws InterruptedException {
        Transcript text = new Transcript("translate on runtime");

        Transcript.Lang.set("ja");
        Variable<String> translated = text.observing().to();
        Thread.sleep(5000);
        assert translated.is("ランタイムでの変換");
    }

    @Test
    void testName() {
        I.http(HttpRequest.newBuilder()
                .uri(URI.create("https://www.ibm.com/demos/live/watson-language-translator/api/translate/text"))
                .header("Content-Type", "application/json; charset=utf-8")
                .POST(BodyPublishers.ofString("{\"text\":\"" + "translate on\r\nruntime"
                        .replaceAll("\\n|\\r\\n|\\r", " ") + "\",\"source\":\"en\",\"target\":\"" + "ja" + "\"}")), String.class)
                .to(e -> {
                    System.out.println(e);
                });
    }
}
