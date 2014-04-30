package exml.tueba.util;

import java.io.FileNotFoundException;
import java.util.List;

import exml.GenericMarkable;
import exml.tueba.TuebaDiscRelEdge;
import exml.tueba.TuebaDocument;
import exml.tueba.TuebaEduMarkable;

/**
 * Beispiel-Code: Extraktion von expliziten Diskursrelationen
 * @author Yannick Versley
 *
 */
public class ShowRelations {
	public static void main(String[] args) {
		try {
			TuebaDocument doc = TuebaDocument.loadDocument(args[0]);
			for (TuebaEduMarkable edu: doc.edus.getMarkables()) {
				List<TuebaDiscRelEdge> edges = edu.getDiscRel();
				if (null == edges) continue;
				for (TuebaDiscRelEdge edge: edges) {
					if ("-".equals(edge.getMarking())) continue;
					System.out.format("%s ||| %s ||| %s ||| %s\n",
						edge.getMarking(), edge.getRelation(),
						edu.getWords(doc),
						((GenericMarkable)edge.getArg2()).getWords(doc));
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}