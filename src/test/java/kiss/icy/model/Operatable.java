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

/**
 * @version 2015/04/28 13:19:49
 */
public interface Operatable<M extends Operatable> {

    /**
     * <p>
     * Create new immutable model.
     * </p>
     * 
     * @return An immutable model.
     */
    default M ice() {
        return (M) this;
    }

    /**
     * <p>
     * Create new mutable model.
     * </p>
     * 
     * @return An immutable model.
     */
    default M melt() {
        return (M) this;
    }
}
