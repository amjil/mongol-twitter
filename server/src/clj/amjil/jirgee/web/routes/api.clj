(ns amjil.jirgee.web.routes.api
  (:require
   [amjil.jirgee.web.controllers.health :as health]
   [amjil.jirgee.web.controllers.auth :as auth]
   [amjil.jirgee.web.controllers.profile :as profile]
   [amjil.jirgee.web.controllers.tweet :as tweet]
   [amjil.jirgee.web.controllers.user :as user]
   [amjil.jirgee.web.controllers.ntf :as ntf]
   [amjil.jirgee.web.controllers.ws :as ws]
   [amjil.jirgee.web.controllers.file :as file]
   [amjil.jirgee.web.middleware.exception :as exception]
   [amjil.jirgee.web.middleware.formats :as formats]
   [amjil.jirgee.web.middleware.core :as middleware]
   [ring.util.http-response :refer [file-response]]
   [spec-tools.data-spec :as ds]
   [clojure.spec.alpha :as s]
   [integrant.core :as ig]
   [reitit.coercion.malli :as malli]
   [reitit.ring.coercion :as coercion]
   [reitit.ring.middleware.multipart :as multipart]
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
            :parameters {:query {:user_id string?}}
            :responses {200 {:body any?}}
            :handler (fn [{{:keys [query]} :parameters uinfo :identity}]
                       {:status 200 :body
                        (profile/user-info (:query-fn _opts) uinfo query)})}}]
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
   ["/explore"
    {:swagger {:tags ["explore"]}
     :get {:summary "explore users"
           :middleware [[middleware/wrap-restricted]]
           :parameters {:query [:map
                                [:limit {:optional true} int?]
                                [:offset {:optional true} int?]]}
           :responses {200 {:body any?}}
           :handler (fn [{{:keys [query]} :parameters uinfo :identity}]
                      {:status 200 :body
                       (user/explore (:db-conn _opts) uinfo query)})}}]
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
                       (tweet/query-tweet (:query-fn _opts) uinfo query)})}}]
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
     :post {:summary    "unfavorite tweet."
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
                                [:last_id string?]]}
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
   ["/notifications/newer"
    {:swagger {:tags ["notifications"]}
     :get {:summary    "query newer notifications"
           :parameters {:query [:map
                                [:last_time string?]]}
           :responses  {200 {:body any?}}
           :handler    (fn [{{params     :query} :parameters
                             uinfo            :identity}]
                         {:status 200 :body
                          (ntf/newer-ntfs (:query-fn _opts) uinfo params)})}}]

   ["/file/:id/:year/:month/:filename"
    {:get {:summary    "get a file"
           :swagger    {:tags ["files"]}
           :parameters {:path {:filename string?
                               :year     string?
                               :month    string?
                               :id       string?}}
           :handler    (fn [{{{filename :filename
                               year     :year
                               month    :month
                               id       :id} :path} :parameters
                             token                  :identity}]
                         (file-response (str "/img/" id "/" year "/" month "/" filename) {:root "public"}))}}]
   ["/file/upload"
    {:post {:summary    "put a file"
            :swagger    {:tags ["files"]}
            :parameters {:multipart {:files (s/coll-of multipart/temp-file-part)}}
            :handler    (fn [{params :multipart-params
                              token  :identity}]
                          (file/add-file (:db-conn _opts) params token))}}]
   ["/file/delete/:id"
    {:get {:summary    "put a file"
           :swagger    {:tags ["files"]}
           :parameters {:path {:id int?}}
           :handler    (fn [{{{id :id} :path} :parameters
                             token  :identity}]
                         (file/remove-file (:db-conn _opts) id token))}}]

   ["/fail"
    {:get (fn [_]
            (throw (ex-info "fail" {:type :system.exception/not-found})))}]
   ["/ws/:token"
    #(ws/handler (select-keys _opts [:db-conn :query-fn :token-secret]) %)]])

(derive :reitit.routes/api :reitit/routes)

(defmethod ig/init-key :reitit.routes/api
  [_ {:keys [base-path]
      :or   {base-path ""}
      :as   opts}]
  [base-path route-data (api-routes opts)])
