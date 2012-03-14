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
import exml.objects.Attribute;
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
public class DocumentReader {
	protected Document<?> _doc;
	protected XMLEventReader _reader;
	protected boolean _inBody=false;
	protected Stack<StackEntry<GenericMarkable>> _openTags =
		new Stack<StackEntry<GenericMarkable>>();
	protected List<StackEntry<GenericMarkable>> _to_add =
		new ArrayList<StackEntry<GenericMarkable>>();
	protected List<Fixup<?>> _fixups =
		new ArrayList<Fixup<?>>();
	private static final QName qname_name=QName.valueOf("name");
	private static final QName qname_word=QName.valueOf("word");
	private static final QName qname_form=QName.valueOf("form");
	private static final QName qname_parent=QName.valueOf("parent");
	// xml:id
	private static final QName qname_xmlid=QName.valueOf("{http://www.w3.org/XML/1998/namespace}id");
	
	public void readAttributes(ObjectSchema<?> schema) throws XMLStreamException
	{
		int depth=1;
		while (true) {
			XMLEvent ev=_reader.nextTag();
			if (ev.isStartElement()) {
				StartElement elm=ev.asStartElement();
				String tagname=elm.getName().getLocalPart();
				String attname=elm.getAttributeByName(qname_name).getValue();
				if ("text-attr".equals(tagname)) {
					schema.addAttribute(attname, StringConverter.instance);
				} else if ("enum-attr".equals(tagname)) {
					schema.addAttribute(attname, StringConverter.instance);
				} else if ("node-ref".equals(tagname)) {
					schema.addAttribute(attname, new ReferenceConverter<NamedObject>());
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
					// do stuff with nonterminal node
					readAttributes(_doc.markableSchemaByName(elm.getAttributeByName(qname_name).getValue(),
							true));
				} else if ("edge".equals(tagname)) {
					String edge_name=elm.getAttributeByName(qname_name).getValue();
					ObjectSchema<? extends GenericObject> edgeSchema=_doc.edgeSchemaByName(edge_name, true);
					readAttributes(edgeSchema);
					for (String node_name: elm.getAttributeByName(qname_parent).getValue().split("\\|")) {
						ObjectSchema<? extends NamedObject> nodeSchema=_doc.markableSchemaByName(node_name,true);
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
					String word_val=elm.getAttributeByName(qname_form).getValue();
					GenericTerminal new_term=_doc.createTerminal(word_val);
					javax.xml.stream.events.Attribute att=elm.getAttributeByName(qname_xmlid);
					if (att!=null) {
						new_term.setXMLId(att.getValue());
						_doc.nameForObject(new_term);
					}
					setObjectAttributes(new_term,_doc.terminalSchema(),elm);
					new_term.set_word(word_val);
					ev=_reader.nextTag();
					if (!ev.isEndElement()) {
						throw new RuntimeException("not a closing tag:"+ev);
					}
					continue;
				}
				// if the tag corresponds to a relation, put a new relation object
				else if (_openTags.size()>0) {
					StackEntry<? extends GenericMarkable> entry=_openTags.get(_openTags.size()-1);
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
					ObjectSchema<? extends GenericMarkable> schema=_doc.markableSchemaByName(tagname, true);
					GenericMarkable new_m=schema.createMarkable();
					new_m.setXMLId(elm.getAttributeByName(qname_xmlid).getValue());
					new_m.setStart(_doc.size());
					_doc.nameForObject(new_m);
					setObjectAttributes(new_m,schema,elm);
					_openTags.push(new StackEntry<GenericMarkable>((ObjectSchema<GenericMarkable>) schema, new_m));
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
				StackEntry entry=_openTags.pop();
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
		for (StackEntry entry: _to_add) {
			MarkableLevel mlvl=_doc.markableLevelByName(entry.schema.getName(),true);
			mlvl.addMarkable(entry.value);
		}
	}
	
	@SuppressWarnings("unchecked")
	protected void setObjectAttributes(GenericObject newObj, ObjectSchema<?> objectSchema, StartElement elm) {
		for (Iterator it=elm.getAttributes(); it.hasNext();) {
			javax.xml.stream.events.Attribute att=(javax.xml.stream.events.Attribute)it.next();
			if (att.getName().equals(qname_xmlid)) {
				continue;
			}
			String att_name=att.getName().getLocalPart();
			String att_val=att.getValue();
			Attribute obj_attr=objectSchema.attrs.get(att_name);
			if (obj_attr==null) {
				System.err.println(att_name+"="+att_val+"/"+obj_attr);
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
	public DocumentReader(Document<?> d, XMLEventReader reader)
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
	public static void readDocument(Document<?> d, InputStream is) throws XMLStreamException {
		XMLInputFactory factory=XMLInputFactory.newInstance();
		XMLEventReader xml_reader=factory.createXMLEventReader(is);
		DocumentReader doc_reader=new DocumentReader(d, xml_reader);
		doc_reader.readBody();
		xml_reader.close();
	}
	
	/**
	 * reads one ExportXMLv2 document without doing anything with it
	 * @param args
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
