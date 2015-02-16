package exml;

import exml.objects.Attribute;
import exml.objects.IAccessor;
import gnu.trove.list.array.TIntArrayList;

public class SpanAccessor<T extends GenericMarkable> implements IAccessor<T,Span>{

	@SuppressWarnings("rawtypes")
	public static final SpanAccessor instance = new SpanAccessor();
	@SuppressWarnings("unchecked")
	public static final Attribute<GenericMarkable,Span> span_attribute = 
			new Attribute("span", instance, SpanConverter.instance);
	
	@Override
	public Span get(GenericMarkable m) {
		Span result=new Span();
		result.add(m.getStart());
		int[] holes=m.getHoles();
		if (holes!=null) {
			result.add(holes);
		}
		result.add(m.getEnd());
		return result;
	}

	@Override
	public void put(GenericMarkable m, Span parts) {
		m.setStart(parts.getStart());
		if (parts.size()>2) {
			m.setHoles(parts.getHoles());
		} else {
			m.setHoles(null);
		}
		m.setEnd(parts.getEnd());
	}
}
