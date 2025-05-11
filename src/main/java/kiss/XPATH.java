/*
 * Copyright (C) 2025 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XPATH {

    private static final Pattern SELECTOR = Pattern.compile(""//
            // Group 1: Combinator
            + "\\s*([>+~<\\s,])\\s*"
            // Group 2: Tag name (ns|tag, tag, or *)
            + "|((?:(?:\\w+|\\*)\\|)?(?:[\\w\\-]+(?:\\\\.[\\w\\-]*)*|\\*))"
            // Group 3: ID
            + "|#((?:[\\w\\-]|\\\\.)+)"
            // Group 4: Class
            + "|\\.((?:[\\w\\-]|\\\\.)+)"
            // Group 5: Attribute selector
            // G5:attrName, G6:op, G7:quote, G8:q_val, G9:unq_val. Backref \\7 is correct.
            + "|\\[\\s?([\\w\\-_:]+)(?:\\s*([=~^$*|])?=\\s*(?:(['\"])(.*?)\\7|([^\\]\\s]+)))?\\s*\\]"
            // Pseudo-class part
            // G10: Pseudo-class name (e.g., "nth-child", "not")
            // G11: Pseudo-class argument (e.g., "2n+1", "p.foo", null if no arg)
            + "|:([\\w-]+)(?:\\((.*)\\))?");

    /**
     * @param selector
     * @param axis
     * @return
     */
    public static String convert(String selector, String axis) {
        if (selector.startsWith("xpath:")) return selector.substring(6);

        String suffix = null;
        String currentTag = null;
        StringBuilder xpath = new StringBuilder();
        Matcher matcher = SELECTOR.matcher(selector.trim());
        boolean add = true;

        while (matcher.find()) {
            boolean contextual = matcher.start() == 0;

            // =================================================
            // Combinators
            // =================================================
            String match = matcher.group(1);
            if (match == null) {
                if (contextual) {
                    xpath.append(axis);
                }
            } else {
                match = match.trim();

                if (match.length() == 0) {
                    // Descendant combinator
                    xpath.append("//");
                } else {
                    switch (match.charAt(0)) {
                    case '>': // Child combinator
                        xpath.append('/');
                        break;

                    case '~': // General sibling combinator
                        xpath.append("/following-sibling::");
                        break;

                    case '+': // Adjacent sibling combinator
                        xpath.append("/following-sibling::*[1]");
                        add = false;
                        break;

                    case '<': // Adjacent previous sibling combinator (EXTENSION)
                        xpath.append("/preceding-sibling::");
                        suffix = "[1]";
                        break;

                    case ',': // selector separator
                        xpath.append("|".concat(axis));
                        break;
                    }

                    if (contextual) {
                        xpath.delete(0, 1);
                    }
                }
                continue;
            }

            // =================================================
            // Type (Universal) Selector
            // =================================================
            match = matcher.group(2);
            if (match != null) {
                if (match.equals("*")) {
                    xpath.append("*");
                    currentTag = "*";
                } else {
                    if (add) xpath.append('*');
                    xpath.append("[name()='").append(currentTag = match.replace('|', ':').replaceAll("\\\\(.)", "$1")).append("']");
                }
                continue;
            }

            if (add && xpath.charAt(xpath.length() - 1) == ':') xpath.append("*");

            // =================================================
            // ID Selector
            // =================================================
            match = matcher.group(3);
            if (match != null) {
                xpath.append("[@id='").append(match.replaceAll("\\\\(.)", "$1")).append("']");
                continue;
            }

            // =================================================
            // Class Selector
            // =================================================
            match = matcher.group(4);
            if (match != null) {
                xpath.append("[contains(concat(' ',normalize-space(@class),' '),' ")
                        .append(match.replaceAll("\\\\(.)", "$1"))
                        .append(" ')]");
                continue;
            }

            // =================================================
            // Attribute Selector
            //
            // "|\\[\\s?([\\w\\-_:]+)(?:\\s*([=~^$*|])?=\\s*(?:(['\"])(.*?)\\7|([^\\]\\s]+)))"
            // =================================================
            match = matcher.group(5);
            if (match != null) {
                String value = matcher.group(8);
                if (value == null) value = matcher.group(9);

                if (value == null) {
                    // [att]
                    //
                    // Represents an element with the att attribute, whatever the value
                    // of the attribute.
                    xpath.append("[@").append(match).append("]");
                } else {
                    String type = matcher.group(6);

                    if (type == null) {
                        // [att=val]
                        //
                        // Represents an element with the att attribute whose value
                        // is exactly "val".
                        xpath.append("[@").append(match).append("='").append(value).append("']");
                    } else {
                        switch (type.charAt(0)) {
                        case '~':
                            // [att~=val]
                            //
                            // Represents an element with the att attribute whose value is a
                            // whitespace-separated list of words, one of which is exactly
                            // "val". If "val" contains whitespace, it will never represent
                            // anything (since the words are separated by spaces). Also, if "val"
                            // is the empty string, it will never represent anything.
                            xpath.append("[contains(concat(' ',@").append(match).append(",' '),' ").append(value).append(" ')]");
                            break;

                        case '*':
                            // [att*=val]
                            //
                            // Represents an element with the att attribute whose value contains
                            // at least one instance of the substring "val". If "val" is the
                            // empty string then the selector does not represent anything.
                            xpath.append("[contains(@").append(match).append(",'").append(value).append("')]");
                            break;

                        case '^':
                            // [att^=val]
                            //
                            // Represents an element with the att attribute whose value begins
                            // with the prefix "val". If "val" is the empty string then the
                            // selector does not represent anything.
                            xpath.append("[starts-with(@").append(match).append(",'").append(value).append("')]");
                            break;

                        case '$':
                            // [att$=val]
                            //
                            // Represents an element with the att attribute whose value ends
                            // with the suffix "val". If "val" is the empty string then the
                            // selector does not represent anything.
                            xpath.append("[substring(@")
                                    .append(match)
                                    .append(", string-length(@")
                                    .append(match)
                                    .append(") - string-length('")
                                    .append(value)
                                    .append("') + 1) = '")
                                    .append(value)
                                    .append("']");
                            break;

                        case '|':
                            // [att|=val]
                            //
                            // Represents an element with the att attribute, its value either
                            // being exactly "val" or beginning with "val" immediately followed by
                            // "-" (U+002D).
                            xpath.append("[@")
                                    .append(match)
                                    .append("='")
                                    .append(value)
                                    .append("' or starts-with(@")
                                    .append(match)
                                    .append(", '")
                                    .append(value)
                                    .append("-')]");
                            break;
                        }
                    }
                }
                continue;
            }

            // =================================================
            // Structural Pseudo Classes Selector
            // =================================================
            match = matcher.group(10);
            if (match != null) {
                switch (match.hashCode()) {
                case -947996741: // only-child
                    xpath.append("[count(parent::*/*)=1]");
                    break;

                case 1455900751: // only-of-type
                    xpath.append("[count(parent::*/").append(currentTag).append(")=1]");
                    break;

                case 96634189: // empty
                    xpath.append("[not(node())]");
                    break;

                case 109267: // not
                case 103066: // has
                    xpath.append('[');

                    if (match.charAt(0) == 'n') {
                        xpath.append("not");
                    }
                    xpath.append('(');

                    String sub = convert(matcher.group(11), axis);

                    if (sub.startsWith("descendant::")) {
                        sub = sub.replace("descendant::", "descendant-or-self::");
                    }
                    xpath.append(sub).append(")]");
                    break;

                case -995424086: // parent
                    xpath.append("/parent::*");
                    break;

                case 3506402: // root
                    xpath.delete(0, xpath.length()).append("/*");
                    break;

                case -567445985: // contains
                    xpath.append("[contains(text(),'").append(matcher.group(11)).append("')]");
                    break;

                case -2136991809: // first-child
                case 835834661: // last-child
                case 1292941139: // first-of-type
                case 2025926969: // last-of-type
                case -1754914063: // nth-child
                case -1629748624: // nth-last-child
                case -897532411: // nth-of-type
                case -872629820: // nth-last-of-type
                    int coefficient = 0;
                    int remainder = 0;
                    if (match.startsWith("first")) {
                        remainder = 1;
                    } else if (match.startsWith("last")) {
                        remainder = 1;
                    } else {
                        // nth
                        String arg = matcher.group(11);
                        int index = arg.indexOf('n');

                        if (arg.equals("even")) {
                            coefficient = 2;
                            remainder = 0;
                        } else if (arg.equals("odd")) {
                            coefficient = 2;
                            remainder = 1;
                        } else if (index == -1) {
                            remainder = Integer.parseInt(arg);
                        } else {
                            String before = arg.substring(0, index);
                            String after = arg.substring(index + 1);

                            if (before.isEmpty()) {
                                coefficient = 1;
                            } else if (before.equals("-")) {
                                coefficient = -1;
                            } else {
                                coefficient = Integer.parseInt(before);
                            }

                            if (after.isEmpty()) {
                                remainder = 0;
                            } else {
                                remainder = Integer.parseInt(after);
                            }
                        }
                    }

                    int a = coefficient;
                    int b = remainder;

                    // construct xpath
                    xpath.append("[");
                    String siblingKind;
                    // `tagNameForPseudo` should be available from the outer scope of `convert`
                    // method
                    boolean isOfType = match.contains("of-type"); // pseudoName is `match`

                    if (isOfType) {
                        if ("*".equals(currentTag)) {
                            siblingKind = "*";
                        } else {
                            siblingKind = currentTag; // Assuming tagNameForPseudo is "p" or
                                                      // "ns:item"
                        }
                    } else {
                        siblingKind = "*";
                    }

                    // Optimization for first/last (A=0, B=1)
                    if (a == 0 && b == 1) {
                        if (match.contains("last")) {
                            xpath.append("not(following-sibling::").append(siblingKind).append(")");
                        } else { // first
                            xpath.append("not(preceding-sibling::").append(siblingKind).append(")");
                        }
                    } else { // General An+B, or B-only where B != 1
                        String positionExpression = "(count(" + (match.contains("last") ? "following"
                                : "preceding") + "-sibling::" + siblingKind + ") + 1)";
                        if (a == 0) { // B only (and B != 1)
                            if (b <= 0) {
                                xpath.append("false()");
                            } else {
                                xpath.append(positionExpression).append(" = ").append(b);
                            }
                        } else if (a > 0) { // An+B, A>0
                            String term = "(" + positionExpression + " - " + b + ")";
                            xpath.append("(").append(term).append(" mod ").append(a).append(" = 0)");
                            xpath.append(" and (").append(term).append(" div ").append(a).append(" >= 0)");
                        } else { // An+B, A<0
                            int absA = -a;
                            String term = "(" + b + " - " + positionExpression + ")";
                            xpath.append("(").append(term).append(" mod ").append(absA).append(" = 0)");
                            xpath.append(" and (").append(term).append(" div ").append(absA).append(" >= 0)");
                        }
                    }
                    xpath.append("]");
                    break;
                }
                continue;
            }
        }

        if (suffix != null) {
            xpath.append(suffix);
        }

        return xpath.toString();
    }

    public static void maisn(String[] args) {
        String convert = XPATH.convert("*.a .b\\.b #id\\.id", "");
        System.out.println(convert);
    }

}
