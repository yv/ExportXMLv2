package exml.util;

import exml.MarkableLevel;
import exml.objects.NamedObject;
import exml.tueba.TuebaDocument;
import exml.tueba.TuebaNodeMarkable;
import exml.tueba.TuebaTerminal;

/** This is an example for the <i>tueba-specific</i> API
 * of ExportXMLv2. It does things in a form that is more concrete
 * by using the automatically generated classes of the annotation
 * scheme
 * 
 * @author yannick
 */

public class ChildrenExampleTueba {
	protected TuebaDocument doc;
	protected MarkableLevel<TuebaNodeMarkable> nodes;

	@SuppressWarnings("unchecked")
	public ChildrenExampleTueba(TuebaDocument d) {
		doc=d;
		nodes=(MarkableLevel<TuebaNodeMarkable>) doc.markableLevelByName("node", false);
	}
	
	public void printNode(TuebaNodeMarkable node) {
		System.out.format("(%s ",node.getCat());
		for (NamedObject chld: node.getChildren()) {
			try {
				TuebaTerminal tn=(TuebaTerminal)chld;
				printTerminal(tn);
			} catch (ClassCastException ex) {
				printNode((TuebaNodeMarkable)chld);
			}
		}
		System.out.print(")");
	}
	
	public void printTerminal(TuebaTerminal tn) {
		System.out.format("(%s %s)", tn.getCat(), tn.get_word());
	}

	public void printTrees() {
		for (TuebaNodeMarkable m: nodes.getMarkables()) {
			if (null == m.getParent()) {
				System.out.print(m.getXMLId()+": ");
				printNode(m);
				System.out.println();
			}
		}
	}
	
	public static void main(String[] args) {
		for (String fname: args) {
			try {
				TuebaDocument doc=new TuebaDocument();
				doc.readDocument(fname);
				ChildrenExampleTueba ex=new ChildrenExampleTueba(doc);
				ex.printTrees();
			} catch (Exception ex) {
				ex.printStackTrace();
				System.exit(1);
			}
		}
	}
}
