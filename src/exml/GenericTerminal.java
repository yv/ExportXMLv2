package exml;

import exml.objects.NamedObject;
import exml.objects.ObjectSchema;

/**
 * represents a terminal node (annotated token) that only
 * has generic properties.
 * @author yannickv
 *
 */

public class GenericTerminal extends NamedObject {
	private int _corpus_pos;
	private String _word;
	public GenericTerminal(ObjectSchema<? extends GenericTerminal> schema) {
		super(schema);
	}
	public void set_corpus_pos(int _corpus_pos) {
		this._corpus_pos = _corpus_pos;
	}
	public int get_corpus_pos() {
		return _corpus_pos;
	}
	public void set_word(String _word) {
		this._word = _word;
	}
	public String get_word() {
		return _word;
	}

}
