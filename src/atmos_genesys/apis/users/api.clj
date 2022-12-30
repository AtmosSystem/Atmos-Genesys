(ns atmos-genesys.apis.users.api)


(def routes
  ["/users"
   ["/auth"
    ["/basic"
     ["/login" {:name       ::users-basic-auth-login
                :parameters {:body identity}
                :post       identity}]

     ["/:username/logout" {:name       ::users-basic-auth-logout
                           :parameters {:path {:session-id identity}}
                           :put        identity}]

     ["/:username/logged" {:name       ::users-basic-auth-logged?
                           :parameters {:path {:session-id identity}}
                           :get        identity}]]]

   ["/registration"
    ["/token" {:name       ::users-registration-token
               :parameters {:path {:username identity}}
               :get        identity}]
    ["/create" {:name       ::users-create-registration
                :parameters {:body identity}
                :post       identity}]]])