(ns demo-gr-aphics.cli
  (:require [clojure.tools.cli :as cli]
            [clojure.string :as str]
            [demo-gr-aphics.core])
  (:gen-class))

(defn usage [options-summary]
  (->> ["demo-gr-aphics file processing"
        ""
        "Usage: lein run!!!change!!! filepath delimiter"
        ""
        "Delimiter options:"
        "  pipe  ' | '"
        "  comma ', '"
        "  space ' '"
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
           (#{"pipe" "comma" "space"} (nth arguments 1)))
      {:filepath (first arguments) :delimiter (nth arguments 1)}
      :else ; failed custom validation => exit with usage summary
      {:exit-message (usage summary)})))

(defn -main [& args]
  (let [{:keys [filepath delimiter exit-message ok?]} (validate-args args)]
    (if exit-message
      (println exit-message)
      (demo-gr-aphics.core/process-file! filepath delimiter))))
