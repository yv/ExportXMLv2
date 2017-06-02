package exml.io;

import exml.*;
import exml.objects.*;
import exml.tueba.TuebaDocument;
import net.jpountz.lz4.LZ4BlockInputStream;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.value.ValueType;
import org.xerial.snappy.SnappyInputStream;

import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/** Reads a MessagePack binary XML stream
 */
public class MessagePackReader<T extends GenericTerminal> {
    private MessageUnpacker _unpacker;
    protected List<Fixup<?>> _fixups =
            new ArrayList<>();

    public MessagePackReader(MessageUnpacker unpacker) {
        _unpacker = unpacker;
    }

    /**
     * reads an EXML document. This is a msgpack tuple of
     * - format version (1)
     * - schema description
     * - a list of chunks
     * @param doc the Exml document that will receive the data
     */
    public void readDocument(Document<T> doc) throws IOException {
        int numSegments = _unpacker.unpackArrayHeader();
        if (numSegments != 3) {
            throw new RuntimeException("Wrong number of segments");
        }
        String formatVersion = _unpacker.unpackString();
        if (! "exml1".equals(formatVersion)) {
            throw new RuntimeException("Wrong format version");
        }
        readSchemas(doc);
        readChunks(doc);
    }

    /**
     * reads a schema description.
     * This is a map that maps an
     *
     */
    public void readSchemas(Document<T> doc) throws IOException {
        List<String> levels = new ArrayList<>();
        int numSchemas = _unpacker.unpackMapHeader();
        for (int i=0; i < numSchemas; i++) {
            String level = _unpacker.unpackString();
            if ("word".equals(level)) {
                readSchema(doc.terminalSchema());
            } else {
                readSchema(doc.markableSchemaByName(level, true));
            }
        }
    }

    private void readEnumValues(IEnumConverter conv) throws IOException {
        int nValues = _unpacker.unpackArrayHeader();
        for (int i = 0; i < nValues; i++) {
            String name = _unpacker.unpackString();
            if (conv.indexForName(name) != i) {
                throw new RuntimeException("Name already set:"+name);
            }
        }
    }

    private void readEnumValuesDescriptions(IEnumConverter conv) throws IOException {
        int nValues = _unpacker.unpackArrayHeader();
        List<String> names = new ArrayList<>();
        for (int i = 0; i < nValues; i++) {
            names.add(_unpacker.unpackString());
        }
        int nDescriptions = _unpacker.unpackArrayHeader();
        if (nDescriptions > nValues) {
            throw new RuntimeException("more descriptions than values");
        }
        if (conv instanceof EnumConverter) {
            EnumConverter conv_alias = (EnumConverter) conv;
            for (int i = 0; i < nDescriptions; i++) {
                String description;
                switch (_unpacker.getNextFormat().getValueType()) {
                    case STRING:
                        description = _unpacker.unpackString();
                        break;
                    case NIL:
                        description = null;
                        _unpacker.unpackNil();
                        break;
                    default:
                        throw new RuntimeException("Invalid enum description:" +
                                _unpacker.unpackValue());
                }
                int idx = conv_alias.addVal(names.get(i), description);
                if (idx != i) {
                    throw new RuntimeException("Wrong enum lineup:" + names.get(i));
                }
            }
        } else {
            for (int i = 0; i < nDescriptions; i++) {
                _unpacker.unpackValue();
                int idx = conv.indexForName(names.get(i));
                if (idx != i) {
                    throw new RuntimeException("Wrong enum lineup:" + names.get(i));
                }
            }
        }
        for (int i = 0; i < nDescriptions; i++) {
            int idx = conv.indexForName(names.get(i));
            if (idx != i) {
                throw new RuntimeException("Wrong enum lineup:" + names.get(i));
            }
        }
    }

    public <E extends GenericObject> void readSchema(ObjectSchema<E> schema) throws IOException {
        int nKeys = _unpacker.unpackMapHeader();
        for (int i = 0; i < nKeys; i++) {
            String name = _unpacker.unpackString();
            Attribute attr = schema.getAttribute(name);
            String type;
            switch (_unpacker.getNextFormat().getValueType()) {
                case STRING:
                    type = _unpacker.unpackString();
                    break;
                case INTEGER:
                    type = ConverterKind.values()[_unpacker.unpackInt()].name();
                    break;
                case ARRAY:
                    int nArgs = _unpacker.unpackArrayHeader();
                    type = _unpacker.unpackString();
                    if ("ENUM".equals(type)) {
                        IEnumConverter conv;
                        if (attr.converter instanceof IEnumConverter) {
                            conv = (IEnumConverter)attr.converter;
                        } else {
                            conv = new EnumConverter();
                        }
                        attr = schema.addAttribute(name, conv);
                        if (nArgs == 2) {
                            readEnumValues(conv);
                        } else if (nArgs == 3) {
                            readEnumValuesDescriptions(conv);
                        }
                    } else {
                        throw new RuntimeException("Extra attr parameter:"+type);
                    }
                    break;
                default:
                    throw new RuntimeException("Invalid attr type:"+ _unpacker.unpackValue());
            }
            // TODO verify attribute type and/or create attribute
        }
    }

    public void readChunks(Document<T> doc) throws IOException {
        int nChunks = _unpacker.unpackArrayHeader();
        int start = 0;
        for (int i = 0; i < nChunks; i++) {
            start = readChunk(doc, start);
        }
    }

    /** serializes a document chunk as an array of:
     *  - terminal objects
     *  - list of each markable level
     * @param doc   the document to be serialized
     * @param start the start of the range
     */
    public int readChunk(Document<T> doc, int start) throws IOException {
        int nFields = _unpacker.unpackArrayHeader();
        if (nFields != 2) {
            throw new RuntimeException("Wrong number of chunk fields");
        }
        int nTerminals = readTerminals(doc);
        int nLevels = _unpacker.unpackMapHeader();
        for (int i=0; i<nLevels; i++) {
            String level = _unpacker.unpackString();
            readMarkableLevel(doc, doc.markableLevelByName(level, true));
        }
        // run all fixups
        for (Fixup f: _fixups) {
            try {
                f.run(doc);
            } catch (MissingObjectException ex) {
                throw new RuntimeException(ex);
            }
        }
        return nTerminals;
    }

    public int readTerminals(Document<T> doc) throws IOException {
        int nFields = _unpacker.unpackArrayHeader();
        if (nFields != 3) {
            throw new RuntimeException("wrong number of fields");
        }
        int nObjects = _unpacker.unpackInt();
        List<T> objects = new ArrayList<>(nObjects);
        for (int i=0; i<nObjects; i++) {
            objects.add(doc.createTerminal(null));
        }
        readObjects(doc, doc.terminalSchema(), objects);
        return nObjects;
    }

    public <E extends GenericMarkable> void readMarkableLevel(
            Document<T> doc, MarkableLevel<E> mlevel) throws IOException {
        int nFields = _unpacker.unpackArrayHeader();
        ObjectSchema<E> schema = mlevel.schema;
        if (nFields != 3) {
            throw new RuntimeException("wrong number of fields");
        }
        int nObjects = _unpacker.unpackInt();
        List<E> objects = new ArrayList<>(nObjects);
        for (int i=0; i<nObjects; i++) {
            objects.add(schema.createMarkable());
        }
        readObjects(doc, mlevel.schema, objects);
        for (E object: objects) {
            mlevel.addMarkable(object);
        }
    }

    @SuppressWarnings("unchecked")
    public <E extends AbstractNamedObject> void readObjects(
            Document<T> doc,
            ObjectSchema<E> schema, List<E> objects) throws IOException {
        List<Attribute<E, ?>> attributes = new ArrayList<>(schema.attrs.values());

        int nAttrs = _unpacker.unpackArrayHeader();
        List<String> s_attrs = new ArrayList<>();
        for (int i=0; i<nAttrs; i++) {
            s_attrs.add(_unpacker.unpackString());
        }
        int nAttrs2 = _unpacker.unpackArrayHeader();
        if (nAttrs != nAttrs2) {
            throw new RuntimeException("Number of attributes not matching");
        }

        for (String s_attr: s_attrs) {
            if (":id".equals(s_attr)) {
                int nObjects = _unpacker.unpackArrayHeader();
                if (nObjects != objects.size()) {
                    throw new RuntimeException("Wrong list size");
                }
                for (int i = 0; i < nObjects; i++) {
                    ValueType type = _unpacker.getNextFormat().getValueType();
                    if (type == ValueType.STRING) {
                        String newId = _unpacker.unpackString();
                        objects.get(i).setXMLId(newId);
                        doc.nameForObject(objects.get(i));
                    } else if (type == ValueType.NIL) {
                        // nothing to do
                    } else {
                        throw new RuntimeException("Wrong value type:" + type);
                    }
                }
            } else if (":span".equals(s_attr)) {
                int nObjects = objects.size();
                int nNumbers = _unpacker.unpackArrayHeader();
                if (nNumbers != 2 * objects.size()) {
                    throw new RuntimeException("Wrong span number size");
                }
                int[] offsets = new int[nNumbers];
                for (int i = 0; i < nNumbers; i++) {
                    offsets[i] = _unpacker.unpackInt();
                }
                int last_offset = 0;
                for (int i = 0; i < objects.size(); i++) {
                    GenericMarkable obj = (GenericMarkable)objects.get(i);
                    last_offset += offsets[i];
                    obj.setStart(last_offset);
                    last_offset += offsets[nObjects + i];
                    obj.setEnd(last_offset);
                }
            } else {
                Attribute<E, ?> attr = schema.getAttribute(s_attr);
                int nObjects = _unpacker.unpackArrayHeader();
                if (nObjects != objects.size()) {
                    throw new RuntimeException("Wrong attribute column length");
                }
                for (int i=0; i < nObjects; i++) {
                    E obj = objects.get(i);
                    ValueType type = _unpacker.getNextFormat().getValueType();
                    if (type == ValueType.STRING) {
                        String val = _unpacker.unpackString();
                        try {
                            attr.putString(obj, val, doc);
                        } catch (MissingObjectException ex) {
                            // save/defer any unresolved refs
                            _fixups.add(new Fixup(obj, attr, val));
                        }
                    } else if (type == ValueType.NIL) {
                        _unpacker.unpackNil();
                    } else if (type == ValueType.INTEGER) {
                        ConverterKind converter_kind = attr.converter.getKind();
                        if (ConverterKind.REF.equals(converter_kind)) {
                            // convert int values in refs to object offsets
                            Attribute<E, E> attr_alias = (Attribute<E, E>) attr;
                            int offset = _unpacker.unpackInt();
                            E obj2 = objects.get(i + offset);
                            attr_alias.accessor.put(obj, obj2);
                        } else if (ConverterKind.ENUM.equals(converter_kind)) {
                            Attribute<E, Object> attr_alias = (Attribute<E, Object>) attr;
                            IEnumConverter<Object> converter = (IEnumConverter<Object>) attr.converter;
                            int obj_idx = _unpacker.unpackInt();
                            attr_alias.accessor.put(obj, converter.objectForIndex(obj_idx));
                        } else {
                            throw new RuntimeException("Int in wrong column");
                        }
                    }
                }
            }
        }
    }

    public static InputStream wrapDecompress(InputStream f_in, String filename) throws IOException {
        if (filename.endsWith(".exml.bin")) {
            return f_in;
        } else if (filename.endsWith(".exml.snp")) {
            return new SnappyInputStream(f_in);
        } else if (filename.endsWith(".exml.lz4")) {
            return new LZ4BlockInputStream(f_in);
        } else {
            throw new RuntimeException("Unknown format extension:"+filename);
        }
    }

    public static <T extends GenericTerminal>
        void readBinary(Document<T> doc, String filename) throws IOException {
        InputStream f_in = new FileInputStream(filename);
        InputStream f_uncompressed = wrapDecompress(f_in, filename);
        MessageUnpacker unpack = MessagePack.newDefaultUnpacker(f_uncompressed);
        MessagePackReader<T> reader = new MessagePackReader<>(unpack);
        reader.readDocument(doc);
    }

    public static void main(String[] args) {
        try {
            TuebaDocument doc = new TuebaDocument();
            long time0 = System.currentTimeMillis();
            readBinary(doc, args[0]);
            long time1 = System.currentTimeMillis();
            DocumentWriter.writeDocument(doc,
                new FileOutputStream(args[1]));
            long time2 = System.currentTimeMillis();
            System.err.format("Loading as msgpack: %d ms\n", time1-time0);
            System.err.format("Saving as xml:      %d ms\n", time2-time1);
        } catch (IOException|XMLStreamException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

}
