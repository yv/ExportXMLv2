package exml;

import java.util.ArrayList;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import exml.objects.IConverter;
import gnu.trove.list.array.TIntArrayList;

public class SpanConverter implements IConverter<TIntArrayList> {

	@Override
	public TIntArrayList convertFromString(String s, Document<?> doc)
			throws MissingObjectException {
		TIntArrayList parts = new TIntArrayList();
		for (String subSpan : s.split(",")) {
			if (subSpan.contains("..")) {
				String[] range = subSpan.split("\\.\\.");
				parts.add(doc.getPosition(range[0]));
				parts.add(doc.getPosition(range[1]) + 1);
			} else {
				int posn = doc.getPosition(subSpan);
				parts.add(posn);
				parts.add(posn + 1);
			}
		}
		return parts;
	}

	@Override
	public String convertToString(TIntArrayList obj, Document<?> doc) {
		StringBuffer buf = new StringBuffer();
		ArrayList<String> parts=new ArrayList<String>();
		for (int i=0; i<obj.size();i+=2) {
			int start=obj.get(i);
			int end=obj.get(i+1);
			if (i>0) {
				buf.append(',');
			}
			if (start+1==end) {
				buf.append(doc.nameForObject(doc.getTerminal(start)));
			} else {
				buf.append(String.format("%s..%s",
						doc.nameForObject(doc.getTerminal(start)),
						doc.nameForObject(doc.getTerminal(end-1))));
			}
		}
		return buf.toString();
	}
	
	static final public SpanConverter instance=new SpanConverter();

	@Override
	public void declareAttribute(String name, XMLStreamWriter writer)
			throws XMLStreamException {
		// the span attribute is always implied.
	}
}
