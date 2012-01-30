package exml.objects;


public interface GenericObjectFactory<T extends GenericObject> {
	T createObject(ObjectSchema<T> schema);
}
