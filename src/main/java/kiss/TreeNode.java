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
import java.util.function.Consumer;

/**
 * <p>
 * Diffable node of tree structure.
 * </p>
 * 
 * @version 2017/02/14 13:54:02
 */
public class TreeNode<Context, Self extends TreeNode> {

    /** The associated user context. */
    protected Context context;

    /** The node identity. */
    protected int id;

    /** The children nodes. */
    public List nodes = new ArrayList();

    protected void add(Context context) {
    }

    protected void remove(Context context) {
    }

    protected void insert(Context context, Object index) {
    }

    /**
     * <p>
     * Move this item to end of the context.
     * </p>
     * 
     * @param context
     */
    protected void move(Context context) {
    }

    /**
     * <p>
     * Replace child item.
     * </p>
     * 
     * @param context A context.
     * @param item A new item.
     */
    protected void replace(Context context, Self item) {
    }

    protected void diff(Self next, List<Runnable> patches) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        return hashCode() == obj.hashCode();
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
    public static <Context, Self extends TreeNode<Context, Self>> List<Runnable> diff(Context context, List<Self> prev, List<Self> next) {
        List<Runnable> patches = new ArrayList();
        diffItems(context, prev, next, patches);
        return patches;
    }

    protected static <Context, T> void diff(List<Runnable> patches, List<T> prev, List<T> next, Consumer<T> add, Consumer<T> remove) {
        for (int i = 0, length = next.size(); i < length; i++) {
            T nextItem = next.get(i);
            int prevIndex = prev.indexOf(nextItem);

            if (prevIndex == -1) {
                patches.add(() -> add.accept(nextItem));
            }
        }

        for (int i = 0, length = prev.size(); i < length; i++) {
            T prevItem = prev.get(i);

            if (next.indexOf(prevItem) == -1) {
                patches.add(() -> remove.accept(prevItem));
            }
        }
    }

    protected static <Context, Self extends TreeNode<Context, Self>> void diffItems(Context context, List<Self> prev, List<Self> next, List<Runnable> patches) {
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
                    Self nextItem = next.get(nextPosition++);
                    int index = prev.indexOf(nextItem);

                    if (index == -1) {
                        patches.add(() -> nextItem.insert(context, null));
                    } else {
                        Self prevItem = prev.get(index);

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
                    Self prevItem = prev.get(prevPosition++);
                    patches.add(() -> prevItem.remove(context));
                } else {
                    // prev and next items are remaining
                    Self prevItem = prev.get(prevPosition);
                    Self nextItem = next.get(nextPosition);

                    if (prevItem.id == nextItem.id) {
                        // same item
                        prevItem.diff(nextItem, patches);

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
