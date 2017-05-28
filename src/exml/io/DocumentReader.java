package exml.io;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import exml.Document;
import exml.GenericMarkable;
import exml.GenericTerminal;
import exml.MarkableLevel;
import exml.MissingObjectException;
import exml.SpanAccessor;
import exml.objects.Attribute;
import exml.objects.EnumConverter;
import exml.objects.GenericObject;
import exml.objects.NamedObject;
import exml.objects.ObjectSchema;
import exml.objects.ReferenceConverter;
import exml.objects.Relation;
import exml.objects.StringConverter;


/**
 * reads a document in ExportXMLv2 inline format
 * @author yannickv
 *
 */
public class DocumentReader<E extends GenericTerminal> {
	protected Document<E> _doc;
	protected XMLEventReader _reader;
	protected boolean _inBody=false;
	protected Stack<ReaderStackEntry> _openTags =
		new Stack<ReaderStackEntry>();
	protected List<ReaderStackEntry<GenericMarkable>> _to_add =
		new ArrayList<ReaderStackEntry<GenericMarkable>>();
	protected List<Fixup<?>> _fixups =
		new ArrayList<Fixup<?>>();
	private static final QName qname_name=QName.valueOf("name");
	private static final QName qname_word=QName.valueOf("word");
	private static final QName qname_form=QName.valueOf("form");
	private static final QName qname_parent=QName.valueOf("parent");
	private static final QName qname_descr=QName.valueOf("description");
	// xml:id
	public static final QName qname_xmlid=QName.valueOf("{http://www.w3.org/XML/1998/namespace}id");
	
	public void readAttributes(ObjectSchema<?> schema) throws XMLStreamException
	{
		int depth=1;
		EnumConverter converter = null;
		while (true) {
			XMLEvent ev=_reader.nextTag();
			if (ev.isStartElement()) {
				StartElement elm=ev.asStartElement();
				String tagname=elm.getName().getLocalPart();
				String attname=elm.getAttributeByName(qname_name).getValue();
				if ("text-attr".equals(tagname)) {
					schema.addAttribute(attname, StringConverter.instance);
				} else if ("enum-attr".equals(tagname)) {
					converter = new EnumConverter();
					schema.addAttribute(attname, converter);
				} else if ("node-ref".equals(tagname)) {
					//TODO store up/down relationship
					schema.addAttribute(attname, new ReferenceConverter<NamedObject>());
				} else if ("val".equals(tagname)) {
					String valdescr = elm.getAttributeByName(qname_descr).getValue();
					converter.addVal(attname,valdescr);
				}
				depth++;
			} else if (ev.isEndElement()) {
				if (--depth==0) {
					return;
				}
			}
		}
	}
	
	public void readSchema() throws XMLStreamException {
		while (true) {
			XMLEvent ev=_reader.nextTag();
			if (ev.isStartElement()) {
				StartElement elm=ev.asStartElement();
				String tagname=elm.getName().getLocalPart();
				if ("tnode".equals(tagname)) {
					// do stuff with terminal node
					readAttributes(_doc.terminalSchema());
				} else if ("node".equals(tagname)) {
					//TODO store locality information
					// do stuff with nonterminal node
					readAttributes(_doc.markableSchemaByName(elm.getAttributeByName(qname_name).getValue(),
							true));
				} else if ("edge".equals(tagname)) {
					String edge_name=elm.getAttributeByName(qname_name).getValue();
					ObjectSchema<? extends GenericObject> edgeSchema=_doc.edgeSchemaByName(edge_name, true);
					readAttributes(edgeSchema);
					for (String node_name: elm.getAttributeByName(qname_parent).getValue().split("\\|")) {
						ObjectSchema<? extends NamedObject> nodeSchema;
						if ("word".equals(node_name)) {
							nodeSchema=_doc.terminalSchema();
						} else {
							nodeSchema=_doc.markableSchemaByName(node_name,true);
						}
						nodeSchema.addRelation(edge_name, edgeSchema);
					}
				}
			} else if (ev.isEndElement()) {
				EndElement elm=ev.asEndElement();
				if ("schema".equals(elm.getName().getLocalPart())) {
					return;
				}
			}
		}
	}
	
	private final E make_terminal(StartElement elm)
	{
		String word_val=elm.getAttributeByName(qname_form).getValue();
		E new_term=_doc.createTerminal(word_val);
		javax.xml.stream.events.Attribute att=elm.getAttributeByName(qname_xmlid);
		if (att!=null) {
			new_term.setXMLId(att.getValue());
			_doc.nameForObject(new_term);
		}
		setObjectAttributes(new_term,_doc.terminalSchema(),elm);
		new_term.setWord(word_val);
		return new_term;
	}
	
	private final <M extends GenericMarkable> void push_markable(ObjectSchema<M> schema,StartElement elm)
	{
		M new_m=schema.createMarkable();
		javax.xml.stream.events.Attribute qname_attr = elm.getAttributeByName(qname_xmlid);
		if (qname_attr != null) {
			new_m.setXMLId(qname_attr.getValue());
		}
		new_m.setStart(_doc.size());
		_doc.nameForObject(new_m);
		setObjectAttributes(new_m,schema,elm);
		_openTags.push(new ReaderStackEntry<M>(schema, new_m));
	}
	
	@SuppressWarnings("unchecked")
	public void readBody() throws XMLStreamException {
		if (!_inBody) {
			throw new RuntimeException("should be in body!");
		}
		while (true) {
			XMLEvent ev=_reader.nextTag();
			if (ev.isStartElement()) {
				boolean is_a_rel=false;
				StartElement elm=ev.asStartElement();
				String tagname=elm.getName().getLocalPart();
				if ("word".equals(tagname)) {
					GenericTerminal new_term = make_terminal(elm);
					ev=_reader.nextTag();
					while (!ev.isEndElement()) {
						// terminals should not have embedded nodes, but may have edges
						StartElement sub=ev.asStartElement();
						String relName=sub.getName().getLocalPart();
						Relation relSchema;
						relSchema=_doc.terminalSchema().rels.get(relName);
						if (relSchema!=null) {
							GenericObject relObj=relSchema.schema.createMarkable();
							setObjectAttributes(relObj,relSchema.schema,sub);
							relSchema.get_relation(new_term).add(relObj);
							// read close-tag for relation
							ev=_reader.nextTag();
							if (!ev.isEndElement()) {
								throw new RuntimeException("not a closing tag:"+ev);
							}
							// read either close-tag for word or open tag for new edge
							ev=_reader.nextTag();
						} else {
							throw new RuntimeException("Should close the word tag:"+ev+ev.asStartElement().getName());
						}
					}
					continue;
				}
				// if the tag corresponds to a relation, put a new relation object
				else if (_openTags.size()>0) {
					ReaderStackEntry<? extends GenericMarkable> entry=_openTags.get(_openTags.size()-1);
					Relation relSchema;
					relSchema=entry.schema.rels.get(tagname);
					if (relSchema!=null) {
						is_a_rel=true;
						GenericObject relObj=relSchema.schema.createMarkable();
						setObjectAttributes(relObj,relSchema.schema,elm);
						relSchema.get_relation(entry.value).add(relObj);
						// read close-tag for relation
						ev=_reader.nextTag();
						if (!ev.isEndElement()) {
							throw new RuntimeException("not a closing tag:"+ev);
						}
					}
				}
				// if the tag corresponds to a level, create/register new markable
				// and put on the stack
				if (!is_a_rel) {
					ObjectSchema schema=_doc.markableSchemaByName(tagname, true);
					push_markable(schema, elm);
				}
			} else if (ev.isEndElement()) {
				EndElement elm=ev.asEndElement();
				String tagname=elm.getName().getLocalPart();
				// if we're finished with the body, break out of the loop
				if (_openTags.size()==0) {
					if (!"body".equals(tagname)) {
						throw new RuntimeException("body closed by "+tagname);
					}
					break;
				}
				// do something else
				ReaderStackEntry entry=_openTags.pop();
				entry.value.setEnd(_doc.size());
				_to_add.add(entry);
			}
		}
		// run all fixups
		for (Fixup f: _fixups) {
			try {
				f.run(_doc);
			} catch (MissingObjectException ex) {
				throw new RuntimeException(ex);
			}
		}
		// register all objects in their markable layers
		for (ReaderStackEntry entry: _to_add) {
			MarkableLevel mlvl=_doc.markableLevelByName(entry.schema.getName(),true);
			mlvl.addMarkable(entry.value);
		}
	}
	
	@SuppressWarnings("unchecked")
	protected <E extends GenericObject> void setObjectAttributes(E newObj, ObjectSchema<E> objectSchema, StartElement elm) {
		for (Iterator it=elm.getAttributes(); it.hasNext();) {
			javax.xml.stream.events.Attribute att=(javax.xml.stream.events.Attribute)it.next();
			if (att.getName().equals(qname_xmlid)) {
				continue;
			}
			String att_name=att.getName().getLocalPart();
			String att_val=att.getValue();
			Attribute obj_attr=objectSchema.attrs.get(att_name);
			if (obj_attr == null && "span".equals(att_name)) {
				obj_attr = SpanAccessor.span_attribute;
			}
			if (obj_attr==null) {
				System.err.println("No declared attribute:"+att_name+"="+att_val+"/"+obj_attr);
				continue;
			}
			try {
				obj_attr.putString(newObj, att_val, _doc);
			} catch (MissingObjectException e) {
				_fixups.add(new Fixup(newObj,obj_attr,att_val));
			}
		}
	}

	/**
	 * creates a document reader for a given XML stream and
	 * reads up to the start of the body 
	 * @param d the document that is to be read into
	 * @param reader the XML stream source
	 * @throws XMLStreamException
	 */
	public DocumentReader(Document<E> d, XMLEventReader reader)
	throws XMLStreamException
	{
		_doc=d;
		_reader=reader;
		// read document header up to body
		while (true) {
			XMLEvent ev=reader.nextTag();
			if (ev.isStartElement()) {
				StartElement elm=ev.asStartElement();
				String tagname=elm.getName().getLocalPart();
				if ("schema".equals(tagname)) {
					readSchema();
				} else if ("body".equals(tagname)) {
					_inBody=true;
					break;
				}
			}
		}
	}
	
	/**
	 * reads the XML from the input stream into the current document.
	 * @param d current document
	 * @param is input to be read
	 * @throws XMLStreamException
	 */
	public static <T extends GenericTerminal> void readDocument(Document<T> d, InputStream is) throws XMLStreamException {
		XMLInputFactory factory=XMLInputFactory.newInstance();
		XMLEventReader xml_reader=factory.createXMLEventReader(is);
		DocumentReader<T> doc_reader=new DocumentReader<T>(d, xml_reader);
		doc_reader.readBody();
		xml_reader.close();
	}
	
	/**
	 * reads one ExportXMLv2 document without doing anything with it
	 * @param args command-line arguments
	 */
	public static void main(String[] args) {
		try {
			Document<GenericTerminal> doc=Document.createDocument();
			readDocument(doc, new FileInputStream(args[0]));
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(1);
		}
	}
}
