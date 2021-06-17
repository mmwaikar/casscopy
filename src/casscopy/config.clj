(ns casscopy.config
  (:require [config.core :refer [env]]))

(defn get-db-config
  "Reads values from the source or target map of a config file."
  [src-or-tgt-map]
  (let [{:keys [host port username password keyspace]} src-or-tgt-map]
    {:host     host
     :port     port
     :username username
     :password password
     :keyspace keyspace}))

(defn read-config
  "Reads values from config.edn file from config folder to connect to Cassandra."
  []
  (let [{:keys [source target]} env]
    {:source (get-db-config source)
     :target (get-db-config target)}))
