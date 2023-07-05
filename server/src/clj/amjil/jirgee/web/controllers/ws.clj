(ns amjil.jirgee.web.controllers.ws
  (:require
   [ring.util.http-response :as http-response]))


(defn handler [request]
  {:undertow/websocket
   {:on-open (fn [{:keys [channel]}] (println "WS open!"))
    :on-message (fn [{:keys [channel data]}] (prn "message received"))
    :on-close-message (fn [{:keys [channel message]}] (println "WS closeed!"))}})

(defn message-handler)