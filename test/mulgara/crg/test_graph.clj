(ns mulgara.crg.test-graph
  (use [mulgara.crg.graph :only [simple-graph indexed-graph]]
       [mulgara.crg.node :only [*namespaces* addns ?]]
       [mulgara.crg.core :only [add delete pattern triple]]
       [clojure.test])
  (import [java.net URI URL]
          [mulgara.crg.core Triple]
          [mulgara.crg.node Iri QName Literal BlankNode]))

(def iri-str "http://www.w3.org/1999/02/22-rdf-syntax-ns#type")
(def iri-str-dc "http://purl.org/dc/terms/title")
(def iri-unk "http://example.com/foo")

(defn insert-t [g]
  (let [g (-> g
            (add :ex/a :rdf/type :ex/A)
            (add :ex/b :rdf/type :ex/B)
            (add :ex/b :ex/other :ex/O)
            (add :ex/a :rdf/type :ex/AA)
            (add :ex/c :rdf/type :ex/C))]
    (is (= (into #{} (pattern g ? ? ?))
           #{(triple :ex/a :rdf/type :ex/A)
             (triple :ex/a :rdf/type :ex/AA)
             (triple :ex/b :rdf/type :ex/B)
             (triple :ex/b :ex/other :ex/O)
             (triple :ex/c :rdf/type :ex/C)}))))

(defn delete-t [g]
  (let [g (-> g
            (add :ex/a :rdf/type :ex/A)
            (add :ex/b :rdf/type :ex/B)
            (add :ex/b :ex/other :ex/O)
            (add :ex/c :rdf/type :ex/C)
            (add :ex/a :rdf/type :ex/AA)
            (delete :ex/b :ex/other :ex/O)
            (delete :ex/c :rdf/type :ex/C)
            (delete :ex/a :rdf/type :ex/AA))]
    (is (= (into #{} (pattern g ? ? ?))
           #{(triple :ex/a :rdf/type :ex/A)
             (triple :ex/b :rdf/type :ex/B)}))))

(defn find-t [g]
  (let [g (-> g
            (add :ex/a :rdf/type :ex/AA)
            (add :ex/b :rdf/type :ex/B)
            (add :ex/b :ex/other :ex/O)
            (add :ex/a :rdf/type :ex/A)
            (add :ex/c :rdf/type :ex/C)
            (add :ex/c :ex/another :ex/AC)
            (add :ex/d :ex/other :ex/O))]
    (is (= (into #{} (pattern g :ex/a :rdf/type :ex/AA))
           #{(triple :ex/a :rdf/type :ex/AA)}))
    (is (= (into #{} (pattern g :ex/a :rdf/type ?))
           #{(triple :ex/a :rdf/type :ex/A)
             (triple :ex/a :rdf/type :ex/AA)}))
    (is (= (into #{} (pattern g :ex/c ? :ex/C))
           #{(triple :ex/c :rdf/type :ex/C)}))
    (is (= (into #{} (pattern g :ex/a ? ?))
           #{(triple :ex/a :rdf/type :ex/A)
             (triple :ex/a :rdf/type :ex/AA)}))
    (is (= (into #{} (pattern g ? :ex/other :ex/O))
           #{(triple :ex/b :ex/other :ex/O)
             (triple :ex/d :ex/other :ex/O)}))
    (is (= (into #{} (pattern g ? :rdf/type ?))
           #{(triple :ex/a :rdf/type :ex/A)
             (triple :ex/a :rdf/type :ex/AA)
             (triple :ex/b :rdf/type :ex/B)
             (triple :ex/c :rdf/type :ex/C)}))
    (is (= (into #{} (pattern g ? ? :ex/O))
           #{(triple :ex/b :ex/other :ex/O)
             (triple :ex/d :ex/other :ex/O)}))))

(binding [*namespaces* (addns *namespaces* :ex "http://example.com/")]

  (deftest test-insert
           (insert-t (simple-graph))
           (insert-t (indexed-graph)))

  (deftest test-delete
           (delete-t (simple-graph))
           (delete-t (indexed-graph)))

  (deftest test-pattern
           (find-t (simple-graph))
           (find-t (indexed-graph)))

  )
