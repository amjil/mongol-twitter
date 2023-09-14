(ns amjil.jirgee.web.controllers.file
  (:require 
   [clojure.string :as str]
   [clojure.java.io :as io]
   [clojure.tools.logging :as log]
   [amjil.jirgee.web.utils.db :as db]
   
   [java-time :as time]
   [ring.util.http-response :refer [content-type header ok file-response]])
  (:import
   [java.util UUID]))


(defn add-file [conn params uinfo]
  (let [file (get params "file")
        path     "public/img"
        time-now (str/split (time/format "yyyy-MM" (time/local-date)) #"-")
        uuid     (str (str/replace (UUID/randomUUID) #"-" "") "." (-> file :filename (str/split #"\.") last))
        file-url (apply str (interpose "/" (concat ["/api/file" (str (:id uinfo))] time-now [uuid])))
        filename (apply str (interpose "/" (concat [path (str (:id uinfo))] time-now [uuid])))]

    (io/make-parents filename)
    (io/copy (:tempfile file) (io/file filename))

      (let [params {:filename (:filename file)
                    :url      file-url
                    :user_id (:id uinfo)}
            result (db/insert! conn :attach_file params)]
        (ok (select-keys result [:id :url :filename])))))

(defn remove-file [conn id uinfo]
  (let [result (db/find-one-by-keys conn :attach_file {:user_id (UUID/fromString (:id uinfo))
                                                       :id id})]
    (when-not (empty? result)
      (let [path     "public/img/"
            filename (->> (subs (:url result) 10)
                          (str path))]
        (io/delete-file filename))
      (let [result (db/delete! conn :attach_file {:id id})]
        (log/warn "delete result " result)))
    (ok)))