(ns kit.mgl-chat.web.controllers.channel
  (:require
   [kit.mgl-chat.web.utils.db :as db]
   [kit.mgl-chat.web.utils.job :as job])
  (:import
   [java.util UUID]))

(defn new-channel
  [conn uinfo params]
  (db/insert!
   conn
   :channels
   (assoc params 
          :comm_id (UUID/fromString (:comm_id params))
          :created_by (UUID/fromString (:id uinfo))))
  {})

(defn delete-channel
  [conn id]
  (db/delete!
   conn
   :channels
   {:id (UUID/fromString id)})
  {})

(defn update-channel
  [conn id params]
  (db/update!
   conn
   :channels
   params
   {:id (UUID/fromString id)})
  {})

(defn query-channels
  [conn id params]
  (db/find-by-keys
   conn
   :channels
   (assoc params :comm_id id)))

(defn wrap-channel
  [opts handler]
  (fn [{{{cid :cid} :path} :parameters :as ctx}]
    (if-some [channel (db/find-one-by-keys (:db-conn opts) :channels {:id (UUID/fromString cid)})]
      (handler (assoc ctx :channel channel))
      {:status 404
       :body {}})))

(defn message 
  [conn uinfo cid params]
  (db/insert! 
   conn
   :messages
   {:from_user_id (UUID/fromString (:id uinfo))
    :channel_id (UUID/fromString cid)
    :content (:content params)})
  (job/send-notification 
   {:from_user_id (:id uinfo)
    :channel_id cid
    :content (:content params)})
  {})