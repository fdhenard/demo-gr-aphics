(defproject demo-gr-aphics "0.1.0-SNAPSHOT"
  :description "Demographic record processing with files and a web API"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.3"]
                 [clojure.java-time "0.3.2"]
                 [org.clojure/tools.cli "1.0.206"]
                 [ring/ring-core "1.9.2"]
                 [ring/ring-jetty-adapter "1.9.2"]
                 [mount "0.1.16"]
                 [metosin/reitit "0.5.12"]
                 [metosin/reitit-ring "0.5.12"]
                 [expound "0.8.9"]
                 #_[metosin/muuntaja "0.6.4"]
                 [ring/ring-json "0.5.1"]
                 [ring/ring-defaults "0.3.2"]
                 [org.clojure/data.json "2.0.2"]
                 [clj-http "3.12.1"]]
  :repl-options {:init-ns demo-gr-aphics.core}
  :main demo-gr-aphics.cli)
