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
 * @version 2017/02/07 11:19:13
 */
public interface Definable<C> {

    /**
     * <p>
     * Define something.
     * </p>
     * 
     * @param context A current context.
     */
    void define(C context);
}
