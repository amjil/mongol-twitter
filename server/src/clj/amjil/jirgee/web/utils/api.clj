(ns amjil.jirgee.web.utils.api
  (:require
   [amjil.jirgee.web.controllers.ws :as server.ws]))

(defn register-endpoint!
  [kw f]
  (defmethod server.ws/handle-message kw [message userinfo channle]
    (f message userinfo channle)))

(register-endpoint!
 "ping"
 (fn [_ _ _]
   (constantly {:type :ping :result "pong" :success true})))
