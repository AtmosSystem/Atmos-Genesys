(defproject atmos-genesys "0.1.0-SNAPSHOT"
  :description "The basis of all web projects using atmos tech"
  :url "https://github.com/AtmosSystem/Atmos-Genesys"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url  "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [atmos-kernel "2.2-SNAPSHOT"]
                 [clojure.java-time "1.2.0"]
                 [org.clojure/core.memoize "1.0.257"]
                 ; Security
                 [buddy/buddy-core "1.10.413"]
                 ; Data
                 [atmos-data-kernel "1.0-SNAPSHOT"]
                 ; Web
                 [atmos-web-kernel-reitit "2.0-SNAPSHOT"]
                 ; Logging
                 [atmos-logs "3.0-SNAPSHOT"]
                 [com.taoensso/timbre "5.2.1"]])
