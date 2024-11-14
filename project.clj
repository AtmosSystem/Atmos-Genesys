(defproject org.clojars.atmos-system/atmos-genesys "1.12"
  :description "The basis of all web projects using atmos tech"
  :url "https://github.com/AtmosSystem/Atmos-Genesys"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url  "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.clojars.atmos-system/atmos-kernel "2.2"]
                 [clojure.java-time "1.2.0"]
                 [org.clojure/core.memoize "1.0.257"]
                 [inflections "0.14.1"]
                 ; Security
                 [buddy/buddy-hashers "2.0.167"]
                 [buddy/buddy-auth "3.0.1"]
                 ; Data
                 [org.clojars.atmos-system/atmos-data-kernel "1.1"]
                 ; Web
                 [org.clojars.atmos-system/atmos-web-kernel-reitit "2.0"]
                 ; Logging
                 [org.clojars.atmos-system/atmos-logs "3.7"]]
  :deploy-repositories [["clojars" {:sign-releases false
                                    :url           "https://repo.clojars.org/"
                                    :username :env/clojars_username
                                    :password :env/clojars_password}]])
