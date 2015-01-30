// -*- mode: java -*-
// This is code generated by gen_code.py from the EXML schema

package exml.tueba;
import java.util.List;
import exml.GenericTerminal;
import exml.objects.NamedObject;

public class TuebaTerminal extends GenericTerminal
  implements TuebaNodeInterface
{
    public TuebaTerminal() {
        super(TuebaTerminalSchema.instance);
    }
    private String _word;
    private String _cat;
    private String _morph;
    private String _lemma;
    private String _edge_label;
    private TuebaNodeMarkable _parent;
    private String _wsd_lexunits;
    private String _wsd_comment;
    private TuebaTerminal _syn_parent;
    private String _syn_label;
    private String _comment;
    
    public String getWord() {
        return _word;
    }

    public void setWord(String val) {
        _word = val;
    }
    
    public String getCat() {
        return _cat;
    }

    public void setCat(String val) {
        _cat = val;
    }
    
    public String getMorph() {
        return _morph;
    }

    public void setMorph(String val) {
        _morph = val;
    }
    
    public String getLemma() {
        return _lemma;
    }

    public void setLemma(String val) {
        _lemma = val;
    }
    
    public String getEdge_label() {
        return _edge_label;
    }

    public void setEdge_label(String val) {
        _edge_label = val;
    }
    
    public TuebaNodeMarkable getParent() {
        return _parent;
    }

    public void setParent(TuebaNodeMarkable val) {
        _parent = val;
    }
    
    public String getWsd_lexunits() {
        return _wsd_lexunits;
    }

    public void setWsd_lexunits(String val) {
        _wsd_lexunits = val;
    }
    
    public String getWsd_comment() {
        return _wsd_comment;
    }

    public void setWsd_comment(String val) {
        _wsd_comment = val;
    }
    
    public TuebaTerminal getSyn_parent() {
        return _syn_parent;
    }

    public void setSyn_parent(TuebaTerminal val) {
        _syn_parent = val;
    }
    
    public String getSyn_label() {
        return _syn_label;
    }

    public void setSyn_label(String val) {
        _syn_label = val;
    }
    
    public String getComment() {
        return _comment;
    }

    public void setComment(String val) {
        _comment = val;
    }
    
    public List<TuebaSecEdgeEdge> getSecEdge() {
      List<TuebaSecEdgeEdge> lst = (List<TuebaSecEdgeEdge>) getSlot(TuebaTerminalSchema.IDX_secEdge);
      return lst;
    }
    public List<TuebaRelationEdge> getRelation() {
      List<TuebaRelationEdge> lst = (List<TuebaRelationEdge>) getSlot(TuebaTerminalSchema.IDX_relation);
      return lst;
    }
    public List<TuebaSplitRelationEdge> getSplitRelation() {
      List<TuebaSplitRelationEdge> lst = (List<TuebaSplitRelationEdge>) getSlot(TuebaTerminalSchema.IDX_splitRelation);
      return lst;
    }
    public List<TuebaConnectiveEdge> getConnective() {
      List<TuebaConnectiveEdge> lst = (List<TuebaConnectiveEdge>) getSlot(TuebaTerminalSchema.IDX_connective);
      return lst;
    }
}