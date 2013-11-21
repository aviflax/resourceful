
;; Resourceful -> https://github.com/aviflax/resourceful
;;
;; Avi Flax -> ["CTO"
;;              "SFX Entertainment"
;;              "avif@arc90.com"]
;;
;; we are hiring -> http://arc90.com
;;
;; NYC Clojure User’s Group -> 20 November 2013





















;; NOTHING TO SEE HERE

(require '[clojure.pprint :refer [pprint]])
(require '[compojure.core :refer [defroutes GET PUT POST DELETE]])
(require '[resourceful :refer [resource]])



(def get-books)
(def create-book)
(def get-books)
(def get-authors)
(def get-author)
(def create-author)
(def update-author)
(def delete-author)










;; Compojure is *great*

(defroutes app
  (GET "/authors/:author/books" [author]
    (get-books author))
  (POST "/authors/:author/books" [author title]
    (create-book author)
    (get-books author)))


















;; But it can get out of hand

(defroutes app
  (GET "/authors" []
    (get-authors))
  (POST "/authors" [author]
    (create-author author))
  (GET "/authors/:author" [author]
    (get-author author))
  (PUT "/authors/:author" [author]
    (update-author author))
  (DELETE "/authors/:author" [author]
    (delete-author author))
  (GET "/authors/:author/books" [author]
    (get-books author))
  (POST "/authors/:author/books" [author title]
    (create-book author)
    (get-books author))
  (GET "/authors/:author/books/:book" [author book]
    (get-book book))
  (PUT "/authors/:author/books/:book" [author book]
    (update-book book))
  (DELETE "/authors/:author/books/:book" [author book]
    (delete-book book)))


;; There’s a paradigm mismatch

;; What we’re really implementing are *resources*

(defroutes app
  ;; Collection of Authors
  (GET "/authors" []
    (get-authors))
  (POST "/authors" [author]
    (create-author author))

  ;; An Author
  (GET "/authors/:author" [author]
    (get-author author))
  (PUT "/authors/:author" [author]
    (update-author author))
  (DELETE "/authors/:author" [author]
    (delete-author author))

  ;; Collection of an Author’s Books
  (GET "/authors/:author/books" [author]
    (get-books author))
  (POST "/authors/:author/books" [author title]
    (create-book author)
    (get-books author))

  ;; A Book
  (GET "/authors/:author/books/:book" [author book]
    (get-book book))
  (PUT "/authors/:author/books/:book" [author book]
    (update-book book))
  (DELETE "/authors/:author/books/:book" [author book]
    (delete-book book)))






























;; and our resources are incomplete

(defroutes app
  ;; Collection of Authors
  (GET "/authors" []
    (get-authors))
  (POST "/authors" [name]
    (create-author name)))

;; $ curl -i -X PUT -d "name=Charles Stross" http://localhost/authors
;; HTTP/1.1 404 Not Found

;; Should be: 405 Method Not Allowed

;; 404 is confusing!


;; $ curl -i -X OPTIONS http://localhost/authors
;; HTTP/1.1 404 Not Found

;; Should be: 200/204 with an Allow header listing supported methods





;; resourceful attempts to solve these problems:
;;   * cluttered code
;;   * paradigm mismatch
;;   * incomplete resources / make resources more developer-friendly

;; instead of this:

(defroutes app
  ;; Collection of Authors
  (GET "/authors" []
    (get-authors))
  (POST "/authors" [author]
    (create-author author))

  ;; An Author
  (GET "/authors/:author" [author]
    (get-author author))
  (PUT "/authors/:author" [author]
    (update-author author))
  (DELETE "/authors/:author" [author]
    (delete-author author)))





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



;; possible enhancements
;; debug flag, duplicate method error, return resource definition on OPTIONS







 ;; resources more complete, more developer-friendly

(resource "Collection of Authors"
  "/authors"
  (GET []
    (get-authors))
  (POST [name]
    (create-author name)
    (get-authors)))


;; $ curl -i -X PUT -name "Paolo Bacigalupi" http://localhost/authors
;; HTTP/1.1 405 Method Not Allowed

;; $ curl -i -X OPTIONS http://localhost/authors
;; HTTP/1.1 200 OK
;; Allow: GET,HEAD,OPTIONS,POST









 ;; resource is a macro

(macroexpand-1
'(resource "Collection of Authors"
  "/authors"
  (GET []
    (get-authors))
  (POST [name]
    (create-author name)
    (get-authors))))














































