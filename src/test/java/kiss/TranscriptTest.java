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

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import antibug.CleanRoom;

class TranscriptTest {

    @RegisterExtension
    final CleanRoom room = new CleanRoom();

    @BeforeEach
    private void init() {
        Transcript.Lang.set(Locale.ENGLISH);
        Transcript.root = room.locateDirectory("transcript-temp");
    }

    @AfterEach
    private void reset() throws Exception {
        Transcript.Lang.set(Locale.getDefault());
    }

    /**
     * Create bundle dynamically.
     * 
     * @param base
     * @param lang
     * @param translated
     */
    private void createBundle(Transcript base, String lang, String translated) {
        // build bundle in memory
        Map<String, String> bundle = new ConcurrentSkipListMap();
        bundle.put(base.v, translated);

        // save it into temporary file
        Transcript.bundles.put(lang, bundle);
        Transcript.save(lang);

        // remove from memory
        Transcript.bundles.remove(lang);
    }

    @Test
    void base() {
        Transcript text = new Transcript("Base");
        assert text.toString().equals("Base");
    }

    @Test
    void nullInput() {
        assertThrows(NullPointerException.class, () -> new Transcript((String) null));
    }

    @Test
    void with() {
        Transcript text = new Transcript("You can use {0}.", "context");
        assert text.toString().equals("You can use context.");
    }

    @Test
    void withMultiple() {
        Transcript text = new Transcript("You can {1} {0}.", "context", "use");
        assert text.toString().equals("You can use context.");
    }

    @Test
    @Disabled
    void translateByBundle() {
        Transcript text = new Transcript("base");
        createBundle(text, "fr", "nombre d'unités");
        createBundle(text, "de", "Anzahl der Einheiten");
        createBundle(text, "ja", "基数");

        assert text.observing().to().is("nombre d'unités");
        assert text.observing().to().is("Anzahl der Einheiten");
        assert text.observing().to().is("基数");
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

        Transcript.Lang.set(Locale.JAPANESE);
        Variable<String> translated = text.observing().to();
        Thread.sleep(2000);
        assert translated.is("ランタイムでの変換");
    }
}
