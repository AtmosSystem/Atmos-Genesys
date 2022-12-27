(ns atmos-genesys.apis.users.core)

(defprotocol AUserBasicAuthentication
  (B-login [credentials])
  (B-logout [username])
  (B-logged? [username]))
