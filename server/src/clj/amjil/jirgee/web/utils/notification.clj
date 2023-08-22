(ns amjil.jirgee.web.utils.notification
  (:require 
   [amjil.jirgee.web.utils.db :as db])
  (:import
   [java.util UUID]))

;; (defn create [info]
;;   (db/insert!
;;    conn
;;    :notifications
;;    (merge info
;;           {:to_user_id (UUID/fromString (:to_user_id info))
          ;;  :from_user_id (UUID/fromString (:from_user_id info))})))