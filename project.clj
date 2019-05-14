(defproject demo-gr-aphics "0.1.0-SNAPSHOT"
  :description "Demographic record processing with files and a web API"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [clojure.java-time "0.3.2"]
                 [org.clojure/tools.cli "0.4.2"]
                 [ring/ring-core "1.6.3"]
                 [ring/ring-jetty-adapter "1.6.3"]
                 [mount "0.1.16"]
                 [metosin/reitit "0.3.1"]
                 [metosin/reitit-ring "0.3.1"]
                 [expound "0.7.2"]
                 [metosin/muuntaja "0.6.4"]
                 [ring/ring-json "0.4.0"]
                 [ring/ring-defaults "0.3.2"]
                 [org.clojure/data.json "0.2.6"]]
  :repl-options {:init-ns demo-gr-aphics.core}
  :main demo-gr-aphics.cli)
