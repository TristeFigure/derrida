(defproject derrida "0.1.0"
  :description "Destructuring Destructuring"
  :url         "https://github.com/TristeFigure/derrida"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure        "1.10.0"   :scope "provided"]
                 [org.clojure/clojurescript  "1.10.520" :scope "provided"]]
  :plugins [[lein-codox     "0.10.7"]
            [lein-cljsbuild "1.1.8"]
            [lein-doo       "0.1.11"]]
  :prep-tasks ["compile" ["cljsbuild" "once"]]
  :cljsbuild {:builds {}}

  :profiles
  {:common {:cljsbuild
            {:builds
             {:main {:source-paths ["src"]
                     :compiler     {:output-to     "target/js/main/derrida.js"
                                    :output-dir    "target/js/main/"
                                    :optimizations :none}}}}}
   :dev [:common
         {:dependencies
          [[doo "0.1.11"]
           [codox-theme-rdash "0.1.2"]]}]
   :test [:dev
          {:cljsbuild
           {:builds
            {:main {:source-paths ["test"]
                    :compiler     {:target     :nodejs
                                   :main       derrida.doo-test
                                   :output-to  "target/js/test/derrida.js"
                                   :output-dir "target/js/test/"}}}}}]
   :release [:common
             {:cljsbuild
              {:builds
               {:main {:jar      true
                       :compiler {:optimizations :advanced}}}}}]}

  :doo {:build "main"
        :alias {:default [:node]}}
  :aliases {"test"    ["with-profile" "test" ["do" ["test"] ["doo" "once"]]]
            "deploy"  ["with-profile" "release" "deploy"]
            "release" ["with-profile" "release" "release"]}
  :codox {:source-uri "https://github.com/TristeFigure/derrida/blob/{version}/{filepath}#L{line}"
          :metadata   {:doc/format :markdown}
          :themes     [:rdash]})
