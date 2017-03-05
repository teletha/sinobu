/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.sample.bean.invalid;

/**
 * DOCUMENT.
 * 
 * @author <a href="mailto:Teletha.NPC@gmail.com">Teletha Testarossa</a>
 * @version $ Id: StaticAccessor.java,v 1.0 2007/04/25 18:30:43 Teletha Exp $
 */
public class StaticAccessor {

    private static String name;

    /**
     * Get the name property of this {@link StaticAccessor}.
     * 
     * @return The name prperty.
     */
    public static String getName() {
        return name;
    }

    /**
     * Set the name property of this {@link StaticAccessor}.
     * 
     * @param name The name value to set.
     */
    public static void setName(String name) {
        StaticAccessor.name = name;
    }
}
