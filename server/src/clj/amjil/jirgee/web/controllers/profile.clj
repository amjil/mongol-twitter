(ns amjil.jirgee.web.controllers.profile
  (:require
   [amjil.jirgee.web.utils.db :as db]
   [honey.sql :as hsql]
   [buddy.hashers :as hashers]))

(defn update-password
  [conn uinfo params]
  (when (nil? (:id uinfo))
    (let [msg "Token error!"]
      (throw (ex-info msg {:type :system.exception/business :message msg}))))

  (let [entity (db/get-by-id conn :accounts (:id uinfo))]

    ;; check the entity 
    (when (nil? entity)
      (let [msg "Maybe Token had outdated!"]
        (throw (ex-info msg {:type :system.exception/business :message msg}))))

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