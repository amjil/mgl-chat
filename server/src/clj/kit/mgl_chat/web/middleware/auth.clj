(ns kit.mgl-chat.web.middleware.auth
  (:require
   [kit.mgl-chat.web.utils.db :as db]
   [buddy.auth :as auth]
   [buddy.auth.accessrules :as accessrules]
   [buddy.auth.middleware :as auth-middleware]
   [buddy.auth.backends.token :refer [jwe-backend]]
   [buddy.core.hash :as hash])
  (:import
   [java.util UUID]))

(defn on-error [request _response]
  {:status 403
   :headers {}
   :body (str "Access to " (:uri request) " is not authorized")})

(defn wrap-restricted [handler]
  (accessrules/restrict handler {:handler auth/authenticated?
                                 :on-error on-error}))

(defn on-not-signed-in [request _response]
  {:status 303
   :headers {"location" "/?error=not-signed-in"}})

(defn wrap-signed-in [handler]
  (accessrules/restrict handler {:handler auth/authenticated?
                                 :on-error on-error}))

(defn wrap-is-admin [conn handler]
  (fn [{uinfo :identity :as ctx}]
    (if-some [user (db/find-one-by-keys conn :users
                                        {:id (UUID/fromString (:id uinfo))})]
      (if (true? (:is_admin user))
        (handler ctx)
        {:status 403 :headers {}
         :body {:msg "Not Authorized!"}})
      {:status 403 :headers {}
       :body {:msg "Not Authorized!"}})))

(defn wrap-auth
  [opts]
  (let [backend (jwe-backend {:secret (hash/sha256 (:token-secret opts))
                              :options {:alg :a256kw
                                        :enc :a128gcm}})]
    (fn [handler]
      (-> handler
          (auth-middleware/wrap-authentication backend)
          (auth-middleware/wrap-authorization backend)))))
