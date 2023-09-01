(ns amjil.jirgee.web.utils.notification
  (:require 
   [amjil.jirgee.web.utils.db :as db]
   [amjil.jirgee.web.controllers.ws :as wss])
  (:import
   [java.util UUID]))

(defn create-notification [conn info]
  (let [result
        (db/insert!
         conn
         :notifications
         (merge info
                {:to_user_id (UUID/fromString (:to info))
                 :from_user_id (UUID/fromString (:from info))
                 :nty_id (UUID/fromString (:id info))
                 :content (:content info)
                 ;; 1 following notification
                 ;; 2 tweet reply notification
                 ;; 3 tweet retweet 
                 ;; 4 tweet favorite
                 :types_of (:type info)})
         {:return-keys true})]
    (:id result)))

(defn send-notification [conn ntf-id info]
  (when-let [channel (get @wss/channels (:to info))]
    (let [data
          (db/get-by-id conn
                        :notifications
                        (UUID/fromString ntf-id))]
      (wss/send-response
       {:type :notification :data data}
       channel))))