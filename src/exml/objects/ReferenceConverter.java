package exml.objects;

import exml.Document;
import exml.ReferenceRestriction;
import exml.util.Graph;

public class ReferenceConverter<T extends INamedObject> implements IConverter<T> {
	public ReferenceRestriction restriction=ReferenceRestriction.NONE;
	
	@SuppressWarnings("unchecked")
	@Override
	public T convertFromString(String s, Document doc) {
		return (T)doc.resolveObject(s);
	}

	@Override
	public String convertToString(T obj, Document doc) {
		return doc.nameForObject(obj);
	}

	@Override
	public void getUpDown(INamedObject src, T val, Document doc, Graph g) {
		if (restriction==ReferenceRestriction.UP) {
			g.addEdge(doc.nameForObject(src), doc.nameForObject(val));
		} else if (restriction==ReferenceRestriction.DOWN) {
			g.addEdge(doc.nameForObject(val), doc.nameForObject(src));
		}
	}
}
