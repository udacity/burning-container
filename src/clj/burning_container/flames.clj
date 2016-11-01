(ns burning-container.flames
  (:require [clojure.string :as str]
            [clojure.java.io :as io]
            [me.raynes.conch :refer [programs with-programs let-programs] :as sh]
            [burning-container.docker :as docker]))

(programs sudo grep)

(defn mk-flame-graph! [& {:keys [title pids rate-hz duration-s work-dir] :as opts}]
  (sudo 'perf 'record
        '-F rate-hz
        '-o "perf.data"
        '-p (str/join "," pids)
        '-g '-- 'sleep duration-s
        {:dir work-dir})
  (with-open [kernel-svg (io/writer (str work-dir "/kernel.svg"))]
    (let-programs [stackcollapse "stackcollapse-perf.pl"
                   flamegraph "flamegraph.pl"]
      (flamegraph
       '--title (or title "Flamegraph")
       {:out kernel-svg
        :in (grep '-v "perf_\\|\\[perf"
             {:in (stackcollapse
                   {:in (sudo 'perf 'script '-f '-i "perf.data" {:dir work-dir})})})}))))

(defn sample-container [cid rate-hz duration-s dir]
  (let [cname (:Name (docker/api-get "containers/" cid "/json"))
        title (str cname " - Hz: " rate-hz ", s: " duration-s)]
    (mk-flame-graph! :pids (docker/container-pids cid)
                     :title title
                     :rate-hz rate-hz
                     :duration-s duration-s
                     :work-dir dir)
    (zipmap [:image :data] (map #(str dir "/" %) ["kernel.svg" "perf.data"]))))
