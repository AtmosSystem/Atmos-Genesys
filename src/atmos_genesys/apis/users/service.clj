(ns atmos-genesys.apis.users.service
  (:require [atmos-genesys.apis.users.core :as user-core]
            [atmos-genesys.apis.users.data :as user-data]
            [atmos-genesys.apis.users.spec :as user-spec]
            [atmos-genesys.services.hash :as hash])
  (:import (java.util Map)))


(extend-protocol user-core/UserBasicAuthentication
  Map
  (b-login [credentials]
    {:pre [(user-spec/valid-credentials? credentials)]}
    (let [{:keys [username password remember-me]} credentials
          invalid-credentials-message "Invalid User/Password"
          user-password-map {:username username :password password}]
      (if-let [user-credentials (user-data/user-credentials-> username)]
        (let [password-encoded (hash/encode password :sha256)]
          (if (= (:password user-credentials) password-encoded)
            (user-data/generate-session-> username remember-me)
            (throw (ex-info invalid-credentials-message user-password-map))))
        (throw (ex-info invalid-credentials-message user-password-map)))))
  String
  (b-logout [session-id]
    {:pre [(user-spec/valid-session-id? session-id)]}
    (user-data/delete-session-> session-id))

  (b-logged? [session-id]
    {:pre [(user-spec/valid-session-id? session-id)]}
    (user-data/logged-session->? session-id)))

(extend-protocol user-core/UserRegistration
  String
  (registration-token [username]
    {:pre [(user-spec/valid-username? username)]}
    (user-data/generate-registration-token-> username))
  Map
  (register-user [user-data registration-token]
    {:pre [(user-spec/valid-user-data? user-data)]}
    (if (user-data/valid-registration-token->? registration-token)
      (if-let [user-registered (user-data/register-user-> user-data)]
        (if (user-data/delete-registration-token-> registration-token)
          user-registered))
      (throw (ex-info "Invalid registration token" {:token registration-token})))))