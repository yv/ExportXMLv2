package exml.io;

import exml.GenericMarkable;
import exml.GenericMarkable.DiscourseOrder;
import exml.objects.ObjectSchema;

public class WriterStackEntry<T extends GenericMarkable> implements
	Comparable<WriterStackEntry<T>>
{
	public final ObjectSchema<T> schema;
	public final String levelName;
	public final T value;
	public int cutAt;
	private static DiscourseOrder order= new DiscourseOrder();
	
	public WriterStackEntry(ObjectSchema<T> sc, String lvl, T val) {
		schema=sc; value=val;
		levelName=lvl;
		cutAt=val.getEnd();
	}

	@Override
	public int compareTo(WriterStackEntry<T> o) {
		return order.compare(this.value, o.value);
	}	
}
