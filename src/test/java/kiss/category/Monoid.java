/*
 * Copyright (C) 2016 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.category;

import kiss.Extensible;

/**
 * @version 2016/03/29 14:25:53
 */
public interface Monoid<A> extends Extensible {

    A empty();

    A append(A one, A other);
}
