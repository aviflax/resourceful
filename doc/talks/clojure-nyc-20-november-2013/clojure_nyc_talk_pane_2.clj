





;; this:

(defroutes app
  (resource "Collection of Authors"
    "/authors"
    (GET []
      (get-authors))
    (POST [name]
      (create-author name)
      (get-authors)))

  (resource "An Author"
    "/authors/:author"
    (GET [author]
      (get-author author))
    (PUT [author]
      (update-author author))
    (DELETE [author]
      (delete-author author))))






;; expands to:

(compojure.core/routes
 (compojure.core/HEAD
  "/authors"
  []
  (clojure.core/let
   [get-response__5224__auto__
    (do (get-authors))
    response__5225__auto__
    (clojure.core/dissoc get-response__5224__auto__ :body)]
   (if
    (clojure.core/get-in
     response__5225__auto__
     [:headers "Content-Length"])
    response__5225__auto__
    (clojure.core/if-let
     [length__5226__auto__
      (resourceful/body-length (:body get-response__5224__auto__))]
     (ring.util.response/header
      response__5225__auto__
      "Content-Length"
      length__5226__auto__)
     response__5225__auto__))))
 (GET "/authors" [] (get-authors))
 (POST "/authors" [name] (create-author name) (get-authors))
 (compojure.core/OPTIONS
  "/authors"
  []
  {:status 204,
   :headers {"Allow" "OPTIONS, HEAD, POST, GET"},
   :body nil})
 (compojure.core/ANY
  "/authors"
  []
  {:status 405,
   :headers
   {"Allow" "OPTIONS, HEAD, POST, GET",
    "Content-Type" "text/plain;charset=UTF-8",
    "Content-Length" "18"},
   :body "Method Not Allowed"}))



















