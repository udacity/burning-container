(ns burning-container.rpc
  (:require-macros
   [javelin.core :refer [defc defc=]])
  (:require
   [javelin.core :as j :refer [cell]]
   [castra.core :refer [mkremote]]))

(defc state nil)
(defc= container-list (get state :docker-containers))

(defc sample nil)
(defc= image-url (get sample :image))
(defc error nil)
(defc= error-message (when error (.-message error)))
(defc loading [])
(defc= loading? (seq loading))

(defc sample-rate 99)
(defc sample-duration 60)

(def get-state
  (mkremote 'burning-container.api/get-state state (cell nil) (cell nil)))
(def sample-container*
  (mkremote 'burning-container.api/sample-container sample error loading))

(def clear-error! #(reset! error nil))
(defn sample-container! [container-id]
  (reset! sample nil)
  (sample-container* container-id @sample-rate @sample-duration))

(defn init []
  (get-state)
  (js/setInterval get-state 5000))
