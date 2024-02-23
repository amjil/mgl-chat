(ns app.core
  (:require
    [cljs.spec.alpha :as s]
    [clojure.edn :as edn]
    [uix.core :as uix :refer [defui $]]
    [uix.dom]))

(defui header []
  ($ :header.app-header
    ($ :span "MGL-CHAT")))

(defui footer []
  ($ :footer.app-footer
    ($ :small "made with "
              ($ :a {:href "https://github.com/amjil/mgl-chat"}
                    "MGL-CHAT"))))

(defn use-persistent-state
  "Loads initial state from local storage and persists every updated state value
  Returns a tuple of the current state value and an updater function"
  [store-key initial-value]
  (let [[value set-value!] (uix/use-state initial-value)]
    (uix/use-effect
      (fn []
        (let [value (edn/read-string (js/localStorage.getItem store-key))]
          (set-value! #(str value))))
      [store-key])
    (uix/use-effect
      (fn []
        (js/localStorage.setItem store-key (str value)))
      [value store-key])
    [value set-value!]))

(defui app []
  (let [[token set-token!] (use-persistent-state "mgl-chat/token" "")]
    ($ :.app
      ($ header)
      ($ footer))))

(defonce root
  (uix.dom/create-root (js/document.getElementById "root")))

(defn render []
  (uix.dom/render-root ($ app) root))

(defn ^:export init []
  (render))
