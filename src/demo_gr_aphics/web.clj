(ns demo-gr-aphics.web
  (:require [mount.core :as mount]
            [ring.adapter.jetty :as jetty]
            [reitit.ring :as ring]
            [ring.util.request :as ring-util-req]
            [demo-gr-aphics.core :as core]
            [clojure.spec.alpha :as spec]
            [expound.alpha :as expound]))

(def demog-recs (atom []))

(spec/def :post-demog-rec/body string?)
(spec/def :post-demog-rec/delimiter (spec/and string? core/delimiter-choices))
(spec/def :post-demog-rec/headers (spec/keys :req-un [:post-demog-rec/delimiter]))

(spec/def ::post-demog-rec-request (spec/keys :req-un [:post-demog-rec/body
                                                       :post-demog-rec/headers]))

(defn post-demog-rec! [request]
  (if-let [explain-req (spec/explain-data ::post-demog-rec-request request)]
    (do
      ;; (clojure.pprint/pprint explain-req)
      {:status 400
       :body {:message (expound/expound-str ::post-demog-rec-request request)}})
    (let [ ;; _ (clojure.pprint/pprint request)
          delimiter-kw (keyword (get-in request [:headers :delimiter]))
          delimiter-re (get core/delimiter-regexes delimiter-kw)
          xform-res (core/line->map
                     (ring-util-req/body-string request)
                     delimiter-re)]
      (if-let [error (:error xform-res)]
        {:status 400
         :body error}
        (do
          (swap! demog-recs conj (:result xform-res))
          {:status 201
           :body {:success true}})))))

(def router
  (ring/router
   [["/" {:get (fn [req] {:status 200
                          :headers {"Content-Type" "text/html"}
                          :body "Hello World"})}]
    ["/ping" {:get (fn [req] {:status 200
                              :headers {"Content-Type" "text/html"}
                              :body "Pong"})}]
    ["/records" {:post {:handler post-demog-rec!}}]]))

(def app (ring/ring-handler
          router
          (ring/create-default-handler)))

(mount/defstate webserver
  :start (jetty/run-jetty app {:port 3000
                               :join? false})
  :stop (.stop webserver))
