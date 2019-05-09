(ns demo-gr-aphics.core
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [java-time :as time]
            [clojure.spec.alpha :as spec]
            [clojure.pprint :as pprint]))

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
(spec/def ::date-string (spec/and ::non-blank-string will-coerce-to-local-date?))
(spec/def ::birthdate ::date-string)
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


(defn line-to-map [line delimiter-re]
  (let [[lname fname gender fav-color dob] (str/split line delimiter-re)
        demographic-record {:last-name lname
                            :first-name fname
                            :gender gender
                            :favorite-color fav-color
                            :birthdate dob}
        spec-explain-data (spec/explain-data ::demographic-record demographic-record)
        spec-explain-str (spec/explain-str ::demographic-record demographic-record)
        ]
    (if (not (nil? spec-explain-data))
      ;; (throw (ex-info "invalid demographic record"
      ;;                 {:spec-explain-data spec-explain-data
      ;;                  :spec-explain-str spec-explain-str}))
      {:error {:spec-explain-data spec-explain-data
               :spec-explain-str spec-explain-str}
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


(defn process-file [filepath delimiter-name]
  (let [file-as-str (slurp filepath)
        lines (clojure.string/split file-as-str #"\n")
        delimiter-regex (get delimiter-regexes (keyword delimiter-name))
        val-maps (as-> lines $
                   (map
                    #(-> %
                         (line-to-map delimiter-regex)
                         :result)
                    $)
                   (remove nil? $))
        ;; sorted (sort-by (juxt :gender :last-name) val-maps)
        ;; sorted (sort-by :birthdate val-maps)
        sorted (sort-by :last-name #(compare %2 %1) val-maps)
        displayable (map
                     #(as-> % $
                        (assoc $ :gender (get gender-to-display (:gender $)))
                        (assoc $ :birthdate (time/format "M/d/YYYY" (:birthdate $)))
                        (clojure.walk/stringify-keys $))
                     sorted)
        _ (pprint/print-table displayable)
        ]
    nil))
