package exml.io;

import exml.Document;
import exml.tueba.TuebaDocument;
import exml.tueba.TuebaTerminal;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.InputStream;

/** Utility class for test helpers
 */
public class TestUtils {
    public static InputStream openResource(Class cls, String fname) throws IOException {
        String packagePrefix = String.format("/%s/", cls.getPackage().getName().replace('.', '/'));
        InputStream stream=cls.getResourceAsStream(packagePrefix+fname);
        if (stream == null) {
            throw new RuntimeException("Could not load resource "+packagePrefix+fname);
        }
        return stream;
    }

    public static TuebaDocument readExmlResource(Class cls, String fname) {
        try {
            InputStream in = openResource(cls, fname);
            TuebaDocument doc = new TuebaDocument();
            DocumentReader.readDocument(doc, in);
            return doc;
        } catch (IOException|XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }
}
