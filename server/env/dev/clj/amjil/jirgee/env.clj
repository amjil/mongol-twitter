(ns amjil.jirgee.env
  (:require
    [clojure.tools.logging :as log]
    [amjil.jirgee.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init       (fn []
                 (log/info "\n-=[jirgee starting using the development or test profile]=-"))
   :start      (fn []
                 (log/info "\n-=[jirgee started successfully using the development or test profile]=-"))
   :stop       (fn []
                 (log/info "\n-=[jirgee has shut down successfully]=-"))
   :middleware wrap-dev
   :opts       {:profile       :dev
                :persist-data? true}})
