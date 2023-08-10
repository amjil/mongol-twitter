(ns amjil.jirgee.web.controllers.profile
  (:require
   [clojure.tools.logging :as log]
   [amjil.jirgee.web.utils.db :as db]
   [honey.sql :as hsql]
   [buddy.hashers :as hashers]
   [amjil.jirgee.web.utils.check :as check]))

(defn update-password
  [conn uinfo params]
  (when (nil? (:id uinfo))
    (let [msg "Token error!"]
      (throw (ex-info msg {:type :system.exception/business :message msg}))))

  (let [entity (db/find-one-by-keys conn :users ["id = ?::uuid" (:id uinfo)])]

    ;; check the entity 
    (check/check-must-exist entity "Maybe Token had outdated!")

    ;; check the current password
    (when-not (hashers/check (:current_password params) (:encrypted_password entity))
      (let [msg "Current Password Do Not Match!"]
        (throw (ex-info msg {:type :system.exception/business :message msg}))))

    ;; update ->>
    (let [sqlmap {:update :users
                  :set    {:encrypted_password   (hashers/derive (:new_password params))
                           :updated_at           [:raw "now()"]}
                  :where  [:= :id (:id uinfo)]}]
      (db/execute! conn (hsql/format sqlmap))
      {})))

(defn user-info 
  [conn params]
  (log/warn "user params = " params)
  (let [entity (db/find-one-by-keys conn :users ["id = ?::uuid" (:user_id params)])]

    ;; check the entity 
    (check/check-must-exist entity "Maybe Token had outdated!")

    (let [info (db/find-one-by-keys conn
                                    :user_info ["id = ?::uuid" (:user_id params)]
                                    {:columns [:profile_image_url :sex
                                               :followings_count :profile_banner_url :screen_name :bio :birth_date :location :followers_count]})]
      info)))

(defn update-info 
  [conn token info]
  (log/warn "info = " info)
  (db/update! conn :user_info info ["id = ?::uuid" (:id token)])
  {})