(ns ferret-ql.models.query-engine  
  (:require [net.cgrand.parsley :as parsley]))

;contexts
;process
;group

;default behavior w/ no for clause -- expand object?

(defn dbg [label value]
  (do
    (print "\n\n******" label ":\n\n" value "\n")
    value))

(defmacro defn-dbg [name args body]
  `(def ~name (fn ~args 
    (let [inputs# ~args]
      (do 
        (print "*" '~name "\ninputs :" inputs# "\n")
        (let [output# ~body]
          (do 
            (print "output :" output# "\n\n")
            output#)))))))

(defn find-first-and-rest [predicate coll]
  (loop [rest-coll coll
         first-coll false
         found false]
    (if found
      [first-coll rest-coll]
      (let [first-coll (first rest-coll)]
        (recur
          (rest rest-coll)
          first-coll
          (predicate first-coll))))))

(defn pairs-to-map-OLD [pairs]
  (apply hash-map (mapcat identity pairs)))

(defn pairs-to-map [pairs]
  (apply 
    hash-map 
    (mapcat 
      #(if (sequential? (first %))
          (interleave (first %) (second %)) ;make recursive?
          %)
      pairs)))

(defn transpose [m]
  (vec (apply map vector m)))

(defn transmogrify-value [value func]
  {:__trans-type 
    (cond 
      (map? value) :map
      (sequential? value) :sequential
      :else :scalar)
   :__trans-value
     (cond 
        (map? value) (map #(vector (transmogrify-value (first %) func) (transmogrify-value (second %) func)) value)
        (sequential? value) (map #(transmogrify-value % func) value)
        :else (func value))})

;write a function that takes a "value", traverses it looking for mogrified maps, and rebuilds them using pairs-to-map/flatten/first/etc
; starting from the bottom up

(defn standard-demogrifier [type value]  
    (case type
      :map (pairs-to-map value) ;(pairs-to-map (map #(vector (:__trans-value (first %)) (:__trans-value (second %))) value))
      :sequential (if (sequential? (first value))
                    (transpose value)
                    value)
      :scalar value))

(defn demogrify-value-rec [value func]
  (cond
    (:__trans-type value)
      (func (:__trans-type value) (demogrify-value-rec (:__trans-value value) func))
    (map? value)
      (pairs-to-map (map #(demogrify-value-rec % func) value))
    (sequential? value)
      (map #(demogrify-value-rec % func) value)
    :else 
      value))

(defn demogrify-value [value]
  (demogrify-value-rec value standard-demogrifier))

(defn mogrify [value func]
  (demogrify-value (transmogrify-value value func)))

(defn traverse-object [object func] ;this may be dead
  (if (map? object)
    (pairs-to-map (map #(traverse-object % func) object))
    (if (vector? object)
      (map #(traverse-object % func) object)
      (func object))))


(def expression-parser
  (parsley/parser ;add constants
    :expr #{:var :func-call}
    :func-call ["(" :func :expr* ")"]
    :func #{"merge" "count" "a-count" "avg" "median" "sum" "a-sum" "min" "max" "first" "last" "second" "rest" "sort" "+" "-" "/" "*" "%"}
    :var ["{" :word ["." :word] :* "}"]    
    :word #"\w*"))

(defn parse-expr-into-parsley-Node [expr]    
      (expression-parser expr))


(defn parse-parsley-Node)

(defn parse-parsley-Node-var [var-node]
  {:type :var
   :path (map #(first (:content %)) (filter #(get % :tag) (:content var-node)))})

(defn parse-parsley-Node-func-call [call-node]
  (let [ [func-node rest-nodes] (find-first-and-rest #(= (get % :tag) :func) (:content call-node)) ]
    {:type :func-call
     :func (first (:content func-node))
     :args (map parse-parsley-Node (filter #(get % :tag) rest-nodes))}))

(defn parse-parsley-Node [node]
  (let [first-content (first(:content node))]
    (case (:tag node)
      :net.cgrand.parsley/root (parse-parsley-Node first-content)
      :expr (parse-parsley-Node first-content)
      :var (parse-parsley-Node-var node)
      :func-call (parse-parsley-Node-func-call node)
      nil)))

(defn parse-expr [expr]
  (let [cleansed-expr (clojure.string/replace expr " " "")]
      (or 
        (parse-parsley-Node (parse-expr-into-parsley-Node cleansed-expr))
        expr)))

(defn eval-expr-var-single-context [var-path context]
  (let  [current    (first var-path)]
    (loop [remaining  (rest var-path)
           value       (get context current)]

      (if (or (nil? value) (empty? remaining))
        (or value "")

        (recur           
          (rest remaining)
          (get value (first remaining)))))))


(defn eval-expr-var [var-expr group-context]
  (map
    #(eval-expr-var-single-context (:path var-expr) %)
    group-context))

(defn eval-expr-func-call [func-call-expr args] 
  (case (get func-call-expr :func)
    "count" (count (first args)) 
    "a-count" (count (first (first args))) 
    "sum" (reduce + (first args))
    "a-sum" (reduce + (first (first args)))
    "merge" (flatten (first args)) ;too deep?
    "avg" (double (/ (reduce + (first args)) (count (first args))))
    "max" (apply max (first args))
    "min" (apply min (first args))
    "first" (first (first args))
    "last" (last (first args))


    [:not-found func-call-expr]))


(defn eval-expr-single-context [expr context]
  (let [parsed-expr (parse-expr expr)]
    (if (= (get parsed-expr :type) :var)
      (eval-expr-var-single-context (:path (parse-expr expr)) context)
      expr)))


(defn eval-parsed-expr [parsed-expr group-context]  
  (case (get parsed-expr :type) 
    :var (eval-expr-var parsed-expr group-context)      
    :func-call (eval-expr-func-call parsed-expr (map #(eval-parsed-expr % group-context) (:args parsed-expr)))
    parsed-expr))

(defn eval-expr [expr group-context]
  (eval-parsed-expr (parse-expr expr) group-context))


(defn get-for-contexts [query state]
  (map 
    #(assoc state (query "for") %)
    (state (query "in"))))


(defn group-contexts [query contexts]
  (let [contexts (map-indexed #(assoc %2 "_index_" %) contexts)]
    (map second (group-by
      #(eval-expr-single-context (query "group") %)
      contexts))))


(defn eval-select-value [select group-context]
  (mogrify select #(eval-expr % group-context)))


(defn execute-select [query group-contexts]
  (map
    #(eval-select-value (query "select") %)
    group-contexts))


(defn execute-query [query state]
  (let [query (assoc query "group" (get query "group" "{_index_}"))] 
    (execute-select query (group-contexts query (get-for-contexts query state)))))


(defn test-query [query input]
  (let [state {"_" input }]
		(execute-query query state)))
