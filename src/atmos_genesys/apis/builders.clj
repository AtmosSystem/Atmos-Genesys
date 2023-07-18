(ns atmos-genesys.apis.builders
  (:require
    [atmos-data-kernel.services :as data-service]
    [atmos-logs.core :as log]
    [inflections.core :as inflections]
    [reitit.coercion.spec])
  (:import
    (clojure.lang ExceptionInfo)))

(def default-responses {204 {204 "The resource was not created"}
                        404 {404 "Resource not found"}
                        400 {400 "Bad request. Can't process the request"}})

(defn try-response-or-catch
  ([handler default-response]
   (try-response-or-catch handler 200 default-response))
  ([handler http-code-or-fn default-response]
   (let [default-response (get default-responses default-response)]
     (try

       (if-let [data (handler)]
         (if (fn? http-code-or-fn) (http-code-or-fn data) {http-code-or-fn data})
         (throw (ex-info (-> default-response first val) {})))

       (catch AssertionError e (log/exception e) default-response)
       (catch ExceptionInfo e
         (log/exception e) (let [data (assoc (ex-data e) :message (ex-message e))]
                             {400 data}))))))


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
                    :responses  {201 {:body create-response-spec}
                                 400 {:body string?}}
                    :handler    (http-handler
                                  (fn [{:keys [parameters]}]
                                    (let [data (-> parameters :body)]
                                      (try-response-or-catch #(create-handler data)
                                                             (fn [data-id] {201 {:id data-id}})
                                                             400))))}}]))

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
                         :responses  {200 {:body response-spec}
                                      404 {:body string?}}
                         :handler    (http-handler
                                       (fn [{:keys [parameters]}]
                                         (let [{:keys [id]} (-> parameters :path)]
                                           (try-response-or-catch
                                             #(one-handler single-api-name :by id) 404))))}

              :put      {:parameters {:path {:id path-request-spec}
                                      :body body-request-spec}
                         :responses  {200 {:body map?}}
                         :handler    (http-handler
                                       (fn [{:keys [parameters]}]
                                         (let [data (-> parameters :body)
                                               document-id (-> parameters :path :id)]
                                           (try-response-or-catch #(update-handler document-id data)
                                                                  (fn [updated?] {:updated updated?}) 400))))}

              :delete   {:parameters {:path {:id path-request-spec}}
                         :responses  {200 {:body map?}
                                      400 {:body string?}}
                         :handler    (http-handler
                                       (fn [{:keys [parameters]}]
                                         (let [{:keys [id]} (-> parameters :path)]
                                           (try-response-or-catch #(delete-handler id)
                                                                  (fn [deleted?] {:deleted deleted?}) 400))))}}]))


(defn simple-routes
  [routes]
  (let [{api-name :name http-handler :http-handler handlers :handlers} routes
        route-name (str "/" (-> api-name name))
        child-route-simplified (fn [route-type] (child-route api-name route-type http-handler handlers))]
    [route-name
     (child-route-simplified :collection)
     (child-route-simplified :document)]))