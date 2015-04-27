/*
 * Copyright (C) 2015 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.icy.model;

import java.util.function.BiFunction;

/**
 * @version 2015/04/27 16:54:50
 */
public interface Action<M, P> extends BiFunction<M, P, M> {
}
