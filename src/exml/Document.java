package exml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import exml.objects.Attribute;
import exml.objects.GenericObject;
import exml.objects.GenericObjectFactory;
import exml.objects.IAccessor;
import exml.objects.NamedObject;
import exml.objects.ObjectSchema;
import exml.objects.Relation;

/** represents one ExportXMLv2 corpus, including markable schemas for terminals
 * (normally instances of <code>GenericTerminal</code> or a subclass)
 * and markables (normally instances of <code>GenericMarkable</code>).
 * 
 * @see exml.GenericMarkable
 * @see exml.GenericTerminal
 * @see exml.objects.ObjectSchema
 * 
 * @author yannickv
 *
 * @param <T> the type of terminal objects in this document (normally <code>GenericTerminal</code>
 */
public class Document<T extends GenericTerminal> {
	public static class GenericMarkableFactory
		implements GenericObjectFactory<GenericMarkable> {
		@Override
		public GenericMarkable createObject(ObjectSchema<GenericMarkable> schema) {
			return new GenericMarkable(schema);
		}
	}

	public static class GenericTerminalFactory
	implements GenericObjectFactory<GenericTerminal> {
		@Override
		public GenericTerminal createObject(ObjectSchema<GenericTerminal> schema) {
			return new GenericTerminal(schema);
		}
	}
	
	
	private final ObjectSchema<T> _tschema;
	@SuppressWarnings("unchecked")
	private final HashMap<String,ObjectSchema<? extends GenericMarkable>> _schemas_by_name=
			new HashMap();
	@SuppressWarnings("unchecked")
	private final HashMap<String,ObjectSchema<? extends GenericObject>> _edge_schemas_by_name=
		new HashMap();
	@SuppressWarnings("unchecked")
	private final HashMap<String,MarkableLevel<? extends GenericMarkable>> _levels_by_name=
		new HashMap();
	public final HashMap<String,NamedObject> _obj_by_id=new HashMap<String,NamedObject>();
	
	private final List<T> _terminals=new ArrayList<T>();
	
	public Document(GenericObjectFactory<T> factory)
	{
		_tschema=new ObjectSchema<T>("word",factory);
	}
	
	
	/**
	 * creates a new <code>Document</code> instance based on
	 * <code>GenericTerminalFactory</code>
	 * @return a new Document instance.
	 */
	public static Document<GenericTerminal> createDocument() {
		return new Document<GenericTerminal>(new GenericTerminalFactory());
	}
	
	/**
	 * resolves a (string) reference to an object (markable or terminal)
	 * @param s the ID of the object
	 * @return the object with that ID
	 * @throws MissingObjectException
	 */
	public NamedObject resolveObject(String s) throws MissingObjectException {
		NamedObject val=_obj_by_id.get(s);
		if (val==null) {
			throw new MissingObjectException(s);
		}
		return val;
	}
	
	/**
	 * returns the terminal corresponding to the nth token of the document
	 * @param idx the token index
	 * @return the terminal
	 */
	public T getTerminal(int idx) {
		return _terminals.get(idx);
	}
	
	/**
	 * Returns a list of all terminals in the document. 
	 * Changes to this list change the document structure.
	 * @return list of terminals
	 */
	public List<T> getTerminals() {
		return Collections.unmodifiableList(_terminals);
	}
	
	/**
	 * Returns a list of all terminals in the document that have the target relation. 
	 * Changes to this list change the document structure.
	 * @return list of terminals
	 */
	public List<T> getTerminalsWithEdge(String relName) {
		ArrayList<T> terminalsWithRel = new ArrayList<T>();
		Relation<T,?> att = _tschema.rels.get(relName);
		IAccessor<T,?> accessor = att.accessor;
		for (T t: _terminals) {
			 if (accessor.get(t) != null) {
				 terminalsWithRel.add(t);
			 }
		}
		return terminalsWithRel;
	}	
	
	/**
	 * Returns the value of the input attribute of the nth token of the document.
	 * Can be null if the given token does not have the target attribute.
	 * @param attributeName name of the target attribute
	 * @param idx token id
	 * @return target attribute value of that token; null if token has no such attribute
	 */
	public String getAttribute(String attributeName, int idx) {
		if (attributeName.equals("word")) {
			return (String)_terminals.get(idx).get_word(); //special treatment necessary
		}
		return (String)_terminals.get(idx).getSlotByName(attributeName);
	}	
	
	/**
	 * Returns the values of the given attributes for each terminal in the given span. 
	 * @param attributeNames array of names of target attributes
	 * @param idStart token id of first token in the span
	 * @param idEnd token id of the last token in the span
	 * @return ArrayList of String arrays, one such array for each token in the span. 
	 * 			Each array contains the values of the target attributes for this token. 
	 * 			Can contain null values if a given token does not have the target attribute.
	 */
	public ArrayList<String[]> getAttributes(String[] attributeNames, int idStart, int idEnd) {
		ArrayList<String[]> att = new ArrayList<String[]>();
		for (int i= idStart; i<= idEnd; i++) {
			String[] w = new String[attributeNames.length];
			GenericTerminal t = _terminals.get(i);
			for (int j=0; j<attributeNames.length; j++) {
				if (attributeNames[j].equals("word")) {
					w[j] = (String)t.get_word();
				} else {
					w[j] = (String)t.getSlotByName(attributeNames[j]);
				}
			}
			att.add(w);
		}
		return att;
	}	
	
	
	public static final String nameChars="0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz_-";
	private static Random rnd=new Random();
	public static final String randomName(String prefix, int len) {
		   StringBuilder sb = new StringBuilder(prefix);
		   for( int i = 0; i < len; i++ ) 
		      sb.append( nameChars.charAt( rnd.nextInt(nameChars.length()) ) );
		   return sb.toString();
	}

	/**
	 * gives the ID of an object. Sets an ID if that object has none.
	 * @param o the object
	 * @return the ID of the object.
	 */
	public String nameForObject(NamedObject o) {
		String nm=o.getXMLId();
		if (nm==null) {
			do {
				nm=randomName("m", 6);
			} while (_obj_by_id.containsKey(nm));
			o.setXMLId(nm);
			_obj_by_id.put(nm, o);
		} else if (!_obj_by_id.containsKey(nm)) {
			_obj_by_id.put(nm,o);
		}
		return nm;
	}
	
	/**
	 * retrieves the markable schema with a given name
	 * 
	 * @see exml.objects.ObjectSchema
	 * @param name the name of the markable level
	 * @param create indicates that the markable level should be created if it does not exist yet
	 * @return the markable level
	 */
	public ObjectSchema<? extends GenericMarkable> markableSchemaByName(String name, boolean create) {
		ObjectSchema<? extends GenericMarkable> schema=_schemas_by_name.get(name);
		if (schema==null && create) {
			schema=new ObjectSchema<GenericMarkable>(name,
					new GenericObjectFactory<GenericMarkable>() {
						public GenericMarkable createObject(ObjectSchema<GenericMarkable> sc) {
							return new GenericMarkable(sc);
						}
			});
			schema.addAttribute("span", SpanConverter.instance, SpanAccessor.instance);
			_schemas_by_name.put(name, schema);
		}
		return schema;
	}
	
	/**
	 * retrieves the markable level with a given name
	 * 
	 * @see MarkableLevel
	 * @param name the name of the markable level
	 * @param create indicates that the markable level should be created if it does not exist yet
	 * @return the markable level
	 */
	@SuppressWarnings("unchecked")
	public MarkableLevel<? extends GenericMarkable> markableLevelByName(String name, boolean create) {
		if (_levels_by_name.containsKey(name)) {
			return _levels_by_name.get(name);
		}
		ObjectSchema<? extends GenericMarkable> schema=markableSchemaByName(name,create);
		if (schema==null) return null;
		MarkableLevel<? extends GenericMarkable> result =
			new MarkableLevel(schema, this);
		_levels_by_name.put(name, result);
		return result;
	}

	/**
	 * retrieves the schema for a given kind of edges
	 * 
	 * @see exml.objects.ObjectSchema
	 * @param name the name of the edge definition
	 * @param create indicates that the markable level should be created if it does not exist yet
	 * @return the markable level
	 */
	public ObjectSchema<? extends GenericObject> edgeSchemaByName(String name, boolean create) {
		ObjectSchema<? extends GenericObject> schema=_edge_schemas_by_name.get(name);
		if (schema==null && create) {
			schema=new ObjectSchema<GenericObject>(name,
					new GenericObjectFactory<GenericObject>() {
						public GenericObject createObject(ObjectSchema<GenericObject> sc) {
							return new GenericObject(sc.slotnames);
						}
			});
			_edge_schemas_by_name.put(name, schema);
		}
		return schema;
	}
	
	/**
	 * returns the definition of the terminal level
	 * @return the schema object
	 */
	public ObjectSchema<T> terminalSchema() {
		return _tschema;
	}
	
	/**
	 * adds a new termina
	 * 	 * @param word the token to be added
	 * @return the terminal object
	 */
	public T createTerminal(String word) {
		T term=_tschema.createMarkable();
		term.set_corpus_pos(_terminals.size());
		_terminals.add(term);
		return term;
	}
	
	/**
	 * creates a new markable on the given markable level 
	 * @param level the name of the level
	 * @return the markable
	 */
	public GenericMarkable createMarkable(String level) {
		ObjectSchema<? extends GenericMarkable> ms=_schemas_by_name.get(level);
		return ms.createMarkable();
	}

	/**
	 * the current document size, in tokens
	 * @return size
	 */
	public int size() {
		return _terminals.size();
	}

	/**
	 * given the ID of a terminal, returns its position in the corpus
	 * @param string the terminal node's ID
	 * @return the position
	 * @throws MissingObjectException
	 */
	public int getPosition(String string) throws MissingObjectException {
		T terminal=(T)resolveObject(string);
		return terminal.get_corpus_pos();
	}
}
