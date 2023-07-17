(ns atmos-genesys.apis.data
  (:import
    (java.time Instant)))

(def active-data {:active true})
(def audit-data {:created-at (Instant/now)})
