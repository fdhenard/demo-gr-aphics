(ns demo-gr-aphics.core
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [java-time :as time]
            [clojure.spec.alpha :as spec]
            [clojure.pprint :as pprint]
            [expound.alpha :as expound]))

(defn will-coerce-to-local-date? [x]
  (try
    (do
      (time/local-date x)
      true)
    (catch clojure.lang.ExceptionInfo ei
      (let [as-map (Throwable->map ei)
            cause-type (-> as-map :via (get 1) :type)]
        (if (= cause-type 'java.time.format.DateTimeParseException)
          false
          (throw ei))))))

(defn is-date-lte-today? [x]
  (<= (compare (time/local-date x) (time/local-date)) 0))

(spec/def ::non-blank-string (spec/and string? #(not (str/blank? %))))
(spec/def ::last-name ::non-blank-string)
(spec/def ::first-name ::non-blank-string)
(def gender-options-map {"m" :m
                         "male" :m
                         "f" :f
                         "female" :f})
(def gender-options
  "combinations of 'm', 'male', 'f', 'female', capitalized, uppercased, and not transformed"
  (set (reduce
        (fn [accum item]
          (concat accum [item
                         (str/capitalize item)
                         (str/upper-case item)]))
        []
        (keys gender-options-map))))
(spec/def ::gender gender-options)
(expound/defmsg ::gender (str "should be one of " gender-options))
(spec/def ::birthdate (spec/and
                       ::non-blank-string
                       will-coerce-to-local-date?
                       is-date-lte-today?))
(spec/def ::rainbow-colors #{"red"
                             "orange"
                             "yellow"
                             "green"
                             "blue"
                             "indigo"
                             "violet"})
(spec/def ::favorite-color ::rainbow-colors)
(spec/def ::demographic-record (spec/keys :req-un [::last-name ::first-name ::gender ::favorite-color ::birthdate]))

(def delimiter-regexes {:pipe #"\ \|\ "
                        :comma #",\ "
                        :space #"\ "})
(def delimiter-choices (->> delimiter-regexes
                            keys
                            (map name)
                            set))


(defn line->map [line delimiter-re]
  (let [[lname fname gender fav-color dob] (str/split line delimiter-re)
        demographic-record {:last-name lname
                            :first-name fname
                            :gender gender
                            :favorite-color fav-color
                            :birthdate dob}
        spec-explain-data (spec/explain-data ::demographic-record demographic-record)
        spec-explain-str (spec/explain-str ::demographic-record demographic-record)
        spec-expound-str (expound/expound-str ::demographic-record demographic-record)
        ]
    (if (not (nil? spec-explain-data))
      {:error {:spec-explain-data spec-explain-data
               :spec-explain-str spec-explain-str
               :spec-expound-str spec-expound-str
               :line line}
       :result nil}
      {:result {:last-name lname
                :first-name fname
                :gender (get gender-options-map (str/lower-case gender))
                :favorite-color fav-color
                :birthdate (time/local-date dob)}
       :error nil})))


;; (defn find-delimiter [line]
;;   (let [delim-possiblities [#"\ \|\ " #",\ " #"\ "]]
;;     (loop [remaining-poss delim-possibilities]
;;       (if (empty? remaining-poss)
;;         nil
;;         (let [poss (first remining-poss)
;;               splitted (str/split line poss)]
;;           (if (and (= (count splitted) 5)
;;                    (spec/valid? ::last-name (get splitted 0))
;;                    (spec/valid? ::first-name (get splitted 1))
;;                    (spec/valid? ::gender (get splitted 2))
;;                    (spec/valid? ::rainbow-colors (get splitted 3))
;;                    (spec/valid? ::date-string (get splitted 4)))
;;             poss
;;             (recur (rest remaining-poss))))))))

(def gender-to-display {:m "Male"
                        :f "Female"})

(defn rec->displayable [demog-rec]
  (as-> demog-rec $
    (assoc $ :gender (get gender-to-display (:gender $)))
    (assoc $ :birthdate (time/format "M/d/YYYY" (:birthdate $)))
    (clojure.walk/stringify-keys $)))


(defn process-file! [filepath delimiter-name]
  (let [file (io/as-file filepath)]
    (if (not (.exists file))
      (println (str "file './" filepath "' does not exist"))
      (let [file-as-str (slurp file)
            lines (clojure.string/split file-as-str #"\n")
            delimiter-regex (get delimiter-regexes (keyword delimiter-name))
            xform-results (as-> lines $
                            (map-indexed
                             (fn [idx line]
                               (-> line
                                   (line->map delimiter-regex)
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
            display-sorted-gender-lname (map rec->displayable sorted-by-gender-lname)
            sorted-by-dob (sort-by :birthdate demog-recs)
            display-sorted-dob (map rec->displayable sorted-by-dob)
            sorted-by-lname-desc (sort-by :last-name #(compare %2 %1) demog-recs)
            display-sorted-lname-desc (map rec->displayable sorted-by-lname-desc)
            _ (println "\n\nOutput 1 – sorted by gender (females before males) then by last name ascending")
            _ (pprint/print-table display-sorted-gender-lname)
            _ (println "\n\nOutput 2 – sorted by birth date, ascending")
            _ (pprint/print-table display-sorted-dob)
            _ (println "\n\nOutput 3 - sorted by last name, descending")
            _ (pprint/print-table display-sorted-lname-desc)
            ]
        nil))))
