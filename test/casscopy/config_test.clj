(ns casscopy.config-test
  (:require [clojure.test :refer :all]
            [taoensso.timbre :refer [log debug info error]]
            [fipp.edn :refer [pprint] :rename {pprint fipp}]
            [casscopy.config :as config]))

;; use the below to run the tests in this file
(comment
  (use 'casscopy.config-test)
  (clojure.test/run-tests 'casscopy.config-test)
  )

(deftest should-load-config-file
  (testing "Should load the config file and read source / target DB values."
    (let [cfg (config/read-config)]
      (debug "config file contents:")
      (fipp cfg)
      (is (not-empty cfg) "Config values are empty."))))
