(ns amjil.jirgee.web.utils.token
  (:require
   [buddy.core.codecs :as codecs]
   [buddy.core.hash :as hash]
   [buddy.sign.jwt :as jwt :refer [encrypt]]
   [java-time :as time]
   [clojure.string :as str]))

(defn generate-token []
  (-> (hash/sha256 (.toString (java.util.UUID/randomUUID)))
      (codecs/bytes->hex)))

(defn jwt-token [secret id]
  (let [uuid   (str/replace (.toString (java.util.UUID/randomUUID)) #"-" "")
        exp    (-> (time/plus (time/zoned-date-time) (time/days 90))
                   time/instant
                   time/to-millis-from-epoch)
        claims {:id  id
                :jti uuid
                :exp exp}]
    (encrypt claims secret {:alg :a256kw
                            :enc :a128gcm})))
