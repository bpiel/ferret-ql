(ns ferret-ql.server
  (:require [noir.server :as server]))

(server/load-views-ns 'job-queue.views)

(def server-handler
  (server/gen-handler 
    {:mode :prod
     :ns 'job-queue}))
