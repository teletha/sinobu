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
 * This is an extension point to dynamically create and retrieve an extension corresponding to a
 * given extension key at the associated extension point.
 */
public interface ExtensionFactory<E extends Extensible> extends Extensible {

    /**
     * Gets the extension corresponding to the given extension key at the associated extension
     * point.
     * 
     * @param key An extension key.
     * @return A created extension.
     */
    E create(Class key);
}