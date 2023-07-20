(ns amjil.jirgee.web.controllers.user
  (:require
   [ring.util.http-response :as http-response]

   [amjil.jirgee.web.utils.db :as db]
   [amjil.jirgee.web.utils.check :as check])
  (:import
   [java.util UUID]))


(defn follow
  [conn uinfo params]
  (db/insert!
   conn 
   :followers
   {:followee_id (UUID/fromString (:id params))
    :follower_id (UUID/fromString (:id uinfo))})
  {})

(defn unfollow
  [conn uinfo params]
  (db/delete! 
   conn 
   :followers
   {:follower_id (UUID/fromString (:id uinfo))
    :followee_id (UUID/fromString (:id params))})
  {})
