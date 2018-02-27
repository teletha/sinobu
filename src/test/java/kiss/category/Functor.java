/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.category;

import java.util.function.Function;

/**
 * @version 2016/03/30 2:56:04
 */
public interface Functor<Param> {

    <Return> Functor<Return> fmap(Function<Param, Return> function);
}
