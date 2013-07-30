;; Aspekt I/O
;; Generelle Funktionen zum Lesen von Datensätzen aus einer Datei und zum Eintragen dieser
;; Datensätze in eine Zieldatenbank. Das Eintragen der Daten geschieht nicht direkt, sondern
;; nach einer Datenmutation, welche pro Feld vorgenommen wird.
;;
;; Autor: Christian Meichsner

(ns org.lambdaroyal.util.io
  (:require [clojure.java.jdbc :as sql])
  (:import [org.apache.tomcat.jdbc.pool DataSource]
           [org.lambdaroyal.util ConsoleProgress])
  (:gen-class))

(defn checkDatei [abstract file help]
  "Prüft ob eine Datei, deren Inhalt durch <abstract> beschrieben ist, tatsächliche
  vorhanden ist. Im Fehlerfall wird das Programm beendet und <help> ausgegeben"
	(cond
		(not file)
		(do (println (str "Parameter " abstract " muss angegeben werden." \newline help)) (java.lang.System/exit 1))
	
    (not (.exists file))
		(do (println (str "Parameter " abstract " " (.getName file) " ist nicht vorhanden.")) (java.lang.System/exit 1))
		
    (not (.isFile file))
		(do (println (str "Parameter " abstract " " (.getName file) " ist keine Datei.")) (java.lang.System/exit 1))))

(defn count-zeile [file]
  (with-open
    [rdr (clojure.java.io/reader file)]
    (reduce (fn [acc _](inc acc)) 0 (line-seq rdr))))

(defn loadDatei 
  ([args k key-fun]
  "Laden einer Datei gegeben durch (get args k). 
  Die Datei muss semikolonsepariert sein.
  Die Datei wird in eine transiente Datenstruktur geladen."
  (with-open
    [csvrdr (clojure.java.io/reader (get args k))]
		(let [field (take-nth 2 key-fun)
          _1 (map 
		  #(zipmap field 
			  (clojure.string/split % #";"))
			(line-seq csvrdr))]
         (persistent!
           (reduce #(conj! % %2) (transient []) _1)))))
  ([args k key-fun split-fun & opts]
   "Laden einer Datei gegeben durch (get args k). 
   Eine Zeile wird mitfilge der Funktion (split-fun line) in ihre Spalten geparst.
   Die Datei wird in eine transiente Datenstruktur geladen. Die Typumwandlung wird in dieser
   Variante in dieser Funktion vorgenommen."
    (with-open
      [csvrdr (apply clojure.java.io/reader (get args k) opts)]
		  (let [opts (cond (empty? opts) {} :else (apply hash-map opts))
            field (take-nth 2 key-fun)
            _1 (map 
                #(zipmap field 
                   (split-fun %))
                    (if (= true (get opts :ignore-first))
                      (next
			                  (line-seq csvrdr))
                      ;else
                      (line-seq csvrdr)))]
        (persistent!
          (reduce #(conj! % %2) (transient []) _1))))))

(defn lade-datei-und-convert [args k key-fun split-fun & opts]
  "Laden einer Datei mittels .loadDatei und anwenden der Convertierungsfunktion"
  (let [fun (take-nth 2 (rest key-fun))
        field (take-nth 2 key-fun)
        fun-by-field (zipmap field fun)
        split-fun (cond (nil? split-fun) #(clojure.string/split % #";") :else split-fun)
        typed #(reduce (fn [acc [k v]](conj acc [k ((k fun-by-field) v)])) {} %1)]
    (map typed
         (apply loadDatei args k key-fun split-fun opts))))

(defn insertRecord [tab rec key-fun & fargs]
  "Einfügen eines records in die Datenbanktabelle <tab> unter Berücksichtigung der Typen im Ziel ERD.
  <fargs> wird destructed zu f & args und gibt eine Funktion samt parametern an, welche 
  vor Einfügung des tupels in die Datenbank auf dem tupel angewendet wird (f tupel args)."
  (let [fun (take-nth 2 (rest key-fun))
        field (take-nth 2 key-fun)
        fun-by-field (zipmap field fun)
        typed (reduce (fn [acc [k v]](conj acc [k ((k fun-by-field) v)])) {} rec)
        [f & a] fargs
        record (cond f (apply f typed a) :else typed)]
    (sql/insert-records tab record)))

(defn importDatei [phase datasource args k abstract help tab key-fun & fargs]
  "Laden der Daten mit Inhaltsbeschreibung <abstract> aus 
  einer Datei (<args> <k>)und speichern in die interne 
  Datenbank mit Verbindung 
  <datasource> für Phase <phase>. Bei einem Fehler wird <help> ausgegeben. Gdw.
  args :maxlines enthält, werden nur <:maxlines> Zeilen aus der Datei verarbeitet."
  (do
    ;;Prüfe auf notwendige Parameter und deren Korrektheit
    (checkDatei abstract (get args k) help)
    (let [s (if 
              (:encoding args) (loadDatei args k key-fun #(clojure.string/split % #";") :encoding (:encoding args)) 
              (loadDatei args k key-fun #(clojure.string/split % #";"))) 
          sc (count s) 
          progress (new ConsoleProgress)]
      (do
      (sql/with-connection {:datasource datasource}
        (reduce 
          (fn [acc i]
            (do
              (.showProgress progress (str "Phase " phase " - Import (total " sc ")") (* acc (/ 100 sc)))
              (apply insertRecord tab i key-fun fargs)
              (inc acc))) 1 
          (cond (contains? (:maxlines args)) 
                (take (:maxlines args) s)
                :else
                s)))))))

(defn deleteTabelle [phase datasource tab]
  "Löschen aller bereits vorhandenen records aus einer datenbanktabelle. "
  (let
    [progress (new ConsoleProgress)]
    (do
      (.showProgress progress (str "Phase " phase " - Löschen der Tabelle " tab) 0)
      (sql/with-connection 
        {:datasource datasource}
        (sql/do-commands (str "delete from " tab)))
      (.showProgress progress (str "Phase " phase " - Löschen der Tabelle " tab) 100))))

(defn performUpdate [datasource phase abstract sqlString]
  "Führt ein beliebiges SQL Statement aus und wartet auf das Ergebnis."
  (let [progress (new ConsoleProgress)]
    (do
      (.showProgress progress (str "Phase " phase " - " abstract) 0)
      (sql/with-connection
        {:datasource datasource}
        (sql/do-commands sqlString))
      (.showProgress progress (str "Phase " phase " - " abstract) 100))))

(defn query-and-map [datasource query maxcount fun & param]
  "Führt das angegebene query <query> auf der Datenbankverbindung <datasource> aus und führt auf maximal <maxcount> datensätzen
  die Funktion <fun> mit den Parametern <& param> aus"
  (sql/with-connection {:datasource datasource}
                      (sql/with-query-results res [query]
                                            (dorun maxcount
                                             (map #(apply fun %1 param) res)))))

(defn query2file [datasource query maxcount filename fun & param]
  "Führt das angegebene query <query> auf der Datenbankverbindung <datasource> aus und führt auf maximal <maxcount> datensätzen
  die Funktion <fun> mit den Parametern <& param> aus. Jeder Datensatz wird in die Datei <filename> gespeichert."
    (with-open [wr (clojure.java.io/writer filename :encoding "ISO-8859-1")]
      (let [write-fun 
            (fn [i & param2]
              (do
                (let [res (apply str 
                             (interpose ";"
                                        (let [fin 
                                              (cond (empty? param2)
                                              (fun i)
                                              :else
                                              (apply fun i param))]
                                          (cond 
                                            (map? fin)
                                            (vals fin)
                                            :else
                                            fin))))]
                  (.write wr res))
                (.write wr "\r\n")))]
        (query-and-map datasource query maxcount write-fun param))))

(defn relation2file [content filename fun & param]
  "Speichert die angegebene Relation <content> (sequenz von assoziativen listen aka maps) semicolonsepariert in eine Datei <filename>. Pro Datensatz wird vor der Speicherung in die Datei die Funktion <fun> mit den Parametern <& param> ausgeführt. Als Encoding kommt UTF-8 zur Anwendung"
  (with-open [wr (clojure.java.io/writer filename :encoding "ISO-8859-1")]
    (let [write-fun
            (fn [i & param2]
              (do
                (let [res (apply str 
                             (interpose ";"
                                        (let [fin 
                                              (cond (empty? param2)
                                              (fun i)
                                              :else
                                              (apply fun i param))]
                                          (cond 
                                            (map? fin)
                                            (vals fin)
                                            :else
                                            fin))))]
                  (.write wr res))
                (.write wr "\r\n")))]
      (dorun
       (map #(apply write-fun %1 param) content)))))   


    
