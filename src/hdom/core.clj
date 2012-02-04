(ns hdom.core
  "Manipulate hiccup data as DOM nodes. This lets you add child nodes, modify
  attributes and iterate/reduce nodes."
  {:author "Naitik Shah"}
  (:use
    [clojure.string :only [join trim]]))

(defn- as-str [x] (if (instance? clojure.lang.Named x) (name x) (str x)))

(defn gen-id
  "Generate a random ID suitable for a HTML element."
  []
  (apply str (repeatedly 5 #(char (rand-nth (concat (range 97 123)))))))

(defn make-class
  "Make a class string given a number of arguments. This handles clojure
  keywords correctly and omits the `:` as necessary."
  [& args]
  (trim (join " " (map as-str args))))

(def ^{:private true
       :doc "Regular expression that parses a CSS-style id and class from a tag
            name."}
  re-tag #"([^\s\.#]+)(?:#([^\s\.#]+))?(?:\.([^\s#]+))?")

(defn normalize-element
  "Ensure a tag vector is of the form:

    [\"string-tag-name\" {:attrs \"map\"} (content list)]

  It also parses inline IDs or classes and normalizes them into the
  attributes."
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

(defn reduce-elements
  "Reduce elements, mapping a function over them, possibly modifying them, and
  accumulating a result."
  [memo elements f]
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

(defn update-attr
  "Update a single element and set it's attribute using a callback function.
  The callback gets invoked with the existing value and is expected to return a
  new value."
  [element name f]
  (let [[tag attrs content] (normalize-element element)]
    [tag (assoc attrs name (f (attrs name))) content]))

(defn set-attr
  "Set the value of an attribute ignoring it's current value."
  [element name value]
  (update-attr element name (fn [_] value)))

(defn dissoc-attr
  "Remove an attribute. DOES not return the existing value, only returns the
  updated element with the attribute removed."
  [element name]
  (let [[tag attrs content] (normalize-element element)]
    [tag (dissoc attrs name) content]))

(defn- pad-class
  "Just blindly adds spaces around the string."
  [class]
  (str " " class " "))

(def ^{:private true
       :doc "Functions! A memoizing function that compiles string class names
            to regular expressions."}
  class-regex
  (memoize (fn [class] (java.util.regex.Pattern/compile (pad-class class)))))

(defn- add-class-string
  "Adds a class to an existing class string."
  [class existing]
  (str existing " " class))

(defn- remove-class-string
  "Removes a class from a given class string."
  [class existing]
  (trim (clojure.string/replace (pad-class existing) (class-regex class) "")))

(defn has-class
  "Checks if the class attribute of an element contains a given class."
  [element class]
  (let [[tag attrs content] (normalize-element element)]
    (boolean (re-seq (class-regex class) (pad-class (:class attrs))))))

(defn add-class
  "Adds the given class on the given element."
  [element class]
  (if (has-class element class)
    element
    (update-attr element :class #(add-class-string class %))))

(defn remove-class
  "Removes the given class on the given element."
  [element class]
  (if-not (has-class element class)
    element
    (update-attr element :class #(remove-class-string class %))))

(defn prepend-child
  "Prepends a child (first child parent)."
  [parent child]
  (let [[tag attrs content] (normalize-element parent)]
    [tag attrs (cons child content)]))
