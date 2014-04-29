// -*- mode: java -*-
// This is code generated by gen_code.py from the EXML schema

package exml.tueba;
import exml.objects.GenericObject;
import exml.objects.NamedObject;

public class TuebaRelationEdge extends GenericObject {
    public TuebaRelationEdge() {
        super(TuebaRelationSchema.instance.slotnames);
    }
    public String getType() {
        return (String) getSlot(TuebaRelationSchema.IDX_type);
    }

    public void setType(String val) {
        setSlot(TuebaRelationSchema.IDX_type, val);
    }
    public NamedObject getTarget() {
        return (NamedObject) getSlot(TuebaRelationSchema.IDX_target);
    }

    public void setTarget(NamedObject val) {
        setSlot(TuebaRelationSchema.IDX_target, val);
    }
}