;; Aspect for Time Measurement of function application 
;;
;; Author: Christian Meichsner

(ns org.lambdaroyal.util.time
	(:gen-class))

(defn trace-time
  "Evaluates expr and prints the time it took.  Returns the value of
 expr."
  [f & args]
  (let [start (. System (nanoTime))]
     [(apply f args) (/ (double (- (. System (nanoTime)) start)) 1000000.0)]))

