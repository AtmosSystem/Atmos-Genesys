(defproject atmos-genesys "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url  "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [atmos-kernel "2.2-SNAPSHOT"]
                 [atmos-web-kernel-reitit "2.0-SNAPSHOT"]
                 [atmos-logs "3.0-SNAPSHOT"]]
  :main ^:skip-aot atmos-genesys.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot      :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
