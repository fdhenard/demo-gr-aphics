(ns demo-gr-aphics.file-test
  (:require [clojure.test :as t]
            [clojure.pprint :as pp]
            [demo-gr-aphics.file :as ut]
            [demo-gr-aphics.core :as core]))

(t/deftest lines->canonical-or-error-maps-test
  (t/testing "a successful rec and an erroring rec"
   (let [lines ["lname fname test@test.com indigo 1988-10-29"
                "what"]
         {:keys [demog-rec error]} (ut/lines->canonical-or-error-maps lines core/SPACE)
         #_ (pp/pprint actual)]
     (t/is (= 1 (count demog-rec)))
     (t/is (= 1 (count error))))))

(t/deftest canonical->displayable-for-file-processing-test
  (t/testing "canonical to dt/isplayable for file processing transformation"
    (let [demog-rec (core/line->canonical-or-error-map "lname fname test@test.com indigo 1988-10-29" core/SPACE)
          actual (ut/canonical->displayable-for-file-processing demog-rec)
          #_ (pp/pprint actual)]
      (t/is (= actual {"last-name" "lname"
                       "first-name" "fname"
                       "email" "test@test.com"
                       "favorite-color" "indigo"
                       "birthdate" "10/29/1988"})))))

(t/deftest get-display-sorted-test
  (t/testing "sorting for file output"
   (let [lines ["lname1 fname1 test@test.com red 1990-11-08"
                "lname2 fname2 test2@test.com green 1980-01-13"]
         demog-recs (-> lines
                        (ut/lines->canonical-or-error-maps core/SPACE)
                        :demog-rec)
         #_ (pp/pprint demog-recs)
         actual (ut/get-display-sorted demog-recs #(sort-by :last-name ut/sort-reverse %))
         #_ (pp/pprint actual)]
     (t/is (= "lname2" (-> actual first (get "last-name"))))
     (t/is (= "lname1" (-> actual last (get "last-name")))))))

(t/deftest email-desc-lastname-asc-sort-func-test
  (t/testing "email desc"
   (let [actual (ut/email-desc-lastname-asc {:email "z"
                                             :last-name "a"}
                                            {:email "w"
                                             :last-name "a"})]
     (t/is actual)))
  (t/testing "email same, lastname asc"
    (let [actual (ut/email-desc-lastname-asc {:email "a"
                                              :last-name "a"}
                                             {:email "a"
                                              :last-name "b"})]
      (t/is actual)))
  (t/testing "email same, lastname desc"
    (let [actual (ut/email-desc-lastname-asc {:email "a"
                                              :last-name "b"}
                                             {:email "a"
                                              :last-name "a"})]
      (t/is (not actual))))
  (t/testing "email asc, lastname same"
    (let [actual (ut/email-desc-lastname-asc {:email "a"
                                              :last-name "a"}
                                             {:email "b"
                                              :last-name "a"})]
      (t/is (not actual)))))


;; process-file! t/is the remainder of the namespace that t/is not purely
;; functional (side effecting).  The data transformations take place in
;; the pure functions
(t/deftest process-file-test
  (t/testing "no errors in side effecting process-file! function"
   (t/is (nil? (ut/process-file! "resources/test-file.csv" "pipe")))))
