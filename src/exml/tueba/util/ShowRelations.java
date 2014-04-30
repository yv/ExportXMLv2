package exml.tueba.util;

import java.io.FileNotFoundException;

import exml.GenericMarkable;
import exml.tueba.TuebaDiscRelEdge;
import exml.tueba.TuebaDocument;
import exml.tueba.TuebaEduMarkable;

public class ShowRelations {
	public static void main(String[] args) {
		try {
			TuebaDocument doc = TuebaDocument.loadDocument(args[0]);
			System.out.println(doc.edus.getMarkables());
			for (TuebaEduMarkable edu: doc.edus.getMarkables()) {
				for (TuebaDiscRelEdge edge: edu.getDiscRel()) {
					System.out.format("%s %s [%s] [%s]\n",
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