(ns amjil.jirgee.web.controllers.ws
  (:require
   [ring.util.http-response :as http-response]))

(declare handle-message)

(defn handler [secret request]
  {:undertow/websocket
   {:on-open (fn [{:keys [channel]}]
               (println "WS open!"))
    :on-message (fn [{:keys [channel data]}] 
                  (prn "message received")
                  (handle-message secret channel data))
    :on-close-message (fn [{:keys [channel message]}] (println "WS closeed!"))}})

(defn handle-message [secret channel data]
  nil)