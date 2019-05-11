(ns demo-gr-aphics.web
  (:require [mount.core :as mount]
            [ring.adapter.jetty :as jetty]
            [reitit.ring :as ring]))

(def demog-recs (atom []))

(def router
  (ring/router
   [["/" {:get (fn [req] {:status 200
                          :headers {"Content-Type" "text/html"}
                          :body "Hello World"})}]
    ["/ping" {:get (fn [req] {:status 200
                              :headers {"Content-Type" "text/html"}
                              :body "Pong"})}]]))

(def app (ring/ring-handler
          router
          (ring/create-default-handler)))

(mount/defstate webserver
  :start (jetty/run-jetty app {:port 3000
                               :join? false})
  :stop (.stop webserver))
