/*
 * Copyright (C) 2015 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.icy;

/**
 * @version 2015/04/19 21:53:01
 */
public class Group {

    /** The leader. */
    public final Person leader;

    /** The group name. */
    public final String name;

    /**
     * @param leader
     * @param name
     */
    public Group(Person leader, String name) {
        this.leader = leader;
        this.name = name;
    }
}
