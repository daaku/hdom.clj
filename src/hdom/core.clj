(ns hdom.core
  "Manipulate hiccup data."
  {:author "Naitik Shah"}
  (:require
    [clojure.tools.logging])
  (:use
    [clojure.string :only [join trim]]))

(defn- as-str [x] (if (instance? clojure.lang.Named x) (name x) (str x)))

(defn gen-id []
  (apply str (repeatedly 5 #(char (rand-nth (concat (range 97 123)))))))

(defn make-class [& args]
  (trim (join " " (map as-str args))))

(def ^{:doc "Regular expression that parses a CSS-style id and class from a tag name." :private true}
  re-tag #"([^\s\.#]+)(?:#([^\s\.#]+))?(?:\.([^\s#]+))?")

(defn normalize-element
  "Ensure a tag vector is of the form [tag-name attrs content]."
  [[tag & content]]
  (when (not (or (keyword? tag) (symbol? tag) (string? tag)))
    (throw (IllegalArgumentException. (str tag " is not a valid tag name."))))
  (let [[_ tag id class] (re-matches re-tag (as-str tag))
        tag-attrs        {:id id
                          :class (if class (.replace ^String class "." " "))}
        map-attrs        (first content)]
    (if (map? map-attrs)
      [tag (merge tag-attrs map-attrs) (next content)]
      [tag tag-attrs content])))

(defn reduce-elements [memo elements f]
  (reduce
    (fn [[memo processed] el]
      (cond
        (vector? el) (let [[memo el] (apply f [memo (normalize-element el)])
                           [tag attrs content] el
                           [memo content] (reduce-elements memo content f)]
                       [memo (conj processed [tag attrs (seq content)])])
        (seq? el) (let [[memo el] (reduce-elements memo el f)]
                    [memo (conj processed (seq el))])
        :else [memo (conj processed el)]))
    [memo []]
    elements))

(defn- add-class-string [class existing]
  (str class " " existing))

(defn add-class [element class]
  (let [[tag attrs content] (normalize-element element)]
    [tag (assoc attrs :class (add-class-string class (attrs :class))) content]))

(defn remove-class [element class]
  )

(defn has-class [element class]
  )
