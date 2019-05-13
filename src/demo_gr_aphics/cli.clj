(ns demo-gr-aphics.cli
  (:require [clojure.tools.cli :as cli]
            [clojure.string :as str]
            [demo-gr-aphics.core :as core]
            [mount.core :as mount]
            [demo-gr-aphics.web]
            [demo-gr-aphics.file])
  (:gen-class))

(defn usage [options-summary]
  (->> [""
        "demo-gr-aphics line processing - web or file"
        ""
        "Usage:"
        ""
        ""
        "For file processing: "
        "$ lein run!!!change!!! filepath delimiter"
        ""
        "Delimiter options:"
        "  pipe  ' | '"
        "  comma ', '"
        "  space ' '"
        ""
        ""
        "For webserver: "
        "$ lein run !!!!change!!! webserver"
        ""
        "  then navigate to http://localhost:3000"
        ""]
       (str/join \newline)))

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (str/join \newline errors)))

(defn validate-args [args]
  (let [{:keys [options arguments errors summary]} (cli/parse-opts args [])]
    (cond
      (:help options) ; help => exit OK with usage summary
      {:exit-message (usage summary) :ok? true}
      errors ; errors => exit with description of errors
      {:exit-message (error-msg errors)}
      ;; custom validation on arguments
      (and (= 2 (count arguments))
           (core/delimiter-choices (nth arguments 1)))
      {:filepath (first arguments) :delimiter (nth arguments 1)}
      (and (= 1 (count arguments))
           (= "webserver" (nth arguments 0)))
      {:webserver? true}
      :else ; failed custom validation => exit with usage summary
      {:exit-message (usage summary)})))

(defn -main [& args]
  (let [{:keys [filepath delimiter webserver? exit-message ok?]} (validate-args args)]
    (cond
      exit-message
      (println exit-message)
      webserver?
      (-> (mount/only #{#'demo-gr-aphics.web/webserver})
          mount/start)
      :else
      (demo-gr-aphics.file/process-file! filepath delimiter))))
