/*
 * Copyright (C) 2015 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.icy;

/**
 * @version 2015/04/19 19:32:31
 */
public interface ModelOperationNest<Ops extends ModelOperationSet, Sub extends ModelOperationSet> {

    void apply(Ops operations, Sub sub);
}
