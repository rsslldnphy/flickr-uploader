(ns flickr-uploader.core
  (:gen-class)
  (:require [environ.core :refer [env]]
            [clojure.java.browse :as browse])
  (:import [com.flickr4java.flickr Flickr REST]
           [com.flickr4java.flickr.auth Permission]
           [com.flickr4java.flickr RequestContext]
           [com.flickr4java.flickr.uploader Uploader UploadMetaData]
           [org.scribe.model Verifier]
           [java.io File]
           [java.util Collections]))

(def api-key (env :flickr-api-key))
(def secret  (env :flickr-secret))

(defn start-auth
  [f auth-interface]
  (let [access-token (.getRequestToken auth-interface)
        auth-url (.getAuthorizationUrl auth-interface access-token Permission/WRITE)]
    (browse/browse-url auth-url)
    access-token))

(defn complete-auth
  [f auth-interface access-token token-key]
  (let [request-token (.getAccessToken auth-interface access-token (Verifier. token-key))
        auth          (.checkToken auth-interface request-token)]
    (.setAuth (RequestContext/getRequestContext) auth)))

(defn regex-escape
  [url]
  (-> url
      (clojure.string/replace #"\." "\\\\.")
      (clojure.string/replace #"\/" "\\\\/")))

(defn upload-picture
  [f file]
  (let [metadata      (doto (UploadMetaData.)
                        (.setPublicFlag true)
                        (.setFilename (.getName file))
                        (.setTitle (.getName file))
                        (.setSafetyLevel Flickr/SAFETYLEVEL_MODERATE)
                        (.setTags ["dog" "painting" "lovely" "nice"]))
        uploader        (.getUploader f)
        photo-id (.upload uploader file metadata)
        photos-interface (.getPhotosInterface f)
        photo            (.getPhoto photos-interface photo-id)
        photo-info {:farm (.getFarm photo)
                    :server (.getServer photo)
                    :secret (.getSecret photo)
                    :id     photo-id}
        photo-url  (format "https://farm%s.staticflickr.com/%s/%s_%s.jpg" (:farm photo-info) (:server photo-info) (:id photo-info) (:secret photo-info))]
    (printf "find . -name '*.md' | xargs sed -i '' 's/%s/%s/g'\n" (regex-escape (.getPath file)) (regex-escape photo-url))))

(defn -main [dir]
  (let [f              (Flickr. api-key secret (REST.))
        auth-interface (.getAuthInterface f)
        access-token   (start-auth f auth-interface)
        _              (println "Paste the key from the browser: ")
        token-key      (read-line)]
    (complete-auth f auth-interface access-token token-key)
    (doseq [file (file-seq (File. dir))
            :when (.endsWith (.getName file) ".jpg")]
      (upload-picture f file))))
