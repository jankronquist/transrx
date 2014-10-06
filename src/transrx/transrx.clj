(ns transrx.transrx
  (:require [rx.lang.clojure.interop :as rx])
    (:import [rx Observable]))
 
; @REPL
;(require '[rx.lang.clojure.interop :as rx])
;(import [rx Observable])


; returns new observable with transducer applied
; VERY LIMITED: I just got the example below to work....
(defn obsduce [observable xform]
  (Observable/create (rx/action [^rx.Subscriber s]
    (-> observable
      (.subscribe (rx/action [v] ((xform (fn
                                           ([old] old) ;TODO: what should happen here?
                                           ([old value] (.onNext s value)))) 0 v)) ;TODO: handle 0?? wat?
                                           ; TODO early finish
                  (rx/action [e] (.onError s e))
                  (rx/action [] (.onCompleted s)))))))



; utility to print all elements of an observable 
(defn printo [o]
  (-> o
      (.finallyDo (rx/action [] (println "Finished transform")))
      (.subscribe (rx/action [v] (println "Got value" v))
                  (rx/action [e] (println "Get error" e))
                  (rx/action [] (println "Sequence complete")))))


; an observable that returns a list of integers
(def o (Observable/create (rx/action [^rx.Subscriber s]
                     (loop [i 0]
                       (when (< i 10)
                         (.onNext s i)
                         (recur (inc i))))
                     (.onCompleted s))))

; example use
(printo (obsduce o (comp (filter even?) (mapcat (fn [x] (repeat x x))))))
