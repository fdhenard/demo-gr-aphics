(ns demo-gr-aphics.core-test
  (:require [clojure.test :as t]
            [clojure.pprint :as pp]
            [java-time :as time]
            [demo-gr-aphics.core :as ut]))

(defn get-problems-from-line-map [line-map]
  (get-in line-map [:spec-explain-data :clojure.spec.alpha/problems]))

(t/deftest line->canonical-or-error-map-test
  (t/testing "success"
    (let [res (ut/line->canonical-or-error-map "lname | fname | test@testing.com | blue | 2013-03-04" ut/PIPE)
          expecting {:type :demog-rec
                     :last-name "lname"
                     :first-name "fname"
                     :email "test@testing.com"
                     :favorite-color "blue"
                     :birthdate (time/local-date "2013-03-04")}]
      (t/is (= res expecting))))
  (t/testing "general fail"
    (let [{:keys [type]} (ut/line->canonical-or-error-map "lname" ut/PIPE)]
      (t/is (= :error type))))
  (t/testing "fail - email invalid"
    (let [actual (ut/line->canonical-or-error-map "lname | fname | not-an-email | red | 2013-02-03" ut/PIPE)
          #_ (pp/pprint actual)
          problems (get-problems-from-line-map actual)
          _ (t/is (and problems (= (count problems) 1)))
          {:keys [path]} (first problems)
            #_ (pp/pprint problem)]
      (t/is (= path [:email]))))
  (t/testing "fail - dob invalid"
    (let [actual (ut/line->canonical-or-error-map "lname | fname | test@test.com | red | wrong" ut/PIPE)
          problems (get-problems-from-line-map actual)
          _ (t/is (and problems (= (count problems) 1)))
          {:keys [path pred]} (first problems)
          #_ (pp/pprint problem)
          #_ (println (println (str "pred type: " (type (:pred problem)))))]
      (t/is (= path [:birthdate]))
      (t/is (= pred 'demo-gr-aphics.core/will-coerce-to-local-date?))))
  (t/testing "fail - dob invalid 2"
    (let [actual (ut/line->canonical-or-error-map "lname | fname | test@example.com | red | 2018-99-99" ut/PIPE)
          problems (get-problems-from-line-map actual)
          _ (t/is (and problems (= (count problems) 1)))
          #_ (pp/pprint problem)
          #_ (println (println (str "pred type: " (type (:pred problem)))))
          {:keys [path pred]} (first problems)]
      (t/is (= path [:birthdate]))
      (t/is (= pred 'demo-gr-aphics.core/will-coerce-to-local-date?)))))
