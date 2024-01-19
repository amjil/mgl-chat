(ns kit.mgl-chat.web.routes.app
  (:require
   [kit.mgl-chat.web.middleware.core :as middleware]))

(defn routes []
  ["" {:middleware middleware/wrap-restricted}])