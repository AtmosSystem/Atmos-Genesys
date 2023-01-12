(ns atmos-genesys.apis.users.spec
  (:require [atmos-genesys.services.regex :as regexes]
            [atmos-kernel.spec :as atmos-spec]
            [clojure.spec.alpha :as s]))

(s/def ::username (s/and ::atmos-spec/non-blank-string #(re-matches regexes/email %)))
(s/def ::password string?)
(s/def ::remember-me boolean?)
(s/def ::session-id ::atmos-spec/non-blank-string)
(s/def ::registration-token (s/and ::atmos-spec/non-blank-string #(>= 8 (count %))))
(s/def ::registration-type ::atmos-spec/non-blank-string)   ; email, social profiles
(s/def ::first-name string?)
(s/def ::last-name string?)

(s/def ::user-credentials (s/keys :req-un [::username ::password ::remember-me]))
(s/def ::user-data (s/keys :req-un [::registration-type ::username ::first-name ::last-name ::password]))
(s/def ::registration-token (s/keys :req-un [::username]))
(s/def ::create-registration (s/keys :req-un [::user-data ::registration-token]))

(def de-serializer-maps {:login               {:data-spec ::user-credentials}
                         :logout              {:data-spec ::session-id}
                         :logged?             {:data-spec ::session-id}
                         :registration-token  {:data-spec ::registration-token}
                         :create-registration {:data-spec ::create-registration}})

(defn valid-credentials? [credentials] (s/valid? ::user-credentials credentials))
(defn valid-username? [username] (s/valid? ::username username))
(defn valid-session-id? [session-id] (s/valid? ::session-id session-id))
(defn valid-user-data? [user-data] (s/valid? ::user-data user-data))