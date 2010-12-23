package exml.objects;

import java.util.List;

import elkfed.ml.util.Alphabet;

public class GenericObjectSchema extends ObjectSchema<GenericNamedObject> {
	public final Alphabet<String> slotnames=new Alphabet<String>();
	
	@SuppressWarnings("unchecked")
	void addAttribute(String name, IConverter cvt) {
		int idx=slotnames.lookupIndex(name);
		attrs.add(new Attribute(name, new GenericAccessor(idx), cvt));
	}
	
	@SuppressWarnings("unchecked")
	void addRelation(String name, List<Attribute> attrs) {
		Relation new_rel;
		int idx=slotnames.lookupIndex(name);
		new_rel=new Relation(name, new GenericAccessor(idx));
		new_rel.relAttrs.addAll(attrs);
		rels.add(new_rel);
	}
}
