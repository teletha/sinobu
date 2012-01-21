/*
 * Copyright (C) 2012 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package hub.powerassert;

import kiss.Extensible;

/**
 * @version 2012/01/21 16:35:09
 */
public interface Renderer<T> extends Extensible {

    String getDescription(T value);
}
