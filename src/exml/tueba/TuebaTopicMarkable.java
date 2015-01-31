// -*- mode: java -*-
// This is code generated by gen_code.py from the EXML schema

package exml.tueba;
import exml.GenericMarkable;
import exml.annotations.EXMLAttribute;
import exml.annotations.MarkableSchema;
import java.util.List;

@MarkableSchema(TuebaTopicSchema.class)
public class TuebaTopicMarkable extends GenericMarkable
{
    public TuebaTopicMarkable() {
        super(TuebaTopicSchema.instance);
    }
    private String _description;
    @EXMLAttribute("description")
    public String getDescription() {
        return _description;
    }

    public void setDescription(String val) {
       _description = val;
    }
    public List<TuebaDiscRelEdge> getDiscRel() {
      List<TuebaDiscRelEdge> lst = (List<TuebaDiscRelEdge>) getSlot(TuebaTopicSchema.IDX_discRel);
      return lst;
    }
}