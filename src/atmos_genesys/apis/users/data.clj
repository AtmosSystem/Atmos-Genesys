(ns atmos-genesys.apis.users.data
  (:require [atmos-data-kernel.core :refer [add-data delete-key get-by key-exists? set-key-value]]
            [atmos-genesys.apis.users.spec :as user-spec]
            [atmos-genesys.services.data.core :refer [data-device]]
            [atmos-genesys.services.hash :as hash]
            [atmos-logs.core :as log]
            [clojure.spec.alpha :as s])
  (:import (java.util UUID)))

(def users (data-device :users))
(def sessions (data-device :sessions))
(def session-expiration-time 20)                            ; In seconds
(def registrations (data-device :registration))
(def registration-expiration-time 2000)                     ; In seconds

(defn create-data-key [key-type data]
  (str (name key-type) ":" data))

(defn generate-session->
  [username remember-me]
  (when-let [session-id (hash/encode (-> (UUID/randomUUID) str) :sha1)]
    (let [data-key (create-data-key :session session-id)
          encrypted-username (hash/encode username :sha256)]
      (set-key-value sessions data-key {:username encrypted-username}
                     {:expire (if-not remember-me {:type :expire :value session-expiration-time})}))
    (do
      (log/info "New logging session generated")
      session-id)))

(s/fdef generate-session->
        :args (s/cat :username ::user-spec/username :remember-me ::user-spec/remember-me)
        :ret ::user-spec/session-id)

(defn user-credentials->
  [username]
  (if-let [user-data (first (get-by users #(= (:username %) username)))]
    (select-keys user-data [:username :password])))

(s/fdef user-credentials->
        :args (s/cat :username ::user-spec/username)
        :ret ::user-spec/user-credentials)

(defn delete-session->
  [session-id]
  (let [data-key (create-data-key :session session-id)]
    (if-let [deleted? (delete-key sessions data-key)]
      (case deleted?
        0 false
        1 (do
            (log/info (str "The session [" session-id "] was deleted"))
            true)))))

(s/fdef delete-session->
        :args (s/cat :session-id ::user-spec/session-id)
        :ret boolean?)

(defn logged-session->?
  [session-id]
  (let [data-key (create-data-key :session session-id)]
    (if-let [exists? (key-exists? sessions data-key)]
      (case exists?
        0 false
        1 true))))

(s/fdef logged-session->?
        :args (s/cat :session-id ::user-spec/session-id)
        :ret boolean?)

(defn generate-registration-token->
  [username]
  (when-let [token (hash/encode (-> (UUID/randomUUID) str) :sha256)]
    (let [data-key (create-data-key :registration token)
          encrypted-username (hash/encode username :sha256)]
      (set-key-value registrations data-key {:username encrypted-username}
                     {:type :expire :value registration-expiration-time}))
    (do
      (log/info "New registration token generated")
      token)))

(s/fdef generate-registration-token->
        :args (s/cat :username ::user-spec/username)
        :ret ::user-spec/registration-token)

(defn valid-registration-token->?
  [token]
  (let [data-key (create-data-key :registration token)]
    (if-let [exists? (key-exists? sessions data-key)]
      (case exists?
        0 false
        1 true))))

(s/fdef valid-registration-token->?
        :args (s/cat :token ::user-spec/registration-token)
        :ret boolean?)

(defn register-user->
  [user-data]
  (let [encrypted-password (hash/encode (:password user-data) :sha256)
        user-data (assoc user-data :password encrypted-password)]
    (if-let [data-added (add-data users user-data)]
      (do
        (log/info "New user registered")
        data-added))))

(s/fdef register-user->
        :args (s/cat :user-data ::user-spec/user-data))
