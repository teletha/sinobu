package kiss.xml;

import static kiss.xml.FindAssetion.*;

import org.junit.jupiter.api.Test;

import kiss.I;
import kiss.XML;

public class FindNthLastChildTest {

    @Test
    public void nthLastChild() {
        XML xml = I.xml("""
                <m>
                    <Q/>
                    <Q/>
                    <Q/>
                    <Q/>
                    <P/>
                    <Q/>
                    <Q/>
                </m>
                """);

        assert select(xml, 1, "Q:nth-last-child(1)");
        assert select(xml, 1, "Q:nth-last-child(2)");
        assert select(xml, 1, "Q:nth-last-child(5)");
        assert select(xml, 3, "Q:nth-last-child(2n)");
        assert select(xml, 1, "Q:nth-last-child(3n)");
        assert select(xml, 1, "Q:nth-last-child(4n)");
        assert select(xml, 3, "Q:nth-last-child(2n+1)");
        assert select(xml, 3, "Q:nth-last-child(odd)");
        assert select(xml, 3, "Q:nth-last-child(even)");
    }

    @Test
    public void nthLastChildNegativeRemainder() {
        XML xml = I.xml("""
                <m>
                    <Q/>
                    <Q/>
                    <Q/>
                    <Q/>
                    <Q/>
                </m>
                """);

        assert select(xml, 5, "Q:nth-last-child(n-1)");
        assert select(xml, 5, "Q:nth-last-child(n-2)");
        assert select(xml, 5, "Q:nth-last-child(n-3)");
        assert select(xml, 5, "Q:nth-last-child(n-4)");
        assert select(xml, 5, "Q:nth-last-child(n-5)");
        assert select(xml, 5, "Q:nth-last-child(n-6)");
    }

    @Test
    public void nthLastChildNegativeCoefficient() {
        XML xml = I.xml("""
                <m>
                    <Q/>
                    <Q/>
                    <Q/>
                    <Q/>
                    <Q/>
                </m>
                """);

        assert select(xml, 1, "Q:nth-last-child(-n+1)");
        assert select(xml, 2, "Q:nth-last-child(-n+2)");
        assert select(xml, 3, "Q:nth-last-child(-n+3)");
        assert select(xml, 4, "Q:nth-last-child(-n+4)");
        assert select(xml, 5, "Q:nth-last-child(-n+5)");
        assert select(xml, 5, "Q:nth-last-child(-n+6)");
    }

    @Test
    public void nthLastChildInvalidArg() {
        XML xml = I.xml("""
                <root>
                    <item/>
                    <item/>
                    <item/>
                    <item/>
                </root>
                """);

        assert select(xml, 0, "item:nth-last-child(-1)");
        assert select(xml, 0, "item:nth-last-child(0)");
        assert select(xml, 0, "item:nth-last-child(-n)");
        assert select(xml, 0, "item:nth-last-child(0n)");
        assert select(xml, 0, "item:nth-last-child(-2n)");
        assert select(xml, 0, "item:nth-last-child(n+100)");
    }

    @Test
    public void nthLastOfType() {
        XML xml = I.xml("""
                <m>
                    <Q/>
                    <P/>
                    <Q/>
                    <P/>
                    <P/>
                    <Q/>
                    <Q/>
                </m>
                """);

        assert select(xml, 1, "Q:nth-last-of-type(1)");
        assert select(xml, 1, "Q:nth-last-of-type(2)");
        assert select(xml, 0, "Q:nth-last-of-type(5)");
        assert select(xml, 4, "Q:nth-last-of-type(n)");
        assert select(xml, 2, "Q:nth-last-of-type(2n)");
        assert select(xml, 1, "Q:nth-last-of-type(3n)");
        assert select(xml, 2, "Q:nth-last-of-type(2n+1)");
        assert select(xml, 2, "Q:nth-last-of-type(odd)");
        assert select(xml, 2, "Q:nth-last-of-type(even)");
    }

    @Test
    public void nthLastOfTypeInvalidArg() {
        XML xml = I.xml("""
                <root>
                    <a/>
                    <a/>
                    <a/>
                    <a/>
                </root>
                """);

        assert select(xml, 0, "a:nth-last-of-type(-1)");
        assert select(xml, 0, "a:nth-last-of-type(0)");
        assert select(xml, 0, "a:nth-last-of-type(-n)");
        assert select(xml, 0, "a:nth-last-of-type(0n)");
        assert select(xml, 0, "a:nth-last-of-type(-2n)");
        assert select(xml, 0, "a:nth-last-of-type(n+100)");
    }

    @Test
    public void nthLastOfTypeSpacedArg() {
        XML xml = I.xml("""
                <root>
                    <x/>
                    <x/>
                    <x/>
                    <x/>
                </root>
                """);

        assert select(xml, 1, "x:nth-last-of-type( 1 )");
        assert select(xml, 2, "x:nth-last-of-type( 2n )");
        assert select(xml, 2, "x:nth-last-of-type( -n + 2 )");
    }

    @Test
    public void nthLastOfTypeWithInterleavedTags() {
        XML xml = I.xml("""
                <root>
                    <a/>
                    <b/>
                    <a/>
                    <b/>
                    <c/>
                    <a/>
                    <c/>
                    <a/>
                </root>
                """);

        assert select(xml, 1, "a:nth-last-of-type(1)");
        assert select(xml, 1, "a:nth-last-of-type(2)");
        assert select(xml, 1, "a:nth-last-of-type(3)");
        assert select(xml, 1, "a:nth-last-of-type(4)");
        assert select(xml, 2, "a:nth-last-of-type(2n)");
        assert select(xml, 2, "a:nth-last-of-type(odd)");
    }

    @Test
    public void nthLastChildWithWhitespaceAndNoise() {
        XML xml = I.xml("""
                <root>
                    <n/>
                    <n/>
                    <n/>
                    <n/>
                </root>
                """);

        assert select(xml, 2, "n:nth-last-child(\n\t 2n + 1 )");
        assert select(xml, 2, "n:nth-last-child( odd )");
        assert select(xml, 2, "n:nth-last-child( even )");
    }

    @Test
    public void nthLastChildWithSignedArguments() {
        XML xml = I.xml("""
                <root>
                    <x/>
                    <x/>
                    <x/>
                    <x/>
                    <x/>
                    <x/>
                    <x/>
                </root>
                """);

        assert select(xml, 4, "x:nth-last-child(+2n+1)");
        assert select(xml, 2, "x:nth-last-child(+3n-1)");
        assert select(xml, 4, "x:nth-last-child(-1n+4)");
    }

    @Test
    public void nthLastOfTypeAmongMixedElements() {
        XML xml = I.xml("""
                <root>
                    <x/>
                    <y/>
                    <x/>
                    <z/>
                    <x/>
                    <y/>
                </root>
                """);

        assert select(xml, 0, "x:nth-last-child(1)");
        assert select(xml, 0, "x:nth-last-child(3)");
        assert select(xml, 0, "x:nth-last-child(5)");

        assert select(xml, 1, "x:nth-last-of-type(1)");
        assert select(xml, 1, "x:nth-last-of-type(2)");
        assert select(xml, 1, "x:nth-last-of-type(3)");
    }

    @Test
    public void nthLastChildInLargeDocument() {
        StringBuilder xmlContent = new StringBuilder("<root>");
        for (int i = 1; i <= 100; i++) {
            xmlContent.append("<e/>");
        }
        xmlContent.append("</root>");

        XML xml = I.xml(xmlContent.toString());

        assert select(xml, 1, "e:nth-last-child(10)");
        assert select(xml, 10, "e:nth-last-child(10n)");
        assert select(xml, 4, "e:nth-last-child(33n+1)");
        assert select(xml, 0, "e:nth-last-child(101)");
    }
}
