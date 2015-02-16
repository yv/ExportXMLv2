package exml.tabular;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;

import exml.GenericMarkable;
import exml.tueba.TuebaDocument;
import exml.tueba.TuebaTerminal;

public class WriteCoNLLX {
	public static void writeFile(TuebaDocument doc, BufferedWriter w) throws IOException {
		int posn=0;
		for (GenericMarkable m: doc.sentences.getMarkables()) {
			HashMap<String,String> id_local2global=new HashMap<String,String>();
			HashMap<String,String> id_global2local=new HashMap<String,String>();
			int offset=0;
			for (;posn<m.getEnd();posn++) {
				offset++; String local_id=""+offset;
				TuebaTerminal n = doc.getTerminal(posn);
				id_local2global.put(local_id, n.getXMLId());
				id_global2local.put(n.getXMLId(), local_id);
			}
			offset=0;
			for (;posn<m.getEnd();posn++) {
				offset++; String local_id=""+offset;
				TuebaTerminal n = doc.getTerminal(posn);
				String[] row = new String[9];
				row[0] = local_id;
				row[1] = n.getWord();
				row[2] = n.getLemma();
				//TODO: use some kind of mapping
				row[3] = n.getCat().substring(0, 1);
				row[4] = n.getCat();
				row[5] = n.getMorph();
				TuebaTerminal n_gov = n.getSyn_parent();
				if (n_gov == null) {
					row[6] = "0";
				} else {
					row[6] = id_global2local.get(n_gov.getXMLId());
				}
				String deprel = n.getSyn_label();
				if (deprel == null) {
					row[7] = "_";
				} else {
					row[7] = deprel;
				}
				row[8] = row[6];
				row[9] = row[7];
				w.write(StringUtils.join(row,"\t"));
			}
		}
	}
}
