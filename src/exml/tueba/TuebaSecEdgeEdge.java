// -*- mode: java -*-
// This is code generated by gen_code.py from the EXML schema

package exml.tueba;
import exml.objects.GenericObject;
import exml.objects.NamedObject;
import exml.annotations.MarkableSchema;

@MarkableSchema(TuebaSecEdgeSchema.class)
public class TuebaSecEdgeEdge extends GenericObject {
    public TuebaSecEdgeEdge() {
        super(TuebaSecEdgeSchema.instance.slotnames);
    }
    private String _cat;
    private TuebaNodeInterface _parent;
    public String getCat() {
        return _cat;
    }

    public void setCat(String val) {
        _cat = val;
    }
    public TuebaNodeInterface getParent() {
        return _parent;
    }

    public void setParent(TuebaNodeInterface val) {
        _parent = val;
    }
}