(defproject ferret-ql "0.1.0-SNAPSHOT"
            :description "General nested object query language"
            :repositories {"stuart" "http://stuartsierra.com/maven2"}
            :warn-on-reflection false
            :ring {:handler job-queue.server/server-handler}
            :dependencies [[org.clojure/clojure "1.4.0"]
                           [cheshire "4.0.0"]
                           [noir "1.3.0-beta8"]
                           [log4j "1.2.17" :exclusions
                             [javax.mail/mail
                              javax.jms/jms
                              com.sun.jdmk/jmxtools
                              com.sun.jmx/jmxri]]
                           [midje "1.5.0-SNAPSHOT"]
                           [com.stuartsierra/lazytest "1.2.3"]
                           [net.cgrand/parsley "0.9.1"]]
            :profiles {:dev {:plugins [[lein-midje "2.0.0-SNAPSHOT"]
                                       [lein-ring "0.7.1"]
                                       [lein-guzheng "0.3.1"]]}}
            :main ferret-ql.server)

