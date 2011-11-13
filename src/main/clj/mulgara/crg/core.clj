(ns ^{:doc "A Clojure RDF Graph API"
      :author "Paul Gearon"}
  mulgara.crg.core
  (:use [mulgara.crg.node :only [iri lit subject predicate object ?subject ?predicate ?object]])
  (:import [mulgara.crg.node Iri Literal QName]))

(defrecord Triple [subject predicate object]
  Object
  (toString [this]
            (str subject " " predicate " " object " .")))

(defn triple
  "Create a new triple"
  [s p o]
  (Triple. (subject s) (predicate p) (object o)))

(defprotocol Graph
  "Defines an object that can add, delete and find triples"
  (graph-add [this s p o] "Add a triple")
  (graph-delete [this s p o] "Delete a triple")
  (graph-pattern [this s p o] "Resolve a pattern"))

(defn add
  "Adds a triple to a graph"
  ([graph s p o] (graph-add graph (subject s) (predicate p) (object o)))
  ([graph triple] (graph-add graph (:subject triple) (:predicate triple) (:object triple))))

(defn delete
  "Removes a triple from a graph"
  ([graph s p o] (graph-delete graph (subject s) (predicate p) (object o)))
  ([graph triple] (graph-delete graph (:subject triple) (:predicate triple) (:object triple))))

(defn pattern
  "Resolves a pattern in a triple against a graph"
  ([graph s p o] (graph-pattern graph (?subject s) (?predicate p) (?object o)))
  ([graph triple] (pattern graph (:subject triple) (:predicate triple) (:object triple))))

