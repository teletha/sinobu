/*
 * Copyright (C) 2012 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.html;

/**
 * @version 2012/02/22 16:16:05
 */
public class CSS {

    protected static final Unit em = new Unit("em");

    protected static final Unit percent = new Unit("%");

    protected void margin(float size, Unit unit) {

    }

    protected void indent(float size, Unit unit) {

    }

    public Style main() {
        return new Style() {

            {
                indent(2, em);
                margin(2, em);
            }
        };
    }

    /**
     * @version 2012/02/22 16:29:03
     */
    public static class Style {

    }

    /**
     * @version 2012/02/22 16:17:46
     */
    private static final class Unit {

        private String unit;

        /**
         * @param unit
         */
        private Unit(String unit) {
            this.unit = unit;
        }
    }
}
