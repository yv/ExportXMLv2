// -*- mode: java -*-
// This is code generated by gen_code.py from the EXML schema

package exml.tueba;
import exml.GenericMarkable;

public class TuebaTopicMarkable extends GenericMarkable {
    public TuebaTopicMarkable() {
        super(TuebaTopicSchema.instance);
    }
    public String getDescription() {
        return (String) getSlot(TuebaTopicSchema.IDX_description);
    }

    public void setDescription(String val) {
        setSlot(TuebaTopicSchema.IDX_description, val);
    }
}