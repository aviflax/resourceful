(ns resourceful
  (:require [clojure.string :refer [join]]
            [compojure.core :refer [ANY HEAD OPTIONS routes]]
            [ring.util.response :refer [header]]))


(defn body-length
  "This should really be private, but I tried that and it breaks `resource`.
   This is ONLY used for HEAD requests — it WILL exhaust any InputStreams passed in."
  [body]
  ;; TODO: is this treating character encodings correctly?
  ;; TODO: this probably isn’t correctly handling large InputStreams
  ;; TODO: should probabyly wrap this whole thing in a try — right?
  ;; TODO: maybe this should throw an exception if it can’t determine a length
  ;;       (such as the case wherein the passed value isn’t a supported type)
  (condp instance? body
    String (count body)
    java.io.File (.length body)
    java.io.InputStream (.available body)
    clojure.lang.ISeq (reduce + (map (comp count str) body))
    nil))


(defmacro resource
  "Provides more concise and more RESTful alternative to Compojure’s `routes`.

  Specifically:

   * Allows the path to be specified only once even if a resource supports multiple methods
   * Adds a HEAD route, if GET is specified and HEAD is not
   * Adds an OPTIONS route which returns an Allow header, if OPTIONS is not specified
   * Adds an ANY route to return a 405 response for any unsupported method

   `methods` should be 1–N standard compojure route forms, except with the path omitted.

   Expands into a call to `routes`, so can be used anywhere `routes` can be used.

   For example:

   (resource \"Collection of the books of an author\"
             \"/authors/:author/books\"
             (GET [author] (get-books author))
             (POST [author title] (create-book author) (get-books author)))"
  [name path & methods]
  (let [method-symbols (set (map first methods))
        allowed (->> (map str method-symbols)
                     (concat ["OPTIONS" (when (or (method-symbols 'HEAD)
                                                  (method-symbols 'GET))
                                              "HEAD")] ,,,)
                     (filter (complement nil?) ,,,)
                     (join ", " ,,,))]
    `(routes
      ;; Building a list “manually” using concat (as opposed to just unquote-splicing)
      ;; because the “when” forms can produce nil values which must be filtered out of the list
      ~@(-> [
            ;; add a HEAD route if GET is provided and HEAD is not
            ;; this MUST come before the provided methods/routes, because Compojure’s GET
            ;; route also handles HEAD requests (and has a bug; it sends Content-Length as 0)
            (when (and (method-symbols 'GET)
                        (not (method-symbols 'HEAD)))
               (let [get-method (-> (filter #(= (first %) 'GET) methods)
                                    first)
                     [_ bindings & exprs] get-method]
                 `(HEAD ~path ~bindings
                    (let [get-response# (do ~@exprs)
                          response# (dissoc get-response# :body)]
                      (if (get-in response# [:headers "Content-Length"])
                          response#
                          ;; TODO: how to handle case where body-length could not determine a length?
                          (header response# "Content-Length" (body-length
                                                               (:body get-response#))))))))
            ]

            (concat ,,,
                    ;; output the provided methods/routes
                    (map (fn [[method-symbol bindings & exprs]]
                             `(~method-symbol ~path ~bindings ~@exprs))
                           methods))

            (concat ,,, [
                    ;; output OPTIONS, if it isn’t already provided
                    (when-not (method-symbols 'OPTIONS)
                      `(OPTIONS ~path [] {:status 204
                                          :headers {"Allow" ~allowed}
                                          :body nil}))

                    ;; output an ANY route to return a 405 for any unsupported method
                    (when-not (method-symbols 'ANY)
                      `(ANY ~path [] {:status 405
                                      :headers {"Allow" ~allowed
                                                "Content-Type" "text/plain;charset=UTF-8"
                                                "Content-Length" "18"}
                                      :body "Method Not Allowed"}))
            ])
            (->> (filter (complement nil?) ,,,))))))
