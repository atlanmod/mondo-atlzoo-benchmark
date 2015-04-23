Metamodel files
---------------
 * BibTeX.ecore
 * BibTeX.km3
 * DocBook.ecore
 * DocBook.km3

Example files
-------------
 * bibtex-example.ecore: BibTeX model
 * docbook-example.ecore: DocBook model
 * xml-example.xml: initial XML file
 * xml-example-final.xml: final XML final

Transformation files
--------------------
 * BibTeX2DocBook.atl

"BibTex.ecore" and "DocBook.ecore" are the metamodels for BibTeX and DocBook models.
Km3 files provide readable versions of these metamodels in the km3 format.

The BibTeX2DocBook transformation ("BibTeX2DocBook.atl") can be tested with the BibTeX model ("bibtex-example.ecore") as input.
It returns a DocBook model ("docbook-example.ecore").

"xml-example.xml" provides a readable version of the bibliography example.
Before executing the BibTex2DocBook transformation, it has to be injected from the XML to the BibTex metamodel.
Injection is not detailled in this example.

"xml-example-final.xml" provides a readable version of the obtained result.
This file is the result of two successives extractions of the DocBook model produced by BibTeX2DocBook:
  1. From the DocBook to the XML model
  2. From the XML model to text
Extractions are not detailled in this example.
