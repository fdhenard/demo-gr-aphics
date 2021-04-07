(ns demo-gr-aphics.file
  (:require [clojure.walk :as walk]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [clojure.pprint :as pprint]
            [demo-gr-aphics.core :as core]))

(def gender->display {:m "Male"
                      :f "Female"})

(defn lines->canonical-or-error-maps
  "transform multiple lines to canonical or error maps from core.  Then group them by their types -> :demog-rec, or :error"
  [lines delimiter-regex]
  (->> lines
       (map-indexed (fn [idx line]
                      (-> line
                          (core/line->canonical-or-error-map delimiter-regex)
                          (assoc :line-num (inc idx)))))
       (group-by :type)))

(defn canonical->displayable-for-file-processing
  "transform a canonical demographic record to the display desired for file processing output"
  [{:keys [gender] :as demog-rec}]
  (-> demog-rec
      core/canonical->displayable
      (dissoc :line-num)
      (assoc :gender (get gender->display gender))
      walk/stringify-keys))

(defn get-display-sorted [canonical-demog-recs sort-fn]
  (let [sorted (sort-fn canonical-demog-recs)]
    (map canonical->displayable-for-file-processing sorted)))

(defn sort-reverse [a b]
  (compare b a))

(defn process-file!
  "side effecting - takes file input, does the transformations, and outputs to standard out"
  [filepath delimiter-name]
  (let [file (io/as-file filepath)]
    (if (not (.exists file))
      (println (str "file './" filepath "' does not exist"))
      (let [file-as-str (slurp file)
            lines (string/split file-as-str #"\n")
            delimiter-regex (get core/DELIMITER_REGEXES (keyword delimiter-name))
            canonical-or-error-maps (lines->canonical-or-error-maps lines delimiter-regex)
            ;; _ (pprint/pprint canonical-or-error-maps)
            canonical-demog-recs (:demog-rec canonical-or-error-maps)
            _ (println "\nerrors:\n")
            _ (doseq [errd-line (:error canonical-or-error-maps)]
                (println (str "error for line number " (:line-num errd-line)
                              ", line: \"" (:line errd-line) "\""))
                (println (str (:spec-explain-str errd-line) "\n")))
            _ (println "\n\nOutput 1 – sorted by gender (females before males) then by last name ascending")
            _ (pprint/print-table (get-display-sorted
                                   canonical-demog-recs
                                   #(sort-by (juxt :gender :last-name) %)))
            _ (println "\n\nOutput 2 – sorted by birth date, ascending")
            _ (pprint/print-table (get-display-sorted
                                   canonical-demog-recs
                                   #(sort-by :birthdate %)))
            _ (println "\n\nOutput 3 - sorted by last name, descending")
            _ (pprint/print-table (get-display-sorted
                                   canonical-demog-recs
                                   #(sort-by :last-name sort-reverse %)))]
        nil))))
