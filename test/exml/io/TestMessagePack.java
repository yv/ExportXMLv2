package exml.io;

import exml.tueba.TuebaDocument;
import exml.tueba.TuebaSentenceMarkable;
import exml.tueba.TuebaTerminal;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessagePacker;
import org.msgpack.core.MessageUnpacker;

import java.io.IOException;
import java.util.ArrayList;

/** Basic tests for serialization and deserialization to msgpack
 */
public class TestMessagePack {
    private TuebaDocument simple;

    @Before
    public void setUp() {
        simple = TestUtils.readExmlResource(getClass(), "simple.exml.xml");
    }

    @Test
    public void testSerialization() throws IOException {
        MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();
        MessagePackWriter<TuebaTerminal> wr = new MessagePackWriter<>(packer);
        wr.writeDocument(simple);
        packer.flush();
        byte[] data = packer.toByteArray();
        assertEquals("must start with a list header", data[0], (byte)0x93);
        TuebaDocument result = new TuebaDocument();
        MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(data);
        MessagePackReader<TuebaTerminal> rd = new MessagePackReader<>(unpacker);
        rd.readDocument(result);
        assertEquals("length should be number of words",
                17, simple.size());
        assertEquals("should import words with non-ascii characters",
                "Ümläüts", simple.getTerminal(11).getWord());
        TuebaSentenceMarkable sent =
                new ArrayList<>(simple.sentences.getMarkables()).get(1);
        assertEquals("correct start offset",
                5, sent.getStart());
        assertEquals("correct end offset",
                11, sent.getEnd());
        assertEquals("three sentence markables",
                3, simple.sentences.getMarkables().size());
        assertEquals("one text markable",
                1, simple.texts.getMarkables().size());
    }
}
