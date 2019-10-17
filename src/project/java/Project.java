
/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
import bee.task.Test;

public class Project extends bee.api.Project {

    {
        product("com.github.teletha", "sinobu", "1.5");
        producer("Nameless Production Committee");
        describe("Sinobu is not obsolete framework but utility, which can manipulate objects as a extremely-condensed facade.");

        require("com.github.teletha", "antibug").atTest();
        require("io.reactivex.rxjava2", "rxjava").atTest();
        require("com.fasterxml.jackson.core", "jackson-databind").atTest();

        versionControlSystem("https://github.com/teletha/sinobu");
    }

    {
        Test.showProlongedTest = 300;
    }
}
