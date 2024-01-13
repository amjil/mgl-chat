(ns kit.mgl-chat.env
  (:require
    [clojure.tools.logging :as log]
    [kit.mgl-chat.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init       (fn []
                 (log/info "\n-=[mgl-chat starting using the development or test profile]=-"))
   :start      (fn []
                 (log/info "\n-=[mgl-chat started successfully using the development or test profile]=-"))
   :stop       (fn []
                 (log/info "\n-=[mgl-chat has shut down successfully]=-"))
   :middleware wrap-dev
   :opts       {:profile       :dev
                :persist-data? true}})
