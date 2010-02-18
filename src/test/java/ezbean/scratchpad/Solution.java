/*
 * Copyright (C) 2010 Nameless Production Committee.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ezbean.scratchpad;

import java.util.ArrayList;

import ezbean.Extensible;
import ezbean.I;
import ezbean.Manageable;
import ezbean.ThreadSpecific;

/**
 * @version 2010/02/18 14:04:22
 */
@SuppressWarnings("serial")
@Manageable(lifestyle = ThreadSpecific.class)
public class Solution extends Error implements Extensible {

    /** The solutions. */
    private ArrayList<String> solutions = new ArrayList();

    private boolean empty = true;

    /**
     * @see java.lang.Throwable#getMessage()
     */
    @Override
    public String getMessage() {
        StringBuilder builder = new StringBuilder("\r\n");

        for (int i = 0; i < solutions.size(); i++) {
            builder.append(i + 1).append(")\r\n").append(solutions.get(i)).append("\r\n\r\n");
        }

        // clear
        empty = true;
        solutions.clear();

        return builder.toString();
    }

    public static Solution quiet(Object solution, Object... params) {
        Solution solver = I.make(Solution.class);

        if (solver.empty && solution instanceof Throwable) {
            solver.empty = false;

            Throwable throwable = (Throwable) solution;
            solver.setStackTrace(throwable.getStackTrace());
        }
        solver.solutions.add(String.format(solution.toString(), params));

        return solver;
    }
}
