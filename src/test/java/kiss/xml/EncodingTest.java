/*
 * Copyright (C) 2020 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.xml;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import kiss.I;
import kiss.XML;

/**
 * @version 2017/03/31 11:30:20
 */
public class EncodingTest {
    @Test
    public void xmlEncoding() throws Exception {
        XML xml = parse("<m><text>てすと</text></m>", StandardCharsets.UTF_8.displayName());

        assert xml.find("text").text().equals("てすと");
    }

    @Test
    public void htmlEncoding() throws Exception {
        XML xml = parse("<html><head><meta charset='euc-jp'><title>てすと</title></head></html>", "euc-jp");

        assert xml.find("title").text().equals("てすと");
    }

    @Test
    public void htmlEncodingMultiple() throws Exception {
        XML xml = parse("<html><head><meta no/><meta charset='euc-jp'/><meta charset='shift_jis'><title>てすと</title></head></html>", "euc-jp");

        assert xml.find("title").text().equals("てすと");
    }

    @Test
    public void htmlEncodingInvalid() throws Exception {
        XML xml = parse("<html><head><meta charset='uft-invalid'><title>てすと</title></head></html>", "utf-8");

        assert xml.find("title").text().equals("てすと");
    }

    /**
     * <p>
     * Parse with encoding.
     * </p>
     */
    private XML parse(String xml, String encoding) {
        try {
            byte[] bytes = xml.getBytes(encoding);
            return I.xml(new ByteArrayInputStream(bytes, 0, bytes.length));
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }
}