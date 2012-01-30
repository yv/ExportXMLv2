package exml.io;

import exml.GenericMarkable;
import exml.objects.ObjectSchema;

public class StackEntry<T extends GenericMarkable> {
	public final ObjectSchema<T> schema;
	public final T value;
	public StackEntry(ObjectSchema<T> sc, T val) {
		schema=sc; value=val;
	}
}
