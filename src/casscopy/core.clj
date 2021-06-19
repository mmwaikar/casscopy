(ns casscopy.core
  (:require [qbits.alia :as alia]
            [qbits.hayt :refer [->raw allow-filtering columns count* select where insert values]]
            [fipp.edn :refer [pprint] :rename {pprint fipp}]
            [taoensso.timbre :refer [debug info]]))

(def src-db-config {:host     "localhost"
                    :port     9042
                    :username "username"
                    :password "pwd"
                    :keyspace "ks"})

(def tgt-db-config {:host     "localhost"
                    :port     9042
                    :username "username"
                    :password "pwd"
                    :keyspace "ks"})

(def repo-type-tables ["repository_types"])
(def cont-type-tables ["container_types" "container_types_by_external_id" "container_types_by_external_key" "container_types_by_repository_key"])
(def art-type-tables ["artifact_types" "artifact_types_by_external_id" "artifact_types_by_external_key" "artifact_types_by_repository_key"])
(def rel-type-tables ["relation_types" "relation_types_by_external_id" "relation_types_by_external_key" "relation_types_by_repository_key"])

(def repo-tables ["repositories" "repositories_by_uri"])
(def cont-tables ["containers" "containers_by_external_id" "containers_by_external_key" "containers_by_repository_key"])
(def art-tables ["artifacts" "artifacts_by_container_key" "artifacts_by_external_id" "artifacts_by_external_key" "artifacts_by_key"
                 "artifacts_by_type_key"])
(def rel-tables ["relations" "relations_by_container_key" "relations_by_external_id" "relations_by_external_key" "relations_by_key"
                 "relations_by_source_key" "relations_by_target_key"])
(def all-tables (concat repo-type-tables cont-type-tables art-type-tables rel-type-tables
                        repo-tables cont-tables art-tables rel-tables))

(defn get-db-map
  "Returns a map which contains a Cassandra cluster, session and keyspace information."
  ([session keyspace] (get-db-map nil session keyspace))

  ([cluster session keyspace]
   {:cluster cluster :session session :keyspace keyspace}))

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

(defn get-table-name
  "Get the name of the table 't' prefixed with the name of the keyspace 'k' followed by a '.'
  i.e. returns 'k.t'."
  [keyspace table-name]
  (str keyspace "." table-name))

(defn get-data [src-db-map table-name]
  (let [ks-table-name (keyword (get-table-name (:keyspace src-db-map) table-name))
        rows (alia/execute (:session src-db-map) (select ks-table-name))]
    ;; (debug "rows...")
    ;; (fipp rows)
    rows))

(defn get-seq-of-seqs [row]
  (into [] row))

(defn insert-row [tgt-db-map table-name row]
  (let [ks-table-name (keyword (get-table-name (:keyspace tgt-db-map) table-name))
        seq-of-seqs (get-seq-of-seqs row)
        insert-stmt (insert ks-table-name (values seq-of-seqs))
        raw-insert-stmt (->raw insert-stmt)]
    ;; (debug "kstn:" ks-table-name)
    ;; (debug "row as seq-of-seqs:" seq-of-seqs)
    ;; (debug "insert-stmt:" insert-stmt)
    ;; (debug "raw-insert-stmt:" raw-insert-stmt)
    (alia/execute (:session tgt-db-map) insert-stmt)))

(defn insert-data [tgt-db-map table-name rows]
  (let [inserted-rows (vec (map #(insert-row tgt-db-map table-name %) rows))]
    inserted-rows))

(defn copy-data [src-db-map tgt-db-map table-name]
  (let [src-rows (get-data src-db-map table-name)
        tgt-rows (get-data tgt-db-map table-name)]
    (info "copying data for:" table-name "...")
    (if (= (count src-rows) (count tgt-rows))
      (info "src & tgt have same number of rows, so no need to copy anything...")
      (let [inserted-rows (insert-data tgt-db-map table-name src-rows)]
        inserted-rows))))

(defn run [opts]
  ;; (println "inserting data in players")
  ;; (insert-data "players" rows)
  ;; (get-data "players")
  (let [src-db-map (connect-to-db src-db-config)
        tgt-db-map (connect-to-db tgt-db-config)]
    (doseq [table-name all-tables]
      (copy-data src-db-map tgt-db-map table-name))
    (disconnect-from-db tgt-db-map)
    (disconnect-from-db src-db-map)))

;; (comment
;;   "to run this program from the cmd line"
;;   clj -X casscopy/run)
