package exml.objects;

public class GenericAccessor<T>  implements IAccessor<GenericObject,T> {
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
	public void put(GenericObject o, T val) {
		o.setSlot(_offset, val);
	}
	
	
}
