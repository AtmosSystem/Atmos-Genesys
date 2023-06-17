(ns atmos-genesys.apis.builders
  (:require [atmos-data-kernel.services :as data-service]
            [atmos-web-kernel-reitit.core :as web]
            [inflections.core :as inflections]
            [reitit.coercion.spec])
  (:import (java.security InvalidParameterException)))

(defn- route-child-names
  [api-name prefix]
  (let [api-namespace (namespace api-name)
        route-name (keyword api-namespace (name prefix))
        single-api-name (name api-name)
        single-api-name (if (= prefix :all) single-api-name (inflections/singular single-api-name))]
    [route-name single-api-name]))

(defn- route-handler
  [route-type handlers & {:keys [default required] :or {default nil required false}}]
  (if-let [route-handler (route-type handlers)]
    (let [{:keys [specs handler]} route-handler
          {:keys [request response] :or {request nil response nil}} specs]
      [request response (or handler default)])
    (if-not required
      [nil nil default]
      (throw (InvalidParameterException. "A valid route type is required on handler")))))

(defmulti child-route (fn [api-name route-type handlers] (keyword route-type)))

(defmethod child-route :all [api-name _ handlers]
  (let [[route-name single-api-name] (route-child-names api-name :all)
        [_ response-specs handler] (route-handler :all handlers :default data-service/all)]
    ["/all/" {:name        route-name
              :coercion    reitit.coercion.spec/coercion
              :responses   {200 {:body response-specs}}
              :conflicting true
              :get         (web/web-handler
                             (fn [_]
                               (handler single-api-name)))}]))

(defmethod child-route :one [api-name _ handlers]
  (let [[route-name single-api-name] (route-child-names api-name :one)
        [request-specs response-specs handler] (route-handler :one handlers :default data-service/get)]
    ["/:id/" {:name        route-name
              :coercion    reitit.coercion.spec/coercion
              :parameters  {:path {:id request-specs}}
              :responses   {200 {:body response-specs}}
              :conflicting true
              :get         (web/web-handler
                             (fn [{:keys [parameters]}]
                               (let [{:keys [id]} (-> parameters :path)]
                                 (handler single-api-name :by id))))}]))

(defmethod child-route :create [api-name _ handlers]
  (let [[route-name _] (route-child-names api-name :create)
        [request-specs response-specs handler] (route-handler :create handlers :required true)]
    ["/" {:name        route-name
          :coercion    reitit.coercion.spec/coercion
          :parameters  {:body request-specs}
          :responses   {200 {:body response-specs}}
          :conflicting true
          :post        (web/web-handler
                         (fn [{:keys [parameters]}]
                           (let [data (-> parameters :body)]
                             (handler data))))}]))

(defmethod child-route :update [api-name _ handlers]
  (let [[route-name _] (route-child-names api-name :update)
        [request-specs response-specs handler] (route-handler :update handlers :required true)]
    ["/" {:name        route-name
          :coercion    reitit.coercion.spec/coercion
          :parameters  {:body request-specs}
          :responses   {200 {:body response-specs}}
          :conflicting true
          :put         (web/web-handler
                         (fn [{:keys [parameters]}]
                           (let [data (-> parameters :body)]
                             (handler data))))}]))

(defmethod child-route :delete [api-name _ handlers]
  (let [[route-name _] (route-child-names api-name :delete)
        [request-specs response-specs handler] (route-handler :update handlers :required true)]
    ["/:id/" {:name        route-name
              :coercion    reitit.coercion.spec/coercion
              :parameters  {:path {:id request-specs}}
              :responses   {200 {:body response-specs}}
              :conflicting true
              :delete      (web/web-handler
                             (fn [{:keys [parameters]}]
                               (let [{:keys [id]} (-> parameters :path)]
                                 (handler id))))}]))


(defn simple-routes
  [routes]
  (let [{api-name :name handlers :handlers} routes
        route-name (str "/" (-> api-name name keyword))
        child-route-simplified (fn [route-type] (child-route api-name route-type handlers))]
    [route-name
     (child-route-simplified :all)
     (child-route-simplified :one)
     (child-route-simplified :create)
     (child-route-simplified :update)
     (child-route-simplified :delete)]))