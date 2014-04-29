package exml;

import exml.objects.IAccessor;
import gnu.trove.list.array.TIntArrayList;

public class SpanAccessor<T extends GenericMarkable> implements IAccessor<T,TIntArrayList>{

	public static final SpanAccessor instance = new SpanAccessor();
	
	@Override
	public TIntArrayList get(GenericMarkable m) {
		TIntArrayList result=new TIntArrayList();
		result.add(m.getStart());
		int[] holes=m.getHoles();
		if (holes!=null) {
			result.add(holes);
		}
		result.add(m.getEnd());
		return result;
	}

	@Override
	public void put(GenericMarkable m, TIntArrayList parts) {
		m.setStart(parts.get(0));
		if (parts.size()>2) {
			m.setHoles(parts.subList(1, parts.size()-2).toArray());
		} else {
			m.setHoles(null);
		}
		m.setEnd(parts.get(parts.size()-1));
	}
}
