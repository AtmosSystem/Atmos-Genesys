(ns atmos-genesys.apis.users.api
  (:require [atmos-genesys.apis.users.core :as c]
            [atmos-genesys.apis.users.spec :as user-spec]
            [atmos-web-kernel-reitit.core :as web]
            [reitit.coercion.spec]))

(def auth-routes
  ["/basic"
   ["/login/" {:name       ::basic-auth-login
               :coercion   reitit.coercion.spec/coercion
               :parameters {:body ::user-spec/user-credentials}
               :post       (web/web-handler
                             (fn [{:keys [parameters]}]
                               (let [credentials (-> parameters :body)]
                                 (c/b-login credentials))))}]

   ["/:session-id/logout/" {:name       ::basic-auth-logout
                            :coercion   reitit.coercion.spec/coercion
                            :parameters {:path {:session-id ::user-spec/session-id}}
                            :put        (web/web-handler
                                          (fn [{:keys [parameters]}]
                                            (let [{:keys [session-id]} (-> parameters :path)]
                                              (c/b-logout session-id))))}]

   ["/:session-id/logged/" {:name       ::basic-auth-logged?
                            :coercion   reitit.coercion.spec/coercion
                            :parameters {:path {:session-id ::user-spec/session-id}}
                            :get        (web/web-handler
                                          (fn [{:keys [parameters]}]
                                            (let [{:keys [session-id]} (-> parameters :path)]
                                              (c/b-logged? session-id))))}]])

(def registration-routes
  [["/:username/token/" {:name       ::registration-token
                         :coercion   reitit.coercion.spec/coercion
                         :parameters {:path {:username ::user-spec/username}}
                         :get        (web/web-handler
                                       (fn [{:keys [parameters]}]
                                         (let [{:keys [username]} (-> parameters :path)]
                                           (c/registration-token username))))}]

   ["/create/" {:name       ::create-registration
                :coercion   reitit.coercion.spec/coercion
                :parameters {:body ::user-spec/new-registration}
                :post       (web/web-handler
                              (fn [{:keys [parameters]}]
                                (let [{:keys [user-data registration-token]} (-> parameters :body)]
                                  (c/register-user user-data registration-token))))}]])


(def routes
  ["/users"
   ["/auth" auth-routes]
   ["/registration" registration-routes]])