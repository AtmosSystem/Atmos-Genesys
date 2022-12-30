(ns atmos-genesys.apis.users.core)

(defprotocol UserBasicAuthentication
  (b-login [credentials])
  (b-logout [session-id])
  (b-logged? [session-id]))


(defprotocol UserRegistration
  (register-user [user-data registration-token])
  (registration-token [username]))