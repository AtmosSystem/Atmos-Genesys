(ns atmos-genesys.services.data.core
  (:require
    [atmos-data-kernel.core :as data-core]
    [atmos-kernel.configuration :as config]
    [clojure.core.memoize :as memo]
    [clojure.spec.alpha :as s]))

; Simple in-memory cache system
(def data-devices-cache (atom {}))

(defn data-device*
  ([device-name configuration-file]
   (if-let [configurations (config/read-edn configuration-file)]
     (if-let [data-devices (-> configurations :data-devices data-core/data-devices)]
       (if-let [{:keys [factory args]} (device-name data-devices)]
         (if-let [factory-cached (device-name @data-devices-cache)]
           factory-cached
           (when-let [new-factory (apply factory args)]
             (swap! data-devices-cache assoc device-name new-factory)
             new-factory))))))
  ([device-name]
   (data-device* device-name :settings)))

(def data-device (memo/lu data-device*))


(s/fdef data-device
        :args (s/cat :device-name keyword?
                     :configuration-file keyword?))