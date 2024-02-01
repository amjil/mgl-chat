(ns kit.mgl-chat.web.routes.api
  (:require
    [kit.mgl-chat.web.middleware.auth :as auth-middleware]
    [kit.mgl-chat.web.controllers.community :as community]
    [kit.mgl-chat.web.controllers.auth :as auth]
    [kit.mgl-chat.web.controllers.channel :as channel]
    [kit.mgl-chat.web.controllers.health :as health]
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
             :responses {200 {:body {:token string?}}}
             :handler (fn [{{:keys [body]} :parameters headers :headers addr :remote-addr}]
                        {:status 200 :body
                         (auth/signup (:db-conn _opts) (:token-secret _opts) body)})}}]]
   ["/communities" 
    {:swagger {:tags ["community"]}
     :post {:summary "new community"
            :middleware [[auth-middleware/wrap-restricted]
                         [(partial auth-middleware/wrap-is-admin (:db-conn _opts))]]
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
  ;;  ["/community/:id"
  ;;   {:swagger {:tags ["community"]}
  ;;    :middleware [[(partial community/wrap-community _opts)]]}
  ;;   ]
   ])

(derive :reitit.routes/api :reitit/routes)

(defmethod ig/init-key :reitit.routes/api
  [_ {:keys [base-path]
      :or   {base-path ""}
      :as   opts}]
  [base-path route-data (api-routes opts)])
