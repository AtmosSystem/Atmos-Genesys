(ns atmos-genesys.services.data.core
  (:require [atmos-data-kernel.core :as data-core]
            [atmos-kernel.configuration :as config]
            [clojure.spec.alpha :as s]))

; Simple in-memory cache system
(def data-devices-cache (atom {}))

(defn data-device*
  ([device-name configuration-file]
   (if-let [data-devices (config/read-edn configuration-file)]
     (if-let [data-device (device-name data-devices)]
       (if data-device
         (let [{:keys [name service]} (data-core/data-device data-device)]
           (if-let [factory (name @data-devices-cache)]
             factory
             (let [{:keys [factory args]} service]
               (when-let [factory (apply factory args)]
                 (swap! data-devices-cache assoc name factory)
                 factory))))))))
  ([device-name]
   (data-device* device-name "data-devices")))

(def data-device (memoize data-device*))


(s/fdef data-device
        :args (s/cat :device-name keyword?
                     :configuration-file string?))