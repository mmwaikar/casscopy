(ns casscopy.db
  (:require [qbits.alia :as alia]
            [taoensso.timbre :refer [debug info]]))

(defn get-db-map
  "Returns a map which contains a Cassandra cluster, session and keyspace information."
  [cluster session keyspace]
  {:cluster cluster :session session :keyspace keyspace})

(defn connect-to-db
  "Connects to a Cassandra keyspace on a particular ip-address, with the given username and password."
  ([db-config]
   (let [{:keys [host port username password keyspace]} db-config]
     (connect-to-db host port username password keyspace)))

  ([ip-address port username password keyspace]
   (let [cluster (alia/cluster {:contact-points [ip-address]
                                :port           port
                                :credentials    {:user username :password password}})
         session (alia/connect cluster)]
     (debug "Connecting to" keyspace "on" (str ip-address ":" port))
     (get-db-map cluster session keyspace))))

(defn disconnect-from-db
  "Disconnects from Cassandra session and cluster information contained in the db-map."
  [db-map]
  (debug "Disconnecting... bye!")
  (alia/shutdown (:session db-map))
  (alia/shutdown (:cluster db-map)))
