package exml.objects;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import exml.Document;
import exml.MissingObjectException;
import exml.ReferenceRestriction;

public class ReferenceConverter<T extends NamedObject> implements IConverter<T> {
	public ReferenceRestriction restriction=ReferenceRestriction.NONE;
	
	@SuppressWarnings("unchecked")
	@Override
	public T convertFromString(String s, Document<?> doc) throws MissingObjectException {
		if (s==null) return null;
		return (T)doc.resolveObject(s);
	}

	@Override
	public String convertToString(T obj, Document<?> doc) {
		return doc.nameForObject(obj);
	}

	@Override
	public void declareAttribute(String name, XMLStreamWriter writer)
			throws XMLStreamException {
		writer.writeStartElement("node-ref");
		writer.writeAttribute("name", name);
		writer.writeEndElement();
	}
}
