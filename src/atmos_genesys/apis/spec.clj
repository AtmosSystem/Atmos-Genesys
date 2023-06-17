(ns atmos-genesys.apis.spec
  (:require [clojure.spec.alpha :as s]
            [reitit.spec :as rs]))

(s/def :api.route/name keyword?)
(s/def :api.route.method/get (s/keys :req-un [::rs/handler]))
(s/def :api.route.method/post (s/keys :req-un [::rs/handler]))
(s/def :api.route.method/put (s/keys :req-un [::rs/handler]))
(s/def :api.route.method/delete (s/keys :req-un [::rs/handler]))
(s/def ::route-api (s/merge ::rs/default-data
                            (s/keys :req-un [:api.route/name]
                                    :opt-un [::rs/parameters
                                             :api.route.method/get
                                             :api.route.method/post
                                             :api.route.method/put
                                             :api.route.method/delete])))