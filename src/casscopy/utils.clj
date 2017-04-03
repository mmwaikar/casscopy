(ns casscopy.utils)

(defn get-creds [ip-address ks-name username pwd]
  {:ip-address ip-address :keyspace ks-name :username username :password pwd})