package exml.io;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.lang3.StringUtils;

import exml.Document;
import exml.GenericMarkable;
import exml.GenericTerminal;
import exml.MarkableLevel;
import exml.objects.Attribute;
import exml.objects.GenericObject;
import exml.objects.NamedObject;
import exml.objects.ObjectSchema;
import exml.objects.ReferenceConverter;
import exml.objects.Relation;

public class DocumentWriter<T extends GenericTerminal> {
	public static String SPACES = "                                                    ";
	protected Document<T> _doc;
	protected XMLStreamWriter _writer;
	@SuppressWarnings("rawtypes")
	protected Stack<WriterStackEntry> _openTags =
			new Stack<WriterStackEntry>();
	
	public DocumentWriter(Document<T> doc, XMLStreamWriter writer) {
		_doc = doc;
		_writer = writer;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private <E extends GenericMarkable> void addEntries(String levelName,
			ArrayList<WriterStackEntry> allMarkables) {
		MarkableLevel<E> level = (MarkableLevel<E>)_doc.markableLevelByName(levelName, false);
		ObjectSchema<E> schema = (ObjectSchema<E>) _doc.markableSchemaByName(levelName, false);
		for (E m: level.getMarkables()) {
			allMarkables.add(
					new WriterStackEntry<E>(schema, m));
		}
	}
	
	@SuppressWarnings("unchecked")
	public void writeBody() throws XMLStreamException
	{
		ObjectSchema<T> tschema = _doc.terminalSchema();
		@SuppressWarnings("rawtypes")
		ArrayList<WriterStackEntry> allMarkables = new ArrayList<WriterStackEntry>();
		for (String levelName: _doc.getMarkableLevelNames()) {
			addEntries(levelName, allMarkables);
		}
		Collections.sort(allMarkables);
		_writer.writeStartElement("body");
		int cur_idx = 0;
		for (WriterStackEntry<? extends GenericMarkable> entry: allMarkables) {
			GenericMarkable m = entry.value;
			while (cur_idx < m.getStart()) {
				T tn = _doc.getTerminal(cur_idx);
				writeTerminal(tschema, tn);
				cur_idx++;
				// all elements that need to end here
				while (!_openTags.isEmpty() &&
						_openTags.peek().cutAt <= cur_idx) {
					_openTags.pop();
					_writer.writeCharacters(SPACES.substring(0, 2*_openTags.size()));
					_writer.writeEndElement();
					_writer.writeCharacters("\n");
				}
			}
			boolean need_span=false;
			if (!_openTags.isEmpty() &&
					_openTags.peek().cutAt < entry.cutAt) {
				entry.cutAt=_openTags.peek().cutAt;
				need_span=true;
			}
			writeMarkableStart(entry, need_span);
			_openTags.push(entry);
		}
		while (cur_idx < _doc.size()) {
			T tn = _doc.getTerminal(cur_idx);
			writeTerminal(tschema, tn);
			cur_idx++;
			// all elements that need to end here
			while (!_openTags.isEmpty() &&
					_openTags.peek().cutAt <= cur_idx) {
				_openTags.pop();
				_writer.writeCharacters(SPACES.substring(0, 2*_openTags.size()));
				_writer.writeEndElement();
				_writer.writeCharacters("\n");
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void writeTerminal(ObjectSchema<T> tschema, T tn)
			throws XMLStreamException {
		_writer.writeCharacters(SPACES.substring(0, 2*_openTags.size()));
		_writer.writeStartElement("word");
		_writer.writeAttribute("xml","http://www.w3.org/XML/1998/namespace", "id", tn.getXMLId());
		for (String key: (Set<String>)tschema.attrs.keySet()) {
			@SuppressWarnings("rawtypes")
			Attribute att = (Attribute)tschema.attrs.get(key);
			Object val = att.accessor.get(tn);
			if (val != null) {
				_writer.writeAttribute(key, att.converter.convertToString(val, _doc));
			}
		}
		for (String key: tschema.rels.keySet()) {
			writeRelation(tn, tschema.rels.get(key));
		}
		_writer.writeEndElement();
		_writer.writeCharacters("\n");
	}
	
	@SuppressWarnings("unchecked")
	public <E extends NamedObject, Obj extends GenericObject>
	   void writeRelation(E obj, Relation<E,Obj> rel) throws XMLStreamException {
			for (Obj edge: rel.get_relation(obj)) {
				_writer.writeCharacters(SPACES.substring(0, 2*_openTags.size()));
				_writer.writeStartElement(rel.name);
				for (String key: (Set<String>)rel.schema.attrs.keySet()) {
					@SuppressWarnings("rawtypes")
					Attribute att = (Attribute)rel.schema.attrs.get(key);
					Object val = att.accessor.get(edge);
					if (val != null) {
						_writer.writeAttribute(key, att.converter.convertToString(val, _doc));
					}
				}
				_writer.writeEndElement();
				_writer.writeCharacters("\n");	
			}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <M extends GenericMarkable>
	void writeMarkableStart(WriterStackEntry<M> entry,
			boolean needs_span) throws XMLStreamException
	{
		ObjectSchema<M> schema=entry.schema;
		M m=entry.value;
		_writer.writeCharacters(SPACES.substring(0, 2*_openTags.size()));
		_writer.writeStartElement(schema.getName());
		_writer.writeAttribute("xml","http://www.w3.org/XML/1998/namespace", "id", m.getXMLId());
		for (String key: (Set<String>)schema.attrs.keySet()) {
			Attribute att = (Attribute)schema.attrs.get(key);
			Object val = att.accessor.get(m);
			if (val != null && (needs_span || !"span".equals(key))) {
				_writer.writeAttribute(key, att.converter.convertToString(val, _doc));
			}
		}
		_writer.writeCharacters("\n");
		for (String key: schema.rels.keySet()) {
			writeRelation(m, schema.rels.get(key));
		}
	}
	
	public <M extends GenericObject> void writeSchemaContents(ObjectSchema<M> schema) throws XMLStreamException {
		for (String key: (Set<String>)schema.attrs.keySet()) {
			@SuppressWarnings("rawtypes")
			Attribute att = (Attribute)schema.attrs.get(key);
			att.converter.declareAttribute(key, _writer);
			_writer.writeCharacters("\n");
		}
	}
	
	public void writeSchema() throws XMLStreamException
	{
		_writer.writeStartElement("schema");
		_writer.writeCharacters("\n");
		// first declare all markable levels and their reference attributes
		_writer.writeStartElement("tnode");
		_writer.writeAttribute("name", "word");
		writeSchemaContents(_doc.terminalSchema());
		_writer.writeEndElement();
		_writer.writeCharacters("\n");
		// declare all markable levels
		for (String key: _doc.getMarkableLevelNames()) {
			_writer.writeStartElement("node");
			_writer.writeAttribute("name", key);
			_writer.writeCharacters("\n");
			writeSchemaContents(_doc.markableSchemaByName(key, false));
			_writer.writeEndElement();
			_writer.writeCharacters("\n");
		}
		// then declare all edges
		for (String key: _doc.getEdgeNames()) {
			_writer.writeStartElement("edge");
			_writer.writeAttribute("name", key);
			List<String> parents=new ArrayList<String>();
			for (String keyM: _doc.getMarkableLevelNames()) {
				ObjectSchema schema = _doc.markableSchemaByName(keyM, false);
				if (schema.rels.containsKey(key)) {
					parents.add(keyM);
				}
			}
			ObjectSchema tschema = _doc.terminalSchema();
			if (tschema.rels.containsKey(key)) {
				parents.add("word");
			}
			_writer.writeAttribute("parent", StringUtils.join(parents,"|"));
			_writer.writeCharacters("\n");
			writeSchemaContents(_doc.edgeSchemaByName(key, false));
			_writer.writeEndElement();
			_writer.writeCharacters("\n");
		}
		_writer.writeEndElement();
		_writer.writeCharacters("\n");
	}
	
	/** writes the given document to an output stream */
	public static <TT extends GenericTerminal>
	void writeDocument(Document<TT> doc, OutputStream os)
				throws XMLStreamException
	{
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		XMLStreamWriter writer = factory.createXMLStreamWriter(os);
		DocumentWriter<TT> my_writer = new DocumentWriter<TT>(doc, writer);
		writer.writeStartDocument();
		writer.writeStartElement("exml-doc");
		my_writer.writeSchema();
		my_writer.writeBody();
		writer.writeEndDocument();
		writer.flush();
		writer.close();
	}
	
	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		try {
			@SuppressWarnings("rawtypes")
			Document doc = Document.createDocument();
			DocumentReader.readDocument(doc, new FileInputStream(args[0]));
			OutputStream os;
			if (args.length > 1) {
				os = new FileOutputStream(args[1]);
			} else {
				os = System.out;
			}
			writeDocument(doc, os);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}
