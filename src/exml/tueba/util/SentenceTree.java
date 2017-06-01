package exml.tueba.util;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import exml.tueba.*;
import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Lists;

import exml.objects.NamedObject;

public class SentenceTree {
	private List<TuebaNodeInterface> _roots;
	private List<TuebaTerminal> _terminals;
	private TuebaDocument _doc;
	private TuebaSentenceMarkable _sent;

	public SentenceTree(List<TuebaNodeInterface> roots, List<TuebaTerminal> terminals,
			TuebaSentenceMarkable sent, TuebaDocument doc) {
		_roots = roots;
		_terminals = terminals;
		_sent = sent;
		_doc = doc;
	}
	
	public List<TuebaNodeInterface> getRoots() {
		return _roots;
	}
	
	public TuebaSentenceMarkable getSent() {
		return _sent;
	}
	
	public int getStart() {
		return _terminals.get(0).getStart();
	}
	
	public void setRoots(List<TuebaNodeInterface> _roots) {
		this._roots = _roots;
	}
	public List<TuebaTerminal> getTerminals() {
		return _terminals;
	}
	public void setTerminals(List<TuebaTerminal> _terminals) {
		this._terminals = _terminals;
	}
	
	class BottomUpIterator implements Iterator<TuebaNodeMarkable> {
		Stack<TuebaNodeMarkable> toVisit = new Stack<TuebaNodeMarkable>();
		Stack<Integer> posn = new Stack<Integer>();
		BottomUpIterator(List<TuebaNodeInterface> roots) {
			for (NamedObject m: Lists.reverse(roots)) {
				try {
					TuebaNodeMarkable node = (TuebaNodeMarkable)m;
					toVisit.push(node);
					posn.push(0);
				} catch(ClassCastException ignored) {}
			}
		}

		@Override
		public boolean hasNext() {
			return !toVisit.isEmpty();
		}

		@Override
		public TuebaNodeMarkable next() {
			TuebaNodeMarkable result = toVisit.pop();
			Integer pos = posn.pop();
			// advance over any terminals
			while (pos != result.getChildren().size()) {
				if (result.getChildren().get(pos) instanceof TuebaTerminal) {
					pos++;
				} else {
					toVisit.push(result);
					posn.push(pos + 1);
					result = (TuebaNodeMarkable)result.getChildren().get(pos);
					pos = 0;
				}
			}
			if (toVisit.size() != posn.size()) {
				System.err.println("toVisit:"+toVisit);
				System.err.println("posn:   "+posn);
			}
			return result;
		}

		@Override
		public void remove() {
			throw new IllegalArgumentException();
		}
		
	}
	
	class BottomUpIterable implements Iterable<TuebaNodeMarkable> {
		@Override
		public Iterator<TuebaNodeMarkable> iterator() {
			return new BottomUpIterator(_roots);
		}
		
	}
	
	public Iterable<TuebaNodeMarkable> ntEnumerateBottomUp() {
		return new BottomUpIterable();
	}

	public static List<SentenceTree> getTrees(TuebaDocument doc) {
		List<SentenceTree> result = new ArrayList<SentenceTree>();
		for (TuebaSentenceMarkable sent: doc.sentences.getMarkables()) {
			List<TuebaNodeInterface> roots = new ArrayList<TuebaNodeInterface>();
			List<TuebaTerminal> terminals = new ArrayList<TuebaTerminal>();
			//System.err.format("sentence %s [%d-%d]\n", sent.getXMLId(), sent.getStart(), sent.getEnd());
			for (int i = sent.getStart(); i < sent.getEnd(); i++) {
				TuebaTerminal node = doc.getTerminal(i);
				terminals.add(node);
				if (node.getParent() == null) {
					roots.add(node);
				}
			}
			for (TuebaNodeMarkable node: doc.nodes.getMarkablesInRange(sent.getStart(), sent.getEnd())) {
				if (node.getParent() == null) {
					roots.add(node);
				}
			}
			// TODO sort roots by start
			// TODO do we want to have a (local) node table?
			result.add(new SentenceTree(roots, terminals, sent, doc));
		}
		return result;
	}
	
	public void reassignParents() {
		reassignParents(_roots, null);
	}
	
	public void reassignParents(List<TuebaNodeInterface> nodes, TuebaNodeMarkable parent) {
		for (NamedObject node: nodes) {
			try {
				TuebaNodeMarkable n = (TuebaNodeMarkable)node;
				n.setParent(parent);
				reassignParents(n.getChildren(), n);
			} catch (ClassCastException ex) {
				TuebaTerminal n = (TuebaTerminal)node;
				n.setParent(parent);
			}
		}
	}
	
	/**
	 * given roots and their yields, reassigns the spans of the nodes
	 */
	public void reassignSpans() {
		reassignSpans(_roots, null);
	}
	
	private void reassignSpans(List<TuebaNodeInterface> nodes, TuebaNodeMarkable parent) {
		int start=Integer.MAX_VALUE;
		int end=Integer.MIN_VALUE;
		for (NamedObject node: nodes) {
			try {
				TuebaNodeMarkable n = (TuebaNodeMarkable)node;
				reassignSpans(n.getChildren(), n);
				if (n.getStart() < start) {
					start = n.getStart();
				}
				if (n.getEnd() > end) {
					end = n.getEnd();
				}
			} catch (ClassCastException ex) {
				TuebaTerminal n = (TuebaTerminal)node;
				n.setParent(parent);
				if (n.getStart() < start) {
					start = n.getStart();
				}
				if (n.getEnd() > end) {
					end = n.getEnd();
				}
			}
		}
		if (parent != null) {
			parent.setStart(start);
			parent.setEnd(end);
		}
	}

	/**
	 * given the roots of this SentenceTree, enters the nodes
	 * into the node markable and removes the others
	 */
	public void replaceNodes() {
		//TODO: remove old nodes
		insertNodes(_roots, 500);
	}
	
	protected int insertNodes(List<TuebaNodeInterface> nodes, int node_num) {
		// System.err.println("insert nodes"+ nodes);
		for (TuebaNodeInterface node: nodes) {
			try {
				TuebaNodeMarkable n = (TuebaNodeMarkable)node;
				_doc.nodes.addMarkable(n);
				node_num = insertNodes(n.getChildren(), node_num);
				if (n.getXMLId() == null) {
					n.setXMLId(String.format("%s_%d", _sent.getXMLId(), node_num));
					node_num += 1;
				}
			} catch (ClassCastException ex) {
				TuebaTerminal n = (TuebaTerminal)node;
				// nothing to do here
			}
		}
		return node_num;
	}
	
	public static void main(String[] args) {
		TuebaDocument doc;
		try {
			doc = TuebaDocument.loadDocument(args[0]);
			for (SentenceTree t : SentenceTree.getTrees(doc)) {
				System.out.println("sentence: "+t.getSent()+" start:"+t.getStart());
				System.out.println(t.getRoots());
				for (TuebaNodeMarkable n : t.ntEnumerateBottomUp()) {
					System.out.println(String.format("visited: (%s %s)",
							n.getCat(),
							StringUtils.join(n.getWords(doc), " ")));
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
