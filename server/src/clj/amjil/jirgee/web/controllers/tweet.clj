(ns amjil.jirgee.web.controllers.tweet
  (:require
   [clojure.tools.logging :as log]
   [amjil.jirgee.web.utils.db :as db]
   [honey.sql :as hsql]
   [next.jdbc :as jdbc]
   [amjil.jirgee.web.utils.check :as check])
  (:import 
   [java.util UUID]))

(defn new-tweet
  [conn uinfo params]
  (let [result (db/insert! conn
                           :tweets
                           (assoc params :user_id (UUID/fromString (:id uinfo)))
                           {:return-keys true})]
    (select-keys result [:id])))

(defn query-tweet
  [query-fn uinfo params]
  (let [limit (or (:limit params) 20)
        offset (or (:offset params) 0)]
    (query-fn :query-followed-tweets {:limit limit :offset offset
                                      :user_id (UUID/fromString (:id uinfo))})))

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
  (let [entity (db/find-one-by-keys conn :favorites ["tweet_id = ? and user_id = ?"
                                                     (UUID/fromString id)
                                                     (UUID/fromString (:id uinfo))])]
    (when (empty? entity)
      (db/insert!
       conn
       :favorites
       {:user_id (UUID/fromString (:id uinfo))
        :tweet_id (UUID/fromString id)})
      (jdbc/execute-one!
       conn
       ["update tweets set favorites_count = favorites_count + 1 where id = ?"
        (UUID/fromString id)])))
  {})

(defn unfavorite-tweet
  [conn uinfo id]
  (let [entity (db/find-one-by-keys conn :favorites ["tweet_id = ? and user_id = ?"
                                                     (UUID/fromString id)
                                                     (UUID/fromString (:id uinfo))])]
    (when-not (empty? entity)
      (db/delete!
       conn
       :favorites
       {:user_id (UUID/fromString (:id uinfo))
        :tweet_id (UUID/fromString id)})
      (jdbc/execute-one!
       conn
       ["update tweets set favorites_count = favorites_count - 1 where id = ?" 
        (UUID/fromString id)])))
  {})

(defn retweet
  [conn uinfo id]
  (log/warn "uinfo = " uinfo)
  (let [entity (jdbc/execute! conn ["select * from retweets 
                                     where tweet_id = ? and user_id = ?"
                                    (UUID/fromString id)
                                    (UUID/fromString (:id uinfo))])]
    (when (empty? entity)
      (let [result (db/insert! conn
                               :tweets
                               {:user_id (UUID/fromString (:id uinfo))
                                ;; :content (:content info)}
                                :content nil}
                               {:return-keys true})]
        (db/insert! conn :retweets {:tweet_id (UUID/fromString id)
                                    :retweet_id (:id result)
                                    :user_id (UUID/fromString (:id uinfo))}))))

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

(defn get-replies 
  [query-fn uinfo id params]
  (let [limit (or (:limit params) 20)
        offset (or (:offset params) 0)]
     (query-fn :query-tweet-replies {:limit limit :offset offset
                                     :tweet-id (UUID/fromString id)})))

(defn tweet-links
  [conn uinfo id info]
  (let [entity (db/find-one-by-keys conn :tweets ["user_id = ? and id = ?" 
                                                  (UUID/fromString (:id uinfo))
                                                  (UUID/fromString id)])
        info "Not authorized to operate"]
    (when (not= (UUID/fromString (:id uinfo)) (:user_id entity))
      (throw (ex-info info {:type :system.exception/unauthorized
                            :message info}))))
  
  (let [entity (db/find-one-by-keys conn :tweet_entities ["tweet_id = ?" (UUID/fromString id)])]
    (when (empty? entity)
      (db/insert!
       conn
       :tweet_entities
       {:tweet_id (UUID/fromString id)
        :media_links
        (into-array String (:links info))})))
  
  {})
