package exml.tabular;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import exml.MissingObjectException;
import exml.tueba.TuebaDocument;
import exml.tueba.TuebaSentenceMarkable;
import exml.tueba.TuebaTerminal;

public class ReadCoNLLX {
	public static class ChunkIterator implements Iterator<TuebaDocument>{
		int _sent_no=0;
		TuebaDocument _cur_doc=null;
		boolean _is_eof=false;
		// read data in chunks of 500k tokens
		int _max_tokens = 500000;
		BufferedReader _rd;
		boolean _use_predicted;

		public ChunkIterator(BufferedReader rd) {
			_rd=rd;
			_use_predicted=false;
		}
		
		protected void readDocument() throws IOException, MissingObjectException {
			TuebaDocument doc = new TuebaDocument();
			while (!_is_eof && doc.size() < _max_tokens) {
				++_sent_no;
				_is_eof = readSentence(_rd, doc, "s"+_sent_no, _use_predicted);
				if (_is_eof) return;
			}
		}

		@Override
		public boolean hasNext() {
			if (_cur_doc == null) {
				try {
					readDocument();
				} catch (IOException ex) {
					return false;
				} catch (MissingObjectException ex) {
					throw new RuntimeException("cannot read", ex);
				}
				return (!_is_eof);
			} else {
				return false;
			}
		}

		@Override
		public TuebaDocument next() {
			if (_cur_doc == null) {
				try {
					readDocument();
				} catch (IOException ex) {
					throw new RuntimeException("cannot read", ex);
				} catch (MissingObjectException ex) {
					throw new RuntimeException("cannot read", ex);
				}
			}
			TuebaDocument val = _cur_doc;
			_cur_doc = null;
			return val;
		}

		@Override
		public void remove() {
			// does nothing
		}
		
	}
	
	/**
	 * reads from the next non-empty line to the next empty line
	 * and adds the corresponding tokens to the document
	 * @param rd
	 * @param doc
	 * @throws IOException 
	 * @throws MissingObjectException 
	 */
	static boolean readSentence(BufferedReader rd, TuebaDocument doc,
			String prefix, boolean use_predicted) throws IOException, MissingObjectException {
		String line;
		HashMap<String,String> id_local2global=new HashMap<String,String>();
		HashMap<String,String> id_global2local=new HashMap<String,String>();
		List<TuebaTerminal> terminals=new ArrayList<TuebaTerminal>();
		List<String> heads = new ArrayList<String>();
		boolean is_eof = true;
		int sent_start = doc.size();
		while((line=rd.readLine())!=null) {
			String[] fields = line.trim().split("[ \t]+");
			if (fields.length<2) {
				// empty line
				if (doc.size() != sent_start) {
					is_eof = false;
					break;
				} else {
					// skip empty lines before the first token
					continue;
				}
			}
			TuebaTerminal n = doc.createTerminal(fields[1]);
			String global_id = prefix+"_w"+fields[0];
			n.setXMLId(global_id);
			doc.nameForObject(n);
			id_local2global.put(fields[0], global_id);
			id_global2local.put(global_id, fields[0]);
			n.setLemma(fields[2]);
			// 3 = CPOSTAG; what about it?
			n.setCat(fields[4]);
			n.setMorph(fields[5]);
			if (!use_predicted) {
				n.setSyn_label(fields[7]);
				heads.add(fields[6]);
			} else {
				n.setSyn_label(fields[9]);
				heads.add(fields[8]);
			}
			terminals.add(n);
		}
		for (int i=0; i<terminals.size(); i++) {
			String hd_s = heads.get(i);
			TuebaTerminal n = terminals.get(i);
			if ("0".equals(hd_s) || "_".equals(hd_s)) {
				n.setSyn_parent(null);
			} else {
				int idx = Integer.parseInt(hd_s)-1;
				n.setSyn_parent(terminals.get(idx));
			}
		}
		TuebaSentenceMarkable m_sent = doc.sentences.addMarkable(sent_start, doc.size());
		m_sent.setXMLId(prefix);
		doc.nameForObject(m_sent);
		return is_eof;
	}
	
	Iterable<TuebaDocument> readFile(final BufferedReader rd) {
		return new Iterable<TuebaDocument>() {

			@Override
			public Iterator<TuebaDocument> iterator() {
				return new ChunkIterator(rd);
			}
			
		};
		
	}
}
