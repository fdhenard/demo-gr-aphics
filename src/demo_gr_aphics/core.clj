(ns demo-gr-aphics.core
  (:require [clojure.string :as str]
            [java-time :as time]
            [clojure.spec.alpha :as spec]
            [expound.alpha :as expound]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; clojure.spec for validating the demographic record coming in from the
;; file or from web
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; finally the demographic record spec for incoming records (not canonical)
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(spec/def ::demographic-record (spec/keys :req-un [::last-name ::first-name ::gender ::favorite-color ::birthdate]))

(def pipe #"\ \|\ ")
(def comma #",\ ")
(def space #"\ ")
(def delimiter-regexes {:pipe pipe
                        :comma comma
                        :space space})
(def delimiter-choices (->> delimiter-regexes
                            keys
                            (map name)
                            set))


(defn line->canonical-or-error-map
  "transform a line to either the canonical demographic record or an error map"
  [line delimiter-re]
  (let [[lname fname gender fav-color dob] (map str/trim (str/split line delimiter-re))
        demographic-record {:last-name lname
                            :first-name fname
                            :gender gender
                            :favorite-color fav-color
                            :birthdate dob}]
    (if-let [spec-explain-data (spec/explain-data ::demographic-record demographic-record)]
      {:type :error
       :spec-explain-data spec-explain-data
       :spec-explain-str (spec/explain-str ::demographic-record demographic-record)
       :spec-expound-str (expound/expound-str ::demographic-record demographic-record)
       :line line}
      {:type :demog-rec
       :last-name lname
       :first-name fname
       :gender (get gender-options-map (str/lower-case gender))
       :favorite-color fav-color
       :birthdate (time/local-date dob)})))

(defn canonical->displayable
  "transform a canonical demographic record to one that is displayable as is commonly needed for file processing and web processing"
  [demog-rec]
  (as-> demog-rec $
    (dissoc $ :type)
    (assoc $ :birthdate (time/format "M/d/YYYY" (:birthdate $)))))
