package exml.io;

import exml.GenericMarkable;
import exml.objects.ObjectSchema;

public class ReaderStackEntry<T extends GenericMarkable> {
	public final ObjectSchema<T> schema;
	public final T value;

	public ReaderStackEntry(ObjectSchema<T> sc, T val) {
		schema=sc; value=val;

	}
}
