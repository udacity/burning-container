(ns burning-container.core
  (:require
    [burning-container.handler :as handler]
    [ring.adapter.jetty :refer [run-jetty]]))

(def server (atom nil))

(defn app [port public-path]
  (run-jetty handler/app {:join? false :port port}))

(defn start-server
  "Start castra demo server (port 33333)."
  [port public-path]
  (swap! server #(or % (app port public-path))))
