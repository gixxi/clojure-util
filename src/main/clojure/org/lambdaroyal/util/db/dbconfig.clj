;; Aspect Read DB configuration and build-up database connection source 
;; 
;; Autor: Christian Meichsner

(ns org.lambdaroyal.util.db.dbconfig
  (:require [clojure.java.io :as io])
  (:import [java.io PushbackReader])
  (:gen-class))

(defn conf [file] 
  "Liesst ein Konfigurationsfile ein, welches einer Assoziativen Liste entspricht"
  (binding [*read-eval* false]
    (with-open [r (io/reader file)]
      (read (PushbackReader. r)))))

(defn datasource [file]
  "Liesst ein Konfigurationsfile ein, und erstellt anhand dieses eine gepoolte JDBC Datasource"
  (let 
    [config (conf file) ds (new org.apache.tomcat.jdbc.pool.DataSource) pp (new org.apache.tomcat.jdbc.pool.PoolProperties)]
	  (do
      (println (str "setup datasource using " config))
  		(.setDriverClassName pp (:classname config))
  		(.setUrl pp (:subname config))
  		(.setUsername pp (:user config))
  		(.setPassword pp (:password config))
  		(.setTestOnBorrow pp true)
  		(.setValidationQuery pp "SELECT 1")
  		(.setMaxActive pp 5)
  		(.setInitialSize pp 1)
  		(.setMaxWait pp 10000)
	  	(.setPoolProperties ds pp)
	  	(identity ds))))

(defn datasource-from-filename [filename]
  (datasource (clojure.java.io/as-file filename)))
