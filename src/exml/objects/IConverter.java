package exml.objects;

import exml.Document;
import exml.MissingObjectException;

public interface IConverter<T> {
	String convertToString(T obj, Document<?> doc);
	T convertFromString(String s, Document<?> doc) throws MissingObjectException;
}
