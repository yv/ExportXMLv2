package exml.objects;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import exml.Document;
import exml.MissingObjectException;

public interface IConverter<T> {
	String convertToString(T obj, Document<?> doc);
	T convertFromString(String s, Document<?> doc) throws MissingObjectException;
	void declareAttribute(String name, XMLStreamWriter writer) throws XMLStreamException;
}
