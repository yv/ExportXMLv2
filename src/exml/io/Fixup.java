package exml.io;

import exml.Document;
import exml.MissingObjectException;
import exml.objects.Attribute;
import exml.objects.GenericObject;

public class Fixup<T extends GenericObject> {
	public final T target;
	public final Attribute<T, ?> attribute;
	public final String value;
	
	public Fixup(T tgt, Attribute<T,?> att, String val)
	{
		target=tgt; attribute=att; value=val;
	}
	
	public void run(Document<?> doc) throws MissingObjectException {
		attribute.putString(target, value, doc);
	}
}
