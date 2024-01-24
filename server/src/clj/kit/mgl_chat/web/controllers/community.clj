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

(defn wrap-community 
  [opts handler]
  (fn [{{{id :id} :path} :parameters :as ctx}]
    (if-some [community (db/find-one-by-keys (:db-conn opts) :communities {:id (UUID/fromString id)})]
      (handler (assoc ctx :community community))
      {:status 404
       :body {}})))