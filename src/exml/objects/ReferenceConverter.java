package exml.objects;

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
}
