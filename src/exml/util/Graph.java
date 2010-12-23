package exml.util;

import java.util.List;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Graph {
	private final Map<String,Set<String>> edges=new HashMap<String,Set<String>>();
	public void addEdge(String from, String to) {
		Set<String> out=edges.get(from);
		if (out==null) {
			out=new HashSet<String>();
			edges.put(from, out);
		}
	}
	/** returns the edges, topologically sorted */
	List<String> getOrder() {
		return null;
	}
}
