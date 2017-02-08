(defproject memory-hole "0.1.0"

  :description "Support Issue Tracker"
  :url "https://github.com/yogthos/memory-hole"

  :dependencies [[bouncer "1.0.0"]
                 [buddy "1.0.0"]
                 [cljs-ajax "0.5.8"]
                 [cljsjs/bootstrap "3.3.6-0"]
                 [cljsjs/react-bootstrap "0.29.2-0"]
                 [cljsjs/react-bootstrap-datetimepicker "0.0.22-0"
                  :exclusions [org.webjars.bower/jquery]]
                 [cljsjs/react-select "1.0.0-beta13-0"]
                 [cljsjs/showdown "1.4.2-0"]
                 [com.andrewmcveigh/cljs-time "0.4.0"]
                 [compojure "1.5.1"]
                 [conman "0.6.0"]
                 [cprop "0.1.8"]
                 [funcool/cuerdas "2.0.3"]
                 [luminus-immutant "0.2.2"]
                 [luminus-migrations "0.2.6"]
                 [luminus-nrepl "0.1.4"]
                 [metosin/compojure-api "1.1.6"]
                 [metosin/ring-http-response "0.8.0"]
                 [mount "0.1.10"]
                 [org.clojars.pntblnk/clj-ldap "0.0.12"]
                 [org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.211" :scope "provided"]
                 [org.clojure/tools.cli "0.3.5"]
                 [org.clojure/tools.logging "0.3.1"]
                 [org.postgresql/postgresql "9.4.1209"]
                 [org.webjars.bower/tether "1.3.3"]
                 [org.webjars/bootstrap "3.3.6"]
                 [org.webjars/font-awesome "4.6.3"]
                 [org.webjars/webjars-locator-jboss-vfs "0.1.0"]
                 [re-com "0.8.3"]
                 [re-frame "0.8.0"]
                 [reagent "0.6.0"]
                 [reagent-utils "0.2.0"]
                 [ring-middleware-format "0.7.0"]
                 [ring-webjars "0.1.1"]
                 [ring/ring-defaults "0.2.1"]
                 [secretary "1.2.3"]
                 [selmer "1.0.7"]
                 [venantius/accountant "0.1.7"]]

  :min-lein-version "2.0.0"

  :jvm-opts ["-server" "-Dconf=.lein-env"]
  :source-paths ["src/clj" "src/cljc"]
  :resource-paths ["resources" "target/cljsbuild"]
  :target-path "target/%s/"
  :main memory-hole.core
  :migratus {:store :database :db ~(get (System/getenv) "DATABASE_URL")}

  :plugins [[lein-cprop "1.0.1"]
            [migratus-lein "0.4.1"]
            [lein-cljsbuild "1.1.3"]
            [lein-immutant "2.1.0"]]
  :clean-targets ^{:protect false}
  [:target-path [:cljsbuild :builds :app :compiler :output-dir] [:cljsbuild :builds :app :compiler :output-to]]
  :figwheel
  {:http-server-root "public"
   :nrepl-port 7002
   :css-dirs ["resources/public/css"]
   :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}


  :profiles
  {:uberjar {:omit-source true
             :prep-tasks ["compile" ["cljsbuild" "once" "min"]]
             :cljsbuild
             {:builds
              {:min
               {:source-paths ["src/cljc" "src/cljs" "env/prod/cljs"]
                :compiler
                {:output-to "target/cljsbuild/public/js/app.js"
                 :externs ["react/externs/react.js"
                           "public/js/simplemde.min.js"
                           "public/js/hljs.js"]
                 :optimizations :advanced
                 :pretty-print false
                 :closure-warnings
                 {:externs-validation :off :non-standard-jsdoc :off}}}}}


             :aot :all
             :uberjar-name "memory-hole.jar"
             :source-paths ["env/prod/clj"]
             :resource-paths ["env/prod/resources"]}

   :dev           [:project/dev :profiles/dev]
   :test          [:project/test :profiles/test]

   :project/dev  {:dependencies [[binaryage/devtools "0.8.1"]
                                 [com.cemerick/piggieback "0.2.1"]
                                 [doo "0.1.7"]
                                 [figwheel-sidecar "0.5.4-7"]
                                 [pjstadig/humane-test-output "0.8.1"]
                                 [prone "1.1.1"]
                                 [ring/ring-devel "1.5.0"]
                                 [ring/ring-mock "0.3.0"]]
                  :plugins      [[com.jakemccrary/lein-test-refresh "0.14.0"]
                                 [lein-doo "0.1.7"]
                                 [lein-figwheel "0.5.4-7"]
                                 [org.clojure/clojurescript "1.9.198"]]
                  :cljsbuild
                  {:builds
                   {:app
                    {:source-paths ["src/cljs" "src/cljc" "env/dev/cljs"]
                     :compiler
                     {:main "memory_hole.app"
                      :asset-path "/js/out"
                      :output-to "target/cljsbuild/public/js/app.js"
                      :output-dir "target/cljsbuild/public/js/out"
                      :source-map true
                      :optimizations :none
                      :pretty-print true}}}}



                  :doo {:build "test"}
                  :source-paths ["env/dev/clj" "test/clj"]
                  :resource-paths ["env/dev/resources"]
                  :repl-options {:init-ns user}
                  :injections [(require 'pjstadig.humane-test-output)
                               (pjstadig.humane-test-output/activate!)]}
   :project/test {:resource-paths ["env/dev/resources" "env/test/resources"]
                  :cljsbuild
                  {:builds
                   {:test
                    {:source-paths ["src/cljc" "src/cljs" "test/cljs"]
                     :compiler
                     {:output-to "target/test.js"
                      :main "memory_hole.doo-runner"
                      :optimizations :whitespace
                      :pretty-print true}}}}

                  }
   :profiles/dev {}
   :profiles/test {}})
