/*
 * Copyright (C) 2012 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package hub.powerassert;

/**
 * @version 2012/01/24 13:14:52
 */
@SuppressWarnings("serial")
public class PowerAssertionError extends AssertionError {

    final PowerAssertContext context;

    /**
     * @param message
     * @param cause
     */
    public PowerAssertionError(PowerAssertContext context) {
        super("");

        this.context = context;
    }

    /**
     * @param detailMessage
     */
    public PowerAssertionError(boolean detailMessage, PowerAssertContext context) {
        super(detailMessage);

        this.context = context;
    }

    /**
     * @param detailMessage
     */
    public PowerAssertionError(char detailMessage, PowerAssertContext context) {
        super(detailMessage);

        this.context = context;
    }

    /**
     * @param detailMessage
     */
    public PowerAssertionError(double detailMessage, PowerAssertContext context) {
        super(detailMessage);

        this.context = context;
    }

    /**
     * @param detailMessage
     */
    public PowerAssertionError(float detailMessage, PowerAssertContext context) {
        super(detailMessage);

        this.context = context;
    }

    /**
     * @param detailMessage
     */
    public PowerAssertionError(int detailMessage, PowerAssertContext context) {
        super(detailMessage);

        this.context = context;
    }

    /**
     * @param detailMessage
     */
    public PowerAssertionError(long detailMessage, PowerAssertContext context) {
        super(detailMessage);

        this.context = context;
    }

    /**
     * @param detailMessage
     */
    public PowerAssertionError(Object detailMessage, PowerAssertContext context) {
        super(detailMessage);

        this.context = context;
    }

    /**
     * @see java.lang.Throwable#getLocalizedMessage()
     */
    @Override
    public String getLocalizedMessage() {
        return super.getLocalizedMessage() + "\n" + context;
    }
}
