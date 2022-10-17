/*
 * Copyright (C) 2022 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss;

import kiss.core.ExpressionBenchmark;
import kiss.json.JSONMappingBenchmark;
import kiss.json.JSONParseHugeBenchmark;
import kiss.json.JSONParseLongBenchmark;
import kiss.json.JSONParseShortBenchmark;
import kiss.json.JSONTraverseBenchmark;
import kiss.xml.XMLParseBenchmark;

public class VisualizableBenchmark {

    public static void main(String[] args) throws Exception {
        LogBenchmark.main(args);
        ExpressionBenchmark.main(args);
        XMLParseBenchmark.main(args);

        JSONParseShortBenchmark.main(args);
        JSONParseLongBenchmark.main(args);
        JSONParseHugeBenchmark.main(args);
        JSONMappingBenchmark.main(args);
        JSONTraverseBenchmark.main(args);
    }
}
