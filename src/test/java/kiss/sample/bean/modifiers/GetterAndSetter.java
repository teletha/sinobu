/*
 * Copyright (C) 2022 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.sample.bean.modifiers;

public interface GetterAndSetter {

    /**
     * Non accessor method.
     */
    void attack();

    /**
     * Non accessor method.
     */
    int getHealth(String name, int type, int version);

    /**
     * Non accessor method.
     */
    void setHealth(String name, int type, int version, int value);
}