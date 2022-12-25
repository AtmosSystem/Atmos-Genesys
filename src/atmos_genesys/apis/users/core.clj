(ns atmos-genesys.apis.users.core)

(defprotocol AUserAuthentication
  (login [credentials] [user password])
  (logout [username])
  (logged? [username]))
