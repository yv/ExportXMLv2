package exml.objects;

import exml.Document;

public class StringConverter implements IConverter<String> {
	@Override
	public String convertFromString(String s, Document<?> doc) {
		return s;
	}

	@Override
	public String convertToString(String obj, Document<?> doc) {
		return obj;
	}

	public static final StringConverter instance=new StringConverter();
}
