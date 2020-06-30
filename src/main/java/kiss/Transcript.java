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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.TimeUnit;

/**
 * The text will be automatically translated. Basic sentences must be written in English. It will be
 * translated online automatically into the language specified in the global variable "lang". Once
 * the text is translated, it is saved to the local disk and loaded from there in the future.
 */
public final class Transcript extends Variable<String> implements CharSequence {

    /** The default language in the current environment. */
    public static final Variable<Locale> Lang = Variable.of(Locale.getDefault());

    /**
     * The text will be automatically translated. Basic sentences must be written in English. It
     * will be translated online automatically into the language specified in the global variable
     * {@link #Lang}. Once the text is translated, it is saved to the local disk and loaded from
     * there in the future.
     * 
     * @param text Basic English sentences.
     * @param context Parameters to be assigned to variables in a sentence. (Optional)
     */
    public Transcript(String text, Object... context) {
        super(null);

        Lang.observing().switchMap(locale -> {
            String lang = locale.getLanguage();

            // First, check inline cache.
            if (lang.equals("en")) {
                return I.signal(text);
            }

            // The next step is to check for already translated text from
            // the locally stored bundle files.
            Map<String, String> bundle = bundles.computeIfAbsent(lang, a -> {
                Map<String, String> map = new ConcurrentSkipListMap();
                BufferedReader reader = null;

                try {
                    reader = Files.newBufferedReader(Path.of(I.env("TranslationDirectory", "language") + "/" + lang + ".txt"));
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        map.put(line.replace('﹍', '\r').replace('␣', '\n'), reader.readLine().replace('﹍', '\r').replace('␣', '\n'));
                        reader.readLine();
                    }
                } catch (Exception e) {
                    // ignore
                } finally {
                    I.quiet(reader);
                }
                return map;
            });

            String cached = bundle.get(text);
            if (cached != null) {
                return I.signal(cached);
            }

            // Finally, we will attempt to translate dynamically online.
            action.accept(I.pair(lang, text));
            return I.signal(text);
        }).startWith(text).to(v -> {
            set(I.express(v, List.of(context)));
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int length() {
        return toString().length();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public char charAt(int index) {
        return toString().charAt(index);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CharSequence subSequence(int start, int end) {
        return toString().subSequence(start, end);
    }

    // ===================================================================
    // Resource Bundle Implemetation
    // ===================================================================
    /** In-memory cache for dynamic bundles. */
    static Map<String, Map<String, String>> bundles = new HashMap();

    /** Coordinator of translation timing */
    private static final Signaling<Ⅱ<String, String>> action = new Signaling();

    static {
        action.expose
                // Perform the translation online. We do not want to make more than one request
                // at the same time, so we have certain intervals.
                .interval(1, TimeUnit.SECONDS)
                .flatMap(text -> {
                    return I.http(HttpRequest.newBuilder()
                            .uri(URI.create("https://www.ibm.com/demos/live/watson-language-translator/api/translate/text"))
                            .header("Content-Type", "application/json")
                            .POST(BodyPublishers
                                    .ofString("{\"text\":\"" + text.ⅱ + "\",\"source\":\"en\",\"target\":\"" + text.ⅰ + "\"}")), JSON.class)
                            .flatMap(v -> v.find("payload.translations.0.translation", String.class))
                            .skipNull()
                            .map(v -> {
                                bundles.get(text.ⅰ).put(text.ⅱ, v);
                                return text.ⅲ(v);
                            });
                })
                // Automatic translation is often done multiple times in a short period of time, and
                // it is not efficient to save the translation results every time you get them, so
                // it is necessary to process them in batches over a period of time.
                .debounce(30, TimeUnit.SECONDS)
                .to(text -> {
                    // Saves the translation of the specified language to the local disk. The next
                    // time it is read from here, it can help reduce translation resources.
                    Path path = Path.of(I.env("TranslationDirectory", "language") + "/" + text.ⅰ + ".txt");
                    BufferedWriter writer = null;
                    try {
                        Files.createDirectories(path.getParent());
                        writer = Files.newBufferedWriter(path);
                        for (Entry<String, String> entry : bundles.get(text.ⅰ).entrySet()) {
                            writer.append(entry.getKey().replace('\r', '﹍').replace('\n', '␣')).append("\n");
                            writer.append(entry.getValue().replace('\r', '﹍').replace('\n', '␣')).append("\n\n");
                        }
                    } catch (Exception e) {
                        // ignore
                    } finally {
                        I.quiet(writer);
                    }
                });
    }
}
