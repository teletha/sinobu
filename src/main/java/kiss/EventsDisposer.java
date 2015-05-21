/*
 * Copyright (C) 2015 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss;

import java.util.ArrayList;
import java.util.List;

/**
 * @version 2015/05/21 9:55:44
 */
public class EventsDisposer implements Disposable {

    private final List<Disposable> disposables = new ArrayList();

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        for (Disposable disposable : disposables) {
            disposable.dispose();
        }
        disposables.clear();
    }

    public EventsDisposer add(Disposable disposable) {
        if (disposable != null) {
            disposables.add(disposable);
        }
        return this;
    }
}
