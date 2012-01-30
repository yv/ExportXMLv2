package exml.objects;

public class NamedObject extends GenericObject {
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

}
