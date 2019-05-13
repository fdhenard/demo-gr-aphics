(ns demo-gr-aphics.file-test
  (:require [clojure.test :refer :all]
            [demo-gr-aphics.file :refer :all]
            [demo-gr-aphics.core :as core]))

(deftest lines->canonical-or-error-maps-test
  (let [lines ["lname fname f indigo 1988-10-29"
               "what"]
        actual (lines->canonical-or-error-maps lines core/space)
        ;; _ (clojure.pprint/pprint actual)
        ]
    (is (= 1 (count (:demog-rec actual))))
    (is (= 1 (count (:error actual))))))

(deftest canonical->displayable-for-file-processing-test
  (let [demog-rec (core/line->canonical-or-error-map "lname fname f indigo 1988-10-29" core/space) 
        actual (canonical->displayable-for-file-processing demog-rec)
        ;; _ (clojure.pprint/pprint actual)
        ]
    (is (= actual {"last-name" "lname"
                   "first-name" "fname"
                   "gender" "Female"
                   "favorite-color" "indigo"
                   "birthdate" "10/29/1988"}))))

(deftest get-display-sorted-test
  (let [lines ["lname1 fname1 m red 1990-11-08"
               "lname2 fname2 f green 1980-01-13"]
        demog-recs (-> lines
                 (lines->canonical-or-error-maps core/space)
                 :demog-rec)
        ;; _ (clojure.pprint/pprint demog-recs)
        actual (get-display-sorted demog-recs #(sort-by :last-name sort-reverse %))
        ;; _ (clojure.pprint/pprint actual)
        ]
    (is (= "lname2" (-> actual first (get "last-name"))))
    (is (= "lname1" (-> actual last (get "last-name"))))))

(deftest process-file-test
  (is (nil? (process-file! "initial-file-test.csv" "pipe"))))
