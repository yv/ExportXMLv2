package exml.objects;

public class GenericNamedObject extends GenericObject implements INamedObject {
	private String _xmlid;
	
	public GenericNamedObject(GenericObjectSchema schema) {
		super(schema.slotnames);
	}
	
	@Override
	public String getXMLId() {
		return _xmlid;
	}

	@Override
	public void setXMLId(String id) {
		_xmlid=id;
	}

}
