(ns demo-gr-aphics.core-test
  (:require [clojure.test :refer :all]
            [demo-gr-aphics.core :refer :all]
            [java-time :as time]))

;; (deftest a-test
;;   (testing "FIXME, I fail."
;;     (is (= 0 1))))

(deftest line-to-map-test
  (testing "success"
    (let [res (line-to-map "lname | fname | m | blue | 2013-03-04" (:pipe delimiter-regexes))
          expecting {:result {:last-name "lname"
                              :first-name "fname"
                              :gender :m
                              :favorite-color "blue"
                              :birthdate (time/local-date "2013-03-04")}
                     :error nil}]
      (is (= res expecting))))
  (testing "general fail"
    (let [actual (line-to-map "lname" (:pipe delimiter-regexes))]
      (is (nil? (:result actual)))
      (is (not (nil? (:error actual))))))
  (testing "fail - gender invalid"
    (let [actual (line-to-map "lname | fname | g | red | 2013-02-03" (:pipe delimiter-regexes))
          ;; _ (clojure.pprint/pprint actual)
          problems (-> actual :error :spec-explain-data :clojure.spec.alpha/problems)]
      (is (= (count problems) 1))
      
      (let [problem (first problems)
                ;; _ (clojure.pprint/pprint problem)
                ]
            (is (= (:path problem) [:gender])))))
  (testing "fail - dob invalid"
    (let [actual (line-to-map "lname | fname | f | red | wrong" (:pipe delimiter-regexes))
          problems (-> actual :error :spec-explain-data :clojure.spec.alpha/problems)]
      (is (= (count problems) 1))
      (let [problem (first problems)
                ;; _ (clojure.pprint/pprint problem)
                ;; _ (println (println (str "pred type: " (type (:pred problem)))))
                ]
            (is (= (:path problem) [:birthdate]))
            (is (= (:pred problem) 'demo-gr-aphics.core/will-coerce-to-local-date?)))))
  (testing "fail - dob invalid 2"
    (let [actual (line-to-map "lname | fname | f | red | 2018-99-99" (:pipe delimiter-regexes))
          problems (-> actual :error :spec-explain-data :clojure.spec.alpha/problems)]
      (is (= (count problems) 1))
          (let [problem (first problems)
                ;; _ (clojure.pprint/pprint problem)
                ;; _ (println (println (str "pred type: " (type (:pred problem)))))
                ]
            (is (= (:path problem) [:birthdate]))
            (is (= (:pred problem) 'demo-gr-aphics.core/will-coerce-to-local-date?))))
)
  )
