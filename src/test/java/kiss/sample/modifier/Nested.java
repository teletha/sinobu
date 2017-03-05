/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.sample.modifier;

/**
 * @version 2010/01/21 11:17:44
 */
public class Nested {

    public static class PublicStatic {
    }

    protected static class ProtectedStatic {
    }

    static class PackagePrivateStatic {
    }

    @SuppressWarnings("unused")
    private static class PrivateStatic {
    }

    public class Public {
    }

    protected class Protected {
    }

    class PackagePrivate {
    }

    @SuppressWarnings("unused")
    private class Private {
    }
}
