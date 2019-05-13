(ns demo-gr-aphics.core
  (:require [clojure.string :as str]
            [java-time :as time]
            [clojure.spec.alpha :as spec]
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


(defn line->canonical-or-error-map [line delimiter-re]
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

(defn rec->displayable [demog-rec]
  (assoc demog-rec :birthdate (time/format "M/d/YYYY" (:birthdate demog-rec))))
