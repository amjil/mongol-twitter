(ns amjil.jirgee.web.utils.db
  (:require
   [next.jdbc :as jdbc]
   [next.jdbc.sql :as sql]
   [next.jdbc.result-set :as rs]))

(defn update! [conn t w s]
  (sql/update! conn t w s
               {:builder-fn rs/as-unqualified-lower-maps}))

(defn get-by-id [conn t id]
  (sql/get-by-id conn t id
                 {:builder-fn rs/as-unqualified-lower-maps}))

(defn find-by-keys
  ([conn t w]
   (sql/find-by-keys conn t w
                     {:builder-fn rs/as-unqualified-lower-maps}))
  ([conn t w ex]
   (sql/find-by-keys conn t w
                     (merge
                      {:builder-fn rs/as-unqualified-lower-maps}
                      ex))))

(defn find-one-by-keys [conn t w]
  (first
   (sql/find-by-keys conn t w
                     {:builder-fn rs/as-unqualified-lower-maps})))

(defn insert! [conn t info]
  (sql/insert! conn t info
               {:builder-fn rs/as-unqualified-lower-maps}))

(defn delete! [conn t w]
  (sql/delete! conn t w))

(defn execute!
  ([conn sqlmap]
   (jdbc/execute! conn sqlmap
                  {:builder-fn rs/as-unqualified-lower-maps}))
  ([conn sqlmap opt]
   (jdbc/execute! conn sqlmap
                  (merge
                   {:builder-fn rs/as-unqualified-lower-maps}
                   opt))))