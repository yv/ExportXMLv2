ExportXMLv2 -- interface library for the EXML file format
---------------------------------------------------------

[![Build Status](https://travis-ci.org/yv/ExportXMLv2.svg?branch=master)](https://travis-ci.org/yv/ExportXMLv2)

This library allows it to read files in the ExportXMLv2 format
for annotated corpora. The goal for ExportXMLv2 (or EXML in short)
is to provide a format that is human-readable and can be edited with
cut and paste in a text editor, but which is powerful enough to support
complex corpus annotation with ad-hoc creation of annotation levels
(as is needed for prototyping and computational linguistics research).
The ExportXMLv2 library aims to be efficient and fast enough for large-scale
production use.

### The EXML format and data model

An ExportXML file consists of two parts:
 
  * A schema part, which contains information about the attributes and
    annotation levels that are part of the body.
  * a body part, which contains the actual corpus data.
  
The data in an EXML file consists of two kinds:

 * Terminal nodes, of which there is one per corpus position (i.e., tokens).
   Tokens can have a multitude of attributes, including string or enumeration
   attributes as well as references to other annotated nodes (e.g. in the case
   of dependency syntax)
 * Markable nodes, which cover a (potentially discontinuous) span in the text,
   and can, like token nodes, have attributes of their own.
   
A minimal example for this could be the following:
```
<exml-doc>
<schema>
<tnode name="word">
<text-attr name="form"/>
</tnode>
</schema>
<body>
<word form="Two"/>
<word form="words"/>
</body>
</exml-doc>
```

This document declares the "word" terminal node with the "form" string attribute, and the
body contains two of terminals.

A slightly bigger example could add markable nodes for sentences:
``
<exml-doc>
<schema>
<tnode name="word">
<text-attr name="form"/>
</tnode>
<node name="sentence">
</node>
</schema>
<body>
<sentence xml:id="s1">
<word form="Two"/>
<word form="words"/>
</sentence>
</body>
</exml-doc>
```

Any token or markable can have an *xml:id* attribute, and references within the XML file
use these IDs to refer to other objects.

The name "word" for tokens, and "form" for the surface form of the token, are hard-wired in
many places, which means it is a bad idea to change them. For the other attributes and nodes,
the existing ("TueBa") data model provides a good starting point, even though you could (and can)
use the EXML format and the ExportXMlv2/exmldoc libraries without being forced to use it.

Besides the ExportXMLv2 Java library, there is a Python library called [exmldoc](https://github.com/yv/exmldoc)
that can be used to manipulate EXML files and the linguistic data contained in them.
Creating simple EXML files by writing XML is easy to do also from other environments,
however discontinuous spans and complex object graphs (i.e. links between nodes as they
occur in discourse structure or coreference graph annotation) are best handled using
the specialized libraries.

### Java Quick Start

To start working with EXML files, we'll use the minimal
example file in `test/exml/io/simple.xml`.

The most convenient way of using the EXML library for linguistic
corpora is to use the existing data model developed for
the TüBa-D/Z treebank, which can be found in the `exml.tueba`
package. This package was generated automatically from a data
model in Python, see the section below for more confortable
means of adapting the data model to accommodate your own annotation
layers.

We can load the file using code such as
```
import exml.tueba.TuebaDocument

TuebaDocument doc = TuebaDocument.readDocument("test/exml/io/simple.exml.xml")
```

There are a number of things we can now do with `doc`:
 * we can retrieve word-level attributes through expressions such as
   `doc.getTerminal(3).getWord()` (for retrieving the surface form of
   the 4th word)
 * we can retrieve objects by the ID they have in the XML file through
   an expression such as `doc.resolveObject("s1_3")`
 * we can retrieve all sentences through an expression such as
   `doc.sentences.getMarkables()` or the sentence(s) covering
   position number 16, by using an expression such as
   `doc.sentences.getOverlappingMarkables(16,16)`

When we have added or modified some of the information inside an EXML
document, we can save it to another file:
```
OutputStream out = new FileOutputStream("simple_out.exml.xml");
DocumentWriter.writeDocument(doc, out);
out.close()
```
