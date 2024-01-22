(ns kit.mgl-chat.web.controllers.community
  (:require
   [kit.mgl-chat.web.utils.db :as db])
  (:import
   [java.util UUID]))

(defn new-community
  [conn uinfo params]
  (db/insert!
   conn
   :communities
   (assoc params :user_id (UUID/fromString (:id uinfo))))
  {})

(defn delete-community
  [conn uinfo id]
  (db/delete!
   conn
   :communities
   {:id (UUID/fromString id)
    :user_id (UUID/fromString (:id uinfo))}))

(defn update-community
  [conn uinfo id params]
  (db/update!
   conn
   :communities
   params
   {:id (UUID/fromString id)
    :user_id (UUID/fromString (:id uinfo))}))

(defn query-communities
  [conn params]
  (let [limit (or (:limit params) 20)
        offset (or (:offset params) 0)]
    (db/find-by-keys
     conn
     :communities
     :all
     {:order-by [[:created_at :desc]] 
      :offset offset :fetch limit})))