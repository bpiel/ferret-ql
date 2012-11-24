(ns ferret-ql.server
  (:require [noir.server :as server]))

(server/load-views-ns 'ferret-ql.views)

(def server-handler
  (server/gen-handler 
    {:mode :prod
     :ns 'ferret-ql}))
