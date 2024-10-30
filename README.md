# SMAX (Separated Markup API for XML)

The standard XML processing APIs, such as DOM and SAX (and LINQ, if you use .net) view an XML document, including its text content, as a tree.
The same is true for implementations of XSLT, XPath and XQuery.
An XML document tree consists of nodes, where the text content is distributed across different leaves at different levels in the tree.

With the tree representation, it is possible to work on the complete text content of a document (or element),
but this will lose the structure of the document.
There are scenarios where text that potentially goes beyond element boundaries must be processed without losing the existing document structure.
Sometimes, this text processing will generate new XML elements that must be merged into the existing structure.

In order to be able to process the document text in a linear way, retaining and modifying the structure (markup), SMAX (Separated Markup API for XML) was designed.
As the name indicates, SMAX treats the markup (element structure) and text of an XML document separately.
A typical use case for SMAX is text analysis and the addition of additional markup around matched text fragments.

SMAX makes the structure of an XML document transparent to text processing functions.
This is at the basis of ['transparent XML'](https://www.xmlprague.cz/day3-2024/#iXML).

# Building

Make a copy of this repository, and do `maven install`.
This should make the SMAX jar available for other projects, like [basex-waxeye](https://github.com/nverwer/basex-waxeye), [basex-ner-xar](https://github.com/nverwer/basex-ner-xar), [exist-ixml-xar](https://github.com/nverwer/exist-ixml-xar), and others.

# Java classes

The class `SmaxDocument` is the SMAX representation of an XML document or document-fragment.
It contains a `SmaxElement`, which is the root element of the markup of document (-fragment) and
a `SmaxContent`, which is the text of the document.

A `SmaxElement` has a name, namespace, attributes, parent and children.
It does not have methods to access its text content, because that is separated from the markup.
Instead, it has a start- and end-position in the text content.
The start- and end-positions are points _between_ characters.
Position _n_ is just before the _n_ th character in the `StringBuffer` of a `SmaxContent`.

A `SmaxDocument` may be a sub-document of a larger `SmaxDocument`, and use (a subset of) the same `SmaxElement`s.
To avoid the use of position offsets in a sub-document,
the root element of a `SmaxDocument` does not necessarily have start-position _0_.

The `SmaxContent` class implements `CharSequence`, like `String` and other character sequences in Java.
Its main purpose is to provide sub-document views with zero-based indexes, without copying any content.
It also provides all methods from `StringBuffer` to manipulate the underlying character sequence efficiently.
The start-position and end-position of every `SmaxElement` in a `SmaxDocument`
are relative to the underlying `SmaxContent`.
This makes it easier to create sub-documents of a `SmaxDocument` without changing start and end positions.

The `SmaxDocument` class has a method
`insertMarkup(SmaxElement newNode, Balancing balancing, int startPos, int endPos)`
which inserts a new XML element into the document, with a character span from `startPos` to `endPos`.
This method modifies the document structure (markup), but not the content.
It can be used to mark ranges of text, and is used text matching transformers.

Surrounding a character span by a new XML element at arbitrary start- and end-positions might lead to
unbalanced, or non-well-formed XML markup.
Therefore, a `Balancing` strategy must be specified.
This tells the `insertMarkup` method how to deal with potentially unbalanced markup.
See the javadoc for available balancing strategies.

# To do / known bugs and features

There will be documentation on balancing, merging SMAX documents, and other SMAX features.

Comments and processing instructions are not (yet) part of SMAX and will be ignored.

CDATA sections, entities and entity references are ignored and will become part of text content.

# Notes

This project contains parts of the larger [SPEAT](https://github.com/nverwer/SPEAT) project.
These parts are for specific uses within other libraries.
Work on SPEAT has been suspended, and its SMAX implementation is obsolete.
