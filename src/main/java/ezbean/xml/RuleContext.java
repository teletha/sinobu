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

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;

import ezbean.I;

/**
 * <p>
 * Context for the {@link XMLScanner}. This class integrates eager and lazy context to reduce
 * footprint.
 * </p>
 * 
 * @version 2010/12/13 23:59:01
 */
class RuleContext extends XMLFilterImpl {

    /** The invocation context. */
    final XMLScanner scanner;

    /** The invoked rule. */
    final RuleMethod rule;

    /** The base content handler. */
    final ContentHandler handler;

    /** The attribute copy. */
    final AttributesImpl atts;

    /** The position that this context has started. */
    int start;

    /** The sax event buffer. */
    Bits bits;

    /**
     * Create RuleContext instance.
     * 
     * @param scanner
     * @param rule
     * @param atts
     */
    RuleContext(XMLScanner scanner, RuleMethod rule, Attributes atts) {
        this.scanner = scanner;
        this.rule = rule;
        this.handler = scanner.getContentHandler();

        // copy attributes if the rule method requires it
        this.atts = new AttributesImpl(atts);

        // initialize content handler
        setContentHandler(handler);
    }

    /**
     * Proceed SAX parser's processing.
     */
    void proceed() {
        if (1 < rule.type) {
            // lazy context
            bits.send(scanner);
        } else {
            // eager context
            bits = new Bits();
            setContentHandler(bits);
        }
    }

    /**
     * Invoke the rule method of this context.
     * 
     * @throws RuntimeException If the rule method can not be invoked or any runtime error will be
     *             thrown in rule method.
     */
    void start() {
        if (1 < rule.type) {
            // lazy context
            bits = new Bits();
            setContentHandler(bits);
        } else {
            // eager context
            try {
                // Invoke rule method. In RuleContext, rule type must indicates 0 (no parameter) or
                // 2 (require attribute only).
                if (rule.type == 0) {
                    rule.method.invoke(scanner);
                } else {
                    rule.method.invoke(scanner, atts);
                }

                // restore content handler (it is posibility that the method #proceed() will be
                // called)
                if (bits != null) {
                    setContentHandler(handler);
                }
            } catch (Exception e) {
                // We must throw the checked exception quietly and pass the original exception
                // instead of wrapped exception.
                throw I.quiet(e);
            }
        }
    }

    /**
     * Finish the execution of the rule method in this context.
     * 
     * @throws RuntimeException If the rule method can not be invoked or any runtime error will be
     *             thrown in rule method.
     */
    void end() {
        try {
            if (1 < rule.type) {
                // lazy context

                // restore content handler
                setContentHandler(handler);

                // retrieve text contents
                StringBuilder contents = new StringBuilder();

                for (Object[] bit : bits.bits) {
                    if (bit.length == 1) {
                        contents.append(((String) bit[0]).trim());
                    }
                }

                // Invoke rule method (lazy evaluation).
                switch (rule.type) {
                case 3: // require content only
                    rule.method.invoke(scanner, contents.toString());
                    break;

                case 4: // require event only
                    rule.method.invoke(scanner, bits);
                    break;

                default: // require content and attribute
                    rule.method.invoke(scanner, contents.toString(), atts);
                }
            } else {
                // eager context
                if (bits != null) {
                    bits.send(scanner);
                }
            }
        } catch (Exception e) {
            // We must throw the checked exception quietly and pass the original exception instead
            // of wrapped exception.
            throw I.quiet(e);
        }
    }
}
