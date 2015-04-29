/*
 * Copyright (C) 2015 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.icy.model;

/**
 * @version 2015/04/25 11:58:21
 */
@Icy
public class ClubDef {

    public String name;

    public Person leader;

    public Seq<Person> members;
}
