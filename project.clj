(defproject casscopy "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/tools.namespace "0.2.11"]
                 [cc.qbits/alia-all "4.0.0-beta8"]
                 [cc.qbits/hayt "4.0.0-beta7"]
                 [com.taoensso/timbre "4.8.0"]
                 [org.clojure/tools.cli "0.3.5"]
                 [clj-time "0.13.0"]]
  :main ^:skip-aot casscopy.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
