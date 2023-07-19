(ns amjil.jirgee.web.controllers.tweet
  (:require
   [clojure.tools.logging :as log]
   [amjil.jirgee.web.utils.db :as db]
   [honey.sql :as hsql]
   [amjil.jirgee.web.utils.check :as check])
  (:import 
   [java.util UUID]))

(defn new-tweet 
  [conn uinfo params]
  (db/insert! conn 
              :tweets 
              (assoc params :user_id (UUID/fromString (:id uinfo))))
  {})

(defn delete-tweet
  [conn uinfo id]
  (db/delete! 
   conn
   :tweets 
   {:id (UUID/fromString id)
    :user_id (UUID/fromString (:id uinfo))})
  {})

(defn favorite-tweet 
  [conn uinfo id]
  (db/insert!
   conn
   :favorites
   {:user_id (UUID/fromString (:id uinfo))
    :tweet_id (UUID/fromString id)})
  {})

(defn unfavorite-tweet 
  [conn uinfo id]
  (db/delete!
   conn
   :favorites
   {:user_id (UUID/fromString (:id uinfo))
    :tweet_id (UUID/fromString id)})
  {})


