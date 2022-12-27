(ns atmos-genesys.apis.routes
  (:require [atmos-genesys.apis.users.api :as users]
            [atmos-logs.web.api :as logs]))


(def all-routes
  (concat logs/routes
          users/routes))