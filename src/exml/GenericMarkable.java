package exml;

import java.util.Comparator;

import exml.objects.NamedObject;
import exml.objects.ObjectSchema;

/**
 * represents a markable (annotated span) that only
 * has generic properties.
 * @author yannickv
 *
 */
public class GenericMarkable extends NamedObject {
	private int _start=-1;
	private int _end=-1;
	private int[] _holes;

	GenericMarkable(ObjectSchema<? extends GenericMarkable> schema) {
		super(schema);
	}
	
	public void setStart(int start) {
		_start=start;
	}
	
	public void setEnd(int end) {
		_end=end;
	}
	
	public void setHoles(int[] holes) {
		_holes=holes;
	}
	
	public int getEnd() {
		return _end;
	}

	public int[] getHoles() {
		return _holes;
	}

	public int getStart() {
		return _start;
	}
	
	/**
	 * orders markables by start, then by length, and then using their object ID
	 * (so that different markables with the same span still compare as non-equal).
	 * The order of multiple markables with the same span is undefined.
	 * @author yannickv
	 *
	 */
	public static class DiscourseOrder implements Comparator<GenericMarkable> {
		@Override
		public int compare(GenericMarkable o1, GenericMarkable o2) {
			if (o1._start<o2._start) {
				return -1;
			} else if (o1._start>o2._start) {
				return +1;
			} else if (o1._end<o2._end) {
				return -1;
			} else if (o1._end>o2._end) {
				return +1;
			} else {
				int id1=System.identityHashCode(o1);
				int id2=System.identityHashCode(o2);
				return id1-id2;
			}
		}
		public static final DiscourseOrder instance=new DiscourseOrder();
	}
}
