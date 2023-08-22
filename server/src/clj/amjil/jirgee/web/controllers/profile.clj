(ns amjil.jirgee.web.controllers.profile
  (:require
   [clojure.tools.logging :as log]
   [amjil.jirgee.web.utils.db :as db]
   [honey.sql :as hsql]
   [buddy.hashers :as hashers]
   [amjil.jirgee.web.utils.check :as check])
  (:import
   [java.util UUID]))

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
  [query-fn uinfo params]
  (log/warn "user params = " params)

  (let [info (query-fn :user-profile-with-following {:id (UUID/fromString (:user_id params))
                                                     :user_id (UUID/fromString (:id uinfo))})]
    info))

(defn update-info 
  [conn token info]
  (log/warn "info = " info)
  (db/update! conn :user_info info ["id = ?::uuid" (:id token)])
  {})