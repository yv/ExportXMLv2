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
	 * @param name the name of the slot as registered in the slots table
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
			System.arraycopy(_slots, 0, newslots, 0, _slots.length);
			_slots=newslots;
			_slots[idx]=val;
		}
	}

	/**
	 * sets the attribute
	 * @param name name of attribute
	 * @param val attribute value
	 */
	public void setSlotByName(String name, Object val) {
		setSlot(_slotnames.lookupIndex(name),val);
	}
}
