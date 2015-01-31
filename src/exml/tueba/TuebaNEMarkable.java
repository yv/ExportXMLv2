// -*- mode: java -*-
// This is code generated by gen_code.py from the EXML schema

package exml.tueba;
import exml.GenericMarkable;
import exml.annotations.EXMLAttribute;
import exml.annotations.MarkableSchema;
import java.util.List;

@MarkableSchema(TuebaNESchema.class)
public class TuebaNEMarkable extends GenericMarkable
{
    public TuebaNEMarkable() {
        super(TuebaNESchema.instance);
    }
    private String _kind;
    @EXMLAttribute("type")
    public String getKind() {
        return _kind;
    }

    public void setKind(String val) {
       _kind = val;
    }
}