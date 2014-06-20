package exml.objects;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import exml.Document;

public class StringConverter implements IConverter<String> {
	@Override
	public String convertFromString(String s, Document<?> doc) {
		return s;
	}

	@Override
	public String convertToString(String obj, Document<?> doc) {
		return obj;
	}

	public static final StringConverter instance=new StringConverter();

	@Override
	public void declareAttribute(String name, XMLStreamWriter writer)
			throws XMLStreamException {
		writer.writeStartElement("text-attr");
		writer.writeAttribute("name", name);
		writer.writeEndElement();
	}
}
