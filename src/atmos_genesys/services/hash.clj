(ns atmos-genesys.services.hash
  (:require
    [buddy.core.codecs :refer [bytes->hex]]
    [buddy.core.hash :as hash]
    [clojure.spec.alpha :as s]))

(def default-algorithms {:sha1   hash/sha1
                         :sha256 hash/sha256
                         :sha512 hash/sha512})

(defn encode
  "Encode data using the selected algorithm"
  [data algorithm & {:keys [algorithms] :or {algorithms default-algorithms}}]
  (if-let [algorithm (-> algorithms algorithm)]
    (bytes->hex (algorithm data))
    (throw (NoSuchMethodException. (str (name algorithm) " algorithm not found")))))

(s/fdef encode
        :args (s/cat :data any? :algorithm keyword?)
        :ret string?)