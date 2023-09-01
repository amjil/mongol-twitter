(ns amjil.jirgee.web.controllers.ntf
  (:import
   [java.util UUID]))

(defn lastest-ntfs 
  [query-fn uinfo info]
  ;; delete old ntfs
  (query-fn :delete-old-ntfs
            {:user-id (UUID/fromString (:id uinfo))
             :last-time (:last-time info)})
  ;; query latest ntfs
  (let [limit (or (:limit info) 20)
        offset (or (:offset info) 0)]
    (query-fn :query-latest-ntfs 
              (assoc info 
                     :user-id
                     (UUID/fromString (:id uinfo))
                     :limit limit 
                     :offset offset))))