(defproject resourceful "0.1.0"
  :description "Provides a more concise and more RESTful alternative to Compojure’s routes"
  :url "https://github.com/aviflax/resourceful"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 ;; the versions of Compojure and Ring should be locked together
                 [compojure "1.1.6"]
                 [ring/ring-core "1.2.1"]])
