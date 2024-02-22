(ns kit.mgl-chat.web.utils.job
  (:require
   [chime.core :as chime]
   [kit.mgl-chat.web.utils.db :as db]
   [clojure.tools.logging :as log])
  (:import 
   [java.time Instant]))

(defn send-notification
  [message]
  (let [now (Instant/now)]
    (chime/chime-at [(.plusSeconds now 0)]
                    (fn [time]
                      (log/warn "Chiming at" time)))))



