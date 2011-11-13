(ns ^{:doc "Nodes for RDF"
      :author "Paul Gearon"}
  mulgara.crg.node
  (:import [java.net URL URI]))

(def ? :?)

(defprotocol Associable "Something that can have add called on it"
  (addns [this prefix namespc] "Associate a prefix with a namespace"))

(defrecord XNamespace [forward back]
  Associable
  (addns [this prefix namespc] (XNamespace. (assoc forward prefix namespc) (assoc back namespc prefix))))

(def default-namespaces
  {:rdf "http://www.w3.org/1999/02/22-rdf-syntax-ns#"
   :rdfs "http://www.w3.org/2000/01/rdf-schema#"
   :xsd "http://www.w3.org/2001/XMLSchema#"
   :owl "http://www.w3.org/2002/07/owl#"
   :skos "http://www.w3.org/2004/02/skos/core#"
   :dc "http://purl.org/dc/elements/1.1/"
   :dcterms "http://purl.org/dc/terms/"
   :dctype "http://purl.org/dc/dcmitype/"
   :dcam "http://purl.org/dc/dcam/"
   :foaf "http://xmlns.com/foaf/0.1/"
   :vcard "http://www.w3.org/2001/vcard-rdf/3.0#"
   :rss "http://purl.org/rss/1.0/modules/content/"
   :sioc "http://rdfs.org/sioc/ns#"
   :time "http://www.w3.org/2006/time#"
   :dbprop "http://dbpedia.org/property/"
   :db "http://dbpedia.org/resource/" })

(defn rmap "Reverse a mapping" [m] (into {} (map #(vector (second %) (first %)) m)))

(def ^:dynamic *namespaces* (XNamespace. default-namespaces (rmap default-namespaces)))

(defrecord Iri [text]
  Object
  (toString [this]
            (str "<" text ">")))

(defrecord QName [prefix local]
  Object
  (toString [this]
            (str (name prefix) \: local)))

(defn- scan-prefix
  "Scans for possible prefix endings. All known prefixes end in either # or /.
   Result is a pair of prefix/name if found, or nil if not found."
  [^String text]
  (let [pos (.lastIndexOf text "#")
        pos (inc (if (< pos 0) (.lastIndexOf text "/") pos))]
    (if (> pos 0) [(subs text 0 pos) (subs text pos)])))

(defn- prefix-name
  "Extracts a prefix and local name from an IRI. Returns a prefix/name if a known namespace is found
   or nil if no namespace is recognized."
  [^String text]
  (if-let [[p n] (scan-prefix text)]
    (if-let [k ((:back *namespaces*) p)] [(name k) n])))

(defn to-iri
  "Converts a QName to an Iri"
  [^QName q]
  (Iri. (str ((:forward *namespaces*) (keyword (:prefix q))) (:local q))))

(defprotocol IriNode
  "A data type that can represent an IRI reference node"
  (iri [this] "Converts this object to an IRI reference"))

(extend-protocol IriNode
  String
  (iri [this] (if-let [[pre local] (prefix-name this)]
                (QName. pre local)
                (Iri. this)))
  clojure.lang.Keyword
  (iri [this] (QName. (or (namespace this) "") (name this)))
  URI
  (iri [this] (iri (str this)))
  URL
  (iri [this] (iri (str this))))

(defrecord Literal [text datatype lang]
  Object
  (toString [this]
            (if lang (str \" text "\"@" lang)
              (if datatype (str \" text "\"^^" datatype)
                (str \" text \")))))

(defmulti literal-dispatch
  "Creates an appropriate literal type based on the data type of the passed parameters."
  #(class %2))

(defmethod literal-dispatch String [^String text data] (Literal. text nil data))
(defmethod literal-dispatch QName [^String text data] (Literal. text data nil))
(defmethod literal-dispatch Iri [^String text data] (Literal. text data nil))
(defmethod literal-dispatch :default [^String text data] (Literal. text (iri (str data)) nil))

(defn lit
  "Creates a literal given a lexical form and either a datatype or a language code."
  ([^String text] (Literal. text nil nil))
  ([^String text data] (literal-dispatch text data)))

(defrecord BlankNode [label]
  Object
  (toString [this] (str label)))

(defn blank-node
  "Creates a new blank node for a given label"
  ([] (BlankNode. (gensym "_:")))
  ([^String label] (BlankNode. (str "_:" label))))

(defprotocol NodeConvertable
  "Data types that can be converted into RDF nodes"
  (subject [n] "Convert data to a subject node")
  (predicate [n] "Convert data to a predicate node")
  (object [n] "Convert data to an object node"))

(extend-protocol NodeConvertable
  nil
    (subject [n] (blank-node))
    (predicate [n] (throw (Exception. "Blank predicate")))
    (object [n] (blank-node))
  String
    (subject [n] (iri n))
    (predicate [n] (iri n))
    (object [n] (lit n))
  URI
    (subject [n] (iri n))
    (predicate [n] (iri n))
    (object [n] (iri n))
  URL
    (subject [n] (iri n))
    (predicate [n] (iri n))
    (object [n] (iri n))
  clojure.lang.Keyword
    (subject [n] (iri n))
    (predicate [n] (iri n))
    (object [n] (iri n))
  Iri
    (subject [n] n)
    (predicate [n] n)
    (object [n] n)
  QName
    (subject [n] n)
    (predicate [n] n)
    (object [n] n)
  Number
    (subject [n] (throw (Exception. "Literal subject")))
    (predicate [n] (throw (Exception. "Literal predicate")))
    (object [n] (lit (str n) (iri :xsd/integer)))
  Double
    (subject [n] (throw (Exception. "Literal subject")))
    (predicate [n] (throw (Exception. "Literal predicate")))
    (object [n] (lit (str n) (iri :xsd/double)))
  Float
    (subject [n] (throw (Exception. "Literal subject")))
    (predicate [n] (throw (Exception. "Literal predicate")))
    (object [n] (lit (str n) (iri :xsd/float))))

(defn ?subject "Converts to a subject node, unless equal to ?"
  [n] (if (= ? n) n (subject n)))

(defn ?predicate "Converts to a predicate node, unless equal to ?"
  [n] (if (= ? n) n (predicate n)))

(defn ?object "Converts to an object node, unless equal to ?"
  [n] (if (= ? n) n (object n)))

