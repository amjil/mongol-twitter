(ns amjil.jirgee.web.utils.notification
  (:require 
   [amjil.jirgee.web.utils.db :as db]
   [amjil.jirgee.web.controllers.ws :as wss]
   [cheshire.core :as cheshire])
  (:import
   [java.util UUID]))

(defn create-notification [conn info]
  (let [result
        (db/insert!
         conn
         :notifications
         {:to_user_id (UUID/fromString (:to info))
          :from_user_id (UUID/fromString (:from info))
          :nty_id (if (nil? (:id info))
                    nil
                    (UUID/fromString (:id info)))
          :content (:content info)
          ;; 1 following notification
          ;; 2 tweet reply notification
          ;; 3 tweet retweet 
          ;; 4 tweet favorite
          :types_of (:types_of info)}
         {:return-keys true})]
    (:id result)))

(defn send-notification [conn ntf-id info]
  (when-let [channel (get @wss/channels (:to info))]
    (let [data
          (db/find-one-by-keys
           conn
           :notifications
           {:id ntf-id})]
      (wss/send-response
       (cheshire/generate-string
        {:type :notification
         :data (assoc data :created_at (str (:created_at data)))})
       channel))))

(defn has-newer-tweets
  [uid]
  (when-let [channel (get @wss/channels uid)]
    (wss/send-response
     {:type :has-newer-tweets}
     channel)))