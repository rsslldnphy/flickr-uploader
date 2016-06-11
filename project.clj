(defproject flickr-uploader "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :main flickr-uploader.core
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [http-kit "2.1.19"]
                 [environ "1.0.3"]
                 [com.flickr4java/flickr4java "2.16"] ]
  :plugins [[lein-environ "1.0.3"]])
