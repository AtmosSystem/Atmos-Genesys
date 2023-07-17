(ns atmos-genesys.services.security
  (:require
    [buddy.core.codecs :refer [bytes->hex]]
    [buddy.core.hash :as hash]
    [buddy.hashers :as hashers]
    [clojure.spec.alpha :as s]))

(def default-encode-algorithms {:sha1   hash/sha1
                                :sha256 hash/sha256
                                :sha512 hash/sha512})


(def default-password-algorithms {:sha1   :pbkdf2+sha1
                                  :sha256 :pbkdf2+sha256
                                  :sha512 :pbkdf2+sha512})

(defn encode-data
  "Encode data"
  [data & {:keys [algorithm algorithms] :or {algorithm :sha256 algorithms default-encode-algorithms}}]
  (if-let [algorithm (-> algorithms algorithm)]
    (bytes->hex (algorithm data))
    (throw (ex-info "Algorithm not found" {:algorithm algorithm}))))

(s/fdef encode-data
        :args (s/cat :data any?)
        :ret string?)

(defn encode-password
  "Encode password"
  [password & {:keys [algorithm algorithms] :or {algorithm :sha256 algorithms default-password-algorithms}}]
  (if-let [algorithm (-> algorithms algorithm)]
    (hashers/derive password {:alg algorithm})
    (throw (ex-info "Algorithm not found" {:algorithm algorithm}))))

(s/fdef encode-password
        :args (s/cat :data string?)
        :ret string?)

(defn verify-password
  [incoming-password encrypted-password]
  (hashers/verify incoming-password encrypted-password))

(s/fdef verify-password
        :args (s/cat :incoming-password string? :encrypted-password string?)
        :ret map?)