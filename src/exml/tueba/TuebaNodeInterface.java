// -*- mode: java -*-
// This is code generated by gen_code.py from the EXML schema

package exml.tueba;
import exml.objects.NamedObject;

import java.util.List;

public interface TuebaNodeInterface extends NamedObject {
    TuebaNodeMarkable getParent();
    void setParent(TuebaNodeMarkable val);
    String getEdge_label();
    void setEdge_label(String val);
    List<TuebaSecEdgeEdge> getSecEdge();
    List<TuebaRelationEdge> getRelation();
}