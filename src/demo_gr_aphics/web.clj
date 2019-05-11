(ns demo-gr-aphics.web
  (:require [mount.core :as mount]
            [ring.adapter.jetty :as jetty]))

(defn handler [request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body "Hello World"})

(mount/defstate webserver
  :start (jetty/run-jetty handler {:port 3000
                                   :join? false})
  :stop (.stop webserver))
