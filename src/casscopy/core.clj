(ns casscopy.core
  (:refer-clojure :exclude [update])
  (:require [qbits.alia :as alia]
            [clojure.tools.cli :refer [parse-opts]]
            [taoensso.timbre :as timbre :refer [log debug info]]
            [casscopy.db :as db]))

(defn foo
  "I don't do a whole lot."
  [x]
  (println x "Hello, World!"))

(def cli-options
  [["-si" "--srcipaddress SRCIPADDRESS" "Source IP Address"]
   ["-ti" "--tgtipaddress TGTIPADDRESS" "Target IP Address"]
   ["-sk" "--srckeyspace SRCKEYSPACE" "Source Keyspace"]
   ["-tk" "--tgtkeyspace TGTKEYSPACE" "Target Keyspace"]
   ["-sp" "--srcpassword SRCPASSWORD" "Source Password"]
   ["-tp" "--tgtpassword TGTPASSWORD" "Target Password"]
   ["-h" "--help"]])

(defn connect-to-db [ipaddress pwd]
  (let [
        cluster (alia/cluster {:contact-points [ipaddress] :credentials {:user dbUser :password pwd}})
        session (alia/connect cluster)]
    (reset! my-cluster cluster)
    (reset! my-session session)))

(defn disconnect-from-db [session-atom cluster-atom]
  (alia/shutdown @session-atom)
  (alia/shutdown @cluster-atom))

(defn copy [source-credentials target-credentials])

(defn -main
  "Copy Cassndra data from one server to another."
  [& args]
  (let [cli-args (parse-opts args cli-options)
        errors (:errors cli-args)
        options (:options cli-args)
        src-ip (:srcipaddress options)
        tgt-ip (:tgtipaddress options)
        src-ks (:srckeyspace options)
        tgt-ks (:tgtkeyspace options)
        src-pwd (:srcpassword options)
        tgt-pwd (:tgtpassword options)]
    (if errors
      (info errors)
      ((try
         (reset! db/src-keyspace src-ks)
         (reset! db/tgt-keyspace tgt-ks)
         (connect-to-db src-ip src-pwd)
         (connect-to-db tgt-ip tgt-pwd)

         (catch Exception e (info (.toString e)))
         (finally (disconnect-from-db db/tgt-session db/tgt-cluster)
                  (disconnect-from-db db/src-session db/src-cluster)))))))
