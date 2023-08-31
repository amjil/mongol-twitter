(ns amjil.jirgee.web.controllers.ws
  (:require
   [ring.util.http-response :as http-response]
   [ring.adapter.undertow.websocket :as undertow-ws]
   [amjil.jirgee.web.utils.token :as token]
   [cheshire.core :as cheshire]
   [clojure.tools.logging :as log]))

(declare handle-message
         handle-event
         handle-request
         send-response)

;; ^:private
(def channels (atom {}))

(defn handler [secret {{token :token} :path-params}]
  {:undertow/websocket
   {:on-open
    (fn [{:keys [channel]}]
      (let [user-info (token/decrypt-token secret token)]
        (swap! channels assoc (:id user-info) channel))
      (println "WS open!"))

    :on-message
    (fn [{:keys [channel data]}]
      (let [user-info (token/decrypt-token secret token)]
        (handle-request user-info data channel)))

    :on-close-message
    (fn [{:keys [_ _]}]
      (let [user-info (token/decrypt-token secret token)]
        (swap! channels dissoc (:id user-info)))
      (println "WS closeed!"))}})

(defn handle-request [userinfo message channel]
  (-> (handle-message message userinfo channel)
      (cheshire/generate-string)
      (send-response channel)))

(defmulti handle-message
  (fn [{:keys [name]} _ _]
    name))

(defn send-response 
  [data channel]
  (undertow-ws/send data channel))
