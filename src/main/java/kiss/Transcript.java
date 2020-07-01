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

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.util.List;

/**
 * The text will be automatically translated. Basic sentences must be written in English. It will be
 * translated online automatically into the language specified in the global variable "lang". Once
 * the text is translated, it is saved to the local disk and loaded from there in the future.
 */
public final class Transcript extends Variable<String> {

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

        I.Lang.observing().switchMap(lang -> {
            // First, check inline cache.
            if ("en".equals(lang)) {
                return I.signal(text);
            }

            // The next step is to check for already translated text from
            // the locally stored bundle files. Iit can help reduce translationresources.
            Bundle bundle = I.bundles.computeIfAbsent(lang, Bundle::new);
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
                            .replaceAll("[\\n|\\r]+", " ") + "\",\"source\":\"en\",\"target\":\"" + lang + "\"}")), JSON.class)
                    .flatMap(v -> v.find("payload.translations.0.translation", String.class))
                    .skipNull()
                    .map(v -> {
                        bundle.put(text, v);
                        I.bundleSave.accept(bundle);
                        return v;
                    });
        }).startWith(text).to(v -> {
            set(I.express(v, List.of(context)));
        });
    }
}