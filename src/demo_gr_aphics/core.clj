(ns demo-gr-aphics.core
  (:require [clojure.string :as str]
            [clojure.spec.alpha :as spec]
            [java-time :as time]
            [expound.alpha :as expound]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; clojure.spec for validating the demographic record coming in from the
;; file or from web
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn will-coerce-to-local-date? [x]
  (try
    (time/local-date x)
    true
    (catch clojure.lang.ExceptionInfo ei
      (let [as-map (Throwable->map ei)
            cause-type (-> as-map :via (get 1) :type)]
        (if (= cause-type 'java.time.format.DateTimeParseException)
          false
          (throw ei))))))

(defn is-date-lte-today? [x]
  (<= (compare (time/local-date x) (time/local-date)) 0))

(spec/def ::NonBlankString (spec/and string? #(not (str/blank? %))))
(spec/def ::last-name ::NonBlankString)
(spec/def ::first-name ::NonBlankString)
(def GENDER_OPTIONS_MAP {"m" :m
                         "male" :m
                         "f" :f
                         "female" :f})
(def GENDER_OPTIONS
  "combinations of 'm', 'male', 'f', 'female', capitalized, uppercased, and not transformed"
  (set (reduce
        (fn [accum item]
          (concat accum [item
                         (str/capitalize item)
                         (str/upper-case item)]))
        []
        (keys GENDER_OPTIONS_MAP))))
(spec/def ::gender GENDER_OPTIONS)
(expound/defmsg ::gender (str "should be one of " GENDER_OPTIONS))
(spec/def ::birthdate (spec/and
                       ::NonBlankString
                       will-coerce-to-local-date?
                       is-date-lte-today?))

;; https://sashat.me/2017/01/11/list-of-20-simple-distinct-colors/
;; + indigo
(spec/def ::Color #{"red" "maroon" "pink"
                     "brown" "orange" "apricot"
                     "olive" "yellow" "beige"
                     "lime" "green" "mint"
                     "teal" "cyan" "navy" "blue"
                     "indigo" "purple" "lavender" "magenta" "violet"
                     "black" "white" "grey"})
(spec/def ::favorite-color ::Color)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; finally the demographic record spec for incoming records (not canonical)
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(spec/def ::DemographicRecord (spec/keys :req-un [::last-name ::first-name ::gender ::favorite-color ::birthdate]))

(def PIPE #"\ \|\ ")
(def COMMA #",\ ")
(def SPACE #"\ ")
(def DELIMITER_REGEXES {:pipe PIPE
                        :comma COMMA
                        :space SPACE})
(def DELIMITER_CHOICES (->> DELIMITER_REGEXES
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
    (if-let [spec-explain-data (spec/explain-data ::DemographicRecord demographic-record)]
      {:type :error
       :spec-explain-data spec-explain-data
       :spec-explain-str (spec/explain-str ::DemographicRecord demographic-record)
       :spec-expound-str (expound/expound-str ::DemographicRecord demographic-record)
       :line line}
      {:type :demog-rec
       :last-name lname
       :first-name fname
       :gender (get GENDER_OPTIONS_MAP (str/lower-case gender))
       :favorite-color fav-color
       :birthdate (time/local-date dob)})))

(defn canonical->displayable
  "transform a canonical demographic record to one that is displayable as is commonly needed for file processing and web processing"
  [{:keys [birthdate] :as demog-rec}]
  (-> demog-rec
      (dissoc :type)
      (assoc :birthdate (time/format "M/d/YYYY" birthdate))))
