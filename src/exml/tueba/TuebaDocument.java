// -*- mode: java -*-
// This is code generated by gen_code.py from the EXML schema

package exml.tueba;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import exml.Document;
import exml.MarkableLevel;
import exml.io.DocumentReader;
import exml.objects.IAccessor;
import exml.objects.NamedObject;

/**
 * TueBa-D/Z-specific Document class, will only work with TueBa-like documents.
 * (see TueBa export format) Adds a list of children to the general Document
 * structure for easy access. Allows fast access to various
 * attributes.
 * 
 * @author Anne Brock, Yannick Versley
 */
public class TuebaDocument extends Document<TuebaTerminal> {
    
    public final MarkableLevel<TuebaSentenceMarkable> sentences;
    public final MarkableLevel<TuebaNodeMarkable> nodes;
	public final IAccessor<TuebaNodeMarkable,String> node_cat;
	public final IAccessor<TuebaNodeMarkable,String> node_edge_label;
	public final IAccessor<TuebaNodeMarkable,TuebaNodeMarkable> node_parent;
	public final IAccessor<TuebaNodeMarkable,String> node_comment;
    public final MarkableLevel<TuebaTextMarkable> texts;
	public final IAccessor<TuebaTextMarkable,String> text_origin;
    public final MarkableLevel<TuebaNEMarkable> nes;
	public final IAccessor<TuebaNEMarkable,String> ne_kind;
    public final MarkableLevel<TuebaEduMarkable> edus;
    public final MarkableLevel<TuebaTopicMarkable> topics;
	public final IAccessor<TuebaTopicMarkable,String> topic_description;
    public final MarkableLevel<TuebaEduRangeMarkable> edu_ranges;
    public final IAccessor<TuebaNodeMarkable, List<NamedObject>> node_children;
	public final IAccessor<TuebaTerminal,String> word_word;
	public final IAccessor<TuebaTerminal,String> word_cat;
	public final IAccessor<TuebaTerminal,String> word_morph;
	public final IAccessor<TuebaTerminal,String> word_lemma;
	public final IAccessor<TuebaTerminal,String> word_edge_label;
	public final IAccessor<TuebaTerminal,TuebaNodeMarkable> word_parent;
	public final IAccessor<TuebaTerminal,String> word_wsd_lexunits;
	public final IAccessor<TuebaTerminal,String> word_wsd_comment;
	public final IAccessor<TuebaTerminal,TuebaTerminal> word_syn_parent;
	public final IAccessor<TuebaTerminal,String> word_syn_label;
	public final IAccessor<TuebaTerminal,String> word_comment;

	/**
	 * Simple constructor; creates a new instance of the class and instantiates
	 * fields for children, category, part-of-speech and word form.
	 */
	public TuebaDocument() {
		super(TuebaTerminalSchema.instance);
        sentences = new MarkableLevel<TuebaSentenceMarkable>(TuebaSentenceSchema.instance,this);
        addMarkableLevel(sentences, "sentence");
        nodes = new MarkableLevel<TuebaNodeMarkable>(TuebaNodeSchema.instance,this);
        addMarkableLevel(nodes, "node");
        node_cat = 
           (IAccessor<TuebaNodeMarkable, String>) TuebaNodeSchema.instance.getAttribute("cat").accessor;
        node_edge_label = 
           (IAccessor<TuebaNodeMarkable, String>) TuebaNodeSchema.instance.getAttribute("func").accessor;
        node_parent = 
           (IAccessor<TuebaNodeMarkable, TuebaNodeMarkable>) TuebaNodeSchema.instance.getAttribute("parent").accessor;
        node_comment = 
           (IAccessor<TuebaNodeMarkable, String>) TuebaNodeSchema.instance.getAttribute("comment").accessor;
        texts = new MarkableLevel<TuebaTextMarkable>(TuebaTextSchema.instance,this);
        addMarkableLevel(texts, "text");
        text_origin = 
           (IAccessor<TuebaTextMarkable, String>) TuebaTextSchema.instance.getAttribute("origin").accessor;
        nes = new MarkableLevel<TuebaNEMarkable>(TuebaNESchema.instance,this);
        addMarkableLevel(nes, "ne");
        ne_kind = 
           (IAccessor<TuebaNEMarkable, String>) TuebaNESchema.instance.getAttribute("type").accessor;
        edus = new MarkableLevel<TuebaEduMarkable>(TuebaEduSchema.instance,this);
        addMarkableLevel(edus, "edu");
        topics = new MarkableLevel<TuebaTopicMarkable>(TuebaTopicSchema.instance,this);
        addMarkableLevel(topics, "topic");
        topic_description = 
           (IAccessor<TuebaTopicMarkable, String>) TuebaTopicSchema.instance.getAttribute("description").accessor;
        edu_ranges = new MarkableLevel<TuebaEduRangeMarkable>(TuebaEduRangeSchema.instance,this);
        addMarkableLevel(edu_ranges, "edu-range");
		node_children = nodes.schema
				.<List<NamedObject>> genericAccessor("children");
        addEdgeSchema("splitRelation", TuebaSplitRelationSchema.instance);
        addEdgeSchema("discRel", TuebaDiscRelSchema.instance);
        addEdgeSchema("secEdge", TuebaSecEdgeSchema.instance);
        addEdgeSchema("relation", TuebaRelationSchema.instance);
        addEdgeSchema("connective", TuebaConnectiveSchema.instance);
        word_word = TuebaTerminalSchema.instance.<String> genericAccessor("form");
        word_cat = TuebaTerminalSchema.instance.<String> genericAccessor("pos");
        word_morph = TuebaTerminalSchema.instance.<String> genericAccessor("morph");
        word_lemma = TuebaTerminalSchema.instance.<String> genericAccessor("lemma");
        word_edge_label = TuebaTerminalSchema.instance.<String> genericAccessor("func");
        word_parent = TuebaTerminalSchema.instance.<TuebaNodeMarkable> genericAccessor("parent");
        word_wsd_lexunits = TuebaTerminalSchema.instance.<String> genericAccessor("wsd-lexunits");
        word_wsd_comment = TuebaTerminalSchema.instance.<String> genericAccessor("wsd-comment");
        word_syn_parent = TuebaTerminalSchema.instance.<TuebaTerminal> genericAccessor("dephead");
        word_syn_label = TuebaTerminalSchema.instance.<String> genericAccessor("deprel");
        word_comment = TuebaTerminalSchema.instance.<String> genericAccessor("comment");
	}

	/**
	 * Works just like the simple constructor, then reads in the data from the
	 * given input file.
	 * 
	 * @param xmlFile
	 *            a file in TueBa export xml format.
	 */
	public static TuebaDocument loadDocument(String xmlFile)
        throws FileNotFoundException
    {
        TuebaDocument doc=new TuebaDocument();
        try {
            doc.readDocument(xmlFile);
        } catch (XMLStreamException ex) {
            throw new RuntimeException("Cannot load document",ex);
        }
        return doc;
	}

	/**
	 * Similar to readDocument() for Document class; reads Document and handles
	 * child list construction.
	 * 
	 * @param fileName
	 *            input xml file name
	 * @throws FileNotFoundException
	 * @throws XMLStreamException
	 */
	public void readDocument(String fileName) throws FileNotFoundException,
			XMLStreamException {
		DocumentReader.readDocument(this, new FileInputStream(fileName));
		nodes.<TuebaNodeMarkable> addToChildList("parent", node_children);
		addToChildList("parent", node_children);
		nodes.sortChildList(node_children);
	}
}