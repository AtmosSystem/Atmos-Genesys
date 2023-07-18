(ns atmos-genesys.apis.builders
  (:require
    [atmos-data-kernel.services :as data-service]
    [atmos-logs.core :as log]
    [inflections.core :as inflections]
    [reitit.coercion.spec])
  (:import (clojure.lang ExceptionInfo)))

(def valid-responses {200 :ok
                      201 :created})

(def invalid-responses {204 "The resource was not created"
                        404 "Resource not found"
                        400 "Bad request. Can't process the request"})

(defmacro try-response-or-catch
  [http-code default-code & forms]
  `(let [default-message# (get invalid-responses ~default-code)]
     (try

       (if-let [data# (do ~@forms)]
         {~http-code data#}
         (throw (ex-info default-message# {})))

       (catch AssertionError e# (log/exception e#) {~default-code default-message#})
       (catch ExceptionInfo e#
         (log/exception e#)

         (let [data# {:type :exception :message (ex-message e#) :data (ex-data e#)}]
           {~default-code data#})))))

(defmacro build-response-or-catch
  [http-code default-code]
  (let [http-code-name (name (get valid-responses http-code))
        default-code-name (str default-code)
        fn-name (symbol (str "try-" http-code-name "-or-" default-code-name))
        fn-args 'forms]
    `(defn ~fn-name
       [& ~fn-args]
       (try-response-or-catch ~http-code ~default-code (last ~fn-args)))))

(declare try-ok-or-400 try-ok-or-404)
(build-response-or-catch 200 400)
(build-response-or-catch 200 404)

(declare try-created-or-400 try-created-or-204)
(build-response-or-catch 201 400)
(build-response-or-catch 201 204)


(defn- route-child-names
  [api-name prefix]
  (let [api-namespace (namespace api-name)
        route-name (keyword api-namespace (name prefix))
        single-api-name (name api-name)
        single-api-name (if (= prefix :collection) single-api-name (inflections/singular single-api-name))]
    [route-name single-api-name]))

(defn- route-handler
  [route-type handlers & {:keys [default required] :or {default nil required false}}]
  (if-let [route-handler (route-type handlers)]
    (let [{:keys [specs handler] :or {specs {}}} route-handler
          {:keys [request response] :or {request nil response nil}} specs]
      [request response (or handler default)])
    (if-not required
      [nil nil default]

      (throw (ex-info "A valid route type is required on handler" {})))))

(defmulti child-route (fn [api-name route-type http-handler handlers] (keyword route-type)))

(defmethod child-route :collection [api-name _ http-handler handlers]
  (let [[route-name single-api-name] (route-child-names api-name :collection)

        {collection-handlers :collection} handlers

        [_ all-response-spec all-handler] (route-handler :all collection-handlers :default data-service/all)
        [create-request-spec create-response-spec create-handler] (route-handler :create collection-handlers :required true)]
    ["" {:name     route-name
         :coercion reitit.coercion.spec/coercion

         :get      {:responses {200 {:body all-response-spec}}
                    :handler   (http-handler
                                 (fn [_]
                                   (all-handler single-api-name)))}

         :post     {:parameters {:body create-request-spec}
                    :responses  {201 {:body create-response-spec}}
                    :handler    (http-handler
                                  (fn [{:keys [parameters]}]
                                    (let [data (-> parameters :body)]
                                      (try-created-or-204
                                        (let [data-id (create-handler data)]
                                          {:id data-id})))))}}]))

(defmethod child-route :document [api-name _ http-handler handlers]
  (let [[route-name single-api-name] (route-child-names api-name :document)

        {document-handlers :document} handlers
        {request-specs :request response-spec :response} (-> document-handlers :specs)
        {path-request-spec :path body-request-spec :body} request-specs

        [_ _ one-handler] (route-handler :one document-handlers :default data-service/get)
        [_ _ update-handler] (route-handler :update document-handlers :required true)
        [_ _ delete-handler] (route-handler :delete document-handlers :required true)]
    ["/{id}" {:name     route-name
              :coercion reitit.coercion.spec/coercion

              :get      {:parameters {:path {:id path-request-spec}}
                         :responses  {200 {:body response-spec}}
                         :handler    (http-handler
                                       (fn [{:keys [parameters]}]
                                         (let [{:keys [id]} (-> parameters :path)]
                                           (try-ok-or-404 (one-handler single-api-name :by id)))))}

              :put      {:parameters {:path {:id path-request-spec}
                                      :body body-request-spec}
                         :responses  {200 {:body map?}}
                         :handler    (http-handler
                                       (fn [{:keys [parameters]}]
                                         (let [data (-> parameters :body)
                                               document-id (-> parameters :path :id)]
                                           (try-ok-or-400
                                             (let [updated? (update-handler document-id data)]
                                               {:updated updated?})))))}

              :delete   {:parameters {:path {:id path-request-spec}}
                         :responses  {200 {:body map?}}
                         :handler    (http-handler
                                       (fn [{:keys [parameters]}]
                                         (let [{:keys [id]} (-> parameters :path)]
                                           (try-ok-or-400
                                             (let [deleted? (delete-handler id)]
                                               {:deleted deleted?})))))}}]))


(defn simple-routes
  [routes]
  (let [{api-name :name http-handler :http-handler handlers :handlers} routes
        route-name (str "/" (-> api-name name))
        child-route-simplified (fn [route-type] (child-route api-name route-type http-handler handlers))]
    [route-name
     (child-route-simplified :collection)
     (child-route-simplified :document)]))