package exml.objects;

import java.util.ArrayList;
import java.util.List;

/** schema for an object that can have attributes and relations */
public class ObjectSchema<T extends INamedObject> {
	public final List<Attribute<T,?>> attrs=new ArrayList<Attribute<T,?>>();
	public final List<Relation<T,?>> rels=new ArrayList<Relation<T,?>>();
}
