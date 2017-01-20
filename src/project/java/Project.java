/*
 * Copyright (C) 2016 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
public class Project extends bee.api.Project {

    {
        product("npc", "Sinobu", "1.0.2");
        producer("Nameless Production Committee");
        describe("Sinobu is not obsolete framework but utility, which can manipulate objects as a extremely-condensed facade.");

        require("npc", "antibug", "0.3").atTest();
        repository("http://npc.bintray.com/maven");

        versionControlSystem("https://github.com/Teletha/Sinobu");
    }
}
