;; Aspekt DB Schema anlegen, Komplette Datenbank löschen, Records Einfügen mit Id Rückgabe
;;
;; Autor: Christian Meichsner

(ns org.lambdaroyal.util.db.dbupdate
  (:require  [org.lambdaroyal.util.db.dbconfig :as dbconfig])
  (:require [clojure.java.jdbc :as sql])
  (:require [clojure.tools.trace :as trace])
  (:import [org.apache.tomcat.jdbc.pool DataSource]
           [org.lambdaroyal.util ConsoleProgress])
  (:import [org.apache.ddlutils.io DatabaseIO])
  (:import [org.apache.ddlutils.model Database])
  (:import [org.apache.ddlutils Platform])
  (:import [org.apache.ddlutils PlatformFactory])
  (:import [org.lambdaroyal.util ConsoleProgress])
	(:gen-class))

(defn dbschema [datasource]
  "Legt in der durch die datasource gegebenen Datenbank das Schema entsprechend src/main/resources/ddl.xml an"
  (let [path (.getAbsolutePath 
                 (new java.io.File 
                  (.toURI 
                    (.getResource 
                      (.getContextClassLoader 
                        (Thread/currentThread)) "ddl.xml")))) 
        databaseio (.read 
                     (new DatabaseIO) path)]
    (do
      (println "Phase schema - Anlegen des Datenbankschemas")
      (.alterTables (PlatformFactory/createNewPlatformInstance datasource) databaseio false))))

(defn purge [datasource & tables]
  "Löscht alle Stamm- und Bewegungsdaten aus der Datenbank"
  (let [db {:datasource datasource} progress (new ConsoleProgress)]
    (do
      (.showProgress progress "Purge database" 0)
      (sql/with-connection db 
        (reduce 
          (fn [acc i]
            (do
              (sql/do-commands (str "delete from " i))
              (.showProgress progress 
                             (str "Purge - table " i)
                             (* acc (Math/floor (/ 100 (count tables)))))
              (+ acc 1))) 0 tables))
      (.showProgress progress "Phase database" 100))))    

