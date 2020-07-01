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

/**
 * @version 2016/08/01 22:39:25
 */
public interface ExtensionFactory<E extends Extensible> extends Extensible {

    /**
     * <p>
     * Create the extension by the specified key.
     * </p>
     * 
     * @param key An extension key.
     * @return A created extension.
     */
    E create(Class key);
}