package exml.objects;

import java.util.ArrayList;
import java.util.List;

import exml.Document;
import exml.util.Graph;

public class Relation<Obj extends INamedObject,Val> {
	public final String name;
	public final IAccessor<Obj,List<Val>> accessor;
	@SuppressWarnings("unchecked")
	public final List<Attribute> relAttrs=new ArrayList<Attribute>();
	public Relation(String nm, IAccessor<Obj,List<Val>> acc) {
		name=nm; accessor=acc;
	}
	
	public <T> void addRelAttribute(Attribute<Val,T> att) {
		relAttrs.add(att);
	}
	
	@SuppressWarnings("unchecked")
	public void getUpDown(Obj src, Document doc, Graph g) {
		List<Val> vals=accessor.get(src);
		for (Val v: vals) {
			for (Attribute att:relAttrs) {
				att.converter.getUpDown(src, att.accessor.get(v), doc, g);
			}
		}
	}
}
