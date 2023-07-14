(ns amjil.jirgee.web.controllers.auth
  (:require
   [ring.util.http-response :as http-response]
   
   [amjil.jirgee.web.utils.db :as db]
   [amjil.jirgee.web.utils.check :as check]
   [amjil.jirgee.web.utils.token :as token]
   ))

(declare check-password)

(defn login
  [conn secret params]
  (let [entity (db/find-one-by-keys conn :users (dissoc params :password :token :code))]
    (-> entity
        (check/check-must-exist "User must exists!")
        (check/check-locked "User locked!"))
        (check/check-password params "User Password Do Not Match!")
    {:token (token/jwt-token secret (:id entity))}))
