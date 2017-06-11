package exml.io;

import exml.Document;
import exml.GenericMarkable;
import exml.GenericTerminal;
import exml.MarkableLevel;
import exml.objects.*;
import exml.tueba.TuebaDocument;
import exml.tueba.TuebaTerminal;
import exml.tueba.TuebaTextMarkable;
import net.jpountz.lz4.LZ4BlockOutputStream;
import net.jpountz.lz4.LZ4Factory;
import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessagePacker;
import org.msgpack.value.StringValue;
import org.msgpack.value.Value;
import org.msgpack.value.ValueFactory;
import org.xerial.snappy.SnappyOutputStream;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import java.util.zip.GZIPOutputStream;

/** Support for writing msgpack files.
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
            if (att.converter.getKind() == ConverterKind.ENUM) {
                IEnumConverter conv = (IEnumConverter) att.converter;
                _packer.packArrayHeader(2);
                _packer.packString(att.converter.getKind().name());
                _packer.packArrayHeader(conv.endIndex());
                for (int i=0; i < conv.endIndex(); i++) {
                    _packer.packString(conv.nameForIndex(i));
                }
            } else {
                _packer.packString(att.converter.getKind().name());
            }
        }
    }

    public void writeChunks(Document<T> doc) throws IOException {
        //TODO look for text boundaries and generate one chunk per 30k tokens
        // length doesn't matter much: most ints will fit into 16bit
        MarkableLevel<? extends GenericMarkable> mlevel = doc.markableLevelByName("text", false);
        IntArrayList stops = new IntArrayList();
        int last_stop = 0;
        if (mlevel != null && ! mlevel.getMarkables().isEmpty()) {
            for (GenericMarkable text: mlevel.getMarkables()) {
                int new_stop=text.getEnd();
                if (new_stop - last_stop > 50000) {
                    stops.add(new_stop);
                    last_stop = new_stop;
                }
            }
        }
        _packer.packArrayHeader(stops.size()+1);
        last_stop = 0;
        for (int new_stop: stops.toArray()) {
            writeChunk(doc, last_stop, new_stop);
            last_stop = new_stop;
        }
        writeChunk(doc, last_stop, doc.size());
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
            writeObjects(doc, mlevel, start, end);
        }
    }

    public <E extends GenericMarkable> void writeObjects(
            Document<T> doc, MarkableLevel<E> mlevel, int start, int end) throws IOException {
        if (start == 0 && end == doc.size()) {
            writeObjects(doc, mlevel.schema, mlevel.getMarkables(), true);
        } else {
            writeObjects(doc, mlevel.schema, mlevel.getMarkablesInRange(start, end), true);
        }
    }

    public <E extends AbstractNamedObject> void writeObjects(
            Document<T> doc,
            ObjectSchema<E> schema, Collection<E> objects,
            boolean includeSpan) throws IOException {
        List<Attribute<E, ?>> attributes = new ArrayList<>(schema.attrs.values());
        _packer.packArrayHeader(3);
        _packer.packInt(objects.size());

        List<E> objects_l = new ArrayList<>(objects);
        List<String> s_attrs = new ArrayList<>();
        List<byte[]> values =  new ArrayList<>();

        MessageBufferPacker partial = MessagePack.newDefaultBufferPacker();
        partial.packArrayHeader(objects.size());
        //Value[] ids = new Value[objects.size()];
        boolean any_id = false;
        for (int i = 0; i < objects_l.size(); i++) {
            String s_val = objects_l.get(i).getXMLId();
            if (s_val != null) {
                //ids[i] = ValueFactory.newString(s_val);
                partial.packString(s_val);
                any_id = true;
            } else {
                partial.packNil();
                //ids[i] = ValueFactory.newNil();
            }
        }
        if (any_id) {
            s_attrs.add(":id");
            values.add(partial.toByteArray());
            partial = MessagePack.newDefaultBufferPacker();
        }
        for (Attribute<E, ?> attr: attributes){
            boolean anyNonNull = false;
            partial = MessagePack.newDefaultBufferPacker();
            partial.packArrayHeader(objects.size());
            //Value[] vals = new Value[objects.size()];
            if (attr.converter.getKind() == ConverterKind.ENUM) {
                // convert Enum values to ints
                IEnumConverter conv = (IEnumConverter) attr.converter;
                for (int i = 0; i < objects_l.size(); i++) {
                    Object val = attr.accessor.get(objects_l.get(i));
                    if (val != null) {
                        anyNonNull = true;
                        int idx = conv.indexForObject(val, false);
                        if (idx == -1) {
                            partial.packString(conv.convertToString(objects_l.get(i), doc));
                        } else {
                            partial.packInt(idx);
                        }
                    } else {
                        partial.packNil();
                    }
                }
            } else {
                // TODO convert same-layer Ref values to int offsets
                for (int i = 0; i < objects_l.size(); i++) {
                    try {
                        String s = attr.getString(objects_l.get(i), doc);
                        if (s == null) {
                            partial.packNil();
                        } else {
                            partial.packString(s);
                            anyNonNull = true;
                        }
                    } catch (NullPointerException e) {
                        //vals[i] = ValueFactory.newNil();
                        partial.packNil();
                    }
                }
            }
            if (anyNonNull) {
                s_attrs.add(attr.name);
                values.add(partial.toByteArray());
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
        for (int val_idx = 0; val_idx < values.size(); val_idx++) {
            _packer.addPayload(values.get(val_idx));
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

    private static OutputStream wrapCompression(OutputStream out, String filename) throws IOException {
        if (filename.endsWith(".exml.bin")) {
            return out;
        } else if (filename.endsWith(".exml.snp")) {
            return new SnappyOutputStream(new BufferedOutputStream(out));
        } else if (filename.endsWith(".exml.lz4")) {
            return new LZ4BlockOutputStream(out, 64 << 10, LZ4Factory.unsafeInstance().highCompressor());
        } else if (filename.endsWith(".exml.bin.gz")) {
            return new GZIPOutputStream(out, 8<<10);
        } else {
            throw new RuntimeException("Unknown format extension:"+filename);
        }
    }

    public static <T extends GenericTerminal>
            void writeBinary(Document<T> doc, String filename) throws IOException {
        OutputStream f = new FileOutputStream(filename);
        OutputStream f_compressed = wrapCompression(f, filename);
        MessagePacker packer = MessagePack.newDefaultPacker(f_compressed);
        MessagePackWriter<T> writer = new MessagePackWriter<>(packer);
        writer.writeDocument(doc);
        packer.flush();
        packer.close();
    }

    public static void main(String[] args) {
        try {
            long time0 = System.currentTimeMillis();
            TuebaDocument doc = TuebaDocument.loadDocument(args[0]);
            long time1 = System.currentTimeMillis();
            System.err.format("Loading as xml:    %d ms\n", time1-time0);
            writeBinary(doc, args[1]);
            long time2 = System.currentTimeMillis();
            System.err.format("Saving as msgpack: %d ms\n", time2 - time1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
