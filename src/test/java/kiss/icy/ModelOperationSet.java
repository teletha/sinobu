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

import kiss.Extensible;

/**
 * @version 2015/04/19 19:50:36
 */
public interface ModelOperationSet<M> extends Extensible {

    /**
     * <p>
     * Create new instance of the associated model.
     * </p>
     * 
     * @return
     */
    M build();

    /**
     * <p>
     * Assign property form the specified model.
     * </p>
     * 
     * @param model
     */
    void with(M model);
}
