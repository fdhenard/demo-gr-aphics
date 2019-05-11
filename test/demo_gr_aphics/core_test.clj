(ns demo-gr-aphics.core-test
  (:require [clojure.test :refer :all]
            [demo-gr-aphics.core :refer :all]
            [java-time :as time]))

(defn get-problems-from-line-map [line-map]
  (get-in line-map [:error :spec-explain-data :clojure.spec.alpha/problems]))

(deftest line->map-test
  (testing "success"
    (let [res (line->map "lname | fname | m | blue | 2013-03-04" 1 (:pipe delimiter-regexes))
          expecting {:result {:last-name "lname"
                              :first-name "fname"
                              :gender :m
                              :favorite-color "blue"
                              :birthdate (time/local-date "2013-03-04")}
                     :error nil}]
      (is (= res expecting))))
  (testing "general fail"
    (let [actual (line->map "lname" 1 (:pipe delimiter-regexes))]
      (is (nil? (:result actual)))
      (is (not (nil? (:error actual))))))
  (testing "fail - gender invalid"
    (let [actual (line->map "lname | fname | g | red | 2013-02-03" 1 (:pipe delimiter-regexes))
          ;; _ (clojure.pprint/pprint actual)
          problems (get-problems-from-line-map actual)]
      (is (and (not (nil? problems)) (= (count problems) 1)))
      (let [problem (first problems)
            ;; _ (clojure.pprint/pprint problem)
            ]
        (is (= (:path problem) [:gender])))))
  (testing "fail - dob invalid"
    (let [actual (line->map "lname | fname | f | red | wrong" 1 (:pipe delimiter-regexes))
          problems (get-problems-from-line-map actual)]
      (is (and (not (nil? problems)) (= (count problems) 1)))
      (let [problem (first problems)
                ;; _ (clojure.pprint/pprint problem)
                ;; _ (println (println (str "pred type: " (type (:pred problem)))))
                ]
            (is (= (:path problem) [:birthdate]))
            (is (= (:pred problem) 'demo-gr-aphics.core/will-coerce-to-local-date?)))))
  (testing "fail - dob invalid 2"
    (let [actual (line->map "lname | fname | f | red | 2018-99-99" 1 (:pipe delimiter-regexes))
          problems (get-problems-from-line-map actual)]
      (is (= (and (not (nil? problems)) (count problems)) 1))
          (let [problem (first problems)
                ;; _ (clojure.pprint/pprint problem)
                ;; _ (println (println (str "pred type: " (type (:pred problem)))))
                ]
            (is (= (:path problem) [:birthdate]))
            (is (= (:pred problem) 'demo-gr-aphics.core/will-coerce-to-local-date?)))))
  )
