(ns demo-gr-aphics.web
  (:require [mount.core :as mount]
            [ring.adapter.jetty :as jetty]
            [reitit.ring :as ring]
            [demo-gr-aphics.core :as core]))

(def demog-recs (atom []))

(defn post-demog-rec [request]
  (swap! demog-recs conj (core/line->map "lname fname m red 2014-10-10" 1 (:space core/delimiter-regexes))))

(def router
  (ring/router
   [["/" {:get (fn [req] {:status 200
                          :headers {"Content-Type" "text/html"}
                          :body "Hello World"})}]
    ["/ping" {:get (fn [req] {:status 200
                              :headers {"Content-Type" "text/html"}
                              :body "Pong"})}]
    ["/records" {:post post-demog-rec}]]))

(def app (ring/ring-handler
          router
          (ring/create-default-handler)))

(mount/defstate webserver
  :start (jetty/run-jetty app {:port 3000
                               :join? false})
  :stop (.stop webserver))
