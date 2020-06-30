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
    private void createBundle(Transcript base, String lang, String translated) {
        // build bundle in memory
        Bundle bundle = new Bundle(lang);
        bundle.put(base.v, translated);
        bundle.put("WARNING : This setting will allow all operations on your account.", "注意 : この設定を行うとあなたのアカウントに対する全ての操作を許可することになります。");
        bundle.put("You can notify LINE by specifying the access token acquired from [LINE Notify](https://notify-bot.line.me/).", "[LINE Notify](https://notify-bot.line.me/)から取得したアクセストークンを指定することでLINEに通知することが出来ます。");
        bundle.put("Display a grouped board with a specified price range.", "指定された値幅でまとめられた板を表示します。");

        // save it into temporary file
        Transcript.bundles.put(lang, bundle);
        bundle.store();

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
        Assertions.assertThrows(NullPointerException.class, () -> new Transcript((String) null));
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
