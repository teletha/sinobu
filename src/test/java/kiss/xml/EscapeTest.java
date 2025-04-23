/*
 * Copyright (C) 2025 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.xml;

import org.junit.jupiter.api.Test;

import kiss.XML;

class EscapeTest {

    @Test
    void normal() {
        escapeIdempotent("ok", "ok");
    }

    @Test
    void empty() {
        escapeIdempotent("", "");
    }

    @Test
    void blank() {
        escapeIdempotent(" ", " ");
    }

    @Test
    void Null() {
        escapeIdempotent(null, "");
    }

    @Test
    void escape() {
        escapeIdempotent("&", "&amp;");
        escapeIdempotent("<", "&lt;");
        escapeIdempotent(">", "&gt;");
        escapeIdempotent("'", "&apos;");
        escapeIdempotent("\"", "&quot;");
    }

    @Test
    void complex() {
        escapeIdempotent("<html>", "&lt;html&gt;");
        escapeIdempotent("<html lang='\"locale\"'>", "&lt;html lang=&apos;&quot;locale&quot;&apos;&gt;");
    }

    @Test
    void alreadyEscapedNamedEntities() {
        escapeIdempotent("&amp;", "&amp;");
        escapeIdempotent("&lt;", "&lt;");
        escapeIdempotent("&gt;", "&gt;");
        escapeIdempotent("&quot;", "&quot;");
        escapeIdempotent("&apos;", "&apos;");
    }

    @Test
    void alreadyEscapedNumericEntities() {
        escapeIdempotent("&#34;", "&#34;");
        escapeIdempotent("&#x22;", "&#x22;");
        escapeIdempotent("&#x3C;", "&#x3C;");
        escapeIdempotent("&#39;", "&#39;");
    }

    @Test
    void unknownNamedEntityShouldEscapeAmpersand() {
        escapeIdempotent("&unknown;", "&amp;unknown;");
        escapeIdempotent("&Copy;", "&amp;Copy;");
    }

    @Test
    void mixedContent() {
        escapeIdempotent("A & B < C > D", "A &amp; B &lt; C &gt; D");
        escapeIdempotent("\"Hello\" & 'World'", "&quot;Hello&quot; &amp; &apos;World&apos;");
    }

    @Test
    void doubleEscapedShouldNotOccur() {
        escapeIdempotent("&amp;amp;", "&amp;amp;");
        escapeIdempotent("&amp;lt;", "&amp;lt;");
        escapeIdempotent("&#38;amp;", "&#38;amp;");
    }

    @Test
    void randomEntitiesShouldEscapeAmp() {
        escapeIdempotent("&eacute;", "&amp;eacute;");
        escapeIdempotent("&nbsp;", "&amp;nbsp;");
    }

    @Test
    void incompleteEntity() {
        escapeIdempotent("&amp", "&amp;amp");
        escapeIdempotent("&lt", "&amp;lt");
        escapeIdempotent("&gt", "&amp;gt");
        escapeIdempotent("&quot", "&amp;quot");
        escapeIdempotent("&apos", "&amp;apos");

        escapeIdempotent("&#38", "&amp;#38");
        escapeIdempotent("&#x26", "&amp;#x26");
    }

    @Test
    void malformedEntity() {
        escapeIdempotent("&foo;", "&amp;foo;");
        escapeIdempotent("&unknown;", "&amp;unknown;");
        escapeIdempotent("&something#;", "&amp;something#;");
    }

    @Test
    void ampersandInText() {
        escapeIdempotent("A & B", "A &amp; B");
        escapeIdempotent("A & B & C", "A &amp; B &amp; C");
    }

    @Test
    void ampFollowedBySharpButNoSemicolon() {
        escapeIdempotent("&#123", "&amp;#123");
        escapeIdempotent("&#x3C", "&amp;#x3C");
        escapeIdempotent("&#xG;", "&amp;#xG;");
    }

    private void escapeIdempotent(String input, String expected) {
        String result = XML.escape(input);
        assert result.equals(expected);

        result = XML.escape(result);
        assert result.equals(expected);
    }
}
