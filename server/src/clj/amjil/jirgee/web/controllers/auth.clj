(ns amjil.jirgee.web.controllers.auth
  (:require
   [ring.util.http-response :as http-response]
   [clojure.tools.logging :as log]

   [honey.sql :as hsql]

   [amjil.jirgee.web.utils.db :as db]
   [amjil.jirgee.web.utils.check :as check]
   [amjil.jirgee.web.utils.token :as token]
   [buddy.hashers :as hashers]))

(declare check-password)

(defn login
  [conn secret params]
  (let [entity (db/find-one-by-keys conn :accounts (dissoc params :password :token :code))]
    (check/check-must-exist entity "User must exists!")
    (check/check-locked entity "User locked!")
    (check/check-password entity params "User Password Do Not Match!")
    {:token (token/jwt-token secret (:id entity))}))

(declare check-before-signup)

(defn signup
  [conn secret params]
  (check-before-signup conn params)
  (let [result (db/insert! conn :accounts {:email (:email params)
                                           :encrypted_password  (hashers/derive (:password params))})]
    (check/check-not-nil (:id result) "Account Save Error!")
    {:token (token/jwt-token secret (:id result))}))

(defn- check-before-signup 
  [conn params]
  (let [entity (db/find-by-keys conn :accounts {:email (:email params)})]
    (check/check-must-not-exist entity "User Must Not Exists!")))

(defn update-password 
  [conn uinfo params]
  (log/warn "req = " params)
  (log/warn "uinfo = " (:identity params))
  
  ;; (let [entity (db/get-by-id conn :accounts (:id uinfo))]

  ;;   ;; check the entity 
  ;;   (when (nil? entity)
  ;;     (let [msg "Maybe Token had outdated!"]
  ;;       (throw (ex-info msg {:type :system.exception/business :message msg}))))

  ;;   ;; check the current password
  ;;   (when-not (hashers/check (:current_password params) (:encrypted_password entity))
  ;;     (let [msg "Current Password Do Not Match!"]
  ;;       (throw (ex-info msg {:type :system.exception/business :message msg}))))

  ;;   ;; update ->>
  ;;   (let [sqlmap {:update :accounts
  ;;                 :set    {:encrypted_password   (hashers/derive (:new_password params))
  ;;                          :updated_at           [:raw "now()"]}
  ;;                 :where  [:= :id (:id uinfo)]}]
  ;;     (db/execute! conn (hsql/format sqlmap))
  ;;     {}))
  )