(ns burning-container.api
  (:require [castra.core :refer [defrpc *session*]]
            [burning-container.config :as config]
            [burning-container.docker :as docker]
            [burning-container.flames :as flames]
            [clojure.string :as str])
  (:import [java.io File]
           [java.util UUID]))

(defn session-id []
  (:id (swap! *session* update-in [:id] #(or % (UUID/randomUUID)))))

(defn ensure-exists! [^String path]
  (.mkdirs (File. path)))

(defn parse-long [str default]
  (try
    (Long/parseLong str)
    (catch Exception _
      default)))

(defn samples-dir [cid]
  (let [d (str config/sample-files-base "/" (session-id) "/" cid)]
    (ensure-exists! d)
    d))

(defrpc sample-container [cid sample-rate-str sample-duration-str]
  (let [{:keys [image data]}
        (flames/sample-container cid
                                 (parse-long sample-rate-str sample-rate-str)
                                 (parse-long sample-duration-str sample-duration-str)
                                 (samples-dir cid))]
    {:image (str/replace image config/sample-files-base "samples")
     :data (str/replace image config/sample-files-base "samples")}))

(defrpc get-state []
  {:session (session-id)
   :docker-containers (docker/list-containers)})
