package exml.objects;

import exml.Document;
import exml.MissingObjectException;

public final class Attribute<Obj,Val> {
	public final String name;
	public final IAccessor<Obj,Val> accessor;
	public final IConverter<Val> converter;
	public Attribute(String nm, IAccessor<Obj,Val> acc, IConverter<Val> conv)
	{
		name=nm; accessor=acc; converter=conv;
	}
	
	public void putString(Obj m, String s, Document<?> doc) throws MissingObjectException {
		accessor.put(m, converter.convertFromString(s, doc));
	}
	
	public String getString(Obj m, Document<?> doc) {
		return converter.convertToString(accessor.get(m), doc);
	}
}
