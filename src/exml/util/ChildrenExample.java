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

public class ChildrenExample {
	protected Document<GenericTerminal> doc;
	protected MarkableLevel<GenericMarkable> nodes;
	protected IAccessor<GenericMarkable, List<NamedObject>> chldAcc;
	protected IAccessor<GenericMarkable, String> node_cat;

	@SuppressWarnings("unchecked")
	public ChildrenExample(Document<GenericTerminal> d) {
		doc=d;
		nodes=(MarkableLevel<GenericMarkable>) doc.markableLevelByName("node", false);
		chldAcc=nodes.schema.<List<NamedObject>>genericAccessor("children");
		nodes.<GenericMarkable>addToChildList("parent", chldAcc);
		doc.addToChildList("parent", chldAcc);
		nodes.sortChildList(chldAcc);
	}
	
	public void printNode(GenericMarkable node) {
		System.out.format("(%s ",node.getSlotByName("cat"));
		for (NamedObject chld: chldAcc.get(node)) {
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
		System.out.format("(%s %s)",
				tn.getSlotByName("pos"),
				//tn.getXMLId(),
				tn.getSlotByName("form"));
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