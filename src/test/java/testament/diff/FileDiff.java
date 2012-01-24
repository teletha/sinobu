/*
 * Copyright (C) 2012 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package testament.diff;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @version 2012/01/19 14:29:54
 */

public class FileDiff {

    public FileDiff(String fromFile, String toFile) {
        Character[] aLines = read(fromFile);
        Character[] bLines = read(toFile);
        List<Difference> diffs = (new Diff<Character>(aLines, bLines)).diff();

        for (Difference diff : diffs) {
            int delStart = diff.getDeletedStart();
            int delEnd = diff.getDeletedEnd();
            int addStart = diff.getAddedStart();
            int addEnd = diff.getAddedEnd();
            String from = toString(delStart, delEnd);
            String to = toString(addStart, addEnd);
            String type = delEnd != Difference.NONE && addEnd != Difference.NONE ? "c"
                    : (delEnd == Difference.NONE ? "a" : "d");

            System.out.println(from + type + to);

            if (delEnd != Difference.NONE) {
                printLines(delStart, delEnd, "<", aLines);
                if (addEnd != Difference.NONE) {
                    System.out.println("---");
                }
            }
            if (addEnd != Difference.NONE) {
                printLines(addStart, addEnd, ">", bLines);
            }
        }
    }

    protected void printLines(int start, int end, String ind, Character[] lines) {
        for (int lnum = start; lnum <= end; ++lnum) {
            System.out.println(ind + " " + lines[lnum]);
        }
    }

    protected String toString(int start, int end) {
        // adjusted, because file lines are one-indexed, not zero.

        StringBuffer buf = new StringBuffer();

        // match the line numbering from diff(1):
        buf.append(end == Difference.NONE ? start : (1 + start));

        if (end != Difference.NONE && start != end) {
            buf.append(",").append(1 + end);
        }
        return buf.toString();
    }

    protected Character[] read(String fileName) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            List<Character> contents = new ArrayList<Character>();

            String in;
            while ((in = br.readLine()) != null) {
                for (char c : in.toCharArray()) {
                    System.out.println(c);
                    contents.add(c);
                }
            }
            return (Character[]) contents.toArray(new Character[] {});
        } catch (Exception e) {
            System.err.println("error reading " + fileName + ": " + e);
            System.exit(1);
            return null;
        }
    }

    public static void main(String[] args) {

        new FileDiff("a.txt", "b.txt");

    }

}
