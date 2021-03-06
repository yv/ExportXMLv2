package exml.util;

import java.io.FileInputStream;
import java.util.List;

import exml.Document;
import exml.GenericMarkable;
import exml.GenericTerminal;
import exml.MarkableLevel;
import exml.io.DocumentReader;
import exml.objects.IAccessor;
import exml.objects.NamedObject;

/** This is an example for the <i>generic</i> API
 * of ExportXMLv2. It does things in a form that is less dependent
 * on the actual annotation scheme of the corpus
 * 
 * @author yannick
 */

public class ChildrenExample {
	protected Document<GenericTerminal> doc;
	protected MarkableLevel<GenericMarkable> nodes;
	public final IAccessor<GenericMarkable, List<NamedObject>> node_children;
	public final IAccessor<GenericMarkable, String> node_cat;
	public final IAccessor<GenericTerminal, String> word_pos;
	public final IAccessor<GenericTerminal, String> word_form;

	@SuppressWarnings("unchecked")
	public ChildrenExample(Document<GenericTerminal> d) {
		doc=d;
		nodes=(MarkableLevel<GenericMarkable>) doc.markableLevelByName("node", false);
		node_children=nodes.schema.<List<NamedObject>>genericAccessor("children");
		nodes.<GenericMarkable>addToChildList("parent", node_children);
		doc.addToChildList("parent", node_children);
		nodes.sortChildList(node_children);
		
		node_cat=nodes.schema.<String>genericAccessor("cat");
		word_pos=doc.terminalSchema().<String>genericAccessor("pos");
		word_form=doc.terminalSchema().<String>genericAccessor("form");
	}
	
	public void printNode(GenericMarkable node) {
		System.out.format("(%s ",node_cat.get(node));
		for (NamedObject chld: node_children.get(node)) {
			try {
				GenericTerminal tn=(GenericTerminal)chld;
				printTerminal(tn);
			} catch (ClassCastException ex) {
				printNode((GenericMarkable)chld);
			}
		}
		System.out.print(")");
	}
	
	public void printTerminal(GenericTerminal tn) {
		System.out.format("(%s %s)", word_pos.get(tn), word_form.get(tn));
	}

	public void printTrees() {
		@SuppressWarnings("unchecked")
		IAccessor<GenericMarkable,GenericMarkable> parentAcc=
				(IAccessor<GenericMarkable, GenericMarkable>) nodes.schema.attrs.get("parent").accessor;
		for (GenericMarkable m: nodes.getMarkables()) {
			if (parentAcc.get(m)==null) {
				System.out.print(m.getXMLId()+": ");
				printNode(m);
				System.out.println();
			}
		}
	}
	
	public static void main(String[] args) {
		for (String fname: args) {
			try {
				Document<GenericTerminal> doc=Document.createDocument();
				DocumentReader.readDocument(doc, new FileInputStream(fname));
				ChildrenExample ex=new ChildrenExample(doc);
				ex.printTrees();
			} catch (Exception ex) {
				ex.printStackTrace();
				System.exit(1);
			}
		}
	}
}
