(ns kit.mgl-chat.web.utils.job
  (:require
   [chime.core :as chime]
   [kit.mgl-chat.web.utils.db :as db]
   [clojure.tools.logging :as log])
  (:import 
   [java.time Instant]
   [java.util UUID]))

(defn send-notification
  [conn message]
  (let [now (Instant/now)]
    (chime/chime-at
     [(.plusSeconds now 0)]
     (fn [time]
       (let [users (db/find-by-keys conn :memberships
                                    {:comm_id (UUID/fromString (:community_id message))}
                                    {:columns [:user_id]})]
         (doall
          (map #(ws/send-notification % (:content message)))))
       (log/warn "Chiming at" time)))))



