package exml.simple;

import exml.GenericTerminal;
import exml.objects.ObjectSchema;

public class SimpleToken extends GenericTerminal {
	public String pos;
	public String lemma;

	public SimpleToken(ObjectSchema<? extends SimpleToken> schema) {
		super(schema);
	}

}
