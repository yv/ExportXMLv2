package exml.objects;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import elkfed.ml.util.Alphabet;

public class ObjectSchema<T extends GenericObject> {
	private final String _name;
	private final Class<T> _cls;
	private final GenericObjectFactory<T> _factory;

	public final Alphabet<String> slotnames;
	public final Map<String, Attribute<T,?>> attrs=
		new HashMap<String,Attribute<T,?>>();
	public final Map<String, Relation<T,?>> rels=
		new HashMap<String,Relation<T,?>>();
	
	public <V> IAccessor<T,V> genericAccessor(String name) {
		int idx=slotnames.lookupIndex(name);
		return new GenericAccessor<T,V>(idx);		
	}

	public void addStringAttribute(String name) {
		addAttribute(name, StringConverter.instance);
	}

	public <V> void addAttribute(String name, IConverter<V> cvt) {
		if (attrs.containsKey(name)) {
			Attribute<T, V> old_attr = (Attribute<T, V>) attrs.get(name);
			if (old_attr.converter != cvt) {
				// TODO detect whether old and new declaration are incompatible
				// System.err.println("Attribute "+name+" already declared, doing nothing.");
			}
		} else {
			attrs.put(name, new Attribute<T,V>(name, this.<V>genericAccessor(name), cvt));
		}
	}
	
	public <V> void addAttribute(String name, IConverter<V> cvt, IAccessor<T,V> avt) {
		attrs.put(name, new Attribute<T,V>(name, avt, cvt));
	}	
	
	public <V> void addAttribute(Attribute<T,V> att) {
		attrs.put(att.name, att);
	}
	
	public Attribute<T,?> getAttribute(String name) {
		return attrs.get(name);
	}
	
	public <V extends GenericObject> void addRelation(String name, ObjectSchema<V> schema) {
		rels.put(name,new Relation<T,V>(name, this.<List<V>>genericAccessor(name), schema));
	}
	
	public <V extends GenericObject> void addRelation(Relation<T,V> rel) {
		rels.put(rel.name, rel);
	}

	public ObjectSchema(String name, Class<T> cls,
			GenericObjectFactory<T> factory, Alphabet<String> xslotnames) {
		_name=name;
		_cls=cls;
		_factory=factory;
		slotnames=xslotnames;
	}
	
	public ObjectSchema(String name, Class<T> cls,
			GenericObjectFactory<T> factory) {
		this(name, cls, factory, new Alphabet<String>());
	}
	
	public ObjectSchema(String name, Class<T> cls) {
		this(name, cls, 
				BeanAccessors.factoryForClass(cls),
				new Alphabet<String>());
	}
	
	/**
	 * returns true if it's possible to create and use objects of
	 * class cls2 with this ObjectSchema
	 * @param cls2 a class of objects
	 * @return true if we can use this schema
	 */
	public boolean isCompatibleWith(Class<?> cls2) {
		return cls2.isAssignableFrom(_cls);
	}

	public T createMarkable() {
		return _factory.createObject(this);
	}
	
	public String getName() {
		return _name;
	}
}
