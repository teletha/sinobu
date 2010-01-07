/*
 * Copyright (C) 2010 Nameless Production Committee.
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
package ezbean.model;

/**
 * <dl>
 * <dt>Imperfect Object Graph</dt>
 * <dd>Imperfect Object Graph is a technique to which it tries to improve efficiency in the graph
 * operation by handling not all information that relates on the object but only limited information
 * on it. Imperfect Object Graph is composed by <em>Node</em> (one or more) and <em>Arc</em> (zero
 * or more) as well as a normal object graph.</dd>
 * <dt>Node</dt>
 * <dd>Test</dd>
 * <dt>Arc</dt>
 * <dd>Test</dd>
 * </dl>
 * 
 * @see ModelWalker
 * @version 2008/06/17 6:27:53
 */
public interface ModelWalkListener {

    /**
     * This method is called whenever the {@link ModelWalker} visits a node in object graph.
     * 
     * @param model A object model of the base node that {@link ModelWalker} started from. This
     *            value must not be <code>null</code>. If the visited node is root, this value will
     *            be a object model of the root node.
     * @param property An arc in object graph. This value must not be <code>null</code>. If the
     *            visited node is root, this value will be a object property of the root node.
     * @param node A current node that {@link ModelWalker} arrives at.
     */
    void enterNode(Model model, Property property, Object node);

    /**
     * This method is called whenever the {@link ModelWalker} leaves a node in object graph.
     * 
     * @param model A object model of the base node that {@link ModelWalker} started from. This
     *            value must not be <code>null</code>. If the visited node is root, this value will
     *            be a object model of the root node.
     * @param property An arc in object graph. This value must not be <code>null</code>. If the
     *            visited node is root, this value will be a object property of the root node.
     * @param node A current node that {@link ModelWalker} arrives at.
     */
    void leaveNode(Model model, Property property, Object node);
}
