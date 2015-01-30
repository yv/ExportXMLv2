package exml.simple;

import exml.Document;
import exml.GenericMarkable;
import exml.GenericTerminal;
import exml.MarkableLevel;
import exml.objects.BeanAccessors;
import exml.objects.ObjectSchema;

public class SimpleDocument<T extends GenericTerminal> extends Document<T> {
	public SimpleDocument(Class<T> cls) {
		super(BeanAccessors.getInstance().schemaForClass(cls));
	}
	
	public SimpleDocument(ObjectSchema<T> schema) {
		super(schema);
	}
	
	public <M extends GenericMarkable> MarkableLevel<M> addMarkableLevel(String name, Class<M> cls) {
		ObjectSchema<M> schema = BeanAccessors.getInstance().schemaForClass(cls);
		MarkableLevel<M> level = new MarkableLevel<M>(schema, this);
		addMarkableLevel(level, name);
		return level;
	}
}
