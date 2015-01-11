package exml.objects;

import exml.Document;
import exml.MissingObjectException;

public interface IAttribute<Obj> {
	/**
	 * changes the attribute based on a string value
	 * @param m the markable/terminal to be changed
	 * @param s a string describing the new value
	 * @param doc the document (to resolve references)
	 * @throws MissingObjectException when the string points to an invalid reference
	 */
	public void putString(Obj m, String s, Document<?> doc) throws MissingObjectException;
	
	/**
	 * returns a string reflecting the current value of the attribute
	 * @param m the markable/terminal
	 * @param doc the document (to resolve references/describe spans)
	 * @return
	 */
	public String getString(Obj m, Document<?> doc);
}
