(ns kit.mgl-chat.web.controllers.ws
  (:require
   [ring.adapter.undertow.websocket :as undertow-ws]
   [kit.mgl-chat.web.utils.token :as token]
   [cheshire.core :as cheshire]
   [clojure.tools.logging :as log]
   [clojure.string :as str]
   [kit.mgl-chat.web.utils.db :as db]))

(declare send-response
         handle-message
         handle-request)

;; ^:private
(def channels (atom {}))

(defn handler [opts {{{token :token} :query} :parameters}]
  {:undertow/websocket
   {:on-open
    (fn [{:keys [channel]}]
      (let [user-info (token/decrypt-token (:token-secret opts) token)]
        (swap! channels assoc (:id user-info) channel))
      (println "WS open!"))

    :on-message
    (fn [{:keys [channel data]}]
      (let [user-info (token/decrypt-token (:token-secret opts) token)
            result (cheshire/parse-string data true)]
        (handle-request (assoc opts :channel channel) user-info result)))

    :on-close-message
    (fn [{:keys [_ _]}]
      (let [user-info (token/decrypt-token (:token-secret opts) token)]
        (swap! channels dissoc (:id user-info)))
      (println "WS closeed!"))}})

(defn send-response
  [data channel]
  (undertow-ws/send data channel))

(defn handle-request [opts userinfo message]
  (when-let [response (handle-message opts userinfo message)]
    (-> response
        (cheshire/generate-string)
        (send-response (:channel opts)))))

(defmulti handle-message
  ;; {:type xxxx :data}
  (fn [_ _ msg]
    (:type msg)))

(defmethod handle-message
  "msg"
  [_ _ message]
  (log/warn "handle-message" message)
  {})

(defn send-notification 
  [uid message]
  (when-let [channel (get @channels uid)]
    (send-response {:type :message :msg message} channel)))