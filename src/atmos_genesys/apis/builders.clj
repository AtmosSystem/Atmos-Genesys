(ns atmos-genesys.apis.builders
  (:require [atmos-data-kernel.services :as data-service]
            [atmos-kernel.serializer.core :as serializer]
            [atmos-web-kernel-reitit.core :as web]
            [inflections.core :as inflections])
  (:import (java.security InvalidParameterException)))

(defn- route-child-name
  [ns prefix]
  (apply keyword (map name [ns prefix])))

(defn- route-handler
  [route-type handlers & {:keys [default required] :or {default nil required false}}]
  (if-let [route-handler (route-type handlers)]
    (let [{:keys [specs handler]} route-handler]
      [specs (or handler default)])
    (if-not required
      [nil default]
      (throw (InvalidParameterException. "A valid route type is required on handler")))))

(defmulti child-route (fn [ns route-type parent-route-name handler-serializers handlers] (keyword route-type)))

(defmethod child-route :all [ns _ parent-route-name handler-serializers handlers]
  (let [full-route-name (route-child-name ns :all)
        {:keys [_ handler]} (route-handler :all handlers :default data-service/all)]
    ["/all/" {:name        full-route-name
              :conflicting true
              :get         (web/web-handler
                             (fn [_]
                               (handler parent-route-name))

                             :serializers (handler-serializers :all))}]))

(defmethod child-route :one [ns _ parent-route-name handler-serializers handlers]
  (let [full-route-name (route-child-name ns :one)
        {:keys [specs handler]} (route-handler :one handlers :default data-service/get)]
    ["/:id/" {:name        full-route-name
              :parameters  {:path {:id specs}}
              :conflicting true
              :get         (web/web-handler
                             (fn [{:keys [path-params]} request]
                               (let [{:keys [id]} path-params
                                     id (serializer/de-serialize id (-> request meta :data-de-serializer))]
                                 (handler parent-route-name :by id)))

                             :serializers (handler-serializers :one))}]))

(defmethod child-route :create [ns _ _ handler-serializers handlers]
  (let [full-route-name (route-child-name ns :create)
        {:keys [specs handler]} (route-handler :create handlers :required true)]
    ["/" {:name        full-route-name
          :parameters  {:body specs}
          :conflicting true
          :post        (web/web-handler
                         (fn [{:keys [body-params]} request]
                           (let [data (serializer/de-serialize body-params (-> request meta :data-de-serializer))]
                             (handler data)))
                         :serializers (handler-serializers :create))}]))

(defmethod child-route :update [ns _ _ handler-serializers handlers]
  (let [full-route-name (route-child-name ns :update)
        {:keys [specs handler]} (route-handler :update handlers :required true)]
    ["/" {:name        full-route-name
          :parameters  {:body specs}
          :conflicting true
          :put         (web/web-handler
                         (fn [{:keys [body-params]} request]
                           (let [data (serializer/de-serialize body-params (-> request meta :data-de-serializer))]
                             (handler data)))
                         :serializers (handler-serializers :update))}]))

(defmethod child-route :delete [ns _ _ handler-serializers handlers]
  (let [full-route-name (route-child-name ns :delete)
        {:keys [specs handler]} (route-handler :update handlers :required true)]
    ["/:id/" {:name        full-route-name
              :parameters  {:path {:id specs}}
              :conflicting true
              :delete      (web/web-handler
                             (fn [{:keys [path-params]} request]
                               (let [{:keys [id]} path-params
                                     id (serializer/de-serialize id (-> request meta :data-de-serializer))]

                                 (handler id)))
                             :serializers (handler-serializers :delete))}]))


(defn simple-routes
  [routes]
  (let [{:keys [serializers handlers]} routes
        {api-name :name} routes
        [singular-name namespace-name parent-route] [(inflections/singular api-name) (namespace api-name) (str "/" (name api-name))]
        child-route-simplified (fn [route-type]
                                 (child-route namespace-name route-type
                                              (if (= route-type :all) api-name singular-name)
                                              serializers handlers))]
    [parent-route
     (child-route-simplified :all)
     (child-route-simplified :one)
     (child-route-simplified :create)
     (child-route-simplified :update)
     (child-route-simplified :delete)]))