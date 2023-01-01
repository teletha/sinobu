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

public interface OnlySetter {

    /**
     * Operation.
     */
    void setString(String value);

    /**
     * Operation.
     */
    void setInt(int value);

    /**
     * Operation.
     */
    void setMyself(OnlySetter value);
}