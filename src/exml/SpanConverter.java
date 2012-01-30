package exml;

import java.util.ArrayList;
import java.util.Arrays;

import exml.objects.IConverter;
import gnu.trove.TIntArrayList;

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
		ArrayList<String> parts=new ArrayList<String>();
		for (int i=0; i<obj.size();i+=2) {
			int start=obj.get(i);
			int end=obj.get(i+1);
			if (start+1==end) {
				parts.add(doc.nameForObject(doc.getTerminal(start)));
			} else {
				parts.add(String.format("%s..%s",
						doc.nameForObject(doc.getTerminal(start)),
						doc.nameForObject(doc.getTerminal(end-1))));
			}
		}
		return Arrays.toString(parts.toArray(new String[parts.size()]));
	}
	
	static final public SpanConverter instance=new SpanConverter();
}
