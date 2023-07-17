(ns atmos-genesys.services.logger
  (:require
    [atmos-logs.core :as log-core]
    [taoensso.timbre :as timbre])
  (:import
    (java.util Map)))

(defmacro handle-log
  [& body]
  `(do
     ~@body
     "OK"))

(extend-protocol log-core/ILoggerActions
  Map
  (info [this]
    (handle-log (timbre/info this)))

  (debug [this]
    (handle-log (timbre/debug this)))

  (error [this]
    (handle-log (timbre/error this)))

  (trace [this]
    (handle-log (timbre/trace this)))

  (warn [this]
    (handle-log (timbre/warn this)))

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
