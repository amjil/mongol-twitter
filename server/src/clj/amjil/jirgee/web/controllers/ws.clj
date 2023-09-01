(ns amjil.jirgee.web.controllers.ws
  (:require
   [ring.util.http-response :as http-response]
   [ring.adapter.undertow.websocket :as undertow-ws]
   [amjil.jirgee.web.utils.token :as token]
   [cheshire.core :as cheshire]
   [clojure.tools.logging :as log]))

(declare handle-message
         handle-request
         send-response)

;; ^:private
(def channels (atom {}))

(defn handler [opts {{token :token} :path-params}]
  {:undertow/websocket
   {:on-open
    (fn [{:keys [channel]}]
      (let [user-info (token/decrypt-token (:token-secret opts) token)]
        (swap! channels assoc (:id user-info) channel))
      (println "WS open!"))

    :on-message
    (fn [{:keys [channel data]}]
      (let [user-info (token/decrypt-token (:token-secret opts) token)]
        (handle-request (assoc opts :channel channel) user-info data)))

    :on-close-message
    (fn [{:keys [_ _]}]
      (let [user-info (token/decrypt-token (:token-secret opts) token)]
        (swap! channels dissoc (:id user-info)))
      (println "WS closeed!"))}})

(defn handle-request [opts userinfo message]
  (-> (handle-message opts userinfo message)
      (cheshire/generate-string)
      (send-response (:channel opts))))

(defmulti handle-message
  (fn [_ _ {:keys [name]}]
    name))

(defn send-response 
  [data channel]
  (undertow-ws/send data channel))
