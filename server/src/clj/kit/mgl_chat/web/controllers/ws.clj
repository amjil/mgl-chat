(ns kit.mgl-chat.web.controllers.ws
  (:require
   [ring.adapter.undertow.websocket :as undertow-ws]))

(declare send-response)

;; ^:private
(def channels (atom {}))

(defn handler [request]
  {:undertow/websocket
   {:on-open
    (fn [ctx]
      (prn ctx)
      (println "WS open!"))

    :on-message
    (fn [ctx]
      (prn ctx)
      (println "WS on-message!"))

    :on-close-message
    (fn [ctx]
      (prn ctx)
      (println "WS closeed!"))}})

(defn send-response
  [data channel]
  (undertow-ws/send data channel))
