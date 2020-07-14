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

import java.util.concurrent.ConcurrentSkipListMap;

@SuppressWarnings("serial")
class Bundle extends ConcurrentSkipListMap<String, String> implements Storable<Bundle> {

    private final String lang;

    /**
     * @param lang
     */
    Bundle(String lang) {
        this.lang = lang;

        restore();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String locate() {
        return I.env("LangDirectory", "lang") + "/" + lang + ".json";
    }
}