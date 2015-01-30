// -*- mode: java -*-
// This is code generated by gen_code.py from the EXML schema

package exml.tueba;
import exml.objects.GenericObject;
import exml.objects.NamedObject;
import exml.annotations.MarkableSchema;

@MarkableSchema(TuebaRelationSchema.class)
public class TuebaRelationEdge extends GenericObject {
    public TuebaRelationEdge() {
        super(TuebaRelationSchema.instance.slotnames);
    }
    private String _type;
    private TuebaNodeInterface _target;
    public String getType() {
        return _type;
    }

    public void setType(String val) {
        _type = val;
    }
    public TuebaNodeInterface getTarget() {
        return _target;
    }

    public void setTarget(TuebaNodeInterface val) {
        _target = val;
    }
}