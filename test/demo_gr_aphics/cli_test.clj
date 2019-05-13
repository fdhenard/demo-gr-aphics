(ns demo-gr-aphics.cli-test
  (:require [clojure.test :refer :all]
            [demo-gr-aphics.cli :refer :all]))

(deftest validate-args-nil-test
  (let [actual (validate-args nil)
        ;; _ (clojure.pprint/pprint actual)
        ]
    (is (contains? actual :exit-message))))

(deftest validate-args-file-run-test
  (let [actual (validate-args ["fpath.txt" "space"])
        ;; _ (clojure.pprint/pprint actual)
        ]
    (is (= actual {:filepath "fpath.txt"
                   :delimiter "space"}))))

(deftest validate-args-webserver-test
  (let [actual (validate-args ["webserver"])
        ;; _ (clojure.pprint/pprint actual)
        ]
    (is (= actual {:webserver? true}))))

(deftest validate-args-error-test
  (let [actual (validate-args ["webserver" "--what"])
        ;; _ (clojure.pprint/pprint actual)
        ]
    (is (contains? actual :exit-message))
    (is (.contains (:exit-message actual) "errors"))))
