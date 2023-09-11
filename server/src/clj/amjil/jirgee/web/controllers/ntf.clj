(ns amjil.jirgee.web.controllers.ntf
  (:import
   [java.util UUID]))

(defn newer-ntfs
  [query-fn uinfo info]
  ;; delete old ntfs
  (query-fn :delete-old-ntfs
            {:user_id (UUID/fromString (:id uinfo))
             :last_time (:last_time info)})
  ;; query latest ntfs
  (query-fn :query-latest-ntfs
            (assoc info
                   :user_id (UUID/fromString (:id uinfo)))))