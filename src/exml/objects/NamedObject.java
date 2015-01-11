package exml.objects;

import java.util.Comparator;

public abstract class NamedObject extends GenericObject {
	private String _xmlid;
	
	public NamedObject(ObjectSchema<? extends NamedObject> schema) {
		super(schema.slotnames);
	}
	
	public String getXMLId() {
		return _xmlid;
	}

	public void setXMLId(String id) {
		_xmlid=id;
	}
	
	public abstract int getStart();
	public abstract int getEnd();
	
	public static class ByPosition implements Comparator<NamedObject> {
		@Override
		public int compare(NamedObject o1, NamedObject o2) {
			int s1=o1.getStart();
			int s2=o2.getStart();
			if (s1!=s2) {
				return s1-s2;
			} else {
				int e1=o1.getEnd();
				int e2=o2.getEnd();
				if (e1!=e2) {
					return e2-e1;
				}
				return 0;
			}
		}
	}
	public static final Comparator<NamedObject> byPosition=new ByPosition();
	public String toString() {
		if (_xmlid != null) {
			return String.format("%s#%s", this.getClass().toString(), _xmlid);
		} else {
			return super.toString();
		}
	}
}
