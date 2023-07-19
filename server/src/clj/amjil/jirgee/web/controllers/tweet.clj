(ns amjil.jirgee.web.controllers.tweet
  (:require
   [clojure.tools.logging :as log]
   [amjil.jirgee.web.utils.db :as db]
   [honey.sql :as hsql]
   [amjil.jirgee.web.utils.check :as check]))

(defn new-tweet 
  [conn uinfo params]
  (db/insert! conn 
              :tweets 
              (assoc params :user_id (java.util.UUID/fromString (:id uinfo))))
  {})



