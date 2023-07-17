(ns amjil.jirgee.web.middleware.core
  (:require
   [clojure.tools.logging :as log]
   [amjil.jirgee.env :as env]
   [ring.middleware.defaults :as defaults]
   [ring.middleware.session.cookie :as cookie]
   [buddy.auth.middleware :refer [wrap-authentication wrap-authorization]]
   [buddy.auth.accessrules :refer [restrict]]
   [buddy.auth :refer [authenticated?]]
   [buddy.auth.backends.token :refer [jwe-backend]]))

(defn on-error [request response]
  {:status 403
   :headers {}
   :body (str "Access to " (:uri request) " is not authorized")})

;; :middleware [[middleware/wrap-restricted]]
(defn wrap-restricted [handler]
  (restrict handler {:handler authenticated?
                     :on-error on-error}))

(defn wrap-base
  [{:keys [metrics site-defaults-config cookie-secret] :as opts}]
  (let [cookie-store (cookie/cookie-store {:key (.getBytes ^String cookie-secret)})
        backend (jwe-backend {:secret (:token-secret opts)
                              :options {:alg :a256kw
                                        :enc :a128gcm}})]
    (fn [handler]
      (cond-> ((:middleware env/defaults) handler opts)
        true (wrap-authentication backend)
        true (wrap-authorization backend)
        true (defaults/wrap-defaults
              (assoc-in site-defaults-config [:session :store] cookie-store))))))
