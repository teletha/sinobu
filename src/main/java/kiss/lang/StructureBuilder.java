/*
 * Copyright (C) 2016 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.lang;

/**
 * @version 2017/02/06 14:04:56
 */
public abstract class StructureBuilder<N> {

    public abstract N enterNode(N parent, String name);

    public abstract void leaveNode(String name);

    public abstract void attribute(N node, String name, String value);
}
