(ns amjil.jirgee.web.routes.api
  (:require
   [amjil.jirgee.web.controllers.health :as health]
   [amjil.jirgee.web.controllers.auth :as auth]
   [amjil.jirgee.web.controllers.profile :as profile]
   [amjil.jirgee.web.controllers.ws :as ws]
   [amjil.jirgee.web.middleware.exception :as exception]
   [amjil.jirgee.web.middleware.formats :as formats]
   [amjil.jirgee.web.middleware.core :as middleware]
   [integrant.core :as ig]
   [reitit.coercion.malli :as malli]
   [reitit.ring.coercion :as coercion]
   [reitit.ring.middleware.muuntaja :as muuntaja]
   [reitit.ring.middleware.parameters :as parameters]
   [reitit.swagger :as swagger]))

(def route-data
  {:coercion   malli/coercion
   :muuntaja   formats/instance
   :swagger    {:id ::api}
   :middleware [;; query-params & form-params
                parameters/parameters-middleware
                ;; content-negotiation
                muuntaja/format-negotiate-middleware
                ;; encoding response body
                muuntaja/format-response-middleware
                ;; exception handling
                coercion/coerce-exceptions-middleware
                  ;; decoding request body
                muuntaja/format-request-middleware
                  ;; coercing response bodys
                coercion/coerce-response-middleware
                  ;; coercing request parameters
                coercion/coerce-request-middleware
                  ;; exception handling
                exception/wrap-exception]})

;; Routes
(defn api-routes [_opts]
  [["/swagger.json"
    {:get {:no-doc  true
           :swagger {:info {:title "amjil.jirgee API"}}
           :handler (swagger/create-swagger-handler)}}]
   ["/health"
    {:get health/healthcheck!}]
   ["/auth"
    {:swagger {:tags ["auth"]}}
    ["/login"
     {:post {:summary "sign in."
             :parameters {:body {:email string?
                                 :password string?}}
             :responses {200 {:body {:token string?}}}
             :handler (fn [{{:keys [body]} :parameters headers :headers addr :remote-addr}]
                        {:status 200 :body
                         (auth/login (:db-conn _opts) (:token-secret _opts) body)})}}]
    ["/signup"
     {:post {:summary "sign up."
             :parameters {:body {:email string?
                                 :password string?}}
             :responses {200 {:body {:token string?}}}
             :handler (fn [{{:keys [body]} :parameters headers :headers addr :remote-addr}]
                        {:status 200 :body
                         (auth/signup (:db-conn _opts) (:token-secret _opts) body)})}}]]
   ["/profile"
    {:swagger {:tags ["profile"]}}
    ["/update_password"
     {:post {:summary "update password."
             :middleware [[middleware/wrap-restricted]]
             :parameters {:body {:current_password string?
                                 :new_password string?}}
             :responses {200 {:body any?}}
             :handler (fn [{{:keys [body]} :parameters uinfo :identity headers :headers addr :remote-addr}]
                        {:status 200 :body
                         (profile/update-password (:db-conn _opts) uinfo body)})}}]]
   ["/fail"
    {:get (fn [_]
            (throw (ex-info "fail" {:type :system.exception/not-found})))}]
   ["/ws"
    ws/handler]])

(derive :reitit.routes/api :reitit/routes)

(defmethod ig/init-key :reitit.routes/api
  [_ {:keys [base-path]
      :or   {base-path ""}
      :as   opts}]
  [base-path route-data (api-routes opts)])
