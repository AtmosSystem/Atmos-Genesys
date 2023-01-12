(ns atmos-genesys.services.logger
  (:require [atmos-logs.core :as log-core]
            [atmos-logs.spec :as log-spec]
            [taoensso.timbre :as timbre])
  (:import (java.util Map)))

(defn- log-valid?
  [log-data]
  (if (log-spec/log-valid? log-data) log-data))

(extend-protocol log-core/ILoggerActions
  Map
  (info [this]
    (if-let [data (log-valid? this)]
      (timbre/info data)))

  (debug [this]
    (if-let [data (log-valid? this)]
      (timbre/debug data)))

  (error [this]
    (if-let [data (log-valid? this)]
      (timbre/error data)))

  (trace [this]
    (if-let [data (log-valid? this)]
      (timbre/trace data)))

  (warn [this]
    (if-let [data (log-valid? this)]
      (timbre/warn data)))

  (exception [this]
    (log-core/error this))

  (fatal [this]
    (log-core/error this))
  String
  (info [this] (timbre/info this))
  (debug [this] (timbre/debug this))
  (error [this] (timbre/error this))
  (trace [this] (timbre/trace this))
  (warn [this] (timbre/warn this))
  (exception [this] (log-core/error this))
  (fatal [this] (log-core/error this))
  Exception
  (info [this] (timbre/info this))
  (error [this] (timbre/error this))
  (exception [this] (log-core/error this))
  (fatal [this] (log-core/error this)))