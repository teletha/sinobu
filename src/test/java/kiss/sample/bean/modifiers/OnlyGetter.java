/*
 * Copyright (C) 2023 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.sample.bean.modifiers;

public interface OnlyGetter {

    /**
     * Getter.
     * 
     * @return A value.
     */
    String getString();

    /**
     * Getter.
     * 
     * @return A value.
     */
    int getInt();

    /**
     * Getter.
     * 
     * @return A value.
     */
    OnlyGetter getMyself();
}