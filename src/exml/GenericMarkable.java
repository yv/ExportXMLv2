package exml;

import exml.objects.GenericNamedObject;
import exml.objects.GenericObjectSchema;


public class GenericMarkable extends GenericNamedObject implements IMarkable {
	private int _start=-1;
	private int _end=-1;
	private int[] _holes;

	GenericMarkable(GenericObjectSchema schema) {
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
	
	@Override
	public int getEnd() {
		return _end;
	}

	@Override
	public int[] getHoles() {
		return _holes;
	}

	@Override
	public int getStart() {
		return _start;
	}
}
