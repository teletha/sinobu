/*
 * Copyright (C) 2016 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss;

/**
 * @version 2016/08/01 22:39:25
 */
public interface ExtensionFactory<E extends Extensible> extends Extensible {

    E create(Class key);
}
