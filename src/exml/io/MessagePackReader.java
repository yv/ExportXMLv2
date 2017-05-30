package exml.io;

import exml.*;
import exml.objects.Attribute;
import exml.objects.ConverterKind;
import exml.objects.NamedObject;
import exml.objects.ObjectSchema;
import exml.tueba.TuebaDocument;
import exml.tueba.TuebaTerminal;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.value.ValueType;

import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yannick on 29.05.17.
 */
public class MessagePackReader<T extends GenericTerminal> {
    private MessageUnpacker _packer;
    protected List<Fixup<?>> _fixups =
            new ArrayList<>();

    public MessagePackReader(MessageUnpacker packer) {
        _packer = packer;
    }

    /**
     * reads an EXML document. This is a msgpack tuple of
     * - format version (1)
     * - schema description
     * - a list of chunks
     * @param doc the Exml document that will receive the data
     */
    public void readDocument(Document<T> doc) throws IOException {
        int numSegments = _packer.unpackArrayHeader();
        if (numSegments != 3) {
            throw new RuntimeException("Wrong number of segments");
        }
        String formatVersion = _packer.unpackString();
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
        int numSchemas = _packer.unpackMapHeader();
        for (int i=0; i < numSchemas; i++) {
            String level = _packer.unpackString();
            if ("word".equals(level)) {
                readSchema(doc.terminalSchema());
            } else {
                readSchema(doc.markableSchemaByName(level, true));
            }
        }
    }

    public void readSchema(ObjectSchema<?> schema) throws IOException {
        int nKeys = _packer.unpackMapHeader();
        for (int i = 0; i < nKeys; i++) {
            String name = _packer.unpackString();
            String type = _packer.unpackString();
            // TODO verify attribute type and/or create attribute
        }
    }

    public void readChunks(Document<T> doc) throws IOException {
        int nChunks = _packer.unpackArrayHeader();
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
        int nFields = _packer.unpackArrayHeader();
        if (nFields != 2) {
            throw new RuntimeException("Wrong number of chunk fields");
        }
        int nTerminals = readTerminals(doc);
        int nLevels = _packer.unpackMapHeader();
        for (int i=0; i<nLevels; i++) {
            String level = _packer.unpackString();
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
        int nFields = _packer.unpackArrayHeader();
        if (nFields != 3) {
            throw new RuntimeException("wrong number of fields");
        }
        int nObjects = _packer.unpackInt();
        List<T> objects = new ArrayList<>(nObjects);
        for (int i=0; i<nObjects; i++) {
            objects.add(doc.createTerminal(null));
        }
        readObjects(doc, doc.terminalSchema(), objects);
        return nObjects;
    }

    public <E extends GenericMarkable> void readMarkableLevel(
            Document<T> doc, MarkableLevel<E> mlevel) throws IOException {
        int nFields = _packer.unpackArrayHeader();
        ObjectSchema<E> schema = mlevel.schema;
        if (nFields != 3) {
            throw new RuntimeException("wrong number of fields");
        }
        int nObjects = _packer.unpackInt();
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
    public <E extends NamedObject> void readObjects(
            Document<T> doc,
            ObjectSchema<E> schema, List<E> objects) throws IOException {
        List<Attribute<E, ?>> attributes = new ArrayList<>(schema.attrs.values());

        int nAttrs = _packer.unpackArrayHeader();
        List<String> s_attrs = new ArrayList<>();
        for (int i=0; i<nAttrs; i++) {
            s_attrs.add(_packer.unpackString());
        }
        int nAttrs2 = _packer.unpackArrayHeader();
        if (nAttrs != nAttrs2) {
            throw new RuntimeException("Number of attributes not matching");
        }

        for (String s_attr: s_attrs) {
            if (":id".equals(s_attr)) {
                int nObjects = _packer.unpackArrayHeader();
                if (nObjects != objects.size()) {
                    throw new RuntimeException("Wrong list size");
                }
                for (int i = 0; i < nObjects; i++) {
                    ValueType type = _packer.getNextFormat().getValueType();
                    if (type == ValueType.STRING) {
                        String newId = _packer.unpackString();
                        objects.get(i).setXMLId(newId);
                    } else if (type == ValueType.NIL) {
                        // nothing to do
                    } else {
                        throw new RuntimeException("Wrong value type:" + type);
                    }
                }
            } else if (":span".equals(s_attr)) {
                int nObjects = objects.size();
                int nNumbers = _packer.unpackArrayHeader();
                if (nNumbers != 2 * objects.size()) {
                    throw new RuntimeException("Wrong span number size");
                }
                int[] offsets = new int[nNumbers];
                for (int i = 0; i < nNumbers; i++) {
                    offsets[i] = _packer.unpackInt();
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
                int nObjects = _packer.unpackArrayHeader();
                if (nObjects != objects.size()) {
                    throw new RuntimeException("Wrong attribute column length");
                }
                for (int i=0; i < nObjects; i++) {
                    E obj = objects.get(i);
                    ValueType type = _packer.getNextFormat().getValueType();
                    if (type == ValueType.STRING) {
                        String val = _packer.unpackString();
                        try {
                            attr.putString(obj, val, doc);
                        } catch (MissingObjectException ex) {
                            // save/defer any unresolved refs
                            _fixups.add(new Fixup(obj, attr, val));
                        }
                    } else if (type == ValueType.NIL) {
                        _packer.unpackNil();
                    } else if (type == ValueType.INTEGER) {
                        ConverterKind converter_kind = attr.converter.getKind();
                        if (ConverterKind.STRING.equals(converter_kind)) {
                            // convert int values in refs to object offsets
                            Attribute<E, E> attr_alias = (Attribute<E, E>) attr;
                            int offset = _packer.unpackInt();
                            E obj2 = objects.get(i + offset);
                            attr_alias.accessor.put(obj, obj2);
                        } else if (ConverterKind.ENUM.equals(converter_kind)) {

                        } else {
                            throw new RuntimeException("Int in wrong column");
                        }
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        try {
            TuebaDocument doc = new TuebaDocument();
            MessageUnpacker unpack = MessagePack.newDefaultUnpacker(
                    new FileInputStream(args[0]));
            MessagePackReader<TuebaTerminal> reader = new MessagePackReader<>(unpack);
            reader.readDocument(doc);
            DocumentWriter.writeDocument(doc,
                new FileOutputStream(args[1]));
        } catch (IOException|XMLStreamException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

}
