package exml.objects;

import exml.Document;
import exml.util.Graph;

public class StringConverter implements IConverter<String> {
	@Override
	public String convertFromString(String s, Document doc) {
		return s;
	}

	@Override
	public String convertToString(String obj, Document doc) {
		return obj;
	}

	@Override
	public void getUpDown(INamedObject src, String val, Document doc, Graph g) {
		// do nothing
	}
	
	public static final StringConverter instance=new StringConverter();
}
