package exml.objects;

import java.util.Map;

import exml.Document;

public final class Attribute<Obj,Val> {
	public final String name;
	public final IAccessor<Obj,Val> accessor;
	public final IConverter<Val> converter;
	public Attribute(String nm, IAccessor<Obj,Val> acc, IConverter<Val> conv)
	{
		name=nm; accessor=acc; converter=conv;
	}
	
	void putProperty(Obj m, Map<String,String> d, Document doc) {
		d.put(name,converter.convertToString(accessor.get(m), doc));
	}
}
