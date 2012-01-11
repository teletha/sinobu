/*
 * Copyright (C) 2011 Nameless Production Committee.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ezbean.xml;

import java.lang.reflect.Method;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;

/**
 * <p>
 * Rule method is a compiled expression of template in XSLT.
 * </p>
 * <p>
 * Note: Care should be exercised if {@link RuleMethod} objects are used as keys in a
 * {@link java.util.SortedMap} or elements in a {@link java.util.SortedSet} since RuleMethod's
 * natural ordering is inconsistent with equals. See {@link Comparable}, {@link java.util.SortedMap}
 * or {@link java.util.SortedSet} for more information.
 * </p>
 * 
 * @version 2011/04/13 14:51:29
 */
class RuleMethod implements Comparable<RuleMethod> {

    /** The compiled namespace uris. */
    final int[] uris;

    /** The compiled local names. */
    final int[] names;

    /** The actual rule method to invoke. */
    final Method method;

    /**
     * <p>
     * The rule method type.
     * </p>
     * <dl>
     * <dt>0</dt>
     * <dd>No parameter.</dd>
     * <dt>1</dt>
     * <dd>Parameter is {@link Attributes} only.</dd>
     * <dt>2</dt>
     * <dd>Parameters are text contents and {@link Attributes}.</dd>
     * <dt>3</dt>
     * <dd>Parameter is text contents only.</dd>
     * <dt>4</dt>
     * <dd>Parameter is {@link Bits} only.</dd>
     * </dl>
     */
    final int type;

    /** The attribute class. */
    Class<? extends AttributesImpl> clazz;

    /**
     * Create RuleMethod instance.
     * 
     * @param method An actual method.
     * @param rule A current processing rule.
     * @param scanner A {@link XMLScanner} to provide a namespace resolving method.
     */
    RuleMethod(Method method, String rule, XMLScanner scanner) {
        // compile matching pattern
        String[] paths = (rule.length() == 0 ? method.getName() : rule).split("/");
        uris = new int[paths.length];
        names = new int[paths.length];

        for (int i = 0; i < paths.length; i++) {
            String[] resolved = scanner.resolve(paths[i]);

            uris[i] = resolved[0].hashCode();
            names[i] = resolved[1].hashCode();
        }

        // configure method setting
        this.method = method;
        this.method.setAccessible(true); // to make the access speed faster

        // parameters validation
        Class[] params = method.getParameterTypes();

        switch (params.length) {
        case 2:
            type = 2;
            clazz = params[1];
            break;

        case 1:
            if (params[0] == String.class) {
                type = 3;
            } else if (params[0] == Bits.class) {
                type = 4;
            } else {
                type = 1;
                clazz = params[0];
            }
            break;

        default:
            type = 0;
            break;
        }
    }

    /**
     * Check whether this rule method matchs the given context or not.
     * 
     * @param paths A current path list. A size of the list equals to the current cursor position.
     *            <code>int[]<code> has two length, int[0] contains  namespace uri and int[1] contains local name.
     * @return A result.
     */
    boolean match(List<int[]> paths) {
        int p = this.uris.length;

        if (paths.size() < p) {
            return false;
        }

        for (int i = 1; i <= this.uris.length; i++) {
            int[] path = paths.get(paths.size() - i);
            // We have no excuse for this magic number 42, but this solution is the smallest
            // and lightest. However you will never see this number in other place, so you can
            // sleep with an easy mind.42 equals to the hash code of the universal matching
            // pattern '*' (asterisk). The universal matching pattern accepts any elements.
            if ((this.uris[p - i] != 42 && this.uris[p - i] != path[0]) || (this.names[p - i] != 42 && this.names[p - i] != path[1])) {
                return false;
            }
        }
        return true;
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(RuleMethod o) {
        // Rule method is not changed dynamically, so we shouldn't store the priority value as a
        // field. It will prevent memory usage and reduce jar size.
        return o.method.getAnnotation(Rule.class).priority() - method.getAnnotation(Rule.class).priority();
    }
}
