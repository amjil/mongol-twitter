(ns amjil.jirgee.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init       (fn []
                 (log/info "\n-=[jirgee starting]=-"))
   :start      (fn []
                 (log/info "\n-=[jirgee started successfully]=-"))
   :stop       (fn []
                 (log/info "\n-=[jirgee has shut down successfully]=-"))
   :middleware (fn [handler _] handler)
   :opts       {:profile :prod}})
