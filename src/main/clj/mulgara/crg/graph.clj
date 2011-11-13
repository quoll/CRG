(ns ^{:doc "A graph implementation with full indexing."
      :author "Paul Gearon"}
  mulgara.crg.graph
  (:use [mulgara.crg.core :only [add delete pattern]]
        [mulgara.crg.node :only [iri lit ?]])
  (:import [mulgara.crg.core Triple Graph]
           [mulgara.crg.node Iri Literal QName]))

(defn- index-add
  "Add elements to a 3-level index"
  [idx a b c]
  (update-in idx [a b] (fn [v] (if (seq v) (conj v c) #{c}))))

(defn- index-delete
  "Remove elements from a 3-level index. Returns the new index, or nil if there is no change."
  [idx a b c]
  (if-let [idx2 (idx a)]
    (if-let [idx3 (idx2 b)]
      (let [new-idx3 (disj idx3 c)]
        (if-not (identical? new-idx3 idx3)
          (let [new-idx2 (if (seq new-idx3) (assoc idx2 b new-idx3) (dissoc idx2 b))
                new-idx (if (seq new-idx2) (assoc idx a new-idx2) (dissoc idx a))]
            new-idx))))))

(defn- simplify [g & ks] (map #(if (= ? %) ? :v) ks))

(defmulti index-fn "Lookup an index in the graph for the requested data" simplify)

(defmethod index-fn [:v :v :v] [{idx :spo} s p o] (let [os (get-in idx [s p])] (if (get os o) [(Triple. s p o)] [])))
(defmethod index-fn [:v :v  ?] [{idx :spo} s p o] (for [o (get-in idx [s p])] (Triple. s p o)))
(defmethod index-fn [:v  ? :v] [{idx :osp} s p o] (for [p (get-in idx [o s])] (Triple. s p o)))
(defmethod index-fn [:v  ?  ?] [{idx :spo} s p o] (let [edx (idx s)] (for [p (keys edx) o (edx p)] (Triple. s p o))))
(defmethod index-fn [ ? :v :v] [{idx :pos} s p o] (for [s (get-in idx [p o])] (Triple. s p o)))
(defmethod index-fn [ ? :v  ?] [{idx :pos} s p o] (let [edx (idx p)] (for [o (keys edx) s (edx o)] (Triple. s p o))))
(defmethod index-fn [ ?  ? :v] [{idx :osp} s p o] (let [edx (idx o)] (for [s (keys edx) p (edx s)] (Triple. s p o))))
(defmethod index-fn [ ?  ?  ?] [{idx :spo} s p o] (for [s (keys idx) p (keys (idx s)) o ((idx s) p)] (Triple. s p o)))

(defrecord GraphIndexed [spo pos osp]
  Graph
  (graph-add [this subj pred obj]
       (let [new-spo (index-add spo subj pred obj)]
         (if (identical? spo new-spo)
           this
           (assoc this :spo new-spo
                       :pos (index-add pos pred obj subj)
                       :osp (index-add osp obj subj pred)))))
  (graph-delete [this subj pred obj]
          (if-let [idx (index-delete spo subj pred obj)]
            (assoc this :spo idx :pos (index-delete pos pred obj subj) :osp (index-delete osp obj subj pred))
            this))
  (graph-pattern [this subj pred obj]
           (index-fn this subj pred obj)))

(defn- uses-spo "Tests is a set of parameters refers to an SPO index"
  [& ks] (#{[:v :v :v] [:v :v  ?] [:v  ?  ?] [ ?  ?  ?]} (map #(if (= ? %) ? :v) ks)))

(defrecord GraphSimple [spo]
  Graph
  (graph-add [this s p o]
       (let [idx (index-add spo s p o)]
         (if (identical? idx spo) this (GraphSimple. idx))))
  (graph-delete [this s p o]
          (if-let [idx (index-delete spo s p o)] (GraphSimple. idx) this))
  (graph-pattern [this s p o]
           (if (uses-spo s p o) 
             (index-fn {:spo spo} s p o)
             (letfn [(match [a b] (or (= a ?) (= a b)))] ;; a may be a value or ?, b may not be ?
               (for [subj (keys spo) pred (keys (spo subj)) obj ((spo subj) pred)
                     :when (and (match s subj) (match p pred) (match o obj))]
                 (Triple. subj pred obj))))))

(defn indexed-graph "Create a fully indexed graph" [] (GraphIndexed. {} {} {}))

(defn simple-graph "Create a graph with minimal indexing" [] (GraphSimple. {}))

