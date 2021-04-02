(ns demo-gr-aphics.cli
  (:require [clojure.string :as str]
            [clojure.tools.cli :as cli]
            [mount.core :as mount]
            [demo-gr-aphics.web]
            [demo-gr-aphics.file]
            [demo-gr-aphics.core :as core])
  (:gen-class))

(def port-default 3000)

(def cli-options
  [["-p" "--port PORT" (str "Port Number (default " port-default ")")
    :default port-default
    :parse-fn #(Integer/parseInt %)
    :validate [#(< 0 % 0x10000) "Must be a number between 0 and 65536"]]])

(defn usage [options-summary]
  (->> [""
        "demo-gr-aphics line processing - web or file"
        ""
        "Usage:"
        ""
        ""
        "For file processing: "
        "$ lein run filepath delimiter"
        ""
        "Delimiter options:"
        "  pipe  ' | '"
        "  comma ', '"
        "  space ' '"
        ""
        ""
        "For webserver: "
        "$ lein run webserver"
        ""
        "Webserver Options:"
        options-summary
        ""
        "  then navigate to http://localhost:<port>"
        ""]
       (str/join \newline)))

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (str/join \newline errors)))

(defn validate-args [args]
  (let [{:keys [options arguments errors summary]} (cli/parse-opts args cli-options)]
    (cond
      errors ; errors => exit with description of errors
      {:exit-message (error-msg errors)}
      ;; custom validation on arguments
      (and (= 2 (count arguments))
           (core/delimiter-choices (nth arguments 1)))
      {:filepath (first arguments) :delimiter (nth arguments 1)}
      (and (= 1 (count arguments))
           (= "webserver" (nth arguments 0)))
      {:webserver? true :port (:port options)}
      :else ; failed custom validation => exit with usage summary
      {:exit-message (usage summary)})))

(defn -main [& args]
  (let [{:keys [filepath delimiter webserver? port exit-message _ok?]} (validate-args args)]
    (cond
      exit-message
      (println exit-message)
      webserver?
      (-> (mount/only #{#'demo-gr-aphics.web/webserver})
          (mount/with-args {:port port})
          mount/start)
      :else
      (demo-gr-aphics.file/process-file! filepath delimiter))))
