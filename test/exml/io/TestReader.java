package exml.io;

import exml.tueba.TuebaDocument;
import exml.tueba.TuebaSentenceMarkable;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/** Tests document loading
 */
public class TestReader {
    private TuebaDocument simple;

    @Before
    public void setUp() {
        simple = TestUtils.readExmlResource(getClass(), "simple.exml.xml");
    }

    @Test
    public void testBasic() {
        assertEquals("length should be number of words",
                17, simple.size());
        assertEquals("three sentence markables",
                3, simple.sentences.getMarkables().size());
        assertEquals("one text markable",
                1, simple.texts.getMarkables().size());
        assertEquals("two topic markables",
                2, simple.topics.getMarkables().size());
    }

    @Test
    public void testMarkableOffsets() {
        assertEquals("should import words with non-ascii characters",
                "Ümläüts", simple.getTerminal(11).getWord());
        TuebaSentenceMarkable sent =
                new ArrayList<>(simple.sentences.getMarkables()).get(1);
        assertEquals("correct start offset",
                5, sent.getStart());
        assertEquals("correct end offset",
                11, sent.getEnd());
        assertEquals("no holes",
                null, sent.getHoles());
    }

    @Test
    public void testMarkableQueries() {
        // getMarkablesinRange returns all markables where the
        // start point overlaps
        List<TuebaSentenceMarkable> sentences_region1 =
                simple.sentences.getMarkablesInRange(0, 7);
        assertEquals("should return two sentences",
                2, sentences_region1.size());
        TuebaSentenceMarkable sent1 = sentences_region1.get(0);
        assertEquals("correct start offset 1",
                0, sent1.getStart());
        assertEquals("correct end offset 1",
                5, sent1.getEnd());
        TuebaSentenceMarkable sent2 = sentences_region1.get(1);
        assertEquals("correct start offset 2",
                5, sent2.getStart());
        assertEquals("correct end offset 2",
                11, sent2.getEnd());
    }
}
