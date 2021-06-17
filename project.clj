(defproject casscopy "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [cc.qbits/alia-all "4.3.3"]
                 [cc.qbits/hayt "4.1.0"]
                 [com.taoensso/timbre "5.1.2"]
                 [fipp "0.6.23"]
                 [yogthos/config "1.1.7"]
                 [org.clojure/tools.cli "1.0.206"]
                 [clj-time "0.13.0"]]
  :main ^:skip-aot casscopy.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}
             :dev     {:resource-paths ["config"]}
             :prod    {:resource-paths ["config"]}}) 
