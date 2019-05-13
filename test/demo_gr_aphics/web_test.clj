(ns demo-gr-aphics.web-test
  (:require [clojure.test :refer :all]
            [demo-gr-aphics.web :refer :all]))

(deftest post-success
  (let [actual (app {:request-method :post
                     :uri "/records"
                     :body "lname fname female green 2014-03-22"
                     :headers {:delimiter "space"}})
        ;; _ (clojure.pprint/pprint actual)
        ]
    (is (= 201 (:status actual)))))

(deftest post-fail-no-body
  (let [actual (app {:request-method :post
                     :uri "/records"
                     ;; :body nil
                     :headers {:delimiter "space"}})
        ;; _ (clojure.pprint/pprint actual)
        ]
    (is (= (:status actual) 400))))

(deftest post-fail-no-headers
  (let [actual (app {:request-method :post
                     :uri "/records"
                     :body "lname fname female green 2014-03-22"})]
    (is (= (:status actual) 400))))

(deftest post-fail-bad-body
  (let [actual (app {:request-method :post
                     :uri "/records"
                     :body "what???"
                     :headers {:delimiter "space"}})
        ;; _ (clojure.pprint/pprint actual)
        ;; _ (println (-> actual :body :spec-expound-str))
        ;; _ (println (-> actual :body :spec-explain-str))
        ]
    (is (= (:status actual) 400))))

(defn load-recs! []
  (letfn [(load-rec-helper! [line delim]
            (app {:request-method :post
                  :uri "/records"
                  :body line
                  :headers {:delimiter delim}}))]
    (reset! demog-recs [])
    (load-rec-helper! "lname1 | fname1 | male | blue | 2012-03-09" "pipe")
    (load-rec-helper! "lname2 | fname2 | f | red | 2011-12-12" "pipe")
    (load-rec-helper! "lname3, fname3, m, indigo, 1988-01-18" "comma")
    (load-rec-helper! "lname4 fname4 F violet 2000-03-06" "space")))

(deftest get-recs-sorted-by-name-success
  (let [_ (load-recs!)
        actual (app {:request-method :get
                     :uri "/records/name"})
        ;; _ (clojure.pprint/pprint actual)
        ]
    (is (= 4 (-> actual :result count)))
    (let [first-one (-> actual :result first)
          last-one (-> actual :result last)]
      (is (= "lname1" (:last-name first-one)))
      (is (= "fname1" (:first-name first-one)))
      (is (= "lname4" (:last-name last-one)))
      (is (= "fname4" (:first-name last-one))))))

(deftest get-recs-sorted-by-birthdate-success
  (let [_ (load-recs!)
        actual (app {:request-method :get
                     :uri "/records/birthdate"})]
    (is (= 4 (-> actual :result count)))
    (let [first-one (-> actual :result first)
          last-one (-> actual :result last)]
      (is (= "1/18/1988" (:birthdate first-one)))
      (is (= "3/9/2012" (:birthdate last-one))))))

(deftest get-recs-sorted-by-gender-success
  (let [_ (load-recs!)
        actual (app {:request-method :get
                     :uri "/records/gender"})]
    (is (= 4 (-> actual :result count)))
    (let [genders (mapv :gender (:result actual))]
      (is (= [:f :f :m :m] genders)))))
