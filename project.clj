(defproject org.clojars.atmos-system/atmos-genesys "0.1.0-SNAPSHOT"
  :description "The basis of all web projects using atmos tech"
  :url "https://github.com/AtmosSystem/Atmos-Genesys"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url  "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.clojars.atmos-system/atmos-kernel "2.2-SNAPSHOT"]
                 [clojure.java-time "1.2.0"]
                 [org.clojure/core.memoize "1.0.257"]
                 [inflections "0.14.1"]
                 ; Security
                 [buddy/buddy-hashers "2.0.167"]
                 ; Data
                 [org.clojars.atmos-system/atmos-data-kernel "1.0-SNAPSHOT"]
                 ; Web
                 [org.clojars.atmos-system/atmos-web-kernel-reitit "2.0-SNAPSHOT"]
                 ; Logging
                 [org.clojars.atmos-system/atmos-logs "3.0-SNAPSHOT"]
                 [com.taoensso/timbre "5.2.1"]]
  :deploy-repositories [["clojars" {:url      "https://repo.clojars.org/"
                                    :username :env/clojars_username
                                    :password :env/clojars_password}]])
