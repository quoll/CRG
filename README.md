# crg

A Clojure RDF Graph API.

## Usage

(let [g (-> (indexed-graph)
          (add "http://example.com/me" :rdf/type :foaf/Person)
          (add "http://example.com/me" :foaf/name "Paul")
          (add "http://example.com/fred" :rdf/type :foaf/Person)
          (add "http://example.com/fred" :foaf/name "Fred"))]
  (pattern g ? :rdf/type ?))

Returns:
[ (triple "http://example.com/me" :rdf/type :foaf/Person)
  (triple "http://example.com/fred" :rdf/type :foaf/Person) ]

The first elements of the triple will be in a record type called "Iri". If a string is promoted to an iri
then it will always be the full iri. If you want to convert it to a QName using a registered namespace
then wrap the string in a call to (iri). If the namespace is not known, then this function will fall back
to using an Iri.

A new namespace can be added by calling the addns function on mulgara.crg.node.*namespaces*.
For example, to add a prefix of "foo" for the namespace "http://foo.com/" then you can bind it with:

(binding [*namespaces* (addns *namespaces* :foo "http://foo.com/")]
  ;; graph access code here
  )

If an IRI can be accepted somewhere, then most types will be converted automatically. Strings will always
be converted to an Iri record, while URIs, URLs, and keywords will be converted to QNames if possible.

The object position of a triple acts a little differently. Strings will be converted to literals, as will
numbers. Literals can be explicitly created with the "lit" function. Literals may only be used in the object
position of a triple.

Nil will be converted to a fresh blank node. To explicitly create a blank node, use the "blank-node" function.
This function takes an optional parameter to set the internal label of the node. Blank nodes may not be used
in the predicate position of a triple.

When creating a graph you have the option of calling simple-graph or indexed-graph. Both have the same
functionality, but with different characteristics. Simple graphs are not fully indexed. This means that they
will be slightly faster to load and use less memory. Indexed graphs are fully indexed. This uses more memory,
and loads data slightly slower, but searches in the graph can be significantly faster. These differences will
only be noticeable for large data sets.

## License

Copyright (C) 2011 Paul Gearon

Distributed under the Eclipse Public License, the same as Clojure.
