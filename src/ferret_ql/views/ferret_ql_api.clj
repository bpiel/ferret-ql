(ns ferret-ql.views.ferret-ql-api
  (:require [noir.core :refer [defpage]]
            [noir.statuses :refer [set-page!]]
            [noir.response :as resp]))

(set-page! 404 "Page not found")

(defpage testget "/testget/:id" {:keys [id]}
  (resp/json [:test id]))