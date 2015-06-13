/*
 * Copyright (C) 2015 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.scratchpad;

/**
 * DOCUMENT.
 * 
 * @version 2008/05/31 12:07:50
 */
public interface Adaptable {

    <M> M getAdapter(Class<M> model);
}
