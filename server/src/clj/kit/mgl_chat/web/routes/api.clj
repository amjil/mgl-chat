(ns kit.mgl-chat.web.routes.api
  (:require
   [kit.mgl-chat.web.middleware.auth :as auth-middleware]
   [kit.mgl-chat.web.controllers.community :as community]
   [kit.mgl-chat.web.controllers.auth :as auth]
   [kit.mgl-chat.web.controllers.channel :as channel]
   [kit.mgl-chat.web.controllers.health :as health]
   [kit.mgl-chat.web.controllers.ws :as ws]
   [kit.mgl-chat.web.middleware.exception :as exception]
   [kit.mgl-chat.web.middleware.formats :as formats]
   [integrant.core :as ig]
   [reitit.coercion.malli :as malli]
   [reitit.ring.coercion :as coercion]
   [reitit.ring.middleware.muuntaja :as muuntaja]
   [reitit.ring.middleware.parameters :as parameters]
   [reitit.swagger :as swagger]))

(def route-data
  {:coercion   malli/coercion
   :muuntaja   formats/instance
   :swagger    {:id ::api}
   :middleware [;; query-params & form-params
                parameters/parameters-middleware
                  ;; content-negotiation
                muuntaja/format-negotiate-middleware
                  ;; encoding response body
                muuntaja/format-response-middleware
                  ;; exception handling
                coercion/coerce-exceptions-middleware
                  ;; decoding request body
                muuntaja/format-request-middleware
                  ;; coercing response bodys
                coercion/coerce-response-middleware
                  ;; coercing request parameters
                coercion/coerce-request-middleware
                  ;; exception handling
                exception/wrap-exception]})

;; Routes
(defn api-routes [_opts]
  [["/swagger.json"
    {:get {:no-doc  true
           :swagger {:info {:title "kit.mgl-chat API"}}
           :handler (swagger/create-swagger-handler)}}]
   ["/health"
    {:get health/healthcheck!}]
   ["/auth"
    {:swagger {:tags ["auth"]}}
    ["/login"
     {:post {:summary "sign in."
             :parameters {:body {:email string?
                                 :password string?}}
             :responses {200 {:body any?}}
             :handler (fn [{{:keys [body]} :parameters headers :headers addr :remote-addr}]
                        {:status 200 :body
                         (auth/login (:db-conn _opts) (:token-secret _opts) body)})}}]
    ["/signup"
     {:post {:summary "sign up."
             :parameters {:body {:email string?
                                 :password string?}}
             :responses {200 {:body any?}}
             :handler (fn [{{:keys [body]} :parameters headers :headers addr :remote-addr}]
                        {:status 200 :body
                         (auth/signup (:db-conn _opts) (:token-secret _opts) body)})}}]]
   ["/message"
    {:swagger {:tags ["message"]}
     :get {:summary "message"
           :parameters {:query {:token string?}}
           :responses {200 {:body any?}}
           :handler #(ws/handler (select-keys _opts [:db-conn :query-fn :token-secret]) %)}}]

   ["/communities"
    {:swagger {:tags ["community"]}
     :post {:summary "new community"
            :middleware [[auth-middleware/wrap-restricted]]
            :parameters {:body {:title string?}}
            :responses {200 {:body any?}}
            :handler (fn [{{:keys [body]} :parameters uinfo :identity}]
                       {:status 200 :body
                        (community/new-community (:db-conn _opts)  uinfo body)})}
     :get {:summary "get communities list"
           :middleware [[auth-middleware/wrap-restricted]]
           :parameters {}
           :responses {200 {:body any?}}
           :handler (fn [{uinfo :identity}]
                      (community/query-communities (:query-fn _opts) uinfo))}}]
   ["/communities/:id"
    {:swagger {:tags ["community"]}
     :middleware [[(partial community/wrap-community _opts)]
                  [auth-middleware/wrap-restricted]]}
    [""
     {:put {:summary "update community"
            :parameters {:body {:title string?}
                         :path {:id string?}}
            :responses {200 {:body any?}}
            :handler (fn [{{{id :id} :path body :body} :parameters uinfo :identity}]
                       (community/update-community (:db-conn _opts) uinfo id body))}
      :delete {:summary "delete community"
               :parameters {:path {:id string?}}
               :responses {200 {:body any?}}
               :handler (fn [{{{id :id} :path} :parameters uinfo :identity}]
                          (community/delete-community (:db-conn _opts) uinfo id))}}]
    ["/join"
     {:swagger {:tags ["community"]}
      :post {:summary "join"
             :parameters {:path {:id string?}}
             :responses {200 {:body any?}}
             :handler (fn [{{{id :id} :path} :parameters uinfo :identity}]
                        {:status 200 :body
                         (community/join (:db-conn _opts) uinfo id)})}}]
    ["/channels"
     {:swagger {:tags ["community"]}
      :post {:summary "new channel"
             :parameters {:body {:title string?}
                          :path {:id string?}}
             :responses {200 {:body any?}}
             :handler (fn [{{{id :id} :path body :body} :parameters uinfo :identity}]
                        {:status 200 :body
                         (channel/new-channel (:db-conn _opts) uinfo (assoc body :comm_id id))})}
      :get {:summary "get channels list"
            :parameters {:path {:id string?}}
            :responses {200 {:body any?}}
            :handler (fn [{{{id :id} :path} :parameters uinfo :identity}]
                       (channel/query-channels (:db-conn _opts) id {}))}}]
    ["/channels/:cid"
     {:swagger {:tags ["community"]}
      :middleware [[(partial channel/wrap-channel _opts)]]}
     [""
      {:put {:summary "update channel"
             :parameters {:body {:title string?}
                          :path {:id string?
                                 :cid string?}}
             :responses {200 {:body any?}}
             :handler (fn [{{{id :id cid :cid} :path body :body} :parameters uinfo :identity}]
                        (channel/update-channel (:db-conn _opts) cid body))}
       :delete {:summary "delete channel"
                :parameters {:path {:id string?
                                    :cid string?}}
                :responses {200 {:body any?}}
                :handler (fn [{{{id :id cid :cid} :path} :parameters uinfo :identity}]
                           (channel/delete-channel (:db-conn _opts) cid))}}]
     ["/message"
      {:post {:summary "send message"
              :parameters {:body {:content string?}
                           :path {:id string?
                                  :cid string?}}
              :responses {200 {:body any?}}
              :handler (fn [{{{id :id cid :cid} :path body :body} :parameters uinfo :identity}]
                         (channel/message (:db-conn _opts) uinfo id cid body))}}]]]])

(derive :reitit.routes/api :reitit/routes)

(defmethod ig/init-key :reitit.routes/api
  [_ {:keys [base-path]
      :or   {base-path ""}
      :as   opts}]
  [base-path route-data (api-routes opts)])
