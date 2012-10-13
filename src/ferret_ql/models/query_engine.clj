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

(defn-dbg pairs-to-map [pairs]
  (apply hash-map (mapcat identity pairs)))

(defn traverse-object [object func]
  (if (map? object)
    (pairs-to-map (map #(traverse-object % func) object))
    (if (vector? object)
      (map #(traverse-object % func) object)
      (func object))))


(def expression-parser
  (parsley/parser ;add constants
    :expr #{:var :func-call}
    :func-call ["(" :func :expr* ")"]
    :func #{"count" "avg" "median" "sum" "min" "max" "first" "last" "second" "rest" "sort" "+" "-" "/" "*" "%"}
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

(defn eval-expr-single-context [expr context]
  (let [parsed-expr (parse-expr expr)]
    (if (= (get parsed-expr :type) :var)
      (eval-expr-var-single-context (:path (parse-expr expr)) context)
      expr)))


(defn eval-expr [expr group-context]
  (let [parsed-expr (parse-expr expr)]
    (if (= (get parsed-expr :type) :var)
      (eval-expr-var (parse-expr expr) group-context)
      expr)))



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
  (traverse-object select #(eval-expr % group-context)))


(defn execute-select [query group-contexts]
  (map
    #(first (eval-select-value (query "select") %))
    group-contexts))


(defn execute-query [query state]
  (let [query (assoc query "group" (get query "group" "{_index_}"))] 
    (execute-select query (group-contexts query (get-for-contexts query state)))))


(defn test-query [query input]
  (let [state {"_" input }]
		(execute-query query state)))