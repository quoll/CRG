(defproject org.mulgara/crg "0.0.1-SNAPSHOT"

  ;; Project Description
  :description "Clojure RDF Graphs"

  ;; Project dependencies, resolved via Maven
  ;; Format is: [<groupId>/<artifactId> "version"]
  :dependencies [[org.clojure/clojure "1.2.1"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [org.mulgara/mrg "0.7"]]

  :dev-dependencies [[lein-dist "0.0.18"]]

  :aot [mulgara.crg.core]

  ;; Use a Maven-like directory structure
  :source-path "src/main/clj"
  :test-path "src/test/clj"

  :target-dir "target"
  :compile-path "target/classes"
  
  :jar-distdir "lib/"
  :pkg [;;include       excludes?    dest
        ["lib/*.jar"                 "lib/ext/"]
        ["src/dist/*"                ""]]
  
  )
