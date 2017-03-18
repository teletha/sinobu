/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
public class Project extends bee.api.Project {

    {
        product("com.github.teletha", "sinobu", "1.0");
        producer("Nameless Production Committee");
        describe("Sinobu is not obsolete framework but utility, which can manipulate objects as a extremely-condensed facade.");

        require("org.ow2.asm", "asm", "5.2");
        require("com.github.teletha", "antibug", "0.3").atTest();
        require("io.reactivex", "rxjava", "1.2.7");

        versionControlSystem("https://github.com/teletha/Sinobu");
    }
}
