package exml.objects;

/** An implementation of NamedObject that is used both for
 * GenericTerminal and GenericMarkable
 */
public abstract class AbstractNamedObject extends GenericObject implements NamedObject {
    private String _xmlid;

    public AbstractNamedObject(ObjectSchema<? extends NamedObject> schema) {
    	super(schema.slotnames);
    }

    public String getXMLId()
    {
    	return _xmlid;
    }

    public void setXMLId(String id)
    {
    	_xmlid=id;
    }

    @Override
    public String toString() {
        if (getXMLId() != null) {
            return String.format("%s#%s", getClass().getSimpleName(), getXMLId());
        } else {
            return String.format("%s@%s", getClass().getSimpleName(),
                    System.identityHashCode(this));
        }
    }
}
