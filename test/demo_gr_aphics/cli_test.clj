(ns demo-gr-aphics.cli-test
  (:require [clojure.test :as t]
            [clojure.pprint :as pp]
            [demo-gr-aphics.cli :as ut]))

(t/deftest validate-args-nil-test
  (let [actual (ut/validate-args nil)
        #_ (pp/pprint actual)]
    (t/is (contains? actual :exit-message))))

(t/deftest validate-args-file-run-test
  (let [actual (ut/validate-args ["fpath.txt" "space"])
        #_ (pp/pprint actual)]
    (t/is (= actual {:filepath "fpath.txt"
                     :delimiter "space"}))))

(t/deftest validate-args-webserver-test
  (let [actual (ut/validate-args ["webserver" "--port" "3001"])
        #_ (pp/pprint actual)]
    (t/is (= actual {:webserver? true :port 3001}))))

(t/deftest validate-args-webserver-default-port-test
  (let [actual (ut/validate-args ["webserver"])
        #_ (pp/pprint actual)]
    (t/is (= actual {:webserver? true :port 3000}))))

(t/deftest validate-args-error-test
  (let [actual (ut/validate-args ["webserver" "--what"])
        #_ (pp/pprint actual)]
    (t/is (contains? actual :exit-message))
    (t/is (.contains (:exit-message actual) "errors"))))
