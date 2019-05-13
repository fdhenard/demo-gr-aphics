(ns demo-gr-aphics.core-test
  (:require [clojure.test :refer :all]
            [demo-gr-aphics.core :refer :all]
            [java-time :as time]))

(defn get-problems-from-line-map [line-map]
  (get-in line-map [:spec-explain-data :clojure.spec.alpha/problems]))

(deftest line->canonical-or-error-map-test
  (testing "success"
    (let [res (line->canonical-or-error-map "lname | fname | m | blue | 2013-03-04" pipe)
          expecting {:type :demog-rec
                     :last-name "lname"
                     :first-name "fname"
                     :gender :m
                     :favorite-color "blue"
                     :birthdate (time/local-date "2013-03-04")}]
      (is (= res expecting))))
  (testing "general fail"
    (let [actual (line->canonical-or-error-map "lname" pipe)]
      (is (= :error (:type actual)))))
  (testing "fail - gender invalid"
    (let [actual (line->canonical-or-error-map "lname | fname | g | red | 2013-02-03" pipe)
          ;; _ (clojure.pprint/pprint actual)
          problems (get-problems-from-line-map actual)]
      (is (and (not (nil? problems)) (= (count problems) 1)))
      (let [problem (first problems)
            ;; _ (clojure.pprint/pprint problem)
            ]
        (is (= (:path problem) [:gender])))))
  (testing "fail - dob invalid"
    (let [actual (line->canonical-or-error-map "lname | fname | f | red | wrong" pipe)
          problems (get-problems-from-line-map actual)]
      (is (and (not (nil? problems)) (= (count problems) 1)))
      (let [problem (first problems)
                ;; _ (clojure.pprint/pprint problem)
                ;; _ (println (println (str "pred type: " (type (:pred problem)))))
                ]
            (is (= (:path problem) [:birthdate]))
            (is (= (:pred problem) 'demo-gr-aphics.core/will-coerce-to-local-date?)))))
  (testing "fail - dob invalid 2"
    (let [actual (line->canonical-or-error-map "lname | fname | f | red | 2018-99-99" pipe)
          problems (get-problems-from-line-map actual)]
      (is (= (and (not (nil? problems)) (count problems)) 1))
          (let [problem (first problems)
                ;; _ (clojure.pprint/pprint problem)
                ;; _ (println (println (str "pred type: " (type (:pred problem)))))
                ]
            (is (= (:path problem) [:birthdate]))
            (is (= (:pred problem) 'demo-gr-aphics.core/will-coerce-to-local-date?)))))
  )
