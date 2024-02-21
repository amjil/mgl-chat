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
   (assoc params :created_by (UUID/fromString (:id uinfo))))
  {})

(defn delete-community
  [conn uinfo id]
  (db/delete!
   conn
   :communities
   {:id (UUID/fromString id)
    :created_by (UUID/fromString (:id uinfo))})
  {})

(defn update-community
  [conn uinfo id params]
  (db/update!
   conn
   :communities
   params
   {:id (UUID/fromString id)
    :created_by (UUID/fromString (:id uinfo))})
  {})

(defn query-communities
  [query-fn uinfo]
  (query-fn :query-communities {:created_by (UUID/fromString (:id uinfo))}))

(defn join
  [conn uinfo id]
  (db/insert! conn :memberships {:user_id (UUID/fromString (:id uinfo))
                                 :comm_id (UUID/fromString id)})
  {})

(defn wrap-community 
  [opts handler]
  (fn [{{{id :id} :path} :parameters :as ctx}]
    (if-some [community (db/find-one-by-keys (:db-conn opts) :communities {:id (UUID/fromString id)})]
      (handler (assoc ctx :community community))
      {:status 404
       :body {}})))