(ns demo-gr-aphics.web
  (:require [clojure.walk :as walk]
            [clojure.string :as str]
            [clojure.spec.alpha :as spec]
            [mount.core :as mount]
            [reitit.ring :as ring]
            [reitit.coercion.spec]
            [expound.alpha :as expound]
            [ring.adapter.jetty :as jetty]
            [ring.util.request :as ring-util-req]
            [ring.middleware.json :as json-middleware]
            [demo-gr-aphics.core :as core]))

(def demog-recs
  "runtime state (db) of all the demographic records added"
  (atom []))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; clojure.spec for the POST request
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(spec/def ::single-line? #(not (.contains (str/trim %) "\n")))
(expound/defmsg ::single-line? "return characters not allowed in body for this endpoint")
(spec/def :post-demog-rec/body (spec/and string? ::single-line?))
(spec/def :post-demog-rec/delimiter (spec/and string? core/DELIMITER_CHOICES))
(expound/defmsg :post-demog-rec/delimiter (str "should be one of " core/DELIMITER_CHOICES))
(spec/def :post-demog-rec/headers (spec/keys :req-un [:post-demog-rec/delimiter]))

(spec/def ::post-demog-rec-request (spec/keys :req-un [:post-demog-rec/body
                                                       :post-demog-rec/headers]))

(defn keywordize-headers [request]
  (assoc request :headers (-> request :headers walk/keywordize-keys)))

(defn post-demog-rec!
  "post function to add demographic record"
  [request]
  (let [request (keywordize-headers request)]
    (if-not (spec/valid? ::post-demog-rec-request request)
      (let [message (expound/expound-str ::post-demog-rec-request request)
            #_ (println message)]
        {:status 400
         :body {:message message}})
      (let [#_ (pprint/pprint request)
            delimiter-kw (keyword (get-in request [:headers :delimiter]))
            #_ (println "delimiter-kw" )
            delimiter-re (get core/DELIMITER_REGEXES delimiter-kw)
            body (-> request ring-util-req/body-string str/trim)
            {:keys [type] :as xform-res} (core/line->canonical-or-error-map body delimiter-re)]
        (case type
          :error
          {:status 400
           :body xform-res}
          :demog-rec
          (do
            (swap! demog-recs conj (dissoc xform-res :type))
            {:status 201
             :body {:success true}})
          (throw (ex-info (str "programming error - unexpected type") {:type type})))))))

(defn get-demog-recs-sorted
  "a shared endpoint function for the GET endpoints and their corresponding sorting orders"
  [sort-by-key-fn _request]
  (let [sorted (sort-by sort-by-key-fn @demog-recs)]
    {:status 200
     :body {:result (map core/canonical->displayable sorted)}}))

(defn wrap-body-string
  "convert the body to a string if it is not.  Does no transformation if the body is already a string which is helpful for automated testing of endpoints through the router"
  [handler]
  (fn [{:keys [body] :as request}]
    (if (string? body)
      (handler request)
      (let [body-str (ring-util-req/body-string request)]
        (handler (assoc request :body body-str))))))

(defn wrap-base
  "some ring middleware"
  [handler]
  (-> handler
      json-middleware/wrap-json-response))

(def router
  (ring/router
   [["/records"
     ["" {:post {:handler post-demog-rec!
                 :middleware [wrap-body-string]}}]
     ["/gender" {:get {:handler (partial get-demog-recs-sorted :gender)}}]
     ["/birthdate" {:get {:handler (partial get-demog-recs-sorted :birthdate)}}]
     ["/name" {:get {:handler (partial get-demog-recs-sorted (juxt :last-name :first-name))}}]]
    ["/ping" {:get {:handler (constantly {:status 200
                                          :body "pong"
                                          :headers {"Content-Type" "text/html"}})}}]]))

(def app (wrap-base
          (ring/ring-handler
           router
           (ring/create-default-handler))))

(mount/defstate webserver
  :start (jetty/run-jetty app {:port (:port (mount/args))
                               :join? false})
  :stop (.stop webserver))
