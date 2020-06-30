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
            Map<String, String> bundle = bundles.computeIfAbsent(lang, Transcript::load);
            String cached = bundle.get(text);
            if (cached != null) {
                return I.signal(cached);
            }

            // Finally, we will attempt to translate dynamically online.
            return I.http(HttpRequest.newBuilder()
                    .uri(URI.create("https://www.ibm.com/demos/live/watson-language-translator/api/translate/text"))
                    .header("Content-Type", "application/json")
                    .POST(BodyPublishers.ofString("{\"text\":\"" + text + "\",\"source\":\"en\",\"target\":\"" + lang + "\"}")), JSON.class)
                    .flatMap(v -> v.find("payload.translations.0.translation", String.class))
                    .skipNull()
                    .effect(translated -> {
                        bundle.put(text, translated);
                        save.accept(lang);
                    });
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
    /** The root directory of cache. */
    static Path root = Path.of(I.env("transcript.cache", System.getProperty("user.dir") + "/language"));

    /** In-memory cache for dynamic bundles. */
    static Map<String, Map<String, String>> bundles = new HashMap();

    /** Coordinator of preservation timing */
    private static final Signaling<String> save = new Signaling();

    static {
        save.expose.debounce(20, TimeUnit.SECONDS).to(Transcript::save);
    }

    /**
     * Saves the translation of the specified language to the local disk. The next time it is read
     * from here, it can help reduce translation resources.
     * 
     * @param lang ISO 639 alpha-2 or alpha-3 language code, or registration of up to 8 English
     *            characters Pre-language subtags (for future extensions). If the language has both
     *            alpha-2 and alpha-3 codes, use the alpha-2 code.
     * @return
     */
    static Map<String, String> load(String lang) {
        Map<String, String> map = new ConcurrentSkipListMap();
        BufferedReader reader = null;

        try {
            reader = Files.newBufferedReader(root.resolve(lang + ".txt"));
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
    }

    /**
     * Write bundle data to local cache.
     * 
     * @param lang ISO 639 alpha-2 or alpha-3 language code, or registration of up to 8 English
     *            characters Pre-language subtags (for future extensions). If the language has both
     *            alpha-2 and alpha-3 codes, use the alpha-2 code.
     */
    static void save(String lang) {
        Path path = root.resolve(lang + ".txt");
        BufferedWriter writer = null;
        try {
            Files.createDirectories(path.getParent());
            writer = Files.newBufferedWriter(path);
            for (Entry<String, String> entry : bundles.get(lang).entrySet()) {
                writer.append(entry.getKey().replace('\r', '﹍').replace('\n', '␣')).append("\n");
                writer.append(entry.getValue().replace('\r', '﹍').replace('\n', '␣')).append("\n\n");
            }
        } catch (Exception e) {
            // ignore
        } finally {
            I.quiet(writer);
        }
    }
}
