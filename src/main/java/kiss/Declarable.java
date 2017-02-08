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
 * @version 2017/02/08 11:10:15
 */
public interface Declarable<N> {

    /**
     * <p>
     * Declare the definition.
     * </p>
     * 
     * @param context A context.
     */
    void declare(N context);
}
