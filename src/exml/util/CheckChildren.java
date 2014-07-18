package exml.util;

import java.util.List;

import exml.MarkableLevel;
import exml.objects.NamedObject;
import exml.tueba.TuebaDocument;
import exml.tueba.TuebaNodeMarkable;
import exml.tueba.TuebaTerminal;

/** This programs runs basic sanity checks on the syntax annotation
 * of an EXML document, in particular that the span of the parent
 * is a superset of the children's spans' union, and a subset of its
 * convex hull.
 * 
 * @author yannick
 */

public class CheckChildren {
	protected TuebaDocument doc;
	protected MarkableLevel<TuebaNodeMarkable> nodes;

	@SuppressWarnings("unchecked")
	public CheckChildren(TuebaDocument d) {
		doc=d;
		nodes=(MarkableLevel<TuebaNodeMarkable>) doc.markableLevelByName("node", false);
	}
	
	public void checkNode(TuebaNodeMarkable node) {
		List<NamedObject> children = node.getChildren();
		if (children.size() == 0) {
			System.out.format("Empty children list: %s %s\n",
					node.getXMLId(),
					node.getWords(doc));
		}
		boolean firstTerm=true;
		int maxPosn = node.getStart();
		for (NamedObject chld: node.getChildren()) {
			int chld_start = chld.getStart();
			int chld_end = chld.getEnd();
			String chld_id = chld.getXMLId();
			String chld_descr;
			try {
				TuebaTerminal tn=(TuebaTerminal)chld;
				checkTerminal(tn);
				chld_descr = String.format("[%s_%s]", tn.get_word(), tn.getCat());
			} catch (ClassCastException ex) {
				TuebaNodeMarkable nt = (TuebaNodeMarkable)chld;
				checkNode(nt);
				chld_descr = String.format("[%s %s]", nt.getCat(), nt.getWords(doc));
			}
			if (firstTerm) {
				if (chld_start != node.getStart()) {
					System.out.format("First child not at start of node: %s %s %s %s\n",
							node.getXMLId(), node.getWords(doc),
							chld_id, chld_descr);
				}
				firstTerm = false;
			} else {
				if (chld_start < node.getStart()) {
					System.out.format("Child before start of node: %s %s %s %s\n",
							node.getXMLId(), node.getWords(doc),
							chld_id, chld_descr);
				}
			}
			if (chld_end > node.getEnd()) {
				System.out.format("Child after end of node: %s %s %s %s\n",
						node.getXMLId(), node.getWords(doc),
						chld_id, chld_descr);
			}
		}
	}
	
	public void checkTerminal(TuebaTerminal tn) {
		//System.out.format("(%s %s)", tn.getCat(), tn.get_word());
	}

	public void printTrees() {
		for (TuebaNodeMarkable m: nodes.getMarkables()) {
			if (null == m.getParent()) {
				System.out.print(m.getXMLId()+": ");
				checkNode(m);
				System.out.println();
			}
		}
	}
	
	public static void main(String[] args) {
		for (String fname: args) {
			try {
				TuebaDocument doc=new TuebaDocument();
				doc.readDocument(fname);
				CheckChildren ex=new CheckChildren(doc);
				ex.printTrees();
			} catch (Exception ex) {
				ex.printStackTrace();
				System.exit(1);
			}
		}
	}
}
