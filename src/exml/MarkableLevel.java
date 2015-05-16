package exml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import exml.objects.Attribute;
import exml.objects.GenericObject;
import exml.objects.IAccessor;
import exml.objects.NamedObject;
import exml.objects.ObjectSchema;

/**
 * stores the markables corresponding to one particular level
 * @author yannickv
 *
 * @param <T> the type of the markables
 */
public class MarkableLevel<T extends GenericMarkable> {
	public final ObjectSchema<T> schema;
	private final SortedSet<T> _markables =
        new TreeSet<T>(GenericMarkable.DiscourseOrder.instance);
	public final Document<?> doc;
	private int max_len=0;
	
	public MarkableLevel(ObjectSchema<T> sc, Document<?> d) {
		schema=sc;
		doc=d;
	}
	
	/**
	 * adds a new markable with a contiguous span from <code>start</code> to <code>end</code>
	 * @param attributes the attributes that the markable should have
	 * @return the new markable
	 * @throws MissingObjectException
	 */
	public T addMarkable(int start, int end, Map<String, String> attributes) throws MissingObjectException {
		T val=schema.createMarkable();
		val.setStart(start);
		val.setEnd(end);
		if (attributes!=null) {
			for (Map.Entry<String, String> entry: attributes.entrySet()) {
				Attribute<T, ?> att=schema.attrs.get(entry.getKey());
				att.putString(val, entry.getValue(), doc);
			}
		}
		addMarkable(val);
		return val;
	}
	
	/**
	 * adds a new markable with a contiguous span from <code>start</code> to <code>end</code>
	 * @param attributes the attributes that the markable should have
	 * @return the new markable
	 * @throws MissingObjectException
	 */
	public T addMarkable(int start, int end) throws MissingObjectException {
		T val=schema.createMarkable();
		val.setStart(start);
		val.setEnd(end);
		addMarkable(val);
		return val;
	}

	/**
	 * adds an existing markable object to this markable level
	 * @param val the markable
	 */
	public void addMarkable(T val) {
		int m_len=val.getEnd()-val.getStart();
		if (m_len>max_len) {
			max_len=m_len;
		}
		_markables.add(val);
	}
	
	/**
	 * retrieves all the markables
	 * @return all markables
	 */
	public Collection<T> getMarkables() {
		return _markables;
	}
	
	/**
	 * returns all the markables that begin between <code>start</code>(inclusive)
	 * and <code>end</code>(exclusive)
	 * @param start minimum start position
	 * @param end past end position
	 * @return
	 */
	public List<T> getMarkablesInRange(int start, int end)
	{
		List<T> result=new ArrayList<T>();
		T dummyMarkable=schema.createMarkable();
		dummyMarkable.setStart(start);
		dummyMarkable.setEnd(Integer.MAX_VALUE);
		SortedSet<T> part=_markables.tailSet(dummyMarkable);
		for (T m: part) {
			if (m.getStart()>=end) {
				break;
			}
			result.add(m);
		}
		return result;
	}
	
	public List<T> getOverlappingMarkables(int start, int end)
	{
		List<T> result=new ArrayList<T>();
		for (T m: getMarkablesInRange(start-max_len, end)) {
			if (m.getStart()<end && m.getEnd()>start) {
				result.add(m);
			}
		}
		return result;
	}
	
	/** adds all markables to their parent's child list */
	public <P extends GenericObject> void addToChildList(
			String parentName,
			IAccessor<P,List<NamedObject>> chldAcc) {
		@SuppressWarnings("unchecked")
		IAccessor<T, P> parentAcc=
				(IAccessor<T, P>)schema.attrs.get(parentName).accessor;
		for (T m: _markables) {
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
		//System.err.println("sortChildList on "+_markables.size()+" markables");
		for (T m: _markables) {
			List<NamedObject> chlds=chldAcc.get(m);
			if (chlds == null) {
				//System.err.println("empty child list on "+m.getXMLId());
				chlds=new ArrayList<NamedObject>();
				chldAcc.put(m,chlds);
			}
			Collections.sort(chlds,NamedObject.byPosition);
		}
	}
}
