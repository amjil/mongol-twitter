(ns amjil.jirgee.web.controllers.auth
  (:require
   [ring.util.http-response :as http-response]
   [clojure.tools.logging :as log]
   [cheshire.core :as cheshire]
   [amjil.jirgee.web.utils.db :as db]
   [amjil.jirgee.web.utils.check :as check]
   [amjil.jirgee.web.utils.token :as token]
   [buddy.hashers :as hashers]))

(declare check-password)

(defn login
  [conn secret params]
  (let [entity (db/find-one-by-keys conn :users (dissoc params :password :token :code))]
    (check/check-must-exist entity "User must exists!")
    (check/check-locked entity "User locked!")
    (check/check-password entity params "User Password Do Not Match!")
    (let [info (db/find-one-by-keys
                conn
                :user_info
                ["id = ?::uuid" (:id entity)]
                {:columns [:profile_image_url :sex
                           :followings_count :profile_banner_url :screen_name :bio :birth_date :location :followers_count
                           :id]})]
      {:token (token/jwt-token secret (:id entity))
       :info info})))

(declare check-before-signup)

(defn signup
  [conn secret params]
  (check-before-signup conn params)
  (let [result (db/insert! conn :users {:email (:email params)
                                        :encrypted_password  (hashers/derive (:password params))})]
    (check/check-not-nil (:id result) "Account Save Error!")
    
    (db/insert! conn :user_info {:id (:id result)})

    {}))

(defn- check-before-signup 
  [conn params]
  (let [entity (db/find-by-keys conn :users {:email (:email params)})]
    (check/check-must-not-exist entity "User Must Not Exists!")))