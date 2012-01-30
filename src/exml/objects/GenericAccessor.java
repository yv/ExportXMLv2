package exml.objects;

public class GenericAccessor<Obj extends GenericObject,T>  implements IAccessor<Obj,T> {
	private int _offset;
	
	public GenericAccessor(int idx) {
		_offset=idx;
	}

	@Override
	@SuppressWarnings("unchecked")
	public T get(GenericObject obj) {
		return (T)obj.getSlot(_offset);
	}

	@Override
	public void put(Obj o, T val) {
		o.setSlot(_offset, val);
	}
	
	
}
