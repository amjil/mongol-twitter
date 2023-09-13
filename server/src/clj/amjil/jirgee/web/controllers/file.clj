(ns amjil.jirgee.web.controllers.file
  (:require 
   [clojure.string :as str]
   [clojure.java.io :as io]
   [amjil.jirgee.web.utils.db :as db]
   
   [java-time :as time]
   [ring.util.http-response :refer [content-type header ok file-response]]))


(defn add-file [conn file uinfo]
  (let [path     "public/img"
        time-now (str/split (time/format "yyyy-MM" (time/local-date)) #"-")
        uuid     (str (str/replace (java.util.UUID/randomUUID) #"-" "") "." (-> file :filename (str/split #"\.") last))
        file-url (apply str (interpose "/" (concat ["/api/file" (str (:id uinfo))] time-now [uuid])))
        filename (apply str (interpose "/" (concat [path (str (:id uinfo))] time-now [uuid])))]

    (io/make-parents filename)
    (io/copy (:tempfile file) (io/file filename))

      (let [params {:filename (:filename file)
                    :url      file-url}
            result (db/insert! conn :attach_file params)]
        (ok {:success true
             :msg     "success"
             :data    (select-keys result [:id :url :filename])}))))