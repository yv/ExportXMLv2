package exml.io;

import exml.Document;
import exml.GenericMarkable;
import exml.GenericTerminal;
import exml.MarkableLevel;
import exml.objects.*;
import exml.tueba.TuebaDocument;
import exml.tueba.TuebaTerminal;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessagePacker;
import org.xerial.snappy.SnappyOutputStream;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/** Support for writing msgpack files
 */
public class MessagePackWriter<T extends GenericTerminal> {
    private MessagePacker _packer;

    // TODO setting optimize for enabling enum-as-int and ref-as-int
    public MessagePackWriter(MessagePacker packer) {
        _packer = packer;
    }

    /**
     * writes an EXML document. This is a msgpack tuple of
     * - format version (1)
     * - schema description
     * - a list of chunks
     * @param doc
     */
    public void writeDocument(Document<T> doc) throws IOException {
        _packer.packArrayHeader(3);
        _packer.packString("exml1");
        writeSchemas(doc);
        writeChunks(doc);
    }

    /**
     * writes a schema description.
     * This is a map that maps an
     *
     */
    public void writeSchemas(Document<T> doc) throws IOException {
        List<String> levels = doc.listMarkableLevels();
        List<String> levels_nonempty = new ArrayList<>();
        int numSchemas = 1;
        for (String level: levels) {
            if (doc.markableLevelByName(level, false)
                    .getMarkables().size() > 0) {
                levels_nonempty.add(level);
                numSchemas++;
            }
        }
        _packer.packMapHeader(numSchemas);
        _packer.packString("word");
        writeSchema(doc.terminalSchema());
        for (String level: levels_nonempty) {
            _packer.packString(level);
            writeSchema(doc.markableSchemaByName(level, false));
        }
    }

    public void writeSchema(ObjectSchema<?> schema) throws IOException {
        // TODO serialize and save an alphabet for enum types
        // TODO what about edges?
        Set<String> keySet = schema.attrs.keySet();
        _packer.packMapHeader(keySet.size());
        for (String key: keySet) {
            @SuppressWarnings("rawtypes")
            Attribute att = (Attribute)schema.attrs.get(key);
            _packer.packString(att.name);
            _packer.packString(att.converter.getKind().name());
        }
    }

    public void writeChunks(Document<T> doc) throws IOException {
        //TODO look for text boundaries and generate one chunk per 30k tokens
        // length doesn't matter much: most ints will fit into 16bit
        _packer.packArrayHeader(1);
        writeChunk(doc, 0, doc.size());
    }

    /** serializes a document chunk as an array of:
     *  - terminal objects
     *  - list of each markable level
     * @param doc   the document to be serialized
     * @param start the start of the range
     * @param end   the end of the range
     */
    public void writeChunk(Document<T> doc, int start, int end) throws IOException {
        _packer.packArrayHeader(2);
        writeObjects(doc, doc.terminalSchema(),
                doc.getTerminals().subList(start, end), false);
        List<String> levels = doc.listMarkableLevels();
        List<String> levels_nonempty = new ArrayList<>();
        for (String level: levels) {
            if (doc.markableLevelByName(level, false)
                    .getMarkables().size() > 0) {
                levels_nonempty.add(level);
            }
        }
        _packer.packMapHeader(levels_nonempty.size());
        for (String level: levels_nonempty) {
            _packer.packString(level);
            MarkableLevel<?> mlevel =
                    doc.markableLevelByName(level, false);
            writeObjects(doc, mlevel);
        }
    }

    public <E extends GenericMarkable> void writeObjects(
            Document<T> doc, MarkableLevel<E> mlevel) throws IOException {
        writeObjects(doc, mlevel.schema, mlevel.getMarkables(), true);
    }

    public <E extends NamedObject> void writeObjects(
            Document<T> doc,
            ObjectSchema<E> schema, Collection<E> objects,
            boolean includeSpan) throws IOException {
        List<Attribute<E, ?>> attributes = new ArrayList<>(schema.attrs.values());
        _packer.packArrayHeader(3);
        _packer.packInt(objects.size());

        List<E> objects_l = new ArrayList<>(objects);
        List<String> s_attrs = new ArrayList<>();
        List<String[]> values =  new ArrayList<>();

        String[] ids = new String[objects.size()];
        boolean any_id = false;
        for (int i = 0; i < objects_l.size(); i++) {
            ids[i] = objects_l.get(i).getXMLId();
            if (ids[i] != null) {
                any_id = true;
            }
        }
        if (any_id) {
            s_attrs.add(":id");
            values.add(ids);
        }
        for (Attribute<E, ?> attr: attributes){
            String[] vals = new String[objects.size()];
            boolean anyNonNull = false;

            for (int i = 0; i < objects_l.size(); i++) {
                try {
                    vals[i] = attr.getString(objects_l.get(i), doc);
                    if (vals[i] != null) {
                        anyNonNull = true;
                    }
                } catch (NullPointerException e) {
                }
            }
            if (anyNonNull) {
                s_attrs.add(attr.name);
                values.add(vals);
            }
        }
        _packer.packArrayHeader(s_attrs.size() + (includeSpan ? 1 : 0));
        for (String s_attr: s_attrs) {
            _packer.packString(s_attr);
        }
        if (includeSpan) {
            _packer.packString(":span");
        }
        _packer.packArrayHeader(s_attrs.size()+ (includeSpan ? 1 : 0));
        for (String[] vals: values){
            // TODO convert enum values to int
            // TODO convert same-layer refs to int offsets
            _packer.packArrayHeader(objects.size());
            for (String s: vals) {
                if (s == null) {
                    _packer.packNil();
                } else {
                    _packer.packString(s);
                }
            }
        }
        if (includeSpan) {
            // TODO what about holes?
            _packer.packArrayHeader(2 * objects.size());
            int last_pos = 0;
            for (NamedObject obj: objects) {
                _packer.packInt(obj.getStart() - last_pos);
                last_pos = obj.getEnd();
            }
            for (NamedObject obj: objects) {
                _packer.packInt(obj.getEnd() - obj.getStart());
            }
        }
    }

    public static <T extends GenericTerminal>
            void writeBinary(Document<T> doc, String filename) throws IOException {
        OutputStream f = new FileOutputStream(filename);
        OutputStream f_compressed = new SnappyOutputStream(f);
        MessagePacker packer = MessagePack.newDefaultPacker(f_compressed);
        MessagePackWriter<T> writer = new MessagePackWriter<>(packer);
        writer.writeDocument(doc);
        packer.flush();
        packer.close();
    }

    public static void main(String[] args) {
        try {
            TuebaDocument doc = TuebaDocument.loadDocument(args[0]);
            writeBinary(doc, args[1]);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
