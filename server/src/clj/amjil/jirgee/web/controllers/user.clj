(ns amjil.jirgee.web.controllers.user
  (:require
   [ring.util.http-response :as http-response]

   [amjil.jirgee.web.utils.db :as db]
   [amjil.jirgee.web.utils.check :as check]
   [amjil.jirgee.web.utils.notification :as ntf]
   [clojure.tools.logging :as log])
  (:import
   [java.util UUID]))


(defn follow
  [conn uinfo params]
  (let [result (db/find-one-by-keys conn :followers
                                    ["followee_id = ? and follower_id = ?"
                                     (UUID/fromString (:id params))
                                     (UUID/fromString (:id uinfo))])]
    (when (nil? result)
      (db/insert!
       conn
       :followers
       {:followee_id (UUID/fromString (:id params))
        :follower_id (UUID/fromString (:id uinfo))})

      ;; (future
      (let [info {:from (:id uinfo)
                  :to (:id params)
                  :content "someone has following you!"
                  :types_of 1}
            result (ntf/create-notification conn info)]
        (ntf/send-notification conn result info))))
      ;; )
  {})

(defn unfollow
  [conn uinfo params]
  (db/delete! 
   conn 
   :followers
   {:follower_id (UUID/fromString (:id uinfo))
    :followee_id (UUID/fromString (:id params))})
  {})

(defn explore 
  [conn uinfo params]
  (let [limit (or (:limit params) 20)
        offset (or (:offset params) 0)]
    (db/find-by-keys conn
                     :user_info
                     (if (empty? (:search params))
                       ["1=1 and id <> ?::uuid" (:id uinfo)]
                       {:screen_name (:search params)})
                     {:columns [:profile_image_url :sex
                                :followings_count :profile_banner_url
                                :screen_name :bio :birth_date :location
                                :followers_count :id
                                :created_at]
                      :order-by [[:created_at :desc]]
                      :offset offset :fetch limit})))
