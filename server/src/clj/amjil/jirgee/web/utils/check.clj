(ns amjil.jirgee.web.utils.check
  (:require 
   [buddy.hashers :as hashers]))

(derive :system.exception/business ::user-not-found)
(derive :system.exception/business ::user-already-exists)
(derive :system.exception/business ::user-locked)
(derive :system.exception/business ::user-password-do-not-match)

(defn check-must-exist [entity info]
  (when (empty? entity)
    (throw (ex-info info {:type ::user-not-found}))))

(defn check-must-not-exist [entity info]
  (when-not (empty? entity)
    (throw (ex-info info {:type ::user-already-exists}))))

(defn check-locked [entity info]
  (when (:locked_at entity)
    (throw (ex-info info {:type ::user-locked}))))

(defn check-password [entity params info]
  (when-not (hashers/check (:password params) (:encrypted_password entity))
    (throw (ex-info info {:type ::user-password-do-not-match}))))