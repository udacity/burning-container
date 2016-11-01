(ns burning-container.handler
  (:require
   [castra.middleware              :refer [wrap-castra]]
   [clojure.java.io                :as    io]
   [compojure.core                 :refer [defroutes GET]]
   [compojure.route                :refer [files resources not-found]]
   [ring.middleware.defaults       :refer [wrap-defaults api-defaults]]
   [ring.middleware.resource       :refer [wrap-resource]]
   [ring.middleware.session        :refer [wrap-session]]
   [ring.middleware.session.cookie :refer [cookie-store]]
   [ring.util.response             :refer [content-type resource-response]]
   [burning-container.config       :as    config]))

(defn wrap-no-cache
  "Middleware that adds no cache headers to response"
  [handler]
  (fn [request]
    (-> (handler request)
        (assoc-in [:headers "Cache-Control"] "no-store, must-revalidate")
        (assoc-in [:headers "Pragma"] "no-cache")
        (assoc-in [:headers "Expires"] "0"))))

(defroutes app-routes
  (GET "/" req
    (-> "index.html"
        (resource-response)
        (content-type "text/html")))
  (resources "/" {:root ""})
  (wrap-no-cache (files "/samples" {:root config/sample-files-base}))
  (not-found (or (io/resource "public/404.html")
                 "Oups! This page doesn't exist! (404 error)")))

(def app
  (-> app-routes
      (wrap-castra 'burning-container.api)
      (wrap-session {:store (cookie-store "a 16-byte secret")})
      (wrap-defaults api-defaults)
      (wrap-resource "public")))
