(ns resourceful
  (:require [clojure.string :refer [join]]
            [compojure.core :refer [ANY HEAD OPTIONS routes]]
            [ring.util.response :refer [header]]))

(defmacro resource
  "Provides more concise and more RESTful alternative to Compojure’s `routes`.

  Specifically:

   * Allows the path to be specified only once even if a resource supports multiple methods
   * Adds an OPTIONS route which returns an Allow header, if OPTIONS is not specified
   * Adds an ANY route to return a 405 response for any unsupported method

   `methods` should be 1–N standard compojure route forms, except with the path omitted.

   Expands into a call to `routes`, so can be used anywhere `routes` can be used.

   For example:

   (resource \"Collection of the books of an author\"
             \"/authors/:author/books\"
             (GET [author] (get-books author))
             (POST [author title] (create-book author) (get-books author)))"
  [description path & methods]
  (let [method-symbols (set (map first methods))
        allowed (->> (map str method-symbols)
                     (concat ["OPTIONS" (when (or (method-symbols 'HEAD)
                                                  (method-symbols 'GET))
                                              "HEAD")] ,,,)
                     (remove nil? ,,,)
                     (join ", " ,,,))]
    `(routes
      ;; Building a list “manually” using concat (as opposed to just unquote-splicing)
      ;; because the “when” forms can produce nil values which must be filtered out of the list
      ~@(remove nil?
          (concat
            ;; output the provided methods/routes
            ;; the method-symbols will be output exactly as provided, so if they were
            ;; provided unqualified, they’ll be output unqualified. I think this is OK
            ;; because the calling NS should have referred the symbols anyway.
            (map (fn [[method-symbol bindings & exprs]]
                   `(~method-symbol ~path ~bindings ~@exprs))
                 methods)

            [
             ;; output OPTIONS, if it isn’t already provided
             (when-not (method-symbols 'OPTIONS)
               `(OPTIONS ~path [] {:status 204
                                   :headers {"Allow" ~allowed
                                             "Description" ~description}
                                   :body nil}))

             ;; output an ANY route to return a 405 for any unsupported method
             (when-not (method-symbols 'ANY)
               `(ANY ~path [] {:status 405
                               :headers {"Allow" ~allowed
                                         "Content-Type" "text/plain;charset=UTF-8"
                                         "Content-Length" "18"}
                               :body "Method Not Allowed"}))
            ])))))
