package exml;

import java.util.HashMap;
import java.util.UUID;

import exml.objects.INamedObject;

public class Document {
	
	private final HashMap<String,INamedObject> _obj_by_id=new HashMap<String,INamedObject>();
	
	public INamedObject resolveObject(String s) {
		return _obj_by_id.get(s);
	}
	
	public String nameForObject(INamedObject o) {
		String nm=o.getXMLId();
		if (nm==null) {
			nm=UUID.randomUUID().toString();
			o.setXMLId(nm);
			_obj_by_id.put(nm, o);
		}
		return nm;
	}
}
