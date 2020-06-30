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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * The text will be automatically translated. Basic sentences must be written in English. It will be
 * translated online automatically into the language specified in the global variable "lang". Once
 * the text is translated, it is saved to the local disk and loaded from there in the future.
 */
public final class Transcript extends Variable<String> implements CharSequence {

    /** The default language in the current environment. */
    public static final Variable<String> Lang = Variable.of(Locale.getDefault().getLanguage());

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

        Lang.observing().switchMap(lang -> {
            // First, check inline cache.
            if (lang.equals("en")) {
                return I.signal(text);
            }

            // The next step is to check for already translated text from
            // the locally stored bundle files. Iit can help reduce translationresources.
            Bundle bundle = bundles.computeIfAbsent(lang, Bundle::new);
            String cached = bundle.get(text);
            if (cached != null) {
                return I.signal(cached);
            }

            // Perform the translation online.
            // TODO We do not want to make more than one request at the same time,
            // so we have certain intervals.
            return I.http(HttpRequest.newBuilder()
                    .uri(URI.create("https://www.ibm.com/demos/live/watson-language-translator/api/translate/text"))
                    .header("Content-Type", "application/json")
                    .POST(BodyPublishers.ofString("{\"text\":\"" + text
                            .replaceAll("\\n|\\r\\n|\\r", " ") + "\",\"source\":\"en\",\"target\":\"" + lang + "\"}")), JSON.class)
                    .flatMap(v -> v.find("payload.translations.0.translation", String.class))
                    .skipNull()
                    .map(v -> {
                        bundles.get(lang).put(text, v);
                        action.accept(bundle);
                        return v;
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
    /** In-memory cache for dynamic bundles. */
    static Map<String, Bundle> bundles = new ConcurrentHashMap();

    /** Coordinator of translation timing */
    private static final Signaling<Bundle> action = new Signaling();

    static {
        // Saves the translation of the specified language to the local disk. The next time it is
        // read from here, it can help reduce translation resources.
        action.expose
                // Automatic translation is often done multiple times in a short period of time, and
                // it is not efficient to save the translation results every time you get them, so
                // it is necessary to process them in batches over a period of time.
                .debounce(30, TimeUnit.SECONDS)
                .to(Bundle::store);
    }
}
