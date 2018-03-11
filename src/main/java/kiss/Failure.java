/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss;

/**
 * @version 2017/05/13 21:04:03
 */
@SuppressWarnings("serial")
public class Failure extends Throwable {

    /**
     * @param message
     */
    public Failure(String message) {
        super(message, null, true, false);
    }

}