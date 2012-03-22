package exml.objects;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import elkfed.ml.util.Alphabet;

public class ObjectSchema<T extends GenericObject> {
	private final String _name;
	private final GenericObjectFactory<T> _factory;

	public final Alphabet<String> slotnames=new Alphabet<String>();
	public final Map<String, Attribute<T,?>> attrs=
		new HashMap<String,Attribute<T,?>>();
	public final Map<String, Relation<T,?>> rels=
		new HashMap<String,Relation<T,?>>();
	
	public <V> void addAttribute(String name, IConverter<V> cvt) {
		int idx=slotnames.lookupIndex(name);
		attrs.put(name, new Attribute<T,V>(name, new GenericAccessor<T,V>(idx), cvt));
	}
	
	public <T2,V> void addAttribute(String name, IConverter<V> cvt, IAccessor<T,V> avt) {
		attrs.put(name, new Attribute<T,V>(name, avt, cvt));
	}	
	
	public Attribute<T,?> getAttribute(String name) {
		return attrs.get(name);
	}
	
	public <V extends GenericObject> void addRelation(String name, ObjectSchema<V> schema) {
		int idx=slotnames.lookupIndex(name);
		rels.put(name,new Relation<T,V>(name, new GenericAccessor<T,List<V>>(idx), schema));
	}

	public ObjectSchema(String name, GenericObjectFactory<T> factory) {
		_name=name;
		_factory=factory;
	}

	public T createMarkable() {
		return _factory.createObject(this);
	}
	
	public String getName() {
		return _name;
	}
}
