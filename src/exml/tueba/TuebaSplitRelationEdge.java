// -*- mode: java -*-
// This is code generated by gen_code.py from the EXML schema

package exml.tueba;
import exml.objects.GenericObject;
import exml.objects.NamedObject;

public class TuebaSplitRelationEdge extends GenericObject {
    public TuebaSplitRelationEdge() {
        super(TuebaSplitRelationSchema.instance.slotnames);
    }
    public String getType() {
        return (String) getSlot(TuebaSplitRelationSchema.IDX_type);
    }

    public void setType(String val) {
        setSlot(TuebaSplitRelationSchema.IDX_type, val);
    }
    public String getTarget() {
        return (String) getSlot(TuebaSplitRelationSchema.IDX_target);
    }

    public void setTarget(String val) {
        setSlot(TuebaSplitRelationSchema.IDX_target, val);
    }
}