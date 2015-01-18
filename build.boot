(set-env!
 :source-paths   #{"src"}
 :resource-paths #{"resources"}
 :dependencies '[[adzerk/boot-cljs      "0.0-2411-3" :scope "test"]
                 [adzerk/boot-cljs-repl "0.1.7"      :scope "test"]
                 [adzerk/boot-reload    "0.2.0"      :scope "test"]
                 [pandeiro/boot-http    "0.3.0"      :scope "test"]
                 [cljsjs/boot-cljsjs     "0.4.0"      :scope "test"]
                 [org.danielsz/cljs-utils "0.1.0-SNAPSHOT"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [secretary "1.2.1"]
                 [org.om/om "0.8.0" :exclusions [com.facebook/react]]
                 [cljsjs/react-with-addons "0.12.2-1"]])

(require
 '[adzerk.boot-cljs      :refer [cljs]]
 '[adzerk.boot-cljs-repl :refer [cljs-repl start-repl]]
 '[adzerk.boot-reload    :refer [reload]]
 '[cljsjs.boot-cljsjs    :refer [from-cljsjs]]
 '[pandeiro.http         :refer [serve]])

(deftask build-dev []
  (comp
   (from-cljsjs :profile :development)
   (sift :to-resource #{#"^cljsjs"}) 
   (cljs :optimizations :none :unified-mode true)))

(deftask build-prod []
  (comp
    (from-cljsjs :profile :production)
    (cljs :optimizations :advanced)))
