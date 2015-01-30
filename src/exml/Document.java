package exml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;

import exml.objects.Attribute;
import exml.objects.BeanAccessors;
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
	
	public Document(ObjectSchema<T> schema)
	{
		_tschema=schema;
	}
	
	public Document(Class<T> cls, GenericObjectFactory<T> factory)
	{
		_tschema=new ObjectSchema<T>("word", cls, factory);
	}
	
	public Document(Class<T> cls) {
		_tschema = new ObjectSchema<T>("word", cls);
	}
	
	
	/**
	 * creates a new <code>Document</code> instance based on
	 * <code>GenericTerminalFactory</code>
	 * @return a new Document instance.
	 */
	public static Document<GenericTerminal> createDocument() {
		return new Document<GenericTerminal>(GenericTerminal.class, new GenericTerminalFactory());
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
		Attribute<T,?> att = _tschema.attrs.get(attributeName);
		if (attributeName.equals("word")) {
			return (String)_terminals.get(idx).getWord(); //special treatment necessary
		}
		return att.getString(_terminals.get(idx), this);
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
	@SuppressWarnings("unchecked")
	public ArrayList<String[]> getAttributes(String[] attributeNames, int idStart, int idEnd) {
		@SuppressWarnings("rawtypes")
		Attribute[] attrs = new Attribute[attributeNames.length];
		for (int i = 0; i<attributeNames.length; i++) {
			attrs[i] = _tschema.getAttribute(attributeNames[i]);
		}
		ArrayList<String[]> att = new ArrayList<String[]>();
		for (int i= idStart; i<= idEnd; i++) {
			String[] w = new String[attributeNames.length];
			T t = _terminals.get(i);
			for (int j=0; j<attributeNames.length; j++) {
				w[i] = attrs[i].getString(t, this);
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
	@SuppressWarnings("unchecked")
	public ObjectSchema<? extends GenericMarkable> markableSchemaByName(String name, boolean create) {
		ObjectSchema<? extends GenericMarkable> schema=_schemas_by_name.get(name);
		if (schema==null && create) {
			System.err.println("Warning: Creating markable schema "+name+" on "+this.getClass());
			schema=new ObjectSchema<GenericMarkable>(name,
					GenericMarkable.class,
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
	@SuppressWarnings({ "unchecked", "rawtypes" })
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
	
	public void addMarkableLevel(MarkableLevel<?> level, String levelname) {
		_levels_by_name.put(levelname, level);
		if (!_schemas_by_name.containsKey(level.schema.getName())) {
			_schemas_by_name.put(level.schema.getName(),
					level.schema);
		}
	}
	
	public <M extends GenericMarkable> MarkableLevel<M>
		markableLevelForClass(Class<M> cls, String name) {
		if (_levels_by_name.containsKey(name)) {
			ObjectSchema<? extends GenericMarkable> schema = _schemas_by_name.get(name);
			if (schema.isCompatibleWith(cls)) {
				return (MarkableLevel<M>) _levels_by_name.get(name);
			} else {
				throw new IllegalArgumentException(
						String.format("Cannot use class %s with schema %s",
								cls.toString(), schema.getName()));
			}
		} else {
			ObjectSchema<M> schema = BeanAccessors.getInstance().schemaForClass(cls);
			MarkableLevel<M> level = new MarkableLevel<M>(schema, this);
			_schemas_by_name.put(name, schema);
			_levels_by_name.put(name, level);
			return level;
		}
	}

	public Set<String> getMarkableLevelNames() {
		return _levels_by_name.keySet();
	}
	
	public Set<String> getEdgeNames() {
		return _edge_schemas_by_name.keySet();
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
					GenericObject.class,
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
	 * adds a schema for one particular kind of edge
	 * 
	 * @see exml.objects.ObjectSchema
	 * @param name the name of the edge definition
	 * @param schema the actual schema
	 */
	public void addEdgeSchema(String name, ObjectSchema<? extends GenericObject> schema)
	{
		_edge_schemas_by_name.put(name, schema);
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
		term.setWord(word);
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
	
	/** adds all markables to their parent's child list */
	public <P extends GenericObject> void addToChildList(
			String parentName,
			IAccessor<P,List<NamedObject>> chldAcc) {
		@SuppressWarnings("unchecked")
		IAccessor<T, P> parentAcc=
				(IAccessor<T, P>)_tschema.attrs.get(parentName).accessor;
		for (T m: _terminals) {
			P parent=parentAcc.get(m);
			if (parent==null) continue;
			List<NamedObject> chlds=chldAcc.get(parent);
			if (chlds == null) {
				chlds=new ArrayList<NamedObject>();
				chldAcc.put(parent,chlds);
			}
			chlds.add(m);
		}
	}
	
	/** sorts the child lists by position */
	public void sortChildList(IAccessor<T,List<NamedObject>> chldAcc) {
		for (T m: _terminals) {
			List<NamedObject> chlds=chldAcc.get(m);
			if (chlds == null) {
				chlds=new ArrayList<NamedObject>();
				chldAcc.put(m,chlds);
			}
			Collections.sort(chlds,NamedObject.byPosition);
		}
	}
}
