(ns kit.mgl-chat.web.controllers.channel
  (:require
   [kit.mgl-chat.web.utils.db :as db])
  (:import
   [java.util UUID]))

(defn new-channel
  [conn params]
  (db/insert!
   conn
   :channels
   params)
  {})

(defn delete-channel
  [conn id]
  (db/delete!
   conn
   :channels
   {:id (UUID/fromString id)}))

(defn update-channel
  [conn id params]
  (db/update!
   conn
   :channels
   params
   {:id (UUID/fromString id)}))
