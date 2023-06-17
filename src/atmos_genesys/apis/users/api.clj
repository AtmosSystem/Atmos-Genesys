(ns atmos-genesys.apis.users.api
  (:require [atmos-genesys.apis.users.core :as c]
            [atmos-genesys.apis.users.spec :as user-spec]
            [atmos-kernel.serializer.core :as serializer]
            [atmos-web-kernel-reitit.core :as web]))

(def auth-routes
  ["/basic"
   ["/login/" {:name       ::basic-auth-login
               :parameters {:body ::user-spec/user-credentials}
               :post       (web/web-handler
                             (fn [{:keys [body-params]}]
                               (let [credentials (serializer/de-serialize body-params (:login user-spec/de-serializer-maps))]
                                 (c/b-login credentials))))}]

   ["/:session-id/logout/" {:name       ::basic-auth-logout
                            :parameters {:path {:session-id ::user-spec/session-id}}
                            :put        (web/web-handler
                                          (fn [{:keys [path-params]}]
                                            (let [{:keys [session-id]} (serializer/de-serialize path-params (:logout user-spec/de-serializer-maps))]
                                              (c/b-logout session-id))))}]

   ["/:session-id/logged/" {:name       ::basic-auth-logged?
                            :parameters {:path {:session-id ::user-spec/session-id}}
                            :get        (web/web-handler
                                          (fn [{:keys [path-params]}]
                                            (let [{:keys [session-id]} (serializer/de-serialize path-params (:logged? user-spec/de-serializer-maps))]
                                              (c/b-logged? session-id))))}]])

(def registration-routes
  [["/:username/token/" {:name       ::registration-token
                         :parameters {:path {:username ::user-spec/username}}
                         :get        (web/web-handler
                                       (fn [{:keys [path-params]}]
                                         (let [{:keys [username]} (serializer/de-serialize path-params (:registration-token user-spec/de-serializer-maps))]
                                           (c/registration-token username))))}]

   ["/create/" {:name       ::create-registration
                :parameters {:body ::user-spec/create-registration}
                :post       (web/web-handler
                              (fn [{:keys [body-params]}]
                                (let [{:keys [user-data registration-token]} (serializer/de-serialize body-params (:create-registration user-spec/de-serializer-maps))]
                                  (c/register-user user-data registration-token))))}]])


(def routes
  ["/users"
   ["/auth" auth-routes]
   ["/registration" registration-routes]])