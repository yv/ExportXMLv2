package exml.objects;

import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import exml.Document;
import exml.MissingObjectException;

public class IDListConverter<T extends NamedObject> implements IConverter<List<T>>{

	@Override
	public String convertToString(List<T> lst, Document<?> doc) {
		if (lst==null || lst.isEmpty()) { return null; }
		StringBuffer buf=new StringBuffer();
		for (T obj: lst) {
			buf.append(" ");
			buf.append(obj.getXMLId());
		}
		return buf.substring(1);
	}

	@Override
	public List<T> convertFromString(String s, Document<?> doc)
			throws MissingObjectException {
		if (s==null || "".equals(s)) { return null; }
		ArrayList<T> lst = new ArrayList<T>();
		for (String obj_name: s.split(" ")) {
			@SuppressWarnings("unchecked")
			T obj = (T)doc.resolveObject(obj_name);
			lst.add(obj);
		}
		return lst;
	}

	@Override
	public void declareAttribute(String name, XMLStreamWriter writer)
			throws XMLStreamException {
		writer.writeStartElement("reflist");
		writer.writeAttribute("name", name);
		writer.writeEndElement();
	}

}
