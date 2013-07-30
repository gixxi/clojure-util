;; Aspect Local Caching. Provides shared access to a cached result of a function call.
;; The call is blocking if the function call is not yet realized otherwise the call returns immediatly with the 
;; preprocessed result
;;
;; Autor: Christian Meichsner

(ns org.lambdaroyal.util.localcache
	(:gen-class))

(def #^{:doc "Global Cache for function cache"}
  global-cache (atom {}))

(defn clear [k]
  "Clears a certain association given by the key from the global cache"
  (swap! global-cache dissoc @global-cache k))

(defn clear-all [k]
  "Clears all associations from the global cache"
  (swap! global-cache empty))

(defn cache [k f & args]
  "Returns a memoized version of the application of the provided function.
  This version is associated with the provided key. Repeatedly arguments 
  for the same key return the previously computed application or block 
  until the function application returns.
  
  Usage: 

  (:key (cache :cacheName f args))

  will return a value associated by key :key from the cached version of the function application (f args)
  "
  (let [cache-state @global-cache
        hit-or-miss 
        (fn [& _]
          (if 
            (contains? cache-state k)
            (identity cache-state)
            (do
              ;;(println (str "add key " k " to map " cache-state " function application " f " args " args))
            (assoc cache-state k (delay (apply f args))))))
        swapped (swap! global-cache hit-or-miss f args)]
    @(get swapped k)))
