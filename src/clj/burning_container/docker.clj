(ns burning-container.docker
  (:require [clojure.string :as str]
            [me.raynes.conch :refer [programs with-programs let-programs] :as sh]
            [cheshire.core :as json]))

(programs curl)

(defn api-get [& api]
  (-> (curl '-s '--unix-socket "/var/run/docker.sock" (apply str "http:/" api))
      (json/parse-string true)))

(defn container-pids [cid]
  (->> (api-get "containers/" cid "/top")
       :Processes
       (map second)))

(defn list-containers []
  (api-get 'containers/json))
