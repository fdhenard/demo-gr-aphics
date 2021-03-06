(ns demo-gr-aphics.web-test
  (:require [clojure.test :as t]
            [clojure.pprint :as pp]
            [clojure.data.json :as json]
            [demo-gr-aphics.web :as ut]))

(t/deftest post-success
  (t/testing "a good post of a new demographic record"
   (let [{:keys [status]} (ut/app {:request-method :post
                           :uri "/records"
                           :body "lname fname test@test.com green 2014-03-22"
                           :headers {:delimiter "space"}})]
     (t/is (= 201 status)))))

(t/deftest post-fail-no-body
  (t/testing "bad post - no body"
   (let [{:keys [status]} (ut/app {:request-method :post
                           :uri "/records"
                           ;; :body nil
                           :headers {:delimiter "space"}})]
     (t/is (= status 400)))))

(t/deftest post-fail-no-headers
  (t/testing "bad post - no headers"
   (let [{:keys [status]} (ut/app {:request-method :post
                           :uri "/records"
                           :body "lname fname test@test.com green 2014-03-22"})]
     (t/is (= status 400)))))

(t/deftest post-fail-bad-body
  (t/testing "bad post - invalid body"
   (let [{:keys [status]} (ut/app {:request-method :post
                           :uri "/records"
                           :body "what???"
                           :headers {:delimiter "space"}})]
       (t/is (= status 400)))))

(defn load-recs!
  "side effecting helper function for the get tests below"
  []
  (letfn [(load-rec-helper! [line delim]
            (ut/app {:request-method :post
                     :uri "/records"
                     :body line
                     :headers {:delimiter delim}}))]
    (reset! ut/demog-recs [])
    (load-rec-helper! "lname1 | fname1 | test@test.com | blue | 2012-03-09" "pipe")
    (load-rec-helper! "lname2 | fname2 | test1@test1.com | red | 2011-12-12" "pipe")
    (load-rec-helper! "lname3, fname3, test2@test2.com, indigo, 1988-01-18" "comma")
    (load-rec-helper! "lname4 fname4 test3@test3.com violet 2000-03-06" "space")))

(defn json->hashmap [str-in]
  (json/read-str str-in :key-fn keyword))

(t/deftest get-recs-sorted-by-name-success
  (t/testing "get success - sort by name"
   (let [_ (load-recs!)
         actual (ut/app {:request-method :get
                         :uri "/records/name"})
         #_ (pp/pprint actual)
         result (-> actual :body json->hashmap :result)
         _ (t/is (= 4 (count result)))
         first-one (first result)
         last-one (last result)]
     (t/is (= "lname1" (:last-name first-one)))
     (t/is (= "fname1" (:first-name first-one)))
     (t/is (= "lname4" (:last-name last-one)))
     (t/is (= "fname4" (:first-name last-one))))))

(t/deftest get-recs-sorted-by-birthdate-success
  (t/testing "get success - sort by birthdate"
   (let [_ (load-recs!)
         actual (ut/app {:request-method :get
                         :uri "/records/birthdate"})
         result (-> actual :body json->hashmap :result)
         _ (t/is (= 4 (count result)))
         first-one (first result)
         last-one (last result)]
     (t/is (= "1/18/1988" (:birthdate first-one)))
     (t/is (= "3/9/2012" (:birthdate last-one))))))

#_(t/deftest get-recs-sorted-by-gender-success
  (t/testing "get success - sort by gender"
   (let [_ (load-recs!)
         actual (ut/app {:request-method :get
                         :uri "/records/gender"})
         result (-> actual :body json->hashmap :result)
         _ (t/is (= 4 (count result)))
         genders (mapv :gender result)]
     (t/is (= ["f" "f" "m" "m"] genders)))))

(t/deftest get-recs-sorted-by-email-success
  (t/testing "get success - sort by email"
    (let [_ (load-recs!)
          actual (ut/app {:request-method :get
                          :uri "/records/email"})
          result (-> actual :body json->hashmap :result)
          _ (t/is (= 4 (count result)))
          emails (map :email result)
          #_ (pp/pprint {:emails emails})]
      (t/is (= '("test1@test1.com"
                 "test2@test2.com"
                 "test3@test3.com"
                 "test@test.com")
               emails)))))
