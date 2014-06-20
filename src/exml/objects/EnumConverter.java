package exml.objects;

import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class EnumConverter extends StringConverter {
	public static class EnumVal {
		public final String name;
		public final String description;
		public EnumVal(String n, String d) {
			name=n; description=d;
		}
	}
	public final List<EnumVal> vals=new ArrayList<EnumVal>();
	
	public void addVal(String n, String d) {
		EnumVal val = new EnumVal(n,d);
		vals.add(val);
	}
	
	@Override
	public void declareAttribute(String name, XMLStreamWriter writer)
			throws XMLStreamException {
		writer.writeStartElement("enum-attr");
		writer.writeAttribute("name", name);
		for (EnumVal val: vals) {
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
