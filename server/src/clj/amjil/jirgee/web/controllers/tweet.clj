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

(defn retweet
  [conn uinfo id info]
  (let [result (db/insert! conn
                           :tweets
                           {:user_id (UUID/fromString (:id uinfo))
                            :content (:content info)}
                           {:return-keys true})]
    (db/insert! conn :retweets {:tweet_id (UUID/fromString id)
                                :retweet_id (:id result)}))
  {})

(defn reply 
  [conn uinfo id info]
  (let [result (db/insert! conn
                           :tweets
                           {:user_id (UUID/fromString (:id uinfo))
                            :content (:content info)}
                           {:return-keys true})]
    (db/insert! conn :replies {:tweet_id (UUID/fromString id)
                               :reply_id (:id result)})))

(defn tweet-links
  [conn uinfo id info]
  (let [entity (db/find-one-by-keys conn :tweets ["user_id = ?" (UUID/fromString (:id uinfo))])
        info "Not authorized to operate"]
    (when (not= (UUID/fromString (:id uinfo)) (:user_id entity))
      (throw (ex-info info {:type :system.exception/unauthorized
                            :message info}))))
  (db/insert! 
   conn 
   :tweet_entities
   {:tweet_id (UUID/fromString id)
    :media_links
    (into-array String (:links info))})
  {})
