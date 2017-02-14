/*
 * Copyright (C) 2016 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * Diffable node of tree structure.
 * </p>
 * 
 * @version 2017/02/14 13:54:02
 */
public class TreeNode<Context> {

    /** The associated user context. */
    protected Context context;

    /** The node identity. */
    protected int id;

    /** The children nodes. */
    List<TreeNode<Context>> nodes = new ArrayList();

    void add(Context context) {
    }

    void remove(Context context) {
    }

    void insert(Context context, Object index) {
    }

    /**
     * <p>
     * Move this item to end of the context.
     * </p>
     * 
     * @param context
     */
    void move(Context context) {
    }

    /**
     * <p>
     * Replace child item.
     * </p>
     * 
     * @param context A context.
     * @param item A new item.
     */
    void replace(Context context, TreeNode<Context> item) {
    }

    /**
     * <p>
     * Diff against the specified state.
     * </p>
     * 
     * @param prev A prev state.
     * @param next A next state.
     * @return A list of gap closers.
     */
    public static <Context> List<Runnable> diff(Context context, List<TreeNode<Context>> prev, List<TreeNode<Context>> next) {
        List<Runnable> patches = new ArrayList();
        diffItems(context, prev, next, patches);
        return patches;
    }

    /**
     * <p>
     * Diff item.
     * </p>
     * 
     * @param next A next state.
     * @return
     */
    private static <Context> void diff(TreeNode<Context> prev, TreeNode<Context> next, List<Runnable> patches) {
        /**
         * <p>
         * We passes the Real DOM from the previous Virtual DOM to the next Virtual DOM. To tell the
         * truth, we don't want to manipulate Real DOM in here. But here is the best place to pass
         * the reference.
         * </p>
         */
        next.context = prev.context;

        // patches.addAll(diff(next.context, prev.attributes, next.attributes));
        // patches.addAll(diff(next.context, prev.classList, next.classList));
        diffItems(next.context, prev.nodes, next.nodes, patches);
    }

    private static <Context> void diff(Context context, List<TreeNode<Context>> prev, List<TreeNode<Context>> next, List<Runnable> patches) {
        for (int i = 0, length = next.size(); i < length; i++) {
            TreeNode<Context> nextItem = next.get(i);
            int prevIndex = prev.indexOf(nextItem);

            if (prevIndex == -1) {
                patches.add(() -> nextItem.add(context));
            }
        }

        for (int i = 0, length = prev.size(); i < length; i++) {
            TreeNode<Context> prevItem = prev.get(i);

            if (next.indexOf(prevItem) == -1) {
                patches.add(() -> prevItem.remove(context));
            }
        }
    }

    private static <Context> void diffItems(Context context, List<TreeNode<Context>> prev, List<TreeNode<Context>> next, List<Runnable> patches) {
        int prevSize = prev.size();
        int nextSize = next.size();
        int max = prevSize + nextSize;
        int prevPosition = 0;
        int nextPosition = 0;
        int actualManipulationPosition = 0;

        for (int i = 0; i < max; i++) {
            if (prevSize <= prevPosition) {
                if (nextSize <= nextPosition) {
                    break; // all items were scanned
                } else {
                    // all prev items are scanned, but next items are remaining
                    TreeNode<Context> nextItem = next.get(nextPosition++);
                    int index = prev.indexOf(nextItem);

                    if (index == -1) {
                        patches.add(() -> nextItem.insert(context, null));
                    } else {
                        TreeNode<Context> prevItem = prev.get(index);

                        /**
                         * {@link VirtualNode#dom}
                         * <p>
                         * We passes the Real DOM from the previous Virtual DOM to the next Virtual
                         * DOM. To tell the truth, we don't want to manipulate Real DOM in here. But
                         * here is the best place to pass the reference.
                         * </p>
                         */
                        nextItem.context = prevItem.context;

                        patches.add(() -> prevItem.move(context));
                    }
                }
            } else {
                if (nextSize <= nextPosition) {
                    // all next items are scanned, but prev items are remaining
                    TreeNode<Context> prevItem = prev.get(prevPosition++);
                    patches.add(() -> prevItem.remove(context));
                } else {
                    // prev and next items are remaining
                    TreeNode<Context> prevItem = prev.get(prevPosition);
                    TreeNode<Context> nextItem = next.get(nextPosition);

                    if (prevItem.id == nextItem.id) {
                        // same item
                        diff(prevItem, nextItem, patches);

                        actualManipulationPosition++;
                        prevPosition++;
                        nextPosition++;
                    } else {
                        // different item
                        int nextItemInPrev = prev.indexOf(nextItem);
                        int prevItemInNext = next.indexOf(prevItem);

                        if (nextItemInPrev == -1) {
                            if (prevItemInNext == -1) {
                                patches.add(() -> prevItem.replace(context, nextItem));
                                prevPosition++;
                            } else {
                                patches.add(() -> nextItem.insert(context, prevItem.context));
                            }
                            nextPosition++;
                            actualManipulationPosition++;
                        } else {
                            if (prevItemInNext == -1) {
                                patches.add(() -> prevItem.remove(context));
                            } else {
                                // both items are found in each other list
                                // hold and skip the current value
                                actualManipulationPosition++;
                            }
                            prevPosition++;
                        }
                    }
                }
            }
        }
    }
}
