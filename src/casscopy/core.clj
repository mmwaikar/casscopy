(ns casscopy.core
  (:refer-clojure :exclude [update])
  (:require [qbits.alia :as alia]
            [qbits.hayt :refer [->raw allow-filtering columns count* select where insert values]]
            [clojure.tools.cli :as cli]
            [taoensso.timbre :as timbre :refer [log debug info]]
            [casscopy.db :as db]
            [casscopy.config :as config]))

(def cli-options
  [["-tns" "--table-names TABLE-NAMES" "Table names to copy data from / to."]
   ["-h" "--help"]])

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

(defn -main
  "Copy Cassandra data from one server (source) to another (target)."
  [& args]
  (let [cli-args (cli/parse-opts args cli-options)
        errors (:errors cli-args)
        options (:options cli-args)
        all-tables (:table-names options)
        config-data (config/read-config)
        src-db-config (:source config-data)
        tgt-db-config (:target config-data)
        src-db-map (db/connect-to-db src-db-config)
        tgt-db-map (db/connect-to-db tgt-db-config)]
    (if errors
      (info errors)
      ((try
         (doseq [table-name all-tables]
           (copy-data src-db-map tgt-db-map table-name))
         (catch Exception e (info (.toString e)))
         (finally (db/disconnect-from-db tgt-db-map)
                  (db/disconnect-from-db src-db-map)))))))
