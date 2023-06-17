(ns atmos-genesys.apis.builders
  (:require [atmos-kernel.serializer.core :as serializer]
            [atmos-web-kernel-reitit.core :as web]
            [clojure.string :as string]))

(defn- route-child-name
  [prefix route-name]
  (keyword (string/join "-" (map name [prefix route-name]))))

(defmulti child-route (fn [route-name parent-route-name handler-serializers handlers] (keyword route-name)))

(defmethod child-route :all [_ parent-route-name handler-serializers handlers]
  (let [route-name (route-child-name :all parent-route-name)
        {:keys [handler _]} (:all handlers)]
    ["/all/" {:name route-name
              :get  (web/web-handler
                      (handler-serializers route-name)
                      (fn [_]
                        (handler parent-route-name)))}]))

(defmethod child-route :single [_ parent-route-name handler-serializers handlers]
  (let [route-name (route-child-name :get parent-route-name)
        {:keys [handler specs]} (:single handlers)]
    ["/:id/" {:name       route-name
              :parameters {:path {:id specs}}
              :get        (web/web-handler
                            (handler-serializers route-name)

                            (fn [{:keys [path-params]} request]
                              (let [{:keys [id]} path-params
                                    id (serializer/de-serialize id (-> request meta :data-de-serializer))]

                                (handler parent-route-name :by id))))}]))

(defmethod child-route :create [_ parent-route-name handler-serializers handlers]
  (let [route-name (route-child-name :create parent-route-name)
        {:keys [handler specs]} (:create handlers)]
    ["/" {:name       route-name
          :parameters {:body specs}
          :post       (web/web-handler
                        (handler-serializers route-name)

                        (fn [{:keys [body-params]} request]
                          (let [data (serializer/de-serialize body-params (-> request meta :data-de-serializer))]
                            (handler data))))}]))

(defmethod child-route :update [_ parent-route-name handler-serializers handlers]
  (let [route-name (route-child-name :update parent-route-name)
        {:keys [handler specs]} (:update handlers)]
    ["/" {:name       route-name
          :parameters {:body specs}
          :put        (web/web-handler
                        (handler-serializers route-name)

                        (fn [{:keys [body-params]} request]
                          (let [data (serializer/de-serialize body-params (-> request meta :data-de-serializer))]
                            (handler data))))}]))

(defmethod child-route :delete [_ parent-route-name handler-serializers handlers]
  (let [route-name (route-child-name :delete parent-route-name)
        {:keys [handler specs]} (:delete handlers)]
    ["/:id/" {:name       route-name
              :parameters {:path {:id specs}}
              :delete     (web/web-handler
                            (handler-serializers route-name)

                            (fn [{:keys [path-params]} request]
                              (let [{:keys [id]} path-params
                                    id (serializer/de-serialize id (-> request meta :data-de-serializer))]

                                (handler id))))}]))


(defn simple-routes
  [routes]
  (let [{:keys [names serializers handlers]} routes
        [singular-name plural-name] names
        parent-route (str "/" (name plural-name))]
    [parent-route
     (child-route :all plural-name serializers handlers)
     (child-route :single singular-name serializers handlers)
     (child-route :create singular-name serializers handlers)
     (child-route :update singular-name serializers handlers)
     (child-route :delete singular-name serializers handlers)]))