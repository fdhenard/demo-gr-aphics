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
    (is (= actual
           {:status 201
            :body {:success true}}))))

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
