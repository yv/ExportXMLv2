package exml.objects;

import java.util.ArrayList;
import java.util.List;

public class Relation<Obj, Val extends GenericObject> {
	public final String name;
	public final IAccessor<Obj, List<Val>> accessor;
	public final ObjectSchema<Val> schema;
	
	public Relation(String name_val, IAccessor<Obj,List<Val>> accessor_val,
			ObjectSchema<Val> schema_val)
	{
		name=name_val;
		accessor=accessor_val;
		schema=schema_val;
	}
	
	public List<Val> get_relation(Obj obj) {
		List<Val> result=accessor.get(obj);
		if (result==null) {
			result=new ArrayList<Val>();
			accessor.put(obj, result);
		}
		return result;
	}
}
