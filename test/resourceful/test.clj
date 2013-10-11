(ns resourceful.test
  (:use clojure.test)
  (:require [resourceful :refer [resource]]
            [compojure.core :refer [GET HEAD OPTIONS POST]]
            [clojure.java.io :refer [file input-stream]]
            [clojure.string :refer [split]]))

;; TODO: use ring-mocks for more realistic testing

(deftest test-resource
  (testing "a resource with just a GET route should handle HEAD"
    (let [handler (resource "foo" "/"
                    (GET [] {:status 200
                             :headers {"Content-Type" "text/plain"}
                             :body "bar"}))
          req {:uri "/"
               :request-method :head
               :headers {"accept" "*/*"}}
          res (handler req)]
      (is (= (:status res) 200))
      (is (empty? (:body res)))
      (is (= (:headers res) {"Content-Type" "text/plain"
                             "Content-Length" "3"}))))


  (testing "a resource with a String response body should handle HEAD properly"
    (let [handler (resource "foo" "/"
                    (GET [] {:status 200
                             :headers {"Content-Type" "text/plain"}
                             :body "bar"}))
          req {:uri "/"
               :request-method :head
               :headers {"accept" "*/*"}}
          res (handler req)]
      (is (= (:status res) 200))
      (is (empty? (:body res)))
      (is (= (:headers res) {"Content-Type" "text/plain"
                             "Content-Length" "3"}))))


  (testing "a resource with a InputStream response body should handle HEAD properly"
    (let [handler (resource "foo" "/"
                    (GET [] {:status 200
                             :headers {"Content-Type" "text/plain"}
                             :body (input-stream (.getBytes "bar"))}))
          req {:uri "/"
               :request-method :head
               :headers {"accept" "*/*"}}
          res (handler req)]
      (is (= (:status res) 200))
      (is (empty? (:body res)))
      (is (= (:headers res) {"Content-Type" "text/plain"
                             "Content-Length" "3"}))))


  (testing "a resource with a File response body should handle HEAD properly"
    (let [handler (resource "foo" "/"
                    (GET [] {:status 200
                             :headers {"Content-Type" "text/plain"}
                             :body (file "resources/test/fixed_length_3.txt")}))
          req {:uri "/"
               :request-method :head
               :headers {"accept" "*/*"}}
          res (handler req)]
      (is (= (:status res) 200))
      (is (empty? (:body res)))
      (is (= (:headers res) {"Content-Type" "text/plain"
                             "Content-Length" "3"}))))


  (testing "a resource with a Seq response body should handle HEAD properly"
    (let [handler (resource "foo" "/"
                    (GET [] {:status 200
                             :headers {"Content-Type" "text/plain"}
                             :body (seq ["f" "o" "o"])}))
          req {:uri "/"
               :request-method :head
               :headers {"accept" "*/*"}}
          res (handler req)]
      (is (= (:status res) 200))
      (is (empty? (:body res)))
      (is (= (:headers res) {"Content-Type" "text/plain"
                             "Content-Length" "3"}))))


  (testing "a resource wherein GET sets Content-Length should return that value on HEAD"
    (let [handler (resource "foo" "/"
                    (GET [] {:status 200
                             :headers {"Content-Type" "text/plain", "Content-Length" "613"}
                             :body "WHATEVER"}))
          req {:uri "/"
               :request-method :head
               :headers {"accept" "*/*"}}
          res (handler req)]
      (is (= (:status res) 200))
      (is (empty? (:body res)))
      (is (= (:headers res) {"Content-Type" "text/plain"
                             "Content-Length" "613"}))))




  (testing "when a GET route does NOT set Content-Length, and the body is an non-standard
            type, the header Content-Length should not be present in the response"
    (let [handler (resource "foo" "/"
                    (GET [] {:status 200
                             :headers {"Content-Type" "text/plain"}
                             :body ["foo"]}))
          req {:uri "/"
               :request-method :head
               :headers {"accept" "*/*"}}
          res (handler req)]
      (is (= (:status res) 200))
      (is (empty? (:body res)))
      (is (= (:headers res) {"Content-Type" "text/plain"}))))



  (testing "when a resource supplies its own HEAD handler, it should be used"
    (let [handler (resource "foo" "/"
                    (HEAD [] {:status 200
                              :headers {"Content-Type" "application/yourmom"
                                        "Content-Length" "1337"
                                        "Cheese" "Moldy"}})
                    (GET [] {:status 200
                             :headers {"Content-Type" "text/plain", "Content-Length" "613"}
                             :body "WHATEVER"}))
          req {:uri "/"
               :request-method :head
               :headers {"accept" "*/*"}}
          res (handler req)]
      (is (= (:status res) 200))
      (is (empty? (:body res)))
      (is (= (:headers res) {"Content-Type" "application/yourmom"
                             "Content-Length" "1337"
                             "Cheese" "Moldy"}))))


  (testing "when a resource doesn’t supply a GET route, it shouldn’t support HEAD"
    (let [handler (resource "foo" "/"
                    (POST [] {:status 200
                              :headers {"Content-Type" "text/plain", "Content-Length" "8"}
                              :body "Success!"}))
          req {:uri "/"
               :request-method :head
               :headers {"accept" "*/*"}}
          res (handler req)]
      (is (= (:status res) 405))
      (is (.contains (get-in res [:headers "Content-Type"]) "text/plain"))
      (is (= (get-in res [:headers "Content-Length"]) "18"))
      (let [res-allow-header (get-in res [:headers "Allow"])
            res-allow-methods (set (split res-allow-header #", "))]
        (is (= res-allow-methods #{"POST" "OPTIONS"})))
      (is (= (:body res "Method Not Allowed")))))


  (testing "when a resource doesn’t supply a POST route, a POST request should return 405"
    (let [handler (resource "foo" "/"
                    (GET [] {:status 200
                              :headers {"Content-Type" "text/plain", "Content-Length" "3"}
                              :body "foo"}))
          req {:uri "/"
               :request-method :post
               :headers {"accept" "*/*"}}
          res (handler req)]
      (is (= (:status res) 405))
      (is (.contains (get-in res [:headers "Content-Type"]) "text/plain"))
      (is (= (get-in res [:headers "Content-Length"]) "18"))
      (let [res-allow-header (get-in res [:headers "Allow"])
            res-allow-methods (set (split res-allow-header #", "))]
        (is (= res-allow-methods #{"GET" "HEAD" "OPTIONS"})))
      (is (= (:body res "Method Not Allowed")))))


  (testing "a resource should support OPTIONS"
    (let [handler (resource "foo" "/"
                    (GET [] {:status 200
                             :headers {"Content-Type" "text/plain"}
                             :body "bar"})
                    (POST [] {:status 200
                              :headers {"Content-Type" "text/plain", "Content-Length" "8"}
                              :body "Success!"}))
          req {:uri "/"
               :request-method :options}
          res (handler req)]
      (is (= (:status res) 204))
      (is (empty? (:body res)))
      (let [res-allow-header (get-in res [:headers "Allow"])
            res-allow-methods (set (split res-allow-header #", "))]
        (is (= res-allow-methods #{"GET" "HEAD" "POST" "OPTIONS"}))))))
