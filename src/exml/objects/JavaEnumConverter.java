package exml.objects;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import exml.Document;

public class JavaEnumConverter<E extends Enum<E>> implements IConverter<E> {
	Class<E> enumClass;
	public static class EnumVal<E> {
		public final String name;
		public String description;
		public final E value;
		public EnumVal(String n, String d, E v) {
			name=n; description=d;
			value=v;
		}
	}
	
	public final HashMap<String,EnumVal<E>> name_map=new HashMap<String,EnumVal<E>>();
	public final List<EnumVal<E>> vals=new ArrayList<EnumVal<E>>();
	
	public JavaEnumConverter(Class<E> vals) {
		for (E e: vals.getEnumConstants()) {
			addVal(e.name(), null, e);
		}
	}
	
	public void addVal(String n, String d, E v) {
		EnumVal<E> val = new EnumVal<E>(n,d,v);
		name_map.put(n, val);
	}

	public void addVal(String n, String d) {
		EnumVal<E> val = name_map.get(n);
		val.description = d;
	}
	
	@Override
	public String convertToString(E s, Document<?> doc) {
		return s.name();
	}
	
	@Override
	public E convertFromString(String s, Document<?> doc) {
		EnumVal<E> val = name_map.get(s);
		return val.value;
	}
	
	@Override
	public void declareAttribute(String name, XMLStreamWriter writer)
			throws XMLStreamException {
		writer.writeStartElement("enum-attr");
		writer.writeAttribute("name", name);
		for (EnumVal<E> val: vals) {
			writer.writeCharacters("\n  ");
			writer.writeStartElement("val");
			writer.writeAttribute("name", val.name);
			if (val.description != null) {
				writer.writeAttribute("description", val.description);
			}
			writer.writeEndElement();
		}
		writer.writeCharacters("\n");
		writer.writeEndElement();
	}
}