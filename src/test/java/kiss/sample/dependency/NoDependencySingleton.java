/*
 * Copyright (C) 2013 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.sample.dependency;

import kiss.Manageable;
import kiss.Singleton;

/**
 * @version 2009/07/01 18:04:56
 */
@Manageable(lifestyle = Singleton.class)
public class NoDependencySingleton {
}
