(ns atmos-genesys.services.logger.core
  (:require [atmos-genesys.services.logger.infrastructure :as i]
            [atmos-logs.core :as c])
  (:import (java.util Map)))

(extend-protocol c/ILoggerActions
  Map
  (info [data])
  (debug [data])
  (error [data])
  (exception [data])
  (fatal [data]))


(extend-protocol c/IPersistenceLogger
  Map
  (add-log [log] (i/add-log-I-> log)))
