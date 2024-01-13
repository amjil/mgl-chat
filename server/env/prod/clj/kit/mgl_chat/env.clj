(ns kit.mgl-chat.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init       (fn []
                 (log/info "\n-=[mgl-chat starting]=-"))
   :start      (fn []
                 (log/info "\n-=[mgl-chat started successfully]=-"))
   :stop       (fn []
                 (log/info "\n-=[mgl-chat has shut down successfully]=-"))
   :middleware (fn [handler _] handler)
   :opts       {:profile :prod}})
