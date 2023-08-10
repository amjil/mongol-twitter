(ns amjil.jirgee.web.routes.api
  (:require
   [amjil.jirgee.web.controllers.health :as health]
   [amjil.jirgee.web.controllers.auth :as auth]
   [amjil.jirgee.web.controllers.profile :as profile]
   [amjil.jirgee.web.controllers.tweet :as tweet]
   [amjil.jirgee.web.controllers.user :as user]
   [amjil.jirgee.web.controllers.ws :as ws]
   [amjil.jirgee.web.middleware.exception :as exception]
   [amjil.jirgee.web.middleware.formats :as formats]
   [amjil.jirgee.web.middleware.core :as middleware]
   [spec-tools.data-spec :as ds]
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
             :responses {200 {:body any?}}
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
    ["/user_info"
     {:get {:summary "get user info."
            :middleware [[middleware/wrap-restricted]]
            :responses {200 {:body any?}}
            :handler (fn [{uinfo :identity}]
                       {:status 200 :body
                        (profile/user-info (:db-conn _opts) uinfo)})}}]
    ["/update_password"
     {:post {:summary "update password."
             :middleware [[middleware/wrap-restricted]]
             :parameters {:body {:current_password string?
                                 :new_password string?}}
             :responses {200 {:body any?}}
             :handler (fn [{{:keys [body]} :parameters uinfo :identity headers :headers addr :remote-addr}]
                        {:status 200 :body
                         (profile/update-password (:db-conn _opts) uinfo body)})}}]
    ["/update_info"
     {:post {:summary "update user info."
             :middleware [[middleware/wrap-restricted]]
             :parameters {:body [:map
                                 [:screen_name {:optional true} string?]
                                 [:sex {:optional true} int?]
                                 [:bio {:optional true} string?]
                                 [:location {:optional true} string?]
                                 [:birth_date {:optional true} string?]]}
             :responses {200 {:body any?}}
             :handler (fn [{{:keys [body]} :parameters uinfo :identity}]
                        {:status 200 :body
                         (profile/update-info (:db-conn _opts) uinfo body)})}}]
    ;; ["/set_profile_image"
    ;;  {:post {:summary "set profile image."
    ;;          :middleware [[middleware/wrap-restricted]]}
    ;;   }]
    ]

   ["/tweets"
    {:swagger {:tags ["tweets"]}
     :post {:summary "new tweet."
            :middleware [[middleware/wrap-restricted]]
            :parameters {:body [:map
                                [:content string?]]}
            :responses {200 {:body any?}}
            :handler (fn [{{:keys [body]} :parameters uinfo :identity}]
                       {:status 200 :body
                        (tweet/new-tweet (:db-conn _opts) uinfo body)})}
     :get {:summary "query tweets."
           :middleware [[middleware/wrap-restricted]]
           :parameters {:query [:map
                                [:limit {:optional true} int?]
                                [:offset {:optional true} int?]]}
           :responses {200 {:body any?}}
           :handler (fn [{{:keys [query]} :parameters uinfo :identity}]
                      {:status 200 :body
                       (tweet/query-tweet (:query-fn _opts) uinfo query)})}
     }]
   ["/tweets/:id"
    {:swagger {:tags ["tweets"]}
     :delete {:summary    "remove tweet."
              :parameters {:path {:id string?}}
              :responses  {200 {:body any?}}
              :handler    (fn [{{{id :id} :path} :parameters
                                uinfo            :identity}]
                            {:status 200 :body
                             (tweet/delete-tweet (:db-conn _opts) uinfo id)})}}]
   ["/tweets/:id/favorite"
    {:swagger {:tags ["tweets"]}
     :post {:summary    "favorite tweet."
            :parameters {:path {:id string?}}
            :responses  {200 {:body any?}}
            :handler    (fn [{{{id :id} :path} :parameters
                              uinfo            :identity}]
                          {:status 200 :body
                           (tweet/favorite-tweet (:db-conn _opts) uinfo id)})}}]
   ["/tweets/:id/unfavorite"
    {:swagger {:tags ["tweets"]}
     :delete {:summary    "unfavorite tweet."
              :parameters {:path {:id string?}}
              :responses  {200 {:body any?}}
              :handler    (fn [{{{id :id} :path} :parameters
                                uinfo            :identity}]
                            {:status 200 :body
                             (tweet/unfavorite-tweet (:db-conn _opts) uinfo id)})}}]
   ["/tweets/:id/retweet"
    {:swagger {:tags ["tweets"]}
     :post {:summary    "retweet tweet."
            :parameters {:path {:id string?}
                         :body [:map
                                [:content string?]]}
            :responses  {200 {:body any?}}
            :handler    (fn [{{{id :id} :path
                               body     :body} :parameters
                              uinfo            :identity}]
                          {:status 200 :body
                           (tweet/retweet (:db-conn _opts) uinfo id body)})}}]
   ["/tweets/:id/replies"
    {:swagger {:tags ["tweets"]}
     :post {:summary    "reply tweet."
            :parameters {:path {:id string?}
                         :body [:map
                                [:content string?]]}
            :responses  {200 {:body any?}}
            :handler    (fn [{{{id :id} :path
                               body     :body} :parameters
                              uinfo            :identity}]
                          {:status 200 :body
                           (tweet/reply (:db-conn _opts) uinfo id body)})}
     :get {:summary    "get tweet replies."
           :parameters {:path {:id string?}
                        :query [:map
                                [:limit {:optional true} int?]
                                [:offset {:optional true} int?]]}
           :responses  {200 {:body any?}}
           :handler    (fn [{{{id :id} :path
                              params :query} :parameters
                             uinfo            :identity}]
                         {:status 200 :body
                          (tweet/get-replies (:query-fn _opts) uinfo id params)})}}]
   ["/tweets/:id/media_links"
    {:swagger {:tags ["tweets"]}
     :post {:summary    "tweet media_links."
            :parameters {:path {:id string?}
                         :body [:map
                                [:links [:set string?]]]}
            :responses  {200 {:body any?}}
            :handler    (fn [{{{id :id} :path
                               body     :body} :parameters
                              uinfo            :identity}]
                          {:status 200 :body
                           (tweet/tweet-links (:db-conn _opts) uinfo id body)})}}]
   ["/users/follow"
    {:swagger {:tags ["users"]}
     :post {:summary    "user follow."
            :parameters {:body [:map
                                [:id string?]]}
            :responses  {200 {:body any?}}
            :handler    (fn [{{body     :body} :parameters
                              uinfo            :identity}]
                          {:status 200 :body
                           (user/follow (:db-conn _opts) uinfo body)})}}]
   ["/users/unfollow"
    {:swagger {:tags ["users"]}
     :post {:summary    "user unfollow."
            :parameters {:body [:map
                                [:id string?]]}
            :responses  {200 {:body any?}}
            :handler    (fn [{{body     :body} :parameters
                              uinfo            :identity}]
                          {:status 200 :body
                           (user/unfollow (:db-conn _opts) uinfo body)})}}]

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
