(set-env!
  :dependencies '[[adzerk/boot-cljs          "1.7.228-2"]
                  [adzerk/boot-reload        "0.4.13"]
                  [compojure                 "1.6.0-beta1"]
                  [hoplon/boot-hoplon        "0.3.0"]
                  [hoplon/castra             "3.0.0-SNAPSHOT"]
                  [hoplon/hoplon             "6.0.0-alpha16"]
                  [org.clojure/clojure       "1.8.0"]
                  [org.clojure/clojurescript "1.9.293"]
                  [pandeiro/boot-http        "0.7.3"]
                  [ring/ring-core            "1.6.0-beta6"]
                  [ring/ring-defaults        "0.3.0-beta1"]
                  [cheshire                  "5.6.3"]
                  [me.raynes/conch           "0.8.0"]]
  :resource-paths #{"resources" "src/clj"}
  :source-paths   #{"src/cljs" "src/hl"})

(require
  '[adzerk.boot-cljs      :refer [cljs]]
  '[adzerk.boot-reload    :refer [reload]]
  '[hoplon.boot-hoplon    :refer [hoplon prerender]]
  '[pandeiro.boot-http    :refer [serve]])

(deftask dev
  "Build burning-container for local development."
  []
  (comp
    (serve
      :port    8000
      :handler 'burning-container.handler/app
      :nrepl {:port 3001}
      :reload  true)
    (watch)
    (speak)
    (hoplon)
    (reload)
    (cljs)))

(deftask run
  "Build burning-container for local development."
  []
  (comp
   (serve :port 8000 :handler 'burning-container.handler/app)
   (hoplon)
   (cljs :optimizations :advanced)
   (wait)))

(deftask prod
  "Build burning-container for production deployment."
  []
  (comp
    (hoplon)
    (cljs :optimizations :advanced)
    (prerender)))

(deftask make-war
  "Build a war for deployment"
  []
  (comp (hoplon)
        (cljs :optimizations :advanced)
        (uber :as-jars true)
        (web :serve 'burning-container.handler/app)
        (war)
        (target :dir #{"target"})))
