package kiss.xml.selector;

import static kiss.xml.selector.FindAssetion.*;

import org.junit.jupiter.api.Test;

import kiss.I;
import kiss.XML;

public class ContainsTest {

    @Test
    public void contains() {
        XML xml = I.xml("""
                <m>
                    <Q>a</Q>
                    <Q>b</Q>
                    <Q>aa</Q>
                </m>
                """);

        assert select(xml, 2, "Q:contains('a')");
        assert select(xml, 1, "Q:contains('aa')");
        assert select(xml, 1, "Q:contains('b')");
    }

    @Test
    public void containsPartialAndOrderInsensitive() {
        XML xml = I.xml("""
                <m>
                    <Q>abc</Q>
                    <Q>cab</Q>
                    <Q>xyz</Q>
                </m>
                """);

        assert select(xml, 2, "Q:contains('ab')");
        assert select(xml, 1, "Q:contains('xyz')");
    }

    @Test
    public void containsCaseSensitivity() {
        XML xml = I.xml("""
                <m>
                    <Q>Apple</Q>
                    <Q>apple</Q>
                    <Q>APPLE</Q>
                </m>
                """);

        assert select(xml, 1, "Q:contains('Apple')");
        assert select(xml, 1, "Q:contains('apple')");
        assert select(xml, 1, "Q:contains('APPLE')");
    }

    @Test
    public void containsWhitespaceAndLineBreaks() {
        XML xml = I.xml("""
                            <m>
                                <Q>hello world</Q>
                                <Q>hello
                world</Q>
                                <Q>helloworld</Q>
                            </m>
                            """);

        assert select(xml, 1, "Q:contains('hello world')");
        assert select(xml, 1, "Q:contains('hello\nworld')");
    }

    @Test
    public void containsTextInNestedElement() {
        XML xml = I.xml("""
                <m>
                    <Q><span>alpha</span> beta</Q>
                    <Q>alpha<span> beta</span></Q>
                    <Q><span>alpha</span></Q>
                </m>
                """);

        assert select(xml, 3, "Q:contains('alpha')");
        assert select(xml, 2, "Q:contains('beta')");
        assert select(xml, 2, "Q:contains('alpha beta')");
    }

    @Test
    public void containsWithSpecialCharacters() {
        XML xml = I.xml("""
                <m>
                    <Q>a &amp; b</Q>
                    <Q>a &lt; b</Q>
                    <Q>a &gt; b</Q>
                    <Q>a " b</Q>
                </m>
                """);

        assert select(xml, 1, "Q:contains('&')");
        assert select(xml, 1, "Q:contains('<')", false);
        assert select(xml, 1, "Q:contains('>')", false);
        assert select(xml, 1, "Q:contains('\"')");
    }

    @Test
    public void containsEmptyText() {
        XML xml = I.xml("""
                <m>
                    <Q></Q>
                    <Q> </Q>
                    <Q>none-empty</Q>
                </m>
                """);

        assert select(xml, 3, "Q:contains('')");
        assert select(xml, 1, "Q:contains(' ')");
        assert select(xml, 1, "Q:contains('none-empty')");
    }
}
