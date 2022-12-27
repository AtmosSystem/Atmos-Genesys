(ns atmos-genesys.apis.users.api)


(def routes
  ["/users"
   ["/auth"
    ["/basic"
     ["/login" {:name       ::users-basic-auth-login
                :parameters {:body identity}
                :post       identity}]

     ["/:username/logout" {:name       ::users-basic-auth-logout
                           :parameters {:path {:username identity}}
                           :put        identity}]

     ["/:username/logged" {:name       ::users-basic-auth-logged?
                           :parameters {:path {:username identity}}
                           :get        identity}]]]])