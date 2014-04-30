// -*- mode: java -*-
// This is code generated by gen_code.py from the EXML schema

package exml.tueba;
import java.util.List;

import exml.GenericMarkable;

public class TuebaEduMarkable extends GenericMarkable {
    public TuebaEduMarkable() {
        super(TuebaEduSchema.instance);
    }
    public List<TuebaDiscRelEdge> getDiscRel() {
      List<TuebaDiscRelEdge> lst = (List<TuebaDiscRelEdge>) getSlot(TuebaEduSchema.IDX_discRel);
      return lst;
    }
}