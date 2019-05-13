(ns demo-gr-aphics.web
  (:require [mount.core :as mount]
            [ring.adapter.jetty :as jetty]
            [reitit.ring :as ring]
            [ring.util.request :as ring-util-req]
            [demo-gr-aphics.core :as core]
            [clojure.spec.alpha :as spec]
            [expound.alpha :as expound]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [ring.middleware.json :as json-middleware]
            [reitit.coercion.spec]
            [reitit.ring.coercion :as rrc]
            [ring.middleware.defaults :as mw-defaults]
            [clojure.string :as str]))

(def demog-recs (atom []))

(spec/def ::single-line? #(not (.contains (str/trim %) "\n")))
(expound/defmsg ::single-line? "return characters not allowed in body for this endpoint")
(spec/def :post-demog-rec/body (spec/and string? ::single-line?))
(spec/def :post-demog-rec/delimiter (spec/and string? core/delimiter-choices))
(expound/defmsg :post-demog-rec/delimiter (str "should be one of " core/delimiter-choices))
(spec/def :post-demog-rec/headers (spec/keys :req-un [:post-demog-rec/delimiter]))

(spec/def ::post-demog-rec-request (spec/keys :req-un [:post-demog-rec/body
                                                       :post-demog-rec/headers]))

(defn keywordize-headers [request]
  (assoc request :headers (-> request :headers clojure.walk/keywordize-keys)))

(defn post-demog-rec! [request]
  (let [request (keywordize-headers request)]
    (if-let [explain-req (spec/explain-data ::post-demog-rec-request request)]
      (let [message (expound/expound-str ::post-demog-rec-request request)
            ;; _ (println message)
            ]
        {:status 400
         :body {:message message}})
      (let [;; _ (clojure.pprint/pprint request)
            delimiter-kw (keyword (get-in request [:headers :delimiter]))
            ;; _ (println "delimiter-kw" )
            delimiter-re (get core/delimiter-regexes delimiter-kw)
            body (-> request ring-util-req/body-string str/trim)
            xform-res (core/line->canonical-or-error-map body delimiter-re)]
        (case (:type xform-res)
          :error
          {:status 400
           :body xform-res}
          :demog-rec
          (do
            (swap! demog-recs conj (dissoc xform-res :type))
            {:status 201
             :body {:success true}})
          (throw (Exception. (str "programming error - unexpected type of '" (:type xform-res) "'"))))))))

(defn get-demog-recs-sorted [sort-by-key-fn request]
  (let [sorted (sort-by sort-by-key-fn @demog-recs)]
    {:status 200
     :body {:result (map core/canonical->displayable sorted)}}))

(defn wrap-body-string [handler]
  (fn [request]
    (if (string? (:body request))
      (handler request)
      (let [body-str (ring.util.request/body-string request)]
        (handler (assoc request :body body-str))))))

(defn wrap-base [handler]
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
                                          :body "ok"
                                          :headers {"Content-Type" "text/html"}})}}]]))

(def app (wrap-base
          (ring/ring-handler
           router
           (ring/create-default-handler))))

(mount/defstate webserver
  :start (jetty/run-jetty app {:port (:port (mount/args))
                               :join? false})
  :stop (.stop webserver))
