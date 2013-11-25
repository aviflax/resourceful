# resourceful

A Clojure library which provides a more concise and more RESTful alternative to
[Compojure’s](https://github.com/weavejester/compojure) routes, in the form of the macro
`resource` which a developer can use to declare a *resource*, as opposed to a *route*.

`resource` expands into a call to Compojure’s `routes`, so it can be used anywhere `routes` can be
used.

This has the following concrete benefits:

* Allows the path to be specified only once even if a resource supports multiple methods
* Adds an OPTIONS route which returns an Allow header (if OPTIONS is not specified)
* Adds an ANY route to return a 405 response for any unsupported method

...and one abstract but important benefit:

* helps developers think in terms of *resources* rather than *routes* by making the *code* about
  *resources* rather than *routes*

## Background

For the story behind Resourceful see [this blog post](http://aviflax.com/post/my-first-open-source-clojure-library-resourceful/).

## Installation

Add the following dependency to your project.clj file:

```clojure
[resourceful "0.1.0"]
```

## Usage

### Requiring the macro `resource`

Add the following line to the `:require` part of your `ns` call:

```clojure
[resourceful :refer [resource]]
```

### Defining Resources

Let’s say you have a simple resource which supports GET and POST.

With “vanilla” Compojure, you might implement it like so:

```clojure
(defroutes app
  (GET "/authors/:author/books" [author]
    (get-books author))
  (POST "/authors/:author/books" [author title]
    (create-book author)
    (get-books author)))
```

...and your resource wouldn’t properly support OPTIONS, PUT, etc.

Whereas with Resourceful, the same resource would be expressed as:

```clojure
(defroutes app
  (resource "Collection of the books of an author"
            "/authors/:author/books"
            (GET [author]
              (get-books author))
            (POST [author title]
              (create-book author)
              (get-books author))))
```

As you can see, the path to the resource only had to be supplied once, and the macro encourages
developers to supply a name for their resource, in accordance with the REST precept that a resource
is really a *concept*.

In addition to the more concise and expressive code, the resulting set of routes are also more
robust and conformant to HTTP than the “vanilla” example the resource will respond properly to
OPTIONS requests and to requests which specify an unsupported method (with a 405).

## Readability advantage

Another advantage is that if your app has multiple resources — and most do — then using `resource`
makes each resource very distinct within the code — whereas when using vanilla Compojure you end up
with just a linear mess of routes. Sure, you could group the routes using comments and blank lines,
but most people don’t bother. With `resource`, the code becomes self-documenting, and the shape of
the code mirrors the shape of your RESTful application/service/API.

## License

Copyright © 2013 Avi Flax

Distributed under the Eclipse Public License, the same as Clojure.
