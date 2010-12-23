package exml.objects;

import exml.Document;
import exml.util.Graph;

public interface IConverter<T> {
	String convertToString(T obj, Document doc);
	T convertFromString(String s, Document doc);
	void getUpDown(INamedObject src, T val, Document doc, Graph g);
}
