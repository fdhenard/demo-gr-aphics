(ns demo-gr-aphics.file
  (:require [demo-gr-aphics.core :as core]
            [clojure.java.io :as io]
            [clojure.pprint :as pprint]))

(def gender->display {:m "Male"
                      :f "Female"})

(defn rec->displayable-for-cli [demog-rec]
  (as-> demog-rec $
    (core/rec->displayable $)
    (assoc $ :gender (get gender->display (:gender $)))
    (clojure.walk/stringify-keys $)))

(defn process-file! [filepath delimiter-name]
  (let [file (io/as-file filepath)]
    (if (not (.exists file))
      (println (str "file './" filepath "' does not exist"))
      (let [file-as-str (slurp file)
            lines (clojure.string/split file-as-str #"\n")
            delimiter-regex (get core/delimiter-regexes (keyword delimiter-name))
            xform-results (as-> lines $
                            (map-indexed
                             (fn [idx line]
                               (-> line
                                   (core/line->map delimiter-regex)
                                   (assoc :line-num (inc idx))))
                             $)
                            (group-by #(if (nil? (:result %)) :error :result) $))
            ;; _ (pprint/pprint xform-results)
            errored-lines (->> (:error xform-results)
                               (map #(-> %
                                         :error
                                         (assoc :line-num (:line-num %)))))
            ;; _ (pprint/pprint errored-lines)
            _ (println "\nerrors:\n")
            _ (doseq [errd-line errored-lines]
                (println (str "error for line number " (:line-num errd-line)
                              ", line: \"" (:line errd-line) "\""))
                (println (str (:spec-explain-str errd-line) "\n")))
            demog-recs (->> (:result xform-results)
                            (map #(:result %)))
            ;; _ (pprint/pprint demog-recs)
            sorted-by-gender-lname (sort-by (juxt :gender :last-name) demog-recs)
            display-sorted-gender-lname (map rec->displayable-for-cli sorted-by-gender-lname)
            sorted-by-dob (sort-by :birthdate demog-recs)
            display-sorted-dob (map rec->displayable-for-cli sorted-by-dob)
            sorted-by-lname-desc (sort-by :last-name #(compare %2 %1) demog-recs)
            display-sorted-lname-desc (map rec->displayable-for-cli sorted-by-lname-desc)
            _ (println "\n\nOutput 1 – sorted by gender (females before males) then by last name ascending")
            _ (pprint/print-table display-sorted-gender-lname)
            _ (println "\n\nOutput 2 – sorted by birth date, ascending")
            _ (pprint/print-table display-sorted-dob)
            _ (println "\n\nOutput 3 - sorted by last name, descending")
            _ (pprint/print-table display-sorted-lname-desc)
            ]
        nil))))
