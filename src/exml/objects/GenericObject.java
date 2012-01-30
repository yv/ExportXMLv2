package exml.objects;

import elkfed.ml.util.Alphabet;

/** implements a generic object that can have attributes
 *  and edges.
 * @author yannickv
 */

public class GenericObject {
	final Alphabet<String> _slotnames;
	private Object[] _slots;
	
	public GenericObject(Alphabet<String> slotnames) {
		_slotnames=slotnames;
		_slots=new Object[slotnames.size()];
	}
	
	public Object getSlot(int idx) {
		try {
			return _slots[idx];
		} catch(ArrayIndexOutOfBoundsException e) {
			return null;
		}
	}
	
	/**
	 * read an attribute
	 * @param name
	 * @return the attribute value
	 */
	public Object getSlotByName(String name) {
		return getSlot(_slotnames.lookupIndex(name));
	}
	
	public void setSlot(int idx, Object val) {
		try {
			_slots[idx]=val;
		} catch (ArrayIndexOutOfBoundsException e) {
			Object[] newslots=new Object[idx+1];
			for (int i=0;i<_slots.length;i++) {
				newslots[i]=_slots[i];
			}
		}
	}

	/**
	 * sets attribute
	 * @param name name of attribute
	 * @param val attribute value
	 */
	public void setSlotByName(String name, Object val) {
		setSlot(_slotnames.lookupIndex(name),val);
	}
}
